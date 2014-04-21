package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.DatabaseType;

/**
 * @since v4.4
 */
public class DatabaseConfigFactory
{
    public static DatabaseConfig getDatabaseConfigFor(DatabaseType databaseType)
    {
        switch (databaseType)
        {
            case MY_SQL:
                return new MySqlDatabaseConfig();
            case ORACLE:
                return new OracleDatabaseConfig();
            case POSTGRES:
                return new PostgresDatabaseConfig();
            case SQL_SERVER:
                return new SqlServerDatabaseConfig();
        }
        throw new IllegalArgumentException("Unknown DatabaseType " + databaseType);
    }
}
