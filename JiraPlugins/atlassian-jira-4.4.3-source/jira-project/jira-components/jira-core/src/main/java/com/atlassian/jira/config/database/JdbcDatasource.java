package com.atlassian.jira.config.database;

import com.atlassian.config.bootstrap.AtlassianBootstrapManager;
import com.atlassian.config.bootstrap.BootstrapException;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.jira.configurator.config.DatabaseType;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.configurator.db.DatabaseConfig;
import com.atlassian.jira.configurator.db.DatabaseConfigFactory;
import com.atlassian.jira.util.dbc.Assertions;
import org.ofbiz.core.entity.config.ConnectionPoolInfo;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.JdbcDatasourceInfo;

import java.sql.Connection;
import java.util.Properties;

/**
 * A JDBC datasource
 */
public final class JdbcDatasource implements Datasource
{
    //
    // This means by default we will wait 60 seconds to try and borrow an object from the connection pool
    //
    private static final long POOL_DEFAULTS_MAX_WAIT = 60000;
    private static final int POOL_DEFAULTS_MIN_SIZE = 2;
    private static final int POOL_DEFAULTS_SLEEP_TIME = 300000;
    private static final int POOL_DEFAULTS_LIFE_TIME = 600000;
    private static final int POOL_DEFAULTS_DEAD_LOCK_MAX_WAIT = 600000;
    private static final int POOL_DEFAULTS_DEAD_LOCK_RETRY_WAIT = 10000;

    private final String jdbcUrl;
    private final String driverClassName;
    private final String username;
    private final String password;
    private final Properties connectionProperties;
    private final int poolSize;
    private final String validationQuery;
    private final Long minEvictableTimeMillis;
    private final Long timeBetweenEvictionRunsMillis;

    private static boolean registerDriverOnConstruct = true;

    public JdbcDatasource(String jdbcUrl, String driverClassName, String username, String password,
            int poolSize, String validationQuery, Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        this.jdbcUrl = Assertions.notBlank("JDBC URL", jdbcUrl);
        this.driverClassName = Assertions.notBlank("Driver class name", driverClassName);
        this.username = Assertions.notBlank("username", username);
        this.password = Assertions.notNull("password", password);
        this.connectionProperties = null;
        Assertions.not("poolSize", poolSize <= 0);
        this.poolSize = poolSize;
        this.validationQuery = validationQuery;
        this.minEvictableTimeMillis = minEvictableTimeMillis;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;

        // JIRA registers the JDBC driver as a side-effect of this constructor which is a bit strange,
        // but turns out to be a simple place to do it.
        registerDriver();
    }

    public JdbcDatasource(String jdbcUrl, String driverClassName, String username, String password, Properties connectionProperties,
            int poolSize, String validationQuery, Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        this.jdbcUrl = Assertions.notBlank("JDBC URL", jdbcUrl);
        this.driverClassName = Assertions.notBlank("Driver class name", driverClassName);
        this.username = Assertions.notBlank("username", username);
        this.password = Assertions.notNull("password", password);
        this.connectionProperties = connectionProperties;
        Assertions.not("poolSize", poolSize <= 0);
        this.poolSize = poolSize;
        this.validationQuery = validationQuery;
        this.minEvictableTimeMillis = minEvictableTimeMillis;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;

        // JIRA registers the JDBC driver as a side-effect of this constructor which is a bit strange,
        // but turns out to be a simple place to do it.
        registerDriver();
    }

