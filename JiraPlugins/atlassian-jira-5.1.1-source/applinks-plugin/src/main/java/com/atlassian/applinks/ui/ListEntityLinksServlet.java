package com.atlassian.applinks.ui;

import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.velocity.ListEntityLinksContext;
import com.atlassian.applinks.ui.velocity.VelocityContextFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * A servlet that renders a velocity template to display all configured entity links.
 *
 * @since 3.0
 */
public class ListEntityLinksServlet extends AbstractApplinksServlet
{
    private static final String TEMPLATE_PATH = "com/atlassian/applinks/ui/admin/list_entity_links.vm";
    private final VelocityContextFactory velocityContextFactory;
    private final WebSudoManager webSudoManager;

    public ListEntityLinksServlet(final I18nResolver i18nResolver,
                                  final MessageFactory messageFactory,
                                  final TemplateRenderer templateRenderer,
                                  final WebResourceManager webResourceManager,
                                  final InternalHostApplication internalHostApplication,
                                  final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                  final DocumentationLinker documentationLinker,
                                  final LoginUriProvider loginUriProvider,
                                  final VelocityContextFactory velocityContextFactory,
                                  final WebSudoManager webSudoManager)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, batchedJSONi18NBuilderFactory,
              documentationLinker, loginUriProvider, internalHostApplication);
        this.velocityContextFactory = velocityContextFactory;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "list-entity-links");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);
            final String[] pathParams = extractParams(request);
            final String typeId = pathParams[pathParams.length - 2];
            final String key = pathParams[pathParams.length - 1];
            final ListEntityLinksContext context = velocityContextFactory.buildListEntityLinksContext(request, typeId, key);
            final String decorator = internalHostApplication.getType() instanceof BambooApplicationType ? "atl.general" : "atl.admin";
            render(TEMPLATE_PATH, ImmutableMap.of("context", (Object)(context), "decorator", decorator), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private String[] extractParams(final HttpServletRequest request)
    {
        final String[] pathParams = StringUtils.split(request.getPathInfo(), '/');

        if (pathParams.length < 2)
        {
            throw new AbstractApplinksServlet.BadRequestException(messageFactory.newLocalizedMessage(
                    "Servlet URL should be of form /listEntityLinks/{entity-type}/{entity-key}"));
        }
        return pathParams;
    }
}
