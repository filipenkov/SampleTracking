package com.atlassian.seraph.filter;

import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;
import com.atlassian.security.auth.trustedapps.UserResolver;
import com.atlassian.security.auth.trustedapps.seraph.filter.SeraphTrustedApplicationsFilter;

/**
 * This is a legacy class kept for backward compatibility.
 *
 * @deprecated since 1.0
 */
@Deprecated
public class TrustedApplicationsFilter extends SeraphTrustedApplicationsFilter
{
    public TrustedApplicationsFilter(final TrustedApplicationsManager appManager, final UserResolver resolver)
    {
        super(appManager, resolver);
    }
}
