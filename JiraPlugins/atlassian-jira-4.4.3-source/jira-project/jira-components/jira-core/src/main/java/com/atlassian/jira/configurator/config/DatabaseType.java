package com.atlassian.jira.configurator.config;

public enum DatabaseType
{
    HSQL("HSQL"), SQL_SERVER("SQL Server"), MY_SQL("MySQL"), ORACLE("Oracle"), POSTGRES("PostgreSQL");

    private String name;

    DatabaseType(final String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return getDisplayName();
    }

    public String getDisplayName()
    {
        return name;
    }
}
