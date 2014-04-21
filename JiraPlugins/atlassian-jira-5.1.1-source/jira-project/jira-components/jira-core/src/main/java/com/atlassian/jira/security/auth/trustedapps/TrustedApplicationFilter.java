package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.dbc.Null;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import com.atlassian.security.auth.trustedapps.ApplicationCertificate;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;
import com.atlassian.seraph.filter.TrustedApplicationsFilter;

import java.security.Principal;

/**
 * Used to ctor inject the {@link TrustedApplicationsManager} into the filter.
 *
 * @since v3.12
 */
// /CLOVER:OFF
public class TrustedApplicationFilter extends TrustedApplicationsFilter
{
    public TrustedApplicationFilter()
    {
        super(new DelegateManager(), new OSUserResolver());
    }

    /**
     * Get users from OSUser. Uses the UserNameTransformer policy that is configured in application properties.
     */
    private static class OSUserResolver implements UserResolver
    {
        private final MultiTenantComponentMap<UserNameTransformer> transformers;

        public OSUserResolver()
        {
            // During filter creation we don't have a tenant context for multitenancy so we need to wrap the usernametransformer
            // in this component-map which will give us the right thing during actual filter execution.
            transformers = MultiTenantContext.getFactory().createComponentMap(new UserNameTransformerMultiTenantCreator());
        }

        public Principal resolve(ApplicationCertificate certificate)
        {
            Null.not("ApplicationCertificate", certificate);
            final String name = transformers.get().transform(certificate.getUserName());
            return ComponentAccessor.getUserUtil().getUser(name);
        }
    }

    /**
     * resolve dependency on the actual manager at runtime
     */
    private static class DelegateManager implements TrustedApplicationsManager
    {
        public TrustedApplication getTrustedApplication(String id)
        {
            return getDelegate().getTrustedApplication(id);
        }

        public CurrentApplication getCurrentApplication()
        {
            return getDelegate().getCurrentApplication();
        }

        private TrustedApplicationsManager getDelegate()
        {
            return ComponentManager.getInstance().getTrustedApplicationsManager();
        }
    }

    // Wrap the UserNameTransformer to make it MultiTenant-aware.
    private static class UserNameTransformerMultiTenantCreator implements MultiTenantCreator<UserNameTransformer>
    {
        @Override
        public UserNameTransformer create(final Tenant tenant)
        {
            final UserNameTransformer.Factory factory = new UserNameTransformer.Factory(
                    new UserNameTransformer.ApplicationPropertiesClassNameRetriever(ComponentAccessor.getApplicationProperties())
            );
            final UserNameTransformer transformer = factory.get();
            Null.not("transformer", transformer);
            return transformer;
        }
    }

}
