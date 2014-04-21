package com.atlassian.jira.config.database;

import com.atlassian.jira.util.KeyValuePair;
import com.atlassian.jira.util.KeyValueParser;
import com.atlassian.multitenant.CustomConfigHandler;
import org.dom4j.Element;

import java.util.Properties;

/**
 * Handler for parsing datasource config from and writing datasource config to XML.
 */
public class DatabaseConfigHandler implements CustomConfigHandler<DatabaseConfig>
{
    private static final int DEFAULT_MAX_ACTIVE = 8;

    @Override
    public Class<DatabaseConfig> getBeanClass()
    {
        return DatabaseConfig.class;
    }

    @Override
    public DatabaseConfig parse(Element element)
    {
        String name = element.elementText("name");
        Element delegatorElement = element.element("delegator-name");
        String delegator = name;
        if (delegatorElement != null)
        {
            delegator = element.elementText("delegator-name");
        }
        String databaseType = element.elementText("database-type");
        String schemaName = element.elementText("schema-name");
        Element datasourceElement = element.element("jndi-datasource");
        Datasource datasource;
        if (datasourceElement != null)
        {
            String jndiName = datasourceElement.elementText("jndi-name");
            datasource = new JndiDatasource(jndiName);
        }
        else
        {
            datasourceElement = element.element("jdbc-datasource");
            if (datasourceElement == null)
            {
                throw new IllegalArgumentException("No datasource specified!");
            }
            String jdbcUrl = datasourceElement.elementText("url");
            String driverClassName = datasourceElement.elementText("driver-class");
            String username = datasourceElement.elementText("username");
            String password = datasourceElement.elementText("password");
            Properties connectionProperties = parseConnectionProperties(datasourceElement.elementText("connection-properties"));
            String poolSizeStr = datasourceElement.elementText("pool-size");
            int poolSize;
            if (poolSizeStr == null)
            {
                poolSize = DEFAULT_MAX_ACTIVE;
            }
            else
            {
                poolSize = Integer.parseInt(poolSizeStr);
            }
            String validationQuery = datasourceElement.elementText("validation-query");
            Long minEvictableTimeMillis = parseLong(datasourceElement.elementText("min-evictable-idle-time-millis"));
            Long timeBetweenEvictionRunsMillis = parseLong(datasourceElement.elementText("time-between-eviction-runs-millis"));
            datasource = new JdbcDatasource(jdbcUrl, driverClassName, username, password, connectionProperties, poolSize, validationQuery, minEvictableTimeMillis, timeBetweenEvictionRunsMillis);
        }
        return new DatabaseConfig(name, delegator, databaseType, schemaName, datasource);
    }

    private Properties parseConnectionProperties(String value)
    {
        if (value == null || value.length() == 0)
        {
            return null;
        }
        // <connection-properties>portNumber=5432;defaultAutoCommit=true</connection-properties>
        // Split on the semicolon
        Properties properties = new Properties();
        final String[] keyValues = value.split(";");
        for (String keyValueText : keyValues)
        {
            final KeyValuePair<String,String> keyValuePair = KeyValueParser.parse(keyValueText);
            properties.setProperty(keyValuePair.getKey(), keyValuePair.getValue());
        }
        return properties;
    }

    private Long parseLong(String s)
    {
        if (s == null)
        {
            return null;
        }
        return Long.parseLong(s);
    }

    @Override
    public void writeTo(Element element, DatabaseConfig databaseConfig)
    {
        element.addElement("name").setText(databaseConfig.getDatasourceName());
        element.addElement("delegator-name").setText(databaseConfig.getDelegatorName());
        element.addElement("database-type").setText(databaseConfig.getDatabaseType());
        if (databaseConfig.getSchemaName() != null)
        {
            element.addElement("schema-name").setText(databaseConfig.getSchemaName());
        }
        if (databaseConfig.getDatasource() instanceof JndiDatasource)
        {
            JndiDatasource jndiDatasource = (JndiDatasource) databaseConfig.getDatasource();
            Element jndi = element.addElement("jndi-datasource");
            jndi.addElement("jndi-name").setText(jndiDatasource.getJndiName());
        }
        if (databaseConfig.getDatasource() instanceof JdbcDatasource)
        {
            JdbcDatasource jdbcDatasource = (JdbcDatasource) databaseConfig.getDatasource();
            Element jdbc = element.addElement("jdbc-datasource");
            jdbc.addElement("url").setText(jdbcDatasource.getJdbcUrl());
            jdbc.addElement("driver-class").setText(jdbcDatasource.getDriverClassName());
            jdbc.addElement("username").setText(jdbcDatasource.getUsername());
            jdbc.addElement("password").setText(jdbcDatasource.getPassword());
            jdbc.addElement("pool-size").setText(Integer.toString(jdbcDatasource.getPoolSize()));
            //    <validation-query>SELECT 'X' FROM DUAL</validation-query>
            if (jdbcDatasource.getValidationQuery() != null)
            {
                jdbc.addElement("validation-query").setText(jdbcDatasource.getValidationQuery());
            }
            //    <min-evictable-idle-time-millis>4000</min-evictable-idle-time-millis>
            if (jdbcDatasource.getMinEvictableTimeMillis() != null)
            {
                jdbc.addElement("min-evictable-idle-time-millis").setText(String.valueOf(jdbcDatasource.getMinEvictableTimeMillis()));
            }
            //    <time-between-eviction-runs-millis>5000</time-between-eviction-runs-millis>
            if (jdbcDatasource.getTimeBetweenEvictionRunsMillis() != null)
            {
                jdbc.addElement("time-between-eviction-runs-millis").setText(String.valueOf(jdbcDatasource.getTimeBetweenEvictionRunsMillis()));
            }
        }
    }
}
