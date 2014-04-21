package com.atlassian.security.auth.trustedapps.seraph.filter;

import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;
import com.atlassian.security.auth.trustedapps.filter.TrustedApplicationsFilter;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.SecurityConfigFactory;

/**
 * Default Seraph implementation of the {@link TrustedApplicationsFilter}
 */
public class SeraphTrustedApplicationsFilter extends TrustedApplicationsFilter
{
    public SeraphTrustedApplicationsFilter(TrustedApplicationsManager appManager, UserResolver resolver)
    {
        this(appManager, resolver, SecurityConfigFactory.getInstance().getRoleMapper());
    }

    protected SeraphTrustedApplicationsFilter(TrustedApplicationsManager appManager, UserResolver resolver, RoleMapper roleMapper)
    {
        super(appManager, resolver, new SeraphAuthenticationController(roleMapper), new SeraphAuthenticationListener());
    }
}
