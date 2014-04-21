package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.CorsAuthenticationProvider;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Servlet for handling configuration for allowing CORS requests with credentials. The existence of an Application Link
 * adds the URL to the whitelist for non-credentialed requests; this configuration controls only whether credentialed
 * CORS requests are enabled.
 * <p/>
 * Because this configuration functions as a simple on/off (currently), it is implemented purely in terms of whether
 * configuration exists for the type or not. If it does, CORS requests with credentials are enabled. If not, not. If
 * more fine-grained CORS configuration is needed, this implementation will need to be revised.
 * 
 * @since 3.7
 */
public class CorsAuthServlet extends AbstractAuthServlet
{
    private static final String TEMPLATE = "auth/cors/config.vm";

    private final CorsService corsService;
    private final WebSudoManager webSudoManager;

    public CorsAuthServlet(I18nResolver i18nResolver,
                           MessageFactory messageFactory,
                           TemplateRenderer templateRenderer,
                           WebResourceManager webResourceManager,
                           ApplicationLinkService applicationLinkService,
                           AdminUIAuthenticator adminUIAuthenticator,
                           BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                           DocumentationLinker documentationLinker,
                           LoginUriProvider loginUriProvider,
                           InternalHostApplication internalHostApplication,
                           XsrfTokenAccessor xsrfTokenAccessor,
                           XsrfTokenValidator xsrfTokenValidator,
                           CorsService corsService,
                           WebSudoManager webSudoManager)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.corsService = corsService;
        this.webSudoManager = webSudoManager;
    }

    /**
     * Unregisters any existing {@link CorsAuthenticationProvider} configuration.
     * 
     * @param req the HTTP request
     * @param resp the HTTP response
     * @throws IOException Thrown if rendering fails.
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            webSudoManager.willExecuteWebSudoRequest(req);

            ApplicationLink link = getRequiredApplicationLink(req);
            corsService.disableCredentials(link);
            
            render(link, false, req, resp);
        }
        catch (WebSudoSessionException e)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    /**
     * Determines whether {@link CorsAuthenticationProvider} configuration exists, and renders accordingly.
     * <p/>
     * Note: The specifics of what is in the configuration block are inconsequential to this implementation.
     * 
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException Thrown if rendering fails.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            webSudoManager.willExecuteWebSudoRequest(req);

            ApplicationLink link = getRequiredApplicationLink(req);
            boolean configured = corsService.allowsCredentials(link);

            render(link, configured, req, resp);
        }
        catch (WebSudoSessionException e)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    /**
     * Examines the request for a "method" parameter and invokes either
     * {@link #doDelete(HttpServletRequest, HttpServletResponse)} or
     * {@link #doPut(HttpServletRequest, HttpServletResponse)} appropriately.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws BadRequestException Thrown if the "method" parameter is not provided.
     * @throws IllegalArgumentException Thrown if the method requested is not "DELETE or "PUT". 
     * @throws IOException Thrown if rendering fails for the invoked method.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        String method = getRequiredParameter(req, "method");
        if ("PUT".equals(method))
        {
            doPut(req, resp);
        }
        else if ("DELETE".equals(method))
        {
            doDelete(req, resp);
        }
        else
        {
            throw new BadRequestException(messageFactory.newLocalizedMessage("Invalid method: " + method));
        }
    }

    /**
     * Puts a simple {@link CorsAuthenticationProvider} configuration block for the target Application Link.
     * <p/>
     * Note: No parameters on the request are needed or parsed.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response
     * @throws IOException Thrown if rendering fails.
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            webSudoManager.willExecuteWebSudoRequest(req);

            ApplicationLink link = getRequiredApplicationLink(req);
            corsService.enableCredentials(link);

            render(link, true, req, resp);
        }
        catch (WebSudoSessionException e)
        {
            webSudoManager.enforceWebSudoProtection(req, resp);
        }
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "cors-auth");
    }

    /**
     * Renders the Velocity template for the provided {@link ApplicationLink}, where CORS is considered "configured" if
     * the {@code allowsCredentials} parameter is {@code true} and "not configured" if it is {@code false}.
     * <p/>
     * As part of this rendering process, the RPC URL for the provided link is tested against the entire corpus of
     * existing links to determine if it shares the same scheme, host and port as any other link. If so, the
     * {@link CorsService#allowsCredentials(ApplicationLink) allowsCredentials} state of all matching links is
     * compared to the current link. If <i>any</i> matching link has a different configuration than the current link,
     * a warning is rendered by the template.
     * <p/>
     * For more information about the conflict check, see {@link AppLinksCorsDefaults#allowsCredentials(String)}.
     *
     * @param link              the working AppLink being configured
     * @param allowsCredentials {@code true} if credentialed requests are allowed for the working AppLink; otherwise
     *                          {@code false}
     * @param request           the HTTP request, used for rendering
     * @param response          the HTTP response, used for rendering
     * @throws IOException Thrown if the Velocity template cannot be rendered successfully.
     */
    private void render(ApplicationLink link, boolean allowsCredentials,
                        HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        RendererContextBuilder builder = createContextBuilder(link);
        builder.put("configured", allowsCredentials);

        //When loading the configuration, we need to look for Application Links which share the same scheme, host and
        //port as the one we're configuring. If there are mismatches in their configuration settings, it may result in
        //behavior the user had not intended.
        Collection<ApplicationLink> matches = corsService.getApplicationLinksByUri(link.getRpcUrl());

        //Note: The "link" provided this method will always be included in the returned collection of matches, as is
        //      documented on CorsService.getApplicationLinksByUri(URI). As a result, the size must be greater than 1.
        boolean conflicted = false;
        if (matches.size() > 1)
        {
            List<ApplicationLink> conflicts = new ArrayList<ApplicationLink>(matches.size());
            for (ApplicationLink match : matches)
            {
                //If this is the working link, skip over it; we already know its configuration state.
                if (link.getId().equals(match.getId()))
                {
                    continue;
                }

                //Otherwise, if the credentials configuration for the working link is not the same as the configuration
                //for this link with a matching scheme, host and port, there is a conflict. Such a conflict means that
                //for the working link or for the matching link credentialed requests will not be handled as configured.
                //See AppLinksCorsDefaults.allowsCredentials(String) for more details.
                if (allowsCredentials != corsService.allowsCredentials(match))
                {
                    conflicts.add(match);
                }
            }

            if (!conflicts.isEmpty())
            {
                conflicted = true;

                builder.put("conflicts", conflicts);
            }
        }
        builder.put("conflicted", conflicted);

        render(TEMPLATE, builder.build(), request, response);
    }
}
