package com.atlassian.config.db;

import com.atlassian.config.ApplicationConfiguration;
import com.atlassian.config.ConfigurationException;
import com.atlassian.config.bootstrap.BootstrapException;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;

/**
 * The DatabaseHelper class provides methods used during bootstrap. In order to be able to mock those methods, the
 * functionality was componentized.
 */
public class DatabaseHelper
{
    private final static Logger log = Logger.getLogger(DatabaseHelper.class);

    /**
     * Test whether the database's lower function supports non-ascii characters and sets a hibernate parameter in the
     * application configuration. This serves as a workaround for postgres which does not support lower casing of
     * non-ascii characters in certain versions. The property set in this method will be used in
     * GeneralUtil.specialToLowerCase() to indicate whether the database supports the lowercasing or not. The property
     * might not be set if the database does not support the query.
     *
     * @param databaseProperties
     * @param applicationConfig
     * @throws BootstrapException
     */
    public void setDatabaseLowerProperty(Properties databaseProperties, ApplicationConfiguration applicationConfig) throws BootstrapException
    {
        Connection conn = null;
        try
        {
            if (isDirectConnection(databaseProperties))
            {
                String driverClassName = databaseProperties.getProperty("hibernate.connection.driver_class");
                String databaseUrl = databaseProperties.getProperty("hibernate.connection.url");
                String userName = databaseProperties.getProperty("hibernate.connection.username");
                String password = databaseProperties.getProperty("hibernate.connection.password");

                if (driverClassName != null)
                    Class.forName(driverClassName);
                
                conn = DriverManager.getConnection(databaseUrl, userName, password);
            }
            else
            {
                conn = getDatasource(databaseProperties.getProperty("hibernate.connection.datasource")).getConnection();
            }

            Statement st = conn.createStatement();
            // This query is not supported by all database (eg. HSQL). Howerver, since this is mainly a workaround for postgres, we don't care much.
            String sqlQuery = "select lower('\u00dcbersicht')";
            ResultSet rs = st.executeQuery(sqlQuery);
            rs.next();
            if (rs.getString(1).equals("\u00dcbersicht"))
            {
                applicationConfig.setProperty("hibernate.database.lower_non_ascii_supported", Boolean.FALSE);
            }
            else
            {
                applicationConfig.setProperty("hibernate.database.lower_non_ascii_supported", Boolean.TRUE);
            }
            applicationConfig.save();
        }

        catch (SQLException e)
        {
            log.info("SQL query could not be excecuted: " + e, e);
        }

        catch (ClassNotFoundException e)
        {
            log.error(e, e);
        }

        catch (ConfigurationException e)
        {
            log.error("Configuration file could not be saved: ", e);
        }

        finally
        {
            try
            {
                if (conn == null)
                {
                    log.error("Connection was null. We could not successfully connect to the specified database!");
                }

                else
                    conn.close();
            }
            catch (SQLException e)
            {
                log.fatal("Could not close database connection opened for first test! Exception: " + e);
            }
        }
    }

    public DataSource getDatasource(String ds) throws BootstrapException
    {
        DataSource dsrc = null;

        log.debug("datasource is " + ds);

        try
        {
            InitialContext ctx = new InitialContext();
            dsrc = (DataSource) ctx.lookup(ds);

            if (dsrc == null)
            {
                throw new NamingException("Could not locate " + ds);
            }
        }
        catch (NamingException e)
        {
            log.error("Could not locate datasource: " + ds, e);
            throw new BootstrapException("Could not locate datasource: " + ds, e);
        }
        catch (ClassCastException e)
        {
            log.error("Couldn't locate Datasource (" + ds + ") in the initial context. An object was bound to this name but whatever we found, it wasn't a Datasource: " + e);
            throw new BootstrapException("Couldn't locate Datasource (" + ds + ") in the initial context. An object was bound to this name but whatever we found, it wasn't a Datasource: ", e);
        }
        return dsrc;
    }

    private boolean isDirectConnection(Properties databaseProperties)
    {
        return databaseProperties.getProperty("hibernate.connection.datasource") == null;
    }
}


