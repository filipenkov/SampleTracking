package com.atlassian.applinks.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.applinks.ui.velocity.ListApplicationLinksContext;
import com.atlassian.applinks.ui.velocity.VelocityContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableList;

/**
 * A servlet that renders a velocity template to display all configured linked applications.
 *
 * @since 3.0
 */
public class ListApplicationLinksServlet extends AbstractAppLinksAdminOnlyServlet
{
    private static final String TEMPLATE_PATH = "com/atlassian/applinks/ui/admin/list_application_links.vm";

    private final VelocityContextFactory velocityContextFactory;
    private final WebSudoManager webSudoManager;


    public ListApplicationLinksServlet(final I18nResolver i18nResolver,
                                       final MessageFactory messageFactory,
                                       final TemplateRenderer templateRenderer,
                                       final WebResourceManager webResourceManager,
                                       final AdminUIAuthenticator adminUIAuthenticator,
                                       final InternalHostApplication internalHostApplication,
                                       final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                       final DocumentationLinker documentationLinker,
                                       final LoginUriProvider loginUriProvider,
                                       final VelocityContextFactory velocityContextFactory,
                                       final WebSudoManager webSudoManager,
                                       final XsrfTokenAccessor xsrfTokenAccessor,
                                       final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, adminUIAuthenticator,
                batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication,
                xsrfTokenAccessor, xsrfTokenValidator);
        this.velocityContextFactory = velocityContextFactory;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "list-application-links");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ListApplicationLinksContext context = velocityContextFactory.buildListApplicationLinksContext(request);
            render(TEMPLATE_PATH, Collections.<String, Object>singletonMap("context", context), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

}
