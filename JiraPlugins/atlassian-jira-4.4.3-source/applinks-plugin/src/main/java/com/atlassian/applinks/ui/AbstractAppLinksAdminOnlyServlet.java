package com.atlassian.applinks.ui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;

/**
 * Extend this class for servlets that are accessible to users that are able to administrate application links.
 *
 * @since v3.0
 */
public abstract class AbstractAppLinksAdminOnlyServlet extends AbstractApplinksServlet implements XsrfProtectedServlet
{
    protected final AdminUIAuthenticator adminUIAuthenticator;

    public AbstractAppLinksAdminOnlyServlet(final I18nResolver i18nResolver,
                                    final MessageFactory messageFactory,
                                    final TemplateRenderer templateRenderer,
                                    final WebResourceManager webResourceManager,
                                    final AdminUIAuthenticator adminUIAuthenticator,
                                    final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                    final DocumentationLinker documentationLinker,
                                    final LoginUriProvider loginUriProvider,
                                    final InternalHostApplication internalHostApplication,
                                    final XsrfTokenAccessor xsrfTokenAccessor,
                                    final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, batchedJSONi18NBuilderFactory,
                documentationLinker, loginUriProvider, internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.adminUIAuthenticator = adminUIAuthenticator;
    }

    @Override
    protected final void doService(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        if (adminUIAuthenticator.canAccessAdminUI(request))
        {
            doProtectedService(request, response);
        }
        else
        {
            if (request.getUserPrincipal() != null)
            {
                throw new UnauthorizedException(messageFactory.newI18nMessage("applinks.error.admin.only"));
            }
            else
            {
                throw new UnauthorizedBecauseUnauthenticatedException();
            }
        }
    }

    /**
     * <p>
     * Override this method for operations that need to occur before control is
     * delegated to {{doGet()}}, {{doPost()}}, etc. This method is invoked
     * after the admin permission-check is done.
     * </p>
     * <p>
     * This method may throw
     * {@link AbstractApplinksServlet.RequestException}s.
     * </p>
     */
    protected void doProtectedService(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
    }
}
