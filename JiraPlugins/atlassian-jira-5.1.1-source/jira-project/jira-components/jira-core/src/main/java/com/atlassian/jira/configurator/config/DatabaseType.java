package com.atlassian.jira.configurator.config;

public enum DatabaseType
{
    HSQL("HSQL", "hsql"),
    SQL_SERVER("SQL Server", "mssql"),
    MY_SQL("MySQL", "mysql"),
    ORACLE("Oracle", "oracle10g"),
    POSTGRES("PostgreSQL", "postgres72");

    private final String name;
    private final String typeName;

    DatabaseType(final String name, final String typeName)
    {
        this.name = name;
        this.typeName = typeName;
    }

    public String toString()
    {
        return getDisplayName();
    }

    public String getDisplayName()
    {
        return name;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public static DatabaseType forJdbcDriverClassName(final String jdbcDriverClass)
    {
        if (jdbcDriverClass == null)
        {
            return DatabaseType.HSQL;
        }
        if (jdbcDriverClass.equals("org.hsqldb.jdbcDriver"))
        {
            return DatabaseType.HSQL;
        }
        if (jdbcDriverClass.equals("com.mysql.jdbc.Driver"))
        {
            return DatabaseType.MY_SQL;
        }
        if (jdbcDriverClass.equals("oracle.jdbc.OracleDriver"))
        {
            return DatabaseType.ORACLE;
        }
        if (jdbcDriverClass.equals("org.postgresql.Driver"))
        {
            return DatabaseType.POSTGRES;
        }
        // SQL Server with jTDS drivers
        if (jdbcDriverClass.equals("net.sourceforge.jtds.jdbc.Driver"))
        {
            return DatabaseType.SQL_SERVER;
        }
        // SQL Server with Microsoft JDBC drivers. Supported for manual setting but not used by default.
        if (jdbcDriverClass.equals("com.microsoft.jdbc.sqlserver.SQLServerDriver"))
        {
            return DatabaseType.SQL_SERVER;
        }
        throw new IllegalArgumentException("Unknown JDBC Driver Class " + jdbcDriverClass);
    }

}
