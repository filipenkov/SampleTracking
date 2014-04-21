package com.atlassian.jira.configurator.db;

import com.atlassian.jira.exception.ParseException;

abstract class AbstractDatabaseConfig implements DatabaseConfig
{
    protected String removeProtocolPrefix(final String jdbcUrl) throws ParseException
    {
        String protocolPrefix = getProtocolPrefix();
        if (!jdbcUrl.startsWith(protocolPrefix))
        {
            throw new ParseException("Unable to parse the JDBC URL '" + jdbcUrl +
                                         "'. It should start with protocol prefix '" + protocolPrefix + "'.");
        }
        // Strip off the protocol prefix
        return jdbcUrl.substring(protocolPrefix.length());
    }

    protected abstract String getProtocolPrefix() throws ParseException;
}
