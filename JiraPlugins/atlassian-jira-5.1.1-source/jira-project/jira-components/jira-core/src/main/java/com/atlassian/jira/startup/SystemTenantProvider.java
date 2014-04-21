package com.atlassian.jira.startup;

import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.DatabaseConfigurationLoader;
import com.atlassian.jira.config.database.Datasource;
import com.atlassian.jira.config.database.JdbcDatasource;
import com.atlassian.jira.config.database.JndiDatasource;
import com.atlassian.jira.config.database.SystemTenantDatabaseConfigurationLoader;
import com.atlassian.multitenant.impl.TenantComponentMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v4.3
 */
public class SystemTenantProvider implements com.atlassian.multitenant.SystemTenantProvider
{
    private static final Logger log = Logger.getLogger(SystemTenantProvider.class);
    public static final String SYSTEM_TENANT_NAME = "_jiraSystemTenant";
    // MT-TODO These will need to be changed to avoid name conflicts to something like "_jiraSystemTenant".  However,
    // it shouldn't be done in 4.3, as it will break the SQLFeed and SQL Plugin.  The upgrade docs have been written
    // to document that using these names will break in 4.4 and providing an alternate solution.
    private static final String SYSTEM_TENANT_DATASOURCE_NAME = "defaultDS";
    public static final String SYSTEM_TENANT_DELEGATOR_NAME = "default";

    @Override
    public TenantComponentMap getSystemTenant()
    {
        Map<Class<?>, Object> configMap = new HashMap<Class<?>, Object>();
        configMap.put(DatabaseConfig.class, getSystemDatabaseConfig());
        return new SystemTenant(configMap);
    }

    /**
     * This method initialises OfBiz entity config util, if it isn't already initialised, and reads the default database
     * config from it, as well as the default delegator.  It then *removes* this from ofbiz, so that it can be added
     * later in the standard multi tenant way, with the system tenant name.
     *
     * @return The system database config
     */
    public DatabaseConfig getSystemDatabaseConfig()
    {
        DatabaseConfigurationLoader databaseConfigurationLoader = getDatabaseConfigLoader();
        if (databaseConfigurationLoader.configExists())
        {
            return databaseConfigurationLoader.loadDatabaseConfiguration();
        }
        else
        {
            EntityConfigUtil entityConfigUtil = EntityConfigUtil.getInstance();
            DatasourceInfo datasourceInfo = entityConfigUtil.getDatasourceInfo("defaultDS");
            // perform migration if datasource is setup in entityengine.xml file
            // AND dbconfig.xml file is not in the home dir
            if (datasourceInfo != null)
            {
                Datasource datasource = null;
                if (datasourceInfo.getJndiDatasource() != null)
                {
                    datasource = new JndiDatasource(datasourceInfo.getJndiDatasource());
                }
                else if (datasourceInfo.getJdbcDatasource() != null)
                {
                    datasource = new JdbcDatasource(datasourceInfo.getJdbcDatasource());
                }
                else
                {
                    log.warn("The datasource defined in entityengine.xml does not appear to be a valid JNDI or JDBC datasource.");
                }
                DatabaseConfig config = null;
                if (datasource != null)
                {
                    log.info("Migrating from entityengine.xml specification of datasource to new dbconfig.xml form");
                    config = new DatabaseConfig(SYSTEM_TENANT_DATASOURCE_NAME, SYSTEM_TENANT_DELEGATOR_NAME,
                            datasourceInfo.getFieldTypeName(), datasourceInfo.getSchemaName(), datasource);
                    databaseConfigurationLoader.saveDatabaseConfiguration(config);
                }

                // Remove them, this will shut down if the connection pull if it happened to have started (the only
                // thing that would cause this is setting the schema detection to auto, or if something else had tried
                // to access the database prior to this method being called.
                try
                {
                    entityConfigUtil.removeDelegator("default");
                    entityConfigUtil.removeDatasource("defaultDS");
                    return config;
                }
                catch (RuntimeException re)
                {
                    log.error("Error removing default datasource and delegator from ofbiz", re);
                }
            }
        }
        return null;
    }

    public DatabaseConfigurationLoader getDatabaseConfigLoader()
    {
        final SystemTenantJiraHomeLocator.SystemJiraHome jiraHome = new SystemTenantJiraHomeLocator.SystemJiraHome();
        return new SystemTenantDatabaseConfigurationLoader(jiraHome);
    }
}


