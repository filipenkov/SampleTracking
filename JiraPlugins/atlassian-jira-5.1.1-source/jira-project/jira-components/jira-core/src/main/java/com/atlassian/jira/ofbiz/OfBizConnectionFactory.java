package com.atlassian.jira.ofbiz;

import org.ofbiz.core.entity.config.DatasourceInfo;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection factory for finding out about OfBiz connections and datasources
 */
public interface OfBizConnectionFactory
{
    /**
     * Get a connection for the current tenant
     *
     * @return Returns a database connection
     * @throws SQLException If an error occured
     */
    public Connection getConnection() throws SQLException;

    /**
     * Get the datasource info for the current tenant
     *
     * @return The datasource info
     */
    public DatasourceInfo getDatasourceInfo();

    /**
     * Get the delegator name for the current tenant
     *
     * @return The name of the delegator
     */
    public String getDelegatorName();
}
