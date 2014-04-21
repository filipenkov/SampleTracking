package com.atlassian.config.bootstrap;

import com.atlassian.config.ApplicationConfiguration;
import com.atlassian.config.ConfigurationException;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.config.db.HibernateConfig;
import com.atlassian.config.db.HibernateConfigurator;
import com.atlassian.config.setup.SetupPersister;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.sql.Connection;

/**
 * Parent BootstrapManaager
 */
public interface AtlassianBootstrapManager
{
    /**
     * @return boolean indicating whether Confluence is bootstrapped.
     */
    boolean isBootstrapped();

    Object getProperty(String key);

    void setProperty(String key, Object value);

    boolean isPropertyTrue(String prop);

    void removeProperty(String key);

    String getString(String key);

    String getFilePathProperty(String key);

    Collection getPropertyKeys();

    Map getPropertiesWithPrefix(String prefix);

    /**
     * This is the build number of the current version that the user is running under.
     * This version is stored in their confluence home confluence.cfg.xml file
     */
    String getBuildNumber();

    void setBuildNumber(String buildNumber);

    boolean isApplicationHomeValid();

    Properties getHibernateProperties();

    void save() throws ConfigurationException;

    boolean isSetupComplete();

    String getOperation();

    void setOperation(String operation);

    void setSetupComplete(boolean complete);


    void bootstrapDatasource(String datasourceName, String hibernateDialect)
       throws BootstrapException;


    SetupPersister getSetupPersister();

    ApplicationConfiguration getApplicationConfig();

    String getApplicationHome();
    String getConfiguredApplicationHome();

    String getBootstrapFailureReason();

    /**
     * Does final initialisation of the BootstrapManager, including looking up the confluence home
     *
     * (Previously was the afterPropertiesSet method)
     *
     * @throws BootstrapException
     */
    void init() throws BootstrapException;

    void publishConfiguration();

    // -------------------------------------------------------------------------------------------------------------- Db
    void bootstrapDatabase(DatabaseDetails dbDetails, boolean embedded) throws BootstrapException;

    HibernateConfigurator getHibernateConfigurator();

    void setHibernateConfigurator(HibernateConfigurator hibernateConfigurator);

    HibernateConfig getHibernateConfig();

    Connection getTestDatasourceConnection(String datasourceName) throws BootstrapException;

    boolean databaseContainsExistingData(Connection connection);

    Connection getTestDatabaseConnection(DatabaseDetails databaseDetails) throws BootstrapException;
}
