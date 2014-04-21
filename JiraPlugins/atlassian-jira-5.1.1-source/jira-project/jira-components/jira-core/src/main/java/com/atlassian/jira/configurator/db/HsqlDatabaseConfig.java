
package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.Settings;
import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

public class HsqlDatabaseConfig extends AbstractDatabaseConfig implements DatabaseConfigConsole
{
    private final String PREFIX = "jdbc:hsqldb:";
    private final String SUFFIX = "/database/jiradb";
    
    private String jiraHome;

    public String getDatabaseType()
    {
        return "HSQL";
    }

    public String getInstanceFieldName()
    {
        return "Database";
    }

    public String getClassName()
    {
        return "org.hsqldb.jdbcDriver";
    }
    
    public String getUsername()
    {
        return "sa";
    }

    public String getPassword()
    {
        return "";
    }

    @Override
    public ConfigField[] getFields()
    {
        return null;
    }

    @Override
    public void setSettings(Settings settings) throws ParseException
    {
        jiraHome = settings.getJiraHome();
    }

    @Override
    public String getInstanceName()
    {
        return "(unused)";
    }

    @Override
    public void saveSettings(Settings newSettings) throws ValidationException
    {
        newSettings.getJdbcDatasourceBuilder()
                .setDriverClassName(getClassName())
                .setJdbcUrl(getUrl())
                .setUsername(getUsername())
                .setPassword(getPassword());
    }

    @Override
    public void testConnection()
    {
        // Nothing to test
    }

    @Override
    public String getUrl()
    {
        return PREFIX + jiraHome + SUFFIX;
    }

    public String getUrl(String hostname, String port, String instance)
    {
        return getUrl();
    }

    public DatabaseInstance parseUrl(String jdbcUrl) throws ParseException
    {
        if (!jdbcUrl.startsWith("jdbc:hsqldb:") || !jdbcUrl.endsWith("/database/jiradb"))
        {
            throw new ParseException("Unable to parse the HSQL JDBC URL '" + jdbcUrl + "'.");
        }
        String stripped = jdbcUrl.substring(PREFIX.length());
        jiraHome = stripped.substring(0, stripped.length() - SUFFIX.length());
        return new DatabaseInstance();
    }

    protected String getProtocolPrefix()
    {
        return PREFIX;
    }
}

