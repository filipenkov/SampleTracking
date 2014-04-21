package com.atlassian.applinks.core.auth.basic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.core.auth.AbstractAuthServlet;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
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
import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang.StringUtils;

/**
 * This servlet provides the interface for configuring Seraph authentication
 * for outgoing HTTP requests via AppLinks.
 *
 * @since 3.0
 */
public class BasicServlet extends AbstractAuthServlet
{
    private static final String TEMPLATE = "auth/basic/config.vm";
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final WebSudoManager webSudoManager;

    public BasicServlet(
            final AdminUIAuthenticator adminUIAuthenticator,
            final ApplicationLinkService applicationLinkService,
            final AuthenticationConfigurationManager authenticationConfigurationManager,
            final I18nResolver i18nResolver,
            final TemplateRenderer templateRenderer,
            final WebResourceManager webResourceManager,
            final MessageFactory messageFactory,
            final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
            final DocumentationLinker documentationLinker,
            final LoginUriProvider loginUriProvider,
            final InternalHostApplication internalHostApplication,
            final WebSudoManager webSudoManager,
            final XsrfTokenAccessor xsrfTokenAccessor,
            final XsrfTokenValidator xsrfTokenValidator)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, applicationLinkService,
                adminUIAuthenticator, batchedJSONi18NBuilderFactory, documentationLinker, loginUriProvider,
                internalHostApplication, xsrfTokenAccessor, xsrfTokenValidator);
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "basic-auth");
    }

    /**
     * Invoke with the name of the application ID as the last elements of the
     * URL path.
     * <p/>
     * /plugins/servlet/applinks/auth/conf/basic/{application_id}
     * /rest/
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);
            final String username = getConfiguredUsername(applicationLink);
            final RendererContextBuilder builder = createContextBuilder(applicationLink);
            builder.put("configured", authenticationConfigurationManager.isConfigured(applicationLink.getId(), BasicAuthenticationProvider.class));
            if (StringUtils.isEmpty(username))
            {
                builder.put("view", "disabled");
            }
            else
            {
                builder.put("username", username)
                    .put("view", "enabled");
            }
            render(TEMPLATE, builder.build(), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            if (Method.PUT == getRequiredMethod(request))
            {
                doPut(request, response);
            }
            else
            {
                doDelete(request, response);
            }
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doPut(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);
            final String usernameInput = request.getParameter("username");
            final String passwordInput1 = request.getParameter("password1");
            final String passwordInput2 = request.getParameter("password2");

            final RendererContextBuilder contextBuilder = createContextBuilder(applicationLink)
                .put("view", "edit")
                .put("usernameInput", usernameInput)
                .put("username", getConfiguredUsername(applicationLink))
                .put("configured", authenticationConfigurationManager.isConfigured(applicationLink.getId(), BasicAuthenticationProvider.class));

            if (StringUtils.isBlank(usernameInput))
            {
                contextBuilder.put("error", messageFactory.newI18nMessage("auth.basic.config.error.nousername"));
            }
            else if (StringUtils.isBlank(passwordInput1) && StringUtils.isBlank(passwordInput2))
            {
                contextBuilder.put("error", messageFactory.newI18nMessage("auth.basic.config.error.nopassword"));
            }
            else if (!StringUtils.equals(passwordInput1, passwordInput2))
            {
                contextBuilder.put("error", messageFactory.newI18nMessage("auth.basic.config.error.mismatch"));
            }
            else
            {
                final Map<String, String> config = ImmutableMap.of(
                    BasicAuthRequestFactoryImpl.USERNAME_KEY, usernameInput,
                    BasicAuthRequestFactoryImpl.PASSWORD_KEY, passwordInput1);
                authenticationConfigurationManager.registerProvider(applicationLink.getId(), BasicAuthenticationProvider.class, config);
                response.sendRedirect("./" + applicationLink.getId());
                return;
            }
            render(TEMPLATE, contextBuilder.build(), request, response);
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            // Enable web sudo protection if needed and if the app we are running in supports it
            webSudoManager.willExecuteWebSudoRequest(request);

            final ApplicationLink applicationLink = getRequiredApplicationLink(request);
            authenticationConfigurationManager.unregisterProvider(applicationLink.getId(), BasicAuthenticationProvider.class);
            response.sendRedirect("./" + applicationLink.getId());
        }
        catch (WebSudoSessionException wse)
        {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    private Method getRequiredMethod(final HttpServletRequest request)
    {
        final String value = getRequiredParameter(request, "method");
        try
        {
            return Method.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            throw new BadRequestException(messageFactory.newLocalizedMessage("Invalid method: " + value));
        }
    }

    private String getConfiguredUsername(final ApplicationLink applicationLink)
    {
        final Map<String, String> config =
                authenticationConfigurationManager.getConfiguration(applicationLink.getId(), BasicAuthenticationProvider.class);
        return config == null ?
                null :
                config.get(BasicAuthRequestFactoryImpl.USERNAME_KEY);
    }

    private static enum Method
    {
        PUT,
        DELETE
    }
}