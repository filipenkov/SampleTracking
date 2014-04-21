package com.atlassian.applinks.core.auth;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.AbstractAppLinksAdminOnlyServlet;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * Base class used by several authentication provider servlets.
 * </p>
 * <p>
 * Extracts the application link ID from the servlet path, retrieves the
 * {@link com.atlassian.applinks.api.ApplicationLink} instance and makes it
 * available to the callback methods
 * ({@link #doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)},
 * {@link #doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)},
 * etc).
 * </p>
 * <p>
 * This class requires the Application Link ID to be the first node in
 * {@link javax.servlet.http.HttpServletRequest#getPathInfo()}.
 * </p>
 * <p>
 * If the application link ID that is on the servlet path does not correspond
 * to any of our locally configured peers,
 * {@link #getRequiredApplicationLink(javax.servlet.http.HttpServletRequest)}
 * throws a {@link com.atlassian.applinks.ui.AbstractApplinksServlet.NotFoundException}.
 * </p>
 *
 * @since v3.0
 */
public abstract class AbstractAuthServlet extends AbstractAppLinksAdminOnlyServlet
{
    public static String HOST_URL_PARAM = "hostUrl";
    private final ApplicationLinkService applicationLinkService;

    protected AbstractAuthServlet(final I18nResolver i18nResolver,
                                  final MessageFactory messageFactory,
                                  final TemplateRenderer templateRenderer,
                                  final WebResourceManager webResourceManager,
                                  final ApplicationLinkService applicationLinkService,
                                  final AdminUIAuthenticator adminUIAuthenticator,
                                  final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                  final DocumentationLinker documentationLinker,
                                  final LoginUriProvider loginUriProvider,
                                  final InternalHostApplication internalHostApplication,
                                  final XsrfTokenAccessor xsrfTokenAccessor,
                                  final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, adminUIAuthenticator,
              batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider, internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.applicationLinkService = applicationLinkService;
    }

    protected ApplicationLink getRequiredApplicationLink(final HttpServletRequest request) throws
            NotFoundException, BadRequestException
    {
        // remove any excess slashes
        final String pathInfo = URI.create(request.getPathInfo()).normalize().toString();
        final String[] elements = StringUtils.split(pathInfo, '/');
        if (elements.length > 0)
        {
            final ApplicationId id = new ApplicationId(elements[0]);
            try
            {
                final ApplicationLink link = applicationLinkService.getApplicationLink(id);
                if (link != null)
                {
                    return link;
                }
                else
                {
                    final NotFoundException exception = new NotFoundException();
                    exception.setTemplate("auth/applink-missing.vm");
                    throw exception;
                }
            }
            catch (TypeNotInstalledException e)
            {
                logger.warn(String.format("Unable to load ApplicationLink %s due to uninstalled type definition (%s).", id.toString(), e.getType()), e);
            }
            throw new NotFoundException(messageFactory.newI18nMessage("auth.config.applink.notfound", id.toString()));
        }
        throw new BadRequestException(messageFactory.newI18nMessage("auth.config.applinkpath.missing"));
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        final List<String> list = new ArrayList<String>();
        list.add("com.atlassian.applinks.applinks-plugin:auth-config-css");
        return list;
    }
}
