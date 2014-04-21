package com.atlassian.plugins.rest.module.sal.websudo;

import com.atlassian.plugins.rest.common.sal.websudo.WebSudoResourceContext;
import com.atlassian.plugins.rest.module.servlet.ServletUtils;
import com.atlassian.sal.api.websudo.WebSudoManager;

import javax.servlet.http.HttpServletRequest;

public class SalWebSudoResourceContext implements WebSudoResourceContext
{
    private static final String BASIC_AUTHZ_TYPE_PREFIX = "Basic ";

    private final WebSudoManager webSudoManager;

    public SalWebSudoResourceContext(final WebSudoManager webSudoManager)
    {
        this.webSudoManager = webSudoManager;
    }

    /**
     * Checks if WebSudo protection is required.
     * <p/>
     * <ul>
     * <li>If clients authenticate using Basic-Auth WebSudo is not required.</li>
     * <li>If the current request is already protected (or if WebSudo is disabled in the host application) WebSudo is not required.</li>
     * </ul>
     *
     * @return true if resource need to be protected by WebSudo
     */
    public boolean shouldEnforceWebSudoProtection()
    {
        final HttpServletRequest r = ServletUtils.getHttpServletRequest();
        // If the servlet request is null (presumably because we are not running in a servlet container) there is no point in making use of WebSudo.
        if (null == r)
        {
            return false;
        }

        // We can skip web sudo if this is a request authenticated by BASIC-AUTH
        final String authHeader = r.getHeader("Authorization"); // as per RFC2616
        if (null != authHeader && authHeader.startsWith(BASIC_AUTHZ_TYPE_PREFIX))
        {
            return false;
        }

        return !webSudoManager.canExecuteRequest(r);
    }
}
