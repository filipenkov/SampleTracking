/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.config.db;

import com.atlassian.config.ConfigurationException;
import com.atlassian.core.util.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class DatabaseDetails
{
    private static final Logger log = Logger.getLogger(DatabaseDetails.class);

    private String driverClassName;
    private String databaseUrl;
    private String userName;
    private String password;
    private int poolSize;
    private String dialect;
    private Properties configProps;
    private List dbNotes = new ArrayList();
    private Properties extraHibernateProperties = new Properties();

    public String getDatabaseUrl()
    {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl)
    {
        // CONFIG-20 - trailing whitespace causes issues with mysql
        this.databaseUrl = nullSafeTrim(databaseUrl);
    }

    public int getPoolSize()
    {
        return poolSize;
    }

    public void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }

    public String getDriverClassName()
    {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName)
    {
        this.driverClassName = nullSafeTrim(driverClassName);
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = nullSafeTrim(userName);
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDialect()
    {
        return dialect;
    }

    public void setDialect(String dialect)
    {
        this.dialect = nullSafeTrim(dialect);
    }

    public List getDbNotes()
    {
        return dbNotes;
    }

    public void setDbNotes(List dbNotes)
    {
        this.dbNotes = dbNotes;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str = str.append(getDriverClassName()).append("\n");
        str = str.append(getDatabaseUrl()).append("\n");
        str = str.append(getDialect()).append("\n");
        str = str.append(getUserName()).append("\n");
        str = str.append(getPassword()).append("\n");

        return str.toString();
    }

    public Properties getConfigProps()
    {
        return configProps;
    }

    /**
     * This method will set the dialect, pool size, and configuration properties based on a given database name.
     */
    public void setupForDatabase(String database)
    {
        int poolSizeToSet = 10;

        if (database.equals("other"))
        {
            setPoolSize(poolSizeToSet);
            return;
        }

        Properties props = getConfigProperties(database);
        setDialect(props.getProperty("dialect"));

        try
        {
            poolSizeToSet = Integer.parseInt(props.getProperty("poolSize"));
        }
        catch (NumberFormatException e)
        {
            log.error("Could find a property for poolSize; nonetheless, defaulting to 10.");
        }

        setPoolSize(poolSizeToSet);

        this.configProps = props;

        storeHibernateProperties(props);

    }

    /**
     * Looks for properties prefixed with "hibernate." in a given properties file
     * and bundles them into exextraHibernateProperties, for use in the hibernate connection.
     * @param props
     */
    private void storeHibernateProperties(Properties props)
    {
        //now look for errata hibernate properties, which might have been passed within the properties file
        Enumeration enu = props.keys();

        while (enu.hasMoreElements())
        {
            String key = (String) enu.nextElement();

            if (key.matches("hibernate.*") && props.getProperty(key) != null)
            {
                extraHibernateProperties.put(key, props.getProperty(key));
            }
            else if (props.getProperty(key) == null) //it's helpful to output any nulls, which might be problematic when
            {                                       //playing with a connection at the back end
                log.warn("database hibernate property present but set to null: [" + key + "] = [" + props.getProperty(key) + "]. Setting this property anyway." );
                extraHibernateProperties.put(key, props.getProperty(key));
            }
        }
    }

    static Properties getConfigProperties(String databaseName)
    {
        return PropertyUtils.getProperties("database-defaults/" + databaseName.toLowerCase() + ".properties", DatabaseDetails.class);
    }

    /**
     * Constructs a default db config instance based upon the contents of a properties file,
     * expected to look something like:
     * <p/>
     * driverClassName=oracle.jdbc.OracleDriver
     * databaseUrl=jdbc:oracle:thin:@localhost:1521:SID
     * userName=
     * password=
     * poolSize= 10
     * dialect=net.sf.hibernate.dialect.HSQLDialect
     * <p/>
     * and be within the pwd/actions dir..
     *
     * @param databaseName
     */
    public static DatabaseDetails getDefaults(String databaseName) throws ConfigurationException
    {
        DatabaseDetails defaults = new DatabaseDetails();

        if ("other".equals(databaseName.toLowerCase()))
            return defaults;

        Properties props = getConfigProperties(databaseName);
        if (props == null)
        {
            throw new ConfigurationException("The default values for '" + databaseName + "' not found. Check that properties file exists in your database-defaults directory");
        }

        defaults.setDriverClassName(props.getProperty("driverClassName"));
        defaults.setDatabaseUrl(props.getProperty("databaseUrl"));
        defaults.setUserName(props.getProperty("userName"));
        defaults.setPassword(props.getProperty("password"));

        defaults.storeHibernateProperties(props);

        // load notes
        List dbNotes = new ArrayList();
        int i = 1;

        while (StringUtils.isNotEmpty(props.getProperty("note" + i)))
        {
            dbNotes.add(props.getProperty("note" + i));
            i++;
        }

        defaults.setDbNotes(dbNotes);

        try
        {
            defaults.setPoolSize(Integer.parseInt(props.getProperty("poolSize")));
        }
        catch (NumberFormatException e)
        {
            log.error("Bad number within poolSize field in " + databaseName + ".");
            throw new ConfigurationException(e.getMessage(), e);
        }

        return defaults;
    }

    public Properties getExtraHibernateProperties()
    {
        return extraHibernateProperties;
    }

    /**
     *
     * @return boolean - whether driver exists in class path.
     */
    public boolean checkDriver()
    {
        try
        {
            Class.forName(getDriverClassName());
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    private static String nullSafeTrim(String str) {
        return str != null ? str.trim() : null;
    }
}