    public JdbcDatasource(DatabaseType databaseType, String hostname, String port, String instance, String username, String password, Integer poolSize,
            String validationQuery, Long minEvictableTimeMillis, Long timeBetweenEvictionRunsMillis)
    {
        final DatabaseConfig databaseConfig = DatabaseConfigFactory.getDatabaseConfigFor(databaseType);

        try
        {
            this.jdbcUrl = databaseConfig.getUrl(hostname, port, instance);
        }
        catch (ValidationException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        this.driverClassName = databaseConfig.getClassName();
        this.username = Assertions.notBlank("username", username);
        this.password = Assertions.notNull("password", password);
        this.connectionProperties = null;
        Assertions.not("poolSize", poolSize <= 0);
        this.poolSize = poolSize;
        this.validationQuery = validationQuery;
        this.minEvictableTimeMillis = minEvictableTimeMillis;
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;

        registerDriver();
    }

    /**
     * This setting is soley so the config tool can avoid the fatal side-effect of registering the JDBC driver in this
     * class's constructor.
     *
     * @param registerDriverOnConstruct If true we do "Class.forName(driverClassName);" in the Constructor.
     */
    public static void setRegisterDriverOnConstruct(boolean registerDriverOnConstruct)
    {
        JdbcDatasource.registerDriverOnConstruct = registerDriverOnConstruct;
    }

    private void registerDriver()
    {
        // The config tool does not run in Tomcat, so it does not have the drivers on it's default classpath.
        // Give it a chance to avoid the fatal side-effect of registering the driver in this class's constructor.
        if (registerDriverOnConstruct)
        {
            try
            {
                Class.forName(driverClassName);
            }
            catch (ClassNotFoundException ex)
            {
                // We don't expect this to happen in standalone, because we supply drivers, but it could happen in WAR edition
                // or if an admin were to manually hack the config file.
                throw new IllegalArgumentException("JDBC Driver class '" + driverClassName + " could not be loaded.'", ex);
            }
        }
    }

    @Override
    public Connection getConnection(AtlassianBootstrapManager bootstrapManager) throws BootstrapException
    {
        return bootstrapManager.getTestDatabaseConnection(createDbDetails());
    }

    @Override
    public DatasourceInfo getDatasource(String datasourceName, String databaseType, String schemaName)
    {
        ConnectionPoolInfo poolInfo = new ConnectionPoolInfo(
                poolSize, POOL_DEFAULTS_MIN_SIZE, POOL_DEFAULTS_MAX_WAIT,
                POOL_DEFAULTS_SLEEP_TIME, POOL_DEFAULTS_LIFE_TIME,
                POOL_DEFAULTS_DEAD_LOCK_MAX_WAIT, POOL_DEFAULTS_DEAD_LOCK_RETRY_WAIT,
                validationQuery,
                minEvictableTimeMillis, timeBetweenEvictionRunsMillis);

        JdbcDatasourceInfo jdbcInfo = new JdbcDatasourceInfo(jdbcUrl, driverClassName, username, password,
                null, connectionProperties, poolInfo);
        return new DatasourceInfo(datasourceName, databaseType, schemaName, jdbcInfo);
    }

    @Override
    public String getDescriptorValue(String databaseType)
    {
        return databaseType + " " + jdbcUrl;
    }

    @Override
    public String getDescriptorLabel()
    {
        return "Database JDBC config";
    }

    DatabaseDetails createDbDetails()
    {
        final DatabaseDetails dbDetails = new DatabaseDetails();
        dbDetails.setDatabaseUrl(jdbcUrl);
        dbDetails.setDriverClassName(driverClassName);
        dbDetails.setUserName(username);
        dbDetails.setPassword(password);
        dbDetails.setPoolSize(poolSize);
        return dbDetails;
    }

    /**
     * The JDBC URL
     *
     * @return The JDBC URL
     */
    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    /**
     * The class name for the driver
     *
     * @return The class name.  May or may not be valid.
     */
    public String getDriverClassName()
    {
        return driverClassName;
    }

    /**
     * The username for the database connection
     *
     * @return The username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * The password for the database connection
     *
     * @return The password
     */
    public String getPassword()
    {
        return password;
    }

    public Properties getConnectionProperties()
    {
        return connectionProperties;
    }

    /**
     * The size of the database connection pool
     *
     * @return The database connection pool size
     */
    public int getPoolSize()
    {
        return poolSize;
    }

    public String getValidationQuery()
    {
        return validationQuery;
    }

    public Long getMinEvictableTimeMillis()
    {
        return minEvictableTimeMillis;
    }

    public Long getTimeBetweenEvictionRunsMillis()
    {
        return timeBetweenEvictionRunsMillis;
    }
}
