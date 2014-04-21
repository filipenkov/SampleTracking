package com.atlassian.jira.config.database;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.bootstrap.DefaultAtlassianBootstrapManager;
import com.atlassian.jira.appconsistency.db.PostgresSchemaConfigCheck;
import com.atlassian.jira.appconsistency.db.PublicSchemaConfigCheck;
import com.atlassian.jira.startup.StartupCheck;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.ofbiz.core.entity.config.DatasourceInfo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The configuration for JIRA to connect to the database.
 *
 * @since 4.3
 */
public final class DatabaseConfig
{

    /**
     * The name of the default JNDI datasource. In single tenant mode, There Is Only One. This is the one true place for
     * this constant to live in JIRA. This should not be used by plugins or considered an API.
     */
    public static final String DEFAULT_DATASOURCE_NAME = "defaultDS";

    /**
     * The name of the default delegator. In single tenant mode, There Is Only One. This is the one true place for this
     * constant to live in JIRA. This should not be used by plugins or considered an API.
     */
    public static final String DEFAULT_DELEGATOR_NAME = "default";

    private final String datasourceName;
    private final String delegatorName;
    private final String databaseType;
    private final String schemaName;
    private final Datasource datasource;


    /**
     * Uses defaults for OfBiz Datasource name and Delegator name.
     *
     * @param databaseType the name that matches the field types defined in entityengine.xml
     * @param schemaName if the database needs it, the name of the schema. e.g. "public" is common in postgres.
     * @param datasource the definition of the jdbc or jndi details.
     */
    public DatabaseConfig(String databaseType, String schemaName, Datasource datasource)
    {
        this(DEFAULT_DATASOURCE_NAME, DEFAULT_DELEGATOR_NAME, databaseType, schemaName, datasource);
    }

    public DatabaseConfig(String datasourceName, String delegatorName, String databaseType, String schemaName,
            Datasource datasource)
    {
        this.datasourceName = Assertions.notBlank("Datasource name", datasourceName);
        this.delegatorName = Assertions.notBlank("Delegator name", delegatorName);
        this.databaseType = Assertions.notBlank("Database type", databaseType);
        this.schemaName = schemaName;
        this.datasource = Assertions.notNull("Datasource", datasource);
    }

    /**
     * Tests the connection using the given bootstrapManager.
     *
     * @param bootstrapManager to use for testing the connection.
     * @return any StartupCheck that has failed this test or null if all OK.
     * @throws BootstrapException on connection failure.
     */
    public StartupCheck testConnection(AtlassianBootstrapManager bootstrapManager) throws BootstrapException
    {
        final Supplier<DatasourceInfo> dsi = Suppliers.ofInstance(getDatasourceInfo());
        PostgresSchemaConfigCheck postgresCheck = new PostgresSchemaConfigCheck(dsi, new ExternalLinkUtilImpl());
        PublicSchemaConfigCheck schemaCheck = new PublicSchemaConfigCheck(dsi);
        StartupCheck failedCheck = null;
        if (!postgresCheck.isOk())
        {
            failedCheck = postgresCheck;
        }
        else if (!schemaCheck.isOk())
        {
            failedCheck = schemaCheck;
        }
        datasource.getConnection(bootstrapManager);

        return failedCheck;
    }

    public boolean isDatabaseEmpty(DefaultAtlassianBootstrapManager bootstrapManager) throws BootstrapException
    {
        Connection conn = datasource.getConnection(bootstrapManager);

        try
        {
            DatabaseMetaData metaData = conn.getMetaData();

            String[] types = { "TABLE", "VIEW", "ALIAS", "SYNONYM" };
            String lookupSchemaName = lookupSchemaName(metaData);
            ResultSet tableSet = metaData.getTables(null, lookupSchemaName, null, types);

            // We think we've got some table data...
            if (tableSet != null)
            {
                // Check if there were actually tables returned in the search
                // if so, we deem this database not empty
                if (tableSet.next())
                {
                    return false;
                }
            }
            return true;
        }
        catch (SQLException e)
        {
            throw new BootstrapException(e);
        }
    }

    private String lookupSchemaName(DatabaseMetaData metaData) throws SQLException
    {
        String lookupSchemaName = null;
        DatasourceInfo datasourceInfo = getDatasourceInfo();
        if (metaData.supportsSchemasInTableDefinitions())
        {
            if (datasourceInfo.getSchemaName() != null && datasourceInfo.getSchemaName().length() > 0)
            {
                lookupSchemaName = datasourceInfo.getSchemaName();
            }
            else
            {
                lookupSchemaName = metaData.getUserName();
            }
        }
        return lookupSchemaName;
    }

    /**
     * Get the name of the data source.  This is also known as the helperName in ofbiz.
     *
     * @return The name of the data source
     */
    public String getDatasourceName()
    {
        return datasourceName;
    }

    /**
     * Get the name of the delegate.
     *
     * @return The name of the delegate.
     */
    public String getDelegatorName()
    {
        return delegatorName;
    }

    /**
     * Get the database type
     *
     * @return The database type
     */
    public String getDatabaseType()
    {
        return databaseType;
    }

    /**
     * Get the schema name
     *
     * @return The schema name
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * Get the datasource
     *
     * @return The datasource
     */
    public Datasource getDatasource()
    {
        return datasource;
    }

    public DatasourceInfo getDatasourceInfo()
    {
        return datasource.getDatasource(datasourceName, databaseType, schemaName);
    }

    public String getDescriptorValue()
    {
        return datasource.getDescriptorValue(databaseType);
    }

    public String getDescriptorLabel()
    {
        return datasource.getDescriptorLabel();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
