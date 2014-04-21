package com.atlassian.jira.startup;

import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.upgrade.ConnectionKeeper;
import com.atlassian.multitenant.MultiTenantContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.config.DatasourceInfo;
import org.ofbiz.core.entity.config.EntityConfigUtil;

import java.sql.Connection;

/**
 * Configures the JIRA database by configuring transactions and setting up HSQL connection hacks.
 *
 * @since v4.4
 */
public class DatabaseLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(DatabaseLauncher.class);

    private static final String HSQLDB = "hsql";
    private static final int CK_CONNECTIONS = 1;
    private static final int CK_SLEEPTIME = 300000;
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String TRANSACTION_ISOLATION_PROPERTY = "jira.transaction.isolation";
    private static final String TRANSACTION_DISABLE_PROPERTY = "jira.transaction.disable";

    private volatile ConnectionKeeper connectionKeeper;

    @Override
    public void start()
    {
        DatabaseConfig databaseConfig = MultiTenantContext.getTenantReference().get().getConfig(DatabaseConfig.class);
        if (databaseConfig == null)
        {
            log.fatal("No database config found");
            return;
        }
        // Add the datasource and delegator

        final DatasourceInfo datasourceInfo = databaseConfig.getDatasourceInfo();
        if (datasourceInfo == null)
        {
            log.fatal("No datasource info found");
            return;
        }

        setupHsqlHacks(datasourceInfo);
        initDatabaseTransactions(datasourceInfo);
        new JiraStartupLogger().printStartingMessageDatabaseOK();
    }

    @Override
    public void stop()
    {
        shutdownHsqlHacks();

        final DatabaseConfig config = MultiTenantContext.getTenantReference().get().getConfig(DatabaseConfig.class);
        if (config != null)
        {
            // shutting down even though database was not yet set up
            String name = config.getDatasourceName();
            final EntityConfigUtil entityConfigUtil = EntityConfigUtil.getInstance();
            // check if delegator was ever configured
            if (entityConfigUtil.getDelegatorInfo(name) != null)
            {
                entityConfigUtil.removeDelegator(name);
            }
            // check if datasource was ever configured
            if (entityConfigUtil.getDatasourceInfo(name) != null)
            {
                entityConfigUtil.removeDatasource(name);
            }
        }
    }

    /**
     * Sets up hacks to ensure HSQL connections stay alive
     *
     * @param datasourceInfo The datasource info
     */
    private void setupHsqlHacks(DatasourceInfo datasourceInfo)
    {
        if (datasourceInfo != null)
        {
            if (HSQLDB.equals(datasourceInfo.getFieldTypeName()))
            {
                final String message1 = "hsqldb is an in-memory database, and susceptible to corruption when abnormally terminated.";
                final String message2 = "DO NOT USE IN PRODUCTION, please switch to a regular database.";
                final String line = StringUtils.repeat("*", message1.length());
                log.warn(NEW_LINE + NEW_LINE + line + NEW_LINE + message1 + NEW_LINE + message2 + NEW_LINE + line + NEW_LINE);
                if (log.isDebugEnabled())
                {
                    log.debug("Will open " + CK_CONNECTIONS + " connections to keep the database alive.");
                    log.debug("Starting ConnectionKeeper with datasource name '" + datasourceInfo.getName() +
                            "', connections to open '" + CK_CONNECTIONS + "' and sleep time '" + CK_SLEEPTIME + "' milliseconds.");
                }
                connectionKeeper = new ConnectionKeeper(datasourceInfo.getName(), CK_CONNECTIONS, CK_SLEEPTIME);
                connectionKeeper.start();
            }
        }
        else
        {
            log.info("Cannot get datasource information from server. Probably using JBoss. Please ensure that you are not using " + HSQLDB + " also.");
        }
    }

    private void shutdownHsqlHacks()
    {
        if (connectionKeeper != null)
        {
            connectionKeeper.shutdown();
        }
    }

    private void initDatabaseTransactions(final DatasourceInfo datasourceInfo)
    {
        boolean startTransaction = true;
        Integer isolationLevel = null;

        // Test for null datasource as it is null under JBoss
        if (datasourceInfo != null)
        {
            // HSQLDB does not support any transaction isolation except for Connection.
            if (HSQLDB.equals(datasourceInfo.getFieldTypeName()))
            {
                log.info("Setting isolation level to '" + Connection.TRANSACTION_READ_UNCOMMITTED + "' as this is the only isolation level '" + HSQLDB + "' supports.");
                isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
            }
        }
        else
        {
            log.info("Cannot get datasource information from server. Probably using JBoss. If using HSQLDB please set '" + TRANSACTION_ISOLATION_PROPERTY + "' to '1'. Other databases should not need this property.");
        }

        try
        {
            if (Boolean.getBoolean(TRANSACTION_DISABLE_PROPERTY))
            {
                log.info("System property + '" + TRANSACTION_DISABLE_PROPERTY + "' set to true.");
                startTransaction = false;
            }

            final String isolationProperty = System.getProperty(TRANSACTION_ISOLATION_PROPERTY);
            if (isolationProperty != null)
            {
                try
                {
                    log.info("System property + '" + TRANSACTION_ISOLATION_PROPERTY + "' set to '" + isolationProperty + "'. Overriding default.");
                    isolationLevel = Integer.valueOf(isolationProperty);
                }
                catch (final NumberFormatException e)
                {
                    log.error("The '" + TRANSACTION_ISOLATION_PROPERTY + "' is set to a non-numeric value '" + isolationProperty + "'.");
                }
            }
        }
        catch (final SecurityException e)
        {
            log.warn(
                    "There was a security problem trying to read transaction configuration system properties. This usually occurs if you are " + "running JIRA with a security manager. As these system properties are not required to be set (unless you are trying to solve another problem) " + "JIRA should function properly.",
                    e);
        }

        log.info("Database transactions enabled: " + startTransaction);
        CoreTransactionUtil.setUseTransactions(startTransaction);

        if (isolationLevel != null)
        {
            log.info("Database transaction isolation level: " + isolationLevel);
            CoreTransactionUtil.setIsolationLevel(isolationLevel);
        }
        else
        {
            log.info("Using JIRA's default for database transaction isolation level: " + CoreTransactionUtil.getIsolationLevel());
        }
    }

}
