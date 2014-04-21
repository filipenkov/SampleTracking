package com.atlassian.applinks.core.auth.oauth.servlets;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.applinks.ui.auth.AdminUIAuthenticator;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang.StringUtils;

/**
 * @since 3.0
 */
public abstract class AbstractOAuthConfigServlet extends AbstractAuthServlet
{
    public static final String MESSAGE_PARAM = "message";

    protected AbstractOAuthConfigServlet(final I18nResolver i18nResolver, final MessageFactory messageFactory,
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
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
    }

    protected String getMessage(final HttpServletRequest request)
    {
        if (request.getParameterMap().containsKey(AbstractOAuthConfigServlet.MESSAGE_PARAM))
        {
            return request.getParameter(AbstractOAuthConfigServlet.MESSAGE_PARAM);
        }
        return null;
    }

    protected final String checkRequiredFormParameter(final HttpServletRequest request, final String parameterName, final Map<String, String> errorMessages, final String messageKey)
    {
        if (StringUtils.isBlank(request.getParameter(parameterName)))
        {
            errorMessages.put(parameterName, i18nResolver.getText(messageKey));
        }
        return request.getParameter(parameterName);
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "oauth-auth");
    }

}
