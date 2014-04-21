package com.atlassian.jira.configurator.config;

/**
 * Stores raw settings as read from the config files
 */
public class Settings
{
    private String jiraHome;
    private String jdbcDriverClass, jdbcUrl, dbUsername, dbPassword;
    private String schemaName;
    private String dbPoolSize = "15";
    private String httpPort, controlPort;

    public String getJiraHome()
    {
        return jiraHome;
    }

    public void setJiraHome(final String jiraHome)
    {
        this.jiraHome = jiraHome;
    }

    public String getJdbcDriverClass()
    {
        return jdbcDriverClass;
    }

    public void setJdbcDriverClass(final String jdbcDriverClass)
    {
        this.jdbcDriverClass = jdbcDriverClass;
    }

    public String getJdbcUrl()
    {
        return jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl)
    {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDbUsername()
    {
        return dbUsername;
    }

    public void setDbUsername(final String dbUsername)
    {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword()
    {
        return dbPassword;
    }

    public void setDbPassword(final String dbPassword)
    {
        this.dbPassword = dbPassword;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public void setSchemaName(final String schemaName)
    {
        this.schemaName = schemaName;
    }

    public String getDbPoolSize()
    {
        return dbPoolSize;
    }

    public void setDbPoolSize(final String dbPoolSize)
    {
        this.dbPoolSize = dbPoolSize;
    }

    public String getHttpPort()
    {
        return httpPort;
    }

    public void setHttpPort(String httpPort)
    {
        this.httpPort = httpPort;
    }

    public String getControlPort()
    {
        return controlPort;
    }

    public void setControlPort(String controlPort)
    {
        this.controlPort = controlPort;
    }

    public DatabaseType getDatabaseType()
    {
        if (jdbcDriverClass == null)
            return DatabaseType.HSQL;
        if (jdbcDriverClass.equals("org.hsqldb.jdbcDriver"))
            return DatabaseType.HSQL;
        if (jdbcDriverClass.equals("com.mysql.jdbc.Driver"))
            return DatabaseType.MY_SQL;
        if (jdbcDriverClass.equals("oracle.jdbc.OracleDriver"))
            return DatabaseType.ORACLE;
        if (jdbcDriverClass.equals("org.postgresql.Driver"))
            return DatabaseType.POSTGRES;
        // SQL Server with jTDS drivers
        if (jdbcDriverClass.equals("net.sourceforge.jtds.jdbc.Driver"))
            return DatabaseType.SQL_SERVER;
        // SQL Server with Microsoft JDBC drivers. Supported for manual setting but not used by default.
        if (jdbcDriverClass.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver"))
            return DatabaseType.SQL_SERVER;

        throw new IllegalStateException("Unknown JDBC Driver Class " + jdbcDriverClass);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Settings settings = (Settings) o;

        if (controlPort != null ? !controlPort.equals(settings.controlPort) : settings.controlPort != null)
        {
            return false;
        }
        if (dbPassword != null ? !dbPassword.equals(settings.dbPassword) : settings.dbPassword != null)
        {
            return false;
        }
        if (dbPoolSize != null ? !dbPoolSize.equals(settings.dbPoolSize) : settings.dbPoolSize != null)
        {
            return false;
        }
        if (dbUsername != null ? !dbUsername.equals(settings.dbUsername) : settings.dbUsername != null)
        {
            return false;
        }
        if (httpPort != null ? !httpPort.equals(settings.httpPort) : settings.httpPort != null)
        {
            return false;
        }
        if (jdbcDriverClass != null ? !jdbcDriverClass.equals(settings.jdbcDriverClass) : settings.jdbcDriverClass != null)
        {
            return false;
        }
        if (jdbcUrl != null ? !jdbcUrl.equals(settings.jdbcUrl) : settings.jdbcUrl != null)
        {
            return false;
        }
        if (jiraHome != null ? !jiraHome.equals(settings.jiraHome) : settings.jiraHome != null)
        {
            return false;
        }
        if (schemaName != null ? !schemaName.equals(settings.schemaName) : settings.schemaName != null)
        {
            return false;
        }

        return true;
    }
}
