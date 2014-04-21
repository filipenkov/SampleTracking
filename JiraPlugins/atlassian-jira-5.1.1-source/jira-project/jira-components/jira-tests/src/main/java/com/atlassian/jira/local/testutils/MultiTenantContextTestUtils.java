package com.atlassian.jira.local.testutils;

import com.atlassian.jira.mock.multitenant.MockMultiTenantComponentFactory;
import com.atlassian.jira.mock.multitenant.MockMultiTenantManager;
import com.atlassian.jira.mock.multitenant.MockTenant;
import com.atlassian.jira.mock.multitenant.MockTenantReference;
import com.atlassian.jira.config.database.DatabaseConfig;
import com.atlassian.jira.config.database.JndiDatasource;
import com.atlassian.jira.startup.JiraHomePathLocator;
import com.atlassian.jira.util.TempDirectoryUtil;
import com.atlassian.multitenant.MultiTenantComponentFactory;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantManager;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;

import java.util.Collections;
import java.util.Map;

/**
 * Utility methods for setting up multi-tenant context in unit tests..
 *
 * @since v4.3
 */
public class MultiTenantContextTestUtils
{
    private static String tempJiraHome;

    /**
     * Sets up multi tenancy for unit tests.  Ensures that there is a tenant reference in the multitenant context,
     * which always returns a tenant that has a basic configuration.
     */
    public static void setupMultiTenantSystem()
    {
        JndiDatasource datasource = new JndiDatasource("java:comp/env/mock");
        DatabaseConfig databaseConfig = new DatabaseConfig("mockdb", "MOCK", datasource);
        Map<Class<?>, Object> configMap = Collections.<Class<?>, Object>singletonMap(DatabaseConfig.class, databaseConfig);
        if (tempJiraHome == null)
        {
            tempJiraHome = TempDirectoryUtil.createTempDirectory(JiraHomePathLocator.Property.JIRA_HOME).getAbsolutePath();
        }
        Tenant mockTenant = new MockTenant("mockTenant", tempJiraHome, configMap);

        TenantReference tenantReference = new MockTenantReference(mockTenant);
        MultiTenantComponentFactory factory = new MockMultiTenantComponentFactory(tenantReference);
        MultiTenantManager manager = new MockMultiTenantManager(tenantReference);

        MultiTenantContext.Builder builder = new MultiTenantContext.Builder();
        MultiTenantContext.setInstance(builder.tenantReference(tenantReference).systemTenant(mockTenant)
                .factory(factory).manager(manager).construct());
    }
}
