package com.atlassian.upm;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handle calls from servlets requesting different UPM pages with different templates.
 */
public final class PluginManagerHandler
{
    private final TemplateRenderer renderer;
    private final PermissionEnforcer permissionEnforcer;
    private final LoginUriProvider loginUriProvider;
    private final WebSudoManager webSudoManager;
    private final PluginAccessorAndController accessor;
    public static final String FRAGMENT_NAME = "fragment";
    static final String JIRA_SERAPH_SECURITY_ORIGINAL_URL = "os_security_originalurl";
    static final String CONF_SERAPH_SECURITY_ORIGINAL_URL = "seraph_originalurl";

    public PluginManagerHandler(final TemplateRenderer renderer, final PermissionEnforcer permissionEnforcer,
                                final LoginUriProvider loginUriProvider, final WebSudoManager webSudoManager,
                                final PluginAccessorAndController accessor)
    {
        this.webSudoManager = checkNotNull(webSudoManager, "webSudoManager");
        this.renderer = checkNotNull(renderer, "renderer");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.loginUriProvider = checkNotNull(loginUriProvider, "loginUriProvider");
        this.accessor =  checkNotNull(accessor, "accessor");
    }

    public void handle(HttpServletRequest request, HttpServletResponse response, String template) throws IOException, ServletException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            if (!permissionEnforcer.isAdmin())
            {
                redirectToLogin(request, response);
                return;
            }
            // UPM-1432: Tunnel the fragment identifier through a query parameter
            if (request.getParameter(FRAGMENT_NAME) != null)
            {
                redirectToFragment(request, response);
                return;
            }

            final Map<String, Object> context = new HashMap<String, Object>();

            // Lets not leave any session attributes laying around if we don't need to
            removeSessionAttributes(request.getSession());
            response.setContentType("text/html;charset=utf-8");
            context.put("pacWebsiteUrl", System.getProperty("pac.website", "https://plugins.atlassian.com"));
            context.put("upmVersion", accessor.getUpmVersion());
            context.put("macBaseurl", Sys.getMacBaseUrl());
            context.put("isOnDemand", Sys.isOnDemand());

            renderer.render(template, context, response.getWriter());
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }

    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        final URI uri = getUri(request);
        addSessionAttributes(request, uri.toASCIIString());
        response.sendRedirect(loginUriProvider.getLoginUri(uri).toASCIIString());
    }

    private void redirectToFragment(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        StringBuffer builder = request.getRequestURL();
        builder.append("#");
        builder.append(request.getParameter(FRAGMENT_NAME));
        String uri = URI.create(builder.toString()).toASCIIString();
        response.sendRedirect(uri);
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }

    private void addSessionAttributes(final HttpServletRequest request, final String uriString)
    {
        // UPM-637 - Seraph tries to be clever and if you're currently logged in user is trying to access a
        // URL that does not have a Seraph role restriction then it will redirect to os_destination. In the case
        // of the UPM we do not want this behavior since we do have an elevated Seraph role but have not way to
        // programatically tell Seraph about it.
        // UPM-637 - this is the JIRA specific string to let Seraph know that it should re-show the login page
        request.getSession().setAttribute(JIRA_SERAPH_SECURITY_ORIGINAL_URL, uriString);
        // UPM-637 - this is the Confluence specific string to let Seraph know that it should re-show the login page
        request.getSession().setAttribute(CONF_SERAPH_SECURITY_ORIGINAL_URL, uriString);
    }

    private void removeSessionAttributes(final HttpSession session)
    {
        session.removeAttribute(JIRA_SERAPH_SECURITY_ORIGINAL_URL);
        session.removeAttribute(CONF_SERAPH_SECURITY_ORIGINAL_URL);
    }
}
