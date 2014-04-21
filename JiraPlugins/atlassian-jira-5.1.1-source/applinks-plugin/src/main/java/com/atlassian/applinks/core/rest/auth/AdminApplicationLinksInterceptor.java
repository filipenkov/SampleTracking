package com.atlassian.applinks.core.rest.auth;

import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.atlassian.sal.api.message.I18nResolver;

import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.InvocationTargetException;

import static com.atlassian.applinks.core.rest.util.RestUtil.unauthorized;
import static com.atlassian.applinks.ui.auth.AdminUIAuthenticator.ADMIN_PASSWORD;
import static com.atlassian.applinks.ui.auth.AdminUIAuthenticator.ADMIN_USERNAME;

/**
 * This interceptor protects rest end point to administrate application links against unauthorized access.
 *
 * @since 3.0
 */
public class AdminApplicationLinksInterceptor implements ResourceInterceptor
{
    private final AdminUIAuthenticator authenticator;
    private final I18nResolver i18nResolver;

    public AdminApplicationLinksInterceptor(final AdminUIAuthenticator authenticator,
                                            final I18nResolver i18nResolver)
    {
        this.authenticator = authenticator;
        this.i18nResolver = i18nResolver;
    }

    public void intercept(final MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        final MultivaluedMap<String, String> params = invocation.getHttpContext().getRequest().getQueryParameters();

        if (authenticator.canAccessAdminUI(params.getFirst(ADMIN_USERNAME), params.getFirst(ADMIN_PASSWORD)))
        {
            invocation.invoke();
        }
        else
        {
            invocation.getHttpContext().getResponse()
                    .setResponse(unauthorized(i18nResolver.getText("applinks.error.admin.only")));
        }
    }

}