package com.atlassian.applinks.ui.auth;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.applinks.ui.AbstractApplinksServlet;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminLoginServlet extends AbstractApplinksServlet
{
    private static final String TEMPLATE_PATH = "com/atlassian/applinks/ui/admin_login.vm";
    public static final String ORIGINAL_URL = "originalUrl";

    private final AdminUIAuthenticator uiAuthenticator;

    public AdminLoginServlet(final MessageFactory messageFactory,
                             final TemplateRenderer templateRenderer,
                             final WebResourceManager webResourceManager,
                             final I18nResolver i18nResolver,
                             final AdminUIAuthenticator uiAuthenticator,
                             final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                             final DocumentationLinker documentationLinker,
                             final LoginUriProvider loginUriProvider,
                             final InternalHostApplication internalHostApplication)
    {
        super(i18nResolver, messageFactory, templateRenderer, webResourceManager, batchedJSONi18NBuilderFactory, 
                documentationLinker, loginUriProvider, internalHostApplication);
        this.uiAuthenticator = uiAuthenticator;
    }

    @Override
    protected List<String> getRequiredWebResources()
    {
        return ImmutableList.of(WEB_RESOURCE_KEY + "admin-login");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        doView(request, response, new HashMap<String, Object>());
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        final String username = request.getParameter(AdminUIAuthenticator.ADMIN_USERNAME);
        final String password = request.getParameter(AdminUIAuthenticator.ADMIN_PASSWORD);

        AdminUIAuthenticator.Result result = uiAuthenticator.logInAsAdmin(username, password, request, response);
        if (result.success())
        {
            response.sendRedirect(StringUtils.defaultIfEmpty(
                    request.getParameter(ORIGINAL_URL),
                    request.getContextPath()));
        }
        else
        {
            doView(request, response, new RendererContextBuilder()
                    .put("error", result.getMessage())
                    .put("username", username)
                    .build());
        }
    }

    private void doView(final HttpServletRequest request, final HttpServletResponse response, final Map<String, Object> context)
            throws ServletException, IOException
    {
        render( TEMPLATE_PATH,
                new RendererContextBuilder(context)
                        .put("req", request)
                        .put("applicationName", internalHostApplication.getName())
                        .put(ORIGINAL_URL, request.getParameter(ORIGINAL_URL))
                        .put("applicationtype", getApplicationTypeId())
                        .build(),
                request, response);
    }

    private String getApplicationTypeId()
    {
        ApplicationType type = internalHostApplication.getType();
        if (type instanceof IdentifiableType)
        {
            return ((IdentifiableType) type).getId().get();
        }
        else
        {
            return "unknown";
        }
    }
}
