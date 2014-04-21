package com.atlassian.config.bootstrap;

import com.atlassian.config.ApplicationConfiguration;
import com.atlassian.config.ConfigurationException;
import com.atlassian.config.HomeLocator;
import com.atlassian.config.db.DatabaseDetails;
import com.atlassian.config.db.HibernateConfig;
import com.atlassian.config.db.HibernateConfigurator;
import com.atlassian.config.setup.SetupPersister;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Generic Bootstrap Manager using Spring & Hibernate
 */
public class DefaultAtlassianBootstrapManager implements AtlassianBootstrapManager
{
    public static final Logger log = Logger.getLogger(DefaultAtlassianBootstrapManager.class);

    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected boolean bootstrapped;
    protected String bootstrapFailureReason;
    /**
     * A helper operation string, used for conditional moves throughout the bootstrapManager in the controller
     */
    private String operation;

    // --------------------------------------------------------------------------------------------- Injected Properties
    private List tables;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    protected ApplicationConfiguration applicationConfig;
    protected SetupPersister setupPersister;
    protected HomeLocator homeLocator;
    protected HibernateConfigurator hibernateConfigurator;
    protected HibernateConfig hibernateConfig;

    // ---------------------------------------------------------------------------------------------------- Constructors

    // -------------------------------------------------------------------------------------------------- Public Methods
    public void init() throws BootstrapException
    {
        /**
         * Importantly, we do not throw a bootstrap exception if the home variable has yet to be set.
         * So, if we can't build a home  we'll let the user know about this via the normal process,
         * CheckListAction
         */
        try
        {
            if (StringUtils.isNotEmpty(homeLocator.getHomePath()))
            {
                applicationConfig.setApplicationHome(homeLocator.getHomePath());
                applicationConfig.setConfigurationFileName(homeLocator.getConfigFileName());

                if (applicationConfig.configFileExists())
                {
                    applicationConfig.load();
                }

                afterConfigurationLoaded();

                setupPersister.setSetupType(applicationConfig.getSetupType());
                if (SetupPersister.SETUP_STATE_COMPLETE.equals(setupPersister.getCurrentStep()))
                {
                    // If persistence upgrade fails, do not publish
                    if (!performPersistenceUpgrade())
                    {
                        return;
                    }

                    applicationConfig.setSetupComplete(true);
                    publishConfiguration();
                }
            }
            else
            {
                // If no Home is set, the bootstrap is still considered successful,
                // we just move on to the checklist action that says there's no home set.
                log.warn("Unable to set up application config: no home set");
            }

            finishBootstrapInitialisation();

            bootstrapped = true;
        }
        catch (ConfigurationException e)
        {
            log.error("Home is not configured properly: " + e);
            bootstrapped = false;
            bootstrapFailureReason = e.getMessage();
        }
    }

    public void publishConfiguration()
    {
        // default implementation does nothing
    }


    // --------------------------------------------------------------------------- ApplicationConfig convenience methods
    /**
     * Get a single property.
     */
    public Object getProperty(String key)
    {
        Object o = null;
        try
        {
            o = applicationConfig.getProperty(key);
        }
        catch (NullPointerException e)
        {
            log.error("BootstrapManager was asked to fetch property (" + key + ") and found a NullPointer");
        }

        return o;
    }

    /**
     * Set a single property.
     */
    public void setProperty(String key, Object value)
    {
        if (value == null)
        {
            applicationConfig.removeProperty(key);
        }
        else
        {
            applicationConfig.setProperty(key, value);
        }
        if (isSetupComplete())
        {
            publishConfiguration();
        }
    }

    public boolean isPropertyTrue(String prop)
    {
        return "true".equals(getString(prop));
    }

    /**
     * Remove a single property.
     */
    public void removeProperty(String key)
    {
        applicationConfig.removeProperty(key);
    }

    /**
     * Convenience method to retrieve a property as a string.
     */
    public String getString(String key)
    {
        return (String) applicationConfig.getProperty(key);
    }

    public String getFilePathProperty(String key)
    {
        return getString(key);
    }

    /**
     * Retrieve all property keys.
     */
    public Collection getPropertyKeys()
    {
        return applicationConfig.getProperties().keySet();
    }

    /**
     * Get a map of all properties with a given prefix.
     */
    public Map getPropertiesWithPrefix(String prefix)
    {
        return applicationConfig.getPropertiesWithPrefix(prefix);
    }

    public void save() throws ConfigurationException
    {
        applicationConfig.save();
    }

    public String getConfiguredApplicationHome()
    {
        return homeLocator.getHomePath();
    }

    /**
     * This should be the first method called before any bootstrapManager logic is performed.
     *
     * @return true for a complete bootstrapManager, otherwise false
     */
    public boolean isSetupComplete()
    {
        return isBootstrapped() && applicationConfig.isSetupComplete();
    }

