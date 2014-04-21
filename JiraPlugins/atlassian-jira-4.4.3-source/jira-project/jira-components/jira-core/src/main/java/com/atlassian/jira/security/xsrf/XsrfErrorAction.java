package com.atlassian.jira.security.xsrf;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This action is synthetically created and pushed onto the webwork stack when an XSRF action is encountered.  It
 * contains code to help the JSP display stuff.
 *
 * @since v4.1
 */
public class XsrfErrorAction extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(XsrfErrorAction.class);

    final XsrfFailureException xsrfFailureException;

    public XsrfErrorAction(final XsrfFailureException xsrfFailureException)
    {
        this.xsrfFailureException = xsrfFailureException;

        final User user = getAuthContext().getLoggedInUser();
        if (user != null)
        {
            request.setAttribute("loggedin", Boolean.TRUE);
        }

        request.setAttribute("xsrfToken", getXsrfToken());
        request.setAttribute("maxInactiveIntervalMinutes", request.getSession(true).getMaxInactiveInterval() / 60);


        request.setAttribute("contextpath", request.getContextPath());
        request.setAttribute("helpUtil", new HelpUtil());
        int parameterCount = getRequestParameters().size();
        log.info("The security token is missing for '" + (user == null ? "anonymous" : user.getName()) + "'. " + (parameterCount == 0 ? "The browser has provided ZERO parameters.  Probably BUG! " : "") + "User-Agent : '" + getBrowserAgent(request) + "'");
    }

    private String getBrowserAgent(final HttpServletRequest request)
    {
        return StringUtils.defaultIfEmpty(request.getHeader("User-Agent"), "Not Provided");
    }

    private JiraAuthenticationContext getAuthContext()
    {
        return ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);
    }

    public XsrfFailureException getException()
    {
        return xsrfFailureException;
    }

    public boolean isSessionExpired()
    {
        return xsrfFailureException.isSessionExpired();
    }

    public Action getAction()
    {
        return xsrfFailureException.getAction();
    }

    public String getRequestURL()
    {
        return xsrfFailureException.getRequestURL();
    }

    public String getRequestMethod()
    {
        return xsrfFailureException.getRequestMethod();
    }

    public boolean getNoRequestParameters()
    {
        return getRequestParameters().size() == 0;
    }

    public Set<Map.Entry<String, List<String>>> getRequestParameters()
    {
        return xsrfFailureException.getRequestParameters();
    }
}