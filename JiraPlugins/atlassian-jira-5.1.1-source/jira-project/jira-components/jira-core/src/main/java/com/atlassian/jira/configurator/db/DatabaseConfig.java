package com.atlassian.jira.configurator.db;

import com.atlassian.jira.configurator.config.ValidationException;
import com.atlassian.jira.exception.ParseException;

/**
 * Implementations of this interface provide DB-specific JDBC configuration operations.
 *
 */
public interface DatabaseConfig
{
    String getDatabaseType();

    /**
     * The name of the "Instance" field for this DB.
     * eg for Oracle it is "SID", while for MySQL and PostgreSQL it is "Database" and for MS-SQL it is "Instance".
     * @return name of the "Instance" field for this DB.
     */
    String getInstanceFieldName();

    String getClassName();

    DatabaseInstance parseUrl(final String jdbcUrl) throws ParseException;

    /**
     * Returns the JDBC URL for this DB config.
     *
     * @param hostname the hostname
     * @param port the TCP/IP port number
     * @param instance the DB "instance"
     * @return the JDBC URL for this DB config.
     *
     * @throws ValidationException If the underlying configuration is invalid for this DB type. eg for Postgres, "Database" (instance) is a required field
     */
    String getUrl(String hostname, String port, String instance) throws ValidationException;
}