    public void setSetupComplete(boolean complete)
    {
        applicationConfig.setSetupComplete(complete);
    }

    // ---------------------------------------------------------------------------------------------------- Build Number
    public String getBuildNumber()
    {
        return applicationConfig.getBuildNumber();
    }

    public void setBuildNumber(String buildNumber)
    {
        applicationConfig.setBuildNumber(buildNumber);
    }


    // -------------------------------------------------------------------------------------- Hibernate / Database Setup

    /**
     * Gets all hibernate properties from the config starting 'hibernate.'
     *
     * @return all hibernate properties in a Properties map
     */
    public Properties getHibernateProperties()
    {
        Properties props = new Properties();
        props.putAll(applicationConfig.getPropertiesWithPrefix("hibernate."));
        return props;
    }

    /**
     * Mediates the call to HibernateConfigurator's instance method, configureDatabase(dbDetails, embedded)
     *
     * @param dbDetails - DatabaseDetails object holding connection details
     * @param embedded  - true if using the default Confluence database, HSQLDB
     * @throws BootstrapException
     */
    public void bootstrapDatabase(DatabaseDetails dbDetails, boolean embedded)
            throws BootstrapException
    {
        try
        {
            hibernateConfigurator.configureDatabase(dbDetails, embedded);
        }
        catch (ConfigurationException e)
        {
            log.fatal("Could not successfully configure database: \n db: " + dbDetails + " \n embedded = " + embedded);
            log.fatal("ConfigurationException reads thus: " + e);
            hibernateConfigurator.unconfigureDatabase();
            throw new BootstrapException(e);
        }

        Connection conn = null;
        try
        {
            conn = getTestDatabaseConnection(dbDetails);
            if (!databaseContainsExistingData(conn))
            {
                throw new BootstrapException("Schema creation complete, but database tables don't seem to exist.");
            }
        }
        finally
        {
            try
            {
                if (conn != null) conn.close();
            }
            catch (SQLException e)
            {
                // ignore exception in finally
            }
        }

        postBootstrapDatabase();
    }



    /**
     * Mediates the call to HibernateConfigurator's instance method, configureDatasource(datasourceName,
     * hibernateDialect)
     *
     * @param datasourceName
     * @param hibernateDialect
     * @throws BootstrapException
     */
    public void bootstrapDatasource(String datasourceName, String hibernateDialect)
            throws BootstrapException
    {
        try
        {
            hibernateConfigurator.configureDatasource(datasourceName, hibernateDialect);
        }
        catch (ConfigurationException e)
        {
            log.fatal("Could not successfully configure datasource: \n db: " + datasourceName + " \n dialect = "
                      + hibernateDialect);
            log.fatal("ConfigurationException reads thus: " + e);
            hibernateConfigurator.unconfigureDatabase();
            throw new BootstrapException(e);
        }
        Connection connection = null;
        try
        {
            connection = getTestDatasourceConnection(datasourceName);
            if (!databaseContainsExistingData(connection))
            {
                throw new BootstrapException("Schema creation complete, but tables could not be found.");
            }
        }
        finally
        {
            try
            {
                if (connection != null) connection.close();
            }
            catch (SQLException e)
            {
                // ignore in finally
            }
        }
        postBootstrapDatabase();
    }

    /**
     * Ensure we can open a connection to the configured database. If this fails, then the database is down or we got
     * the connection string wrong.
     *
     * @throws BootstrapException if the connection fails for any reason.
     */
    public Connection getTestDatabaseConnection(DatabaseDetails databaseDetails) throws BootstrapException
    {
        Connection conn = null;
        try
        {
            Class.forName(databaseDetails.getDriverClassName());
            conn = DriverManager.getConnection(getDbUrl(databaseDetails),
                                               databaseDetails.getUserName(),
                                               databaseDetails.getPassword());
            if (conn == null)
            {
                throw new BootstrapException("Connection was null. We could not successfully connect to the specified database!");
            }
            return conn;
        }
        catch (SQLException e)
        {
            log.error("Could not successfully test your database: ", e);
            throw new BootstrapException(e);
        }
        catch (ClassNotFoundException e)
        {
            log.error("Could not successfully test your database: ", e);
            throw new BootstrapException(e);
        }
    }


