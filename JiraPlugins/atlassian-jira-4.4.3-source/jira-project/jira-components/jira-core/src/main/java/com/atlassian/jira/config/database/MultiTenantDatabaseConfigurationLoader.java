package com.atlassian.jira.config.database;

import com.atlassian.jira.util.RuntimeIOException;
import com.atlassian.multitenant.MultiTenantContext;

/**
 * Implements the {@link DatabaseConfigurationLoader} for MultiTenant mode - where the {@link
 * com.atlassian.multitenant.MultiTenantContext} provides configuration storage for this stuff.
 *
 * @since v4.4
 */
public class MultiTenantDatabaseConfigurationLoader implements DatabaseConfigurationLoader
{
    @Override
    public boolean configExists()
    {
        return MultiTenantContext.getTenantReference().get().getConfig(DatabaseConfig.class) != null;
    }

    @Override
    public DatabaseConfig loadDatabaseConfiguration() throws RuntimeException, RuntimeIOException
    {
        return MultiTenantContext.getTenantReference().get().getConfig(DatabaseConfig.class);
    }

    @Override
    public void saveDatabaseConfiguration(DatabaseConfig config) throws RuntimeIOException
    {
        throw new UnsupportedOperationException("Under multi-tenant, the database configuration is immutable");
    }
}
