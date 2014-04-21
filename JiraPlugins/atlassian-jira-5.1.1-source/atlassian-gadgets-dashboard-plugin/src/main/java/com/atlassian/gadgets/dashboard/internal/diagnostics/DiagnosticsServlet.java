package com.atlassian.gadgets.dashboard.internal.diagnostics;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;

public class DiagnosticsServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(DiagnosticsServlet.class);

    private final TemplateRenderer renderer;
    private final UserManager userManager;
    private final Diagnostics diagnostics;

    private AtomicBoolean displayResult;

    public DiagnosticsServlet(TemplateRenderer renderer, UserManager userManager, Diagnostics diagnostics)
    {
        this.renderer = renderer;
        this.userManager = userManager;
        this.diagnostics = diagnostics;
        displayResult = new AtomicBoolean(true);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        String method = req.getParameter("method");
        if (method != null && "delete".equalsIgnoreCase(method))
        {
            displayResult.set(false);
        }
        else
        {
            final String userName = userManager.getRemoteUsername(req);
            final boolean isAdmin = (userName != null && userManager.isSystemAdmin(userName));

            if (displayResult.get())
            {
                executeDiagnostics(req, resp, isAdmin);
            }
            else
            {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("text/html;charset=UTF-8");
            }
        }
    }

    private void executeDiagnostics(HttpServletRequest req, HttpServletResponse resp, boolean isAdmin)
        throws IOException
    {
        String clientDetectedUriParameter = req.getParameter("uri");
        if (clientDetectedUriParameter == null || clientDetectedUriParameter.length() == 0)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: 'uri'");
            return;
        }

        URI clientDetectedUri = null;
        try
        {
            clientDetectedUri = new URI(clientDetectedUriParameter);
        }
        catch (URISyntaxException e)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required 'uri' parameter must be a valid URI: " + e);
        }

        try
        {
            diagnostics.check(clientDetectedUri);
            Templates.OK.render(renderer, resp);
        }
        catch (UrlSchemeMismatchException e)
        {
            Templates.MISMATCHED_URL_SCHEME.renderError(renderer, e, resp, isAdmin);
        }
        catch (UrlHostnameMismatchException e)
        {
            Templates.MISMATCHED_URL_HOSTNAME.renderError(renderer, e, resp, isAdmin);
        }
        catch (UrlPortMismatchException e)
        {
            Templates.MISMATCHED_URL_PORT.renderError(renderer, e, resp, isAdmin);
        }
        catch (Exception e)
        {
            Templates.UNKNOWN_ERROR.renderError(renderer, new DiagnosticsException(e.getClass().getName() + ": " + e.getMessage(), e), resp, isAdmin);
        }
    }

    private enum Templates
    {
        MISMATCHED_URL_SCHEME("mismatched-scheme.vm"),
        MISMATCHED_URL_HOSTNAME("mismatched-hostname.vm"),
        MISMATCHED_URL_PORT("mismatched-port.vm"),
        UNKNOWN_ERROR("unknown-error.vm"),
        OK("ok.vm");

        private final String templateName;

        Templates(String templateName)
        {
            this.templateName = templateName;
        }

        void render(TemplateRenderer renderer, HttpServletResponse resp) throws IOException
        {
            resp.setContentType("text/html;charset=UTF-8");
            renderer.render("com/atlassian/gadgets/dashboard/internal/diagnostics/templates/" + templateName, resp.getWriter());
        }

        void renderError(TemplateRenderer renderer, DiagnosticsException error, HttpServletResponse resp, boolean isAdmin) throws IOException
        {
            log.error("DIAGNOSTICS: FAILED", error);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("text/html;charset=UTF-8");
            renderer.render("com/atlassian/gadgets/dashboard/internal/diagnostics/templates/" + templateName,
                            ImmutableMap.<String, Object>of("error", error, "isAdmin", isAdmin),
                            resp.getWriter());
        }
    }
}