    /**
     * Gets a test connection to the datasource.
     *
     * @throws BootstrapException if a connection cannot be made.
     */
    public Connection getTestDatasourceConnection(String datasourceName) throws BootstrapException
    {
        DataSource dsrc;

        log.debug("datasource is " + datasourceName);

        try
        {
            InitialContext ctx = new InitialContext();
            dsrc = (DataSource) ctx.lookup(datasourceName);

            if (dsrc == null)
            {
                throw new NamingException("Could not locate " + datasourceName);
            }
        }
        catch (NamingException e)
        {
            log.error("Could not locate datasource: " + datasourceName, e);
            throw new BootstrapException("Could not locate datasource: " + datasourceName, e);
        }
        catch (ClassCastException e)
        {
            log.error("Couldn't locate Datasource (" + datasourceName + ") in the initial context. An object was bound to this name but whatever we found, it wasn't a Datasource: " + e);
            throw new BootstrapException("Couldn't locate Datasource (" + datasourceName + ") in the initial context. An object was bound to this name but whatever we found, it wasn't a Datasource: ", e);
        }

        try
        {
            Connection conn = dsrc.getConnection();
            conn.createStatement();
            return conn;
        }
        catch (SQLException e)
        {
            log.error("Couldn't open a connection on Datasource (" + datasourceName + "): " + e);
            throw new BootstrapException("Couldn't open a connection on Datasource (" + datasourceName + "): ", e);
        }
        catch (NullPointerException e)
        {
            log.error("Couldn't open a connection on Datasource (" + datasourceName + "): " + e);
            throw new BootstrapException("Couldn't open a connection on Datasource (" + datasourceName + "): ", e);
        }
    }

    /**
     * Returns true if any of the specified tables exists, otherwise false.
     *
     * @param connection the Connection to the Db to check
     * @see {@link #setTables(List)}
     */
    public boolean databaseContainsExistingData(Connection connection)
    {
        for (Iterator iterator = getTables().iterator(); iterator.hasNext();)
        {
            String table = (String) iterator.next();
            if (tableExists(connection, table))
                return true;
        }
        return false;
    }

    private boolean tableExists(Connection conn, String table)
    {
        Statement st = null;
        try
        {
            st = conn.createStatement();
            st.executeQuery("select count(*) from " + table);
            return true;
        }
        catch (SQLException e)
        {
            return false;
        }
        finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                }
                catch (SQLException e)
                {
                    // ignore
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ Application Home
    public boolean isApplicationHomeValid()
    {
        return applicationConfig.isApplicationHomeValid();
    }

    // ------------------------------------------------------------------------------------------------ Extension points

    /**
     * Extension point for peforming custom upgardes of dB
     * @return true if successful, false if failed
     */
    protected boolean performPersistenceUpgrade()
    {
        // do nudda
        return true;
    }

    /**
     * Generic extension point to run before bootstrapping can be successful. Common things to do here includes setting
     * up the license
     * @throws ConfigurationException is thrown if bootstrapping should fail
     */
    protected void finishBootstrapInitialisation() throws ConfigurationException
    {
        // Do nudda
    }

    /**
     * Get the URL form the {@link DatabaseDetails} object. Allows sub-classes to post-process the URL
     * @param dbDetails
     * @return database URL
     */
    protected String getDbUrl(DatabaseDetails dbDetails)
    {
        return dbDetails.getDatabaseUrl();
    }

    /**
     * Allows a custom actions to be performed after bootstrapping the database
     */
    protected void postBootstrapDatabase()
    {
        // Do nudda
    }

    /**
     * Extension point for initialization performed after configuration is loaded
     */
    protected void afterConfigurationLoaded() throws ConfigurationException
    {
        // Do nudda
    }



    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public void setApplicationConfig(ApplicationConfiguration applicationConfig)
    {
        this.applicationConfig = applicationConfig;
    }

    public void setHomeLocator(HomeLocator homeLocator)
    {
        this.homeLocator = homeLocator;
    }

    public void setSetupPersister(SetupPersister setupPersister)
    {
        this.setupPersister = setupPersister;
    }

    public HomeLocator getHomeLocator()
    {
        return homeLocator;
    }

    public ApplicationConfiguration getApplicationConfig()
    {
        return applicationConfig;
    }

    public String getApplicationHome()
    {
        return applicationConfig.getApplicationHome();
    }

    public SetupPersister getSetupPersister()
    {
        return setupPersister;
    }

    /**
     * @return boolean indicating whether Confluence is bootstrapped.
     */
    public boolean isBootstrapped()
    {
        return bootstrapped;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public HibernateConfigurator getHibernateConfigurator()
    {
        return hibernateConfigurator;
    }

    public void setHibernateConfigurator(HibernateConfigurator hibernateConfigurator)
    {
        this.hibernateConfigurator = hibernateConfigurator;
    }

    public HibernateConfig getHibernateConfig()
    {
        return hibernateConfig;
    }

    public void setHibernateConfig(HibernateConfig hibernateConfig)
    {
        this.hibernateConfig = hibernateConfig;
    }

    public String getBootstrapFailureReason()
    {
        return bootstrapFailureReason;
    }

    public List getTables()
    {
        return tables;
    }

    public void setTables(List tables)
    {
        this.tables = tables;
    }
}
