package com.atlassian.applinks.ui;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.HtmlSafeContent;
import com.atlassian.applinks.core.util.Message;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.core.util.RendererContextBuilder;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.xsrf.XsrfTokenAccessor;
import com.atlassian.sal.api.xsrf.XsrfTokenValidator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_NAME;
import static com.atlassian.applinks.ui.XsrfProtectedServlet.OVERRIDE_HEADER_VALUE;


/**
 * Abstract base class for servlets, offering exception handling,
 * template rendering (with automatic web resource inclusion), logging
 * and some convenience methods.
 *
 * @since v3.0
 */
public abstract class AbstractApplinksServlet extends HttpServlet
{
    private static final String ERROR_TEMPLATE = "com/atlassian/applinks/ui/auth_container_error.vm";
    public static final String WEB_RESOURCE_KEY = "com.atlassian.applinks.applinks-plugin:";
    public static final String XSRF_AUTH_TEMPLATE = "com/atlassian/applinks/ui/xsrf.vm";

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebResourceManager webResourceManager;
    protected final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory;
    protected final DocumentationLinker documentationLinker;
    protected final InternalHostApplication internalHostApplication;
    protected final TemplateRenderer templateRenderer;
    protected final I18nResolver i18nResolver;
    protected final MessageFactory messageFactory;
    private final LoginUriProvider loginUriProvider;
    private final XsrfTokenAccessor xsrfTokenAccessor;
    private final XsrfTokenValidator xsrfTokenValidator;

    /**
     * This constructor can be used if the servlet is NOT an XsrfProtectedServlet
     */
    public AbstractApplinksServlet(final I18nResolver i18nResolver,
                                   final MessageFactory messageFactory,
                                   final TemplateRenderer templateRenderer,
                                   final WebResourceManager webResourceManager,
                                   final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                   final DocumentationLinker documentationLinker,
                                   final LoginUriProvider loginUriProvider,
                                   final InternalHostApplication internalHostApplication)
    {
        this.i18nResolver = i18nResolver;
        this.messageFactory = messageFactory;
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.batchedJSONi18NBuilderFactory = batchedJSONi18NBuilderFactory;
        this.documentationLinker = documentationLinker;
        this.internalHostApplication = internalHostApplication;
        this.loginUriProvider = loginUriProvider;
        this.xsrfTokenAccessor = null;
        this.xsrfTokenValidator = null;
    }

    public AbstractApplinksServlet(final I18nResolver i18nResolver,
                                   final MessageFactory messageFactory,
                                   final TemplateRenderer templateRenderer,
                                   final WebResourceManager webResourceManager,
                                   final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                   final DocumentationLinker documentationLinker,
                                   final LoginUriProvider loginUriProvider,
                                   final InternalHostApplication internalHostApplication,
                                   final XsrfTokenAccessor xsrfTokenAccessor,
                                   final XsrfTokenValidator xsrfTokenValidator)
    {
        this.i18nResolver = i18nResolver;
        this.messageFactory = messageFactory;
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.batchedJSONi18NBuilderFactory = batchedJSONi18NBuilderFactory;
        this.documentationLinker = documentationLinker;
        this.internalHostApplication = internalHostApplication;
        this.loginUriProvider = loginUriProvider;
        this.xsrfTokenAccessor = xsrfTokenAccessor;
        this.xsrfTokenValidator = xsrfTokenValidator;
    }

    /**
     * Implement this method to get one of more Plugin Web Resources included
     * in the rendered page. Must not return {@code null}.
     *
     * @return a list of web resource keys
     * (e.g. {@code "com.atlassian.applinks.applinks-plugin:basic-js"})
     */
    protected List<String> getRequiredWebResources()
    {
        return Collections.emptyList();
    }

    @Override
    protected final void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setAttribute("ual.view", Boolean.TRUE);
        try
        {
            if (requestRequiresProtection(request))
            {
                if (!xsrfTokenValidator.validateFormEncodedToken(request))
                {
                    Map<String, Object> renderContext = new HashMap<String, Object>();
                    renderContext.put("parameters", request.getParameterNames());
                    renderContext.put("parameterMap", request.getParameterMap());
                    renderContext.put("requestMethod", request.getMethod().toLowerCase());
                    renderContext.put("requestUrl", request.getRequestURL());

                    render(XSRF_AUTH_TEMPLATE, renderContext, request, response);
                    return;
                }
            }
            doService(request, response);
            super.service(request, response);   // delegates to doGet(), doPost(), etc
        }
        catch (UnauthorizedBecauseUnauthenticatedException e)
        {
            final StringBuffer callback = request.getRequestURL();
            if (!StringUtils.isBlank(request.getQueryString()))
            {
                callback.append("?").append(request.getQueryString());
            }
            response.sendRedirect(loginUriProvider.getLoginUri(URIUtil.uncheckedToUri(callback.toString())).toASCIIString());
        }
        catch (UnauthorizedException e)
        {
            final StringBuffer callback = request.getRequestURL();
            if (!StringUtils.isBlank(request.getQueryString()))
            {
                callback.append("?")
                        .append(request.getQueryString());
            }
            render( e.getTemplate(),
                    ImmutableMap.<String, Object>of(
                            "message", ObjectUtils.defaultIfNull(e.getMessage(), ""),
                            "url", loginUriProvider.getLoginUri(URIUtil.uncheckedToUri(callback.toString()))),
                    request, response);
        }
        catch (RequestException re)
        {
            logger.warn(String.format("Unable to serve page: \"%s\": %s: %s",
                    request.getRequestURI(), re.getClass().getName(), re.getMessage()));
            response.setStatus(re.getStatus());
            render( StringUtils.defaultIfEmpty(re.getTemplate(), ERROR_TEMPLATE),
                    ImmutableMap.<String, Object>of(
                            "message", ObjectUtils.defaultIfNull(re.getMessage(), ""),
                            "status", re.getStatus()),
                    request, response);
        }
    }

    private boolean requestRequiresProtection(HttpServletRequest request)
    {
        if (!(this instanceof XsrfProtectedServlet))
        {
            return false;
        }

        String method = request.getMethod();
        if (method.equals(HttpMethod.POST))
        {
            return !OVERRIDE_HEADER_VALUE.equals(request.getHeader(OVERRIDE_HEADER_NAME));
        }

        return false;
    }

    /**
     * <p>
     * Override this method for operations that need to occur before control is
     * delegated to {{doGet()}}, {{doPost()}}, etc.
     * </p>
     * <p>
     * This method may throw
     * {@link AbstractApplinksServlet.RequestException}s.
     * </p>
     */
    protected void doService(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
    }

    /**
     * @return  an empty and immutable velocity render context.
     */
    protected final Map<String, Object> emptyContext()
    {
        return Collections.emptyMap();
    }

    protected void render(final String template, final Map<String, Object> renderContext,
                          final HttpServletRequest request, final HttpServletResponse response)
            throws IOException
    {
        for (final String resource : getRequiredWebResources())
        {
            webResourceManager.requireResource(resource);
        }
        final RendererContextBuilder builder = new RendererContextBuilder(renderContext)
            .put("i18n", i18nResolver)
            .put("webResources", new HtmlSafeContent()
            {
                public CharSequence get()
                {
                    StringWriter writer = new StringWriter();
                    webResourceManager.includeResources(writer);
                    return writer.toString();
                }
            })
            .put("applinksDocs", new HtmlSafeContent()
            {
                public CharSequence get()
                {
                    return batchedJSONi18NBuilderFactory.builder()
                            .withProperties("applinks.docs")
                            .with("applinks.docs.root", internalHostApplication.getDocumentationBaseUrl().toASCIIString())
                            .build();
                }
            })
            .put("applinksI18n" , new HtmlSafeContent()
            {
                public CharSequence get()
                {
                    return batchedJSONi18NBuilderFactory.builder()
                            .withProperties("applinks")
                            .withPluggableApplinksModules().build();
                }
            })
            .put("docLinker", documentationLinker);

        if (xsrfTokenAccessor != null)
        {
            builder.put("xsrftokenParamValue", xsrfTokenAccessor.getXsrfToken(request, response, true))
            .put("xsrftokenParamName", xsrfTokenValidator.getXsrfParameterName());
        }
        response.setContentType("text/html; charset=utf-8");
        templateRenderer.render(template, builder.build(), response.getWriter());
    }

    protected String getRequiredParameter(final HttpServletRequest request, final String name)
            throws BadRequestException
    {
        final String value = request.getParameter(name);
        if (StringUtils.isBlank(value))
        {
            throw new BadRequestException(messageFactory.newI18nMessage("auth.config.parameter.missing", name));
        }
        else
        {
            return value;
        }
    }

    protected RendererContextBuilder createContextBuilder(final ApplicationLink applicationLink)
    {
        final RendererContextBuilder builder = new RendererContextBuilder()
        .put("localApplicationName", internalHostApplication.getName())
        .put("localApplicationType", i18nResolver.getText(internalHostApplication.getType().getI18nKey()))
        .put("remoteApplicationName", applicationLink.getName())
        .put("remoteApplicationType", i18nResolver.getText(applicationLink.getType().getI18nKey()));
        return builder;
    }

    protected static class RequestException extends RuntimeException
    {
        private final int status;
        private final Message message;
        protected String template;

        public RequestException(final int status, final Message message, final Throwable cause)
        {
            super(cause);
            this.message = message;
            this.status = status;
        }

        public RequestException(final int status, final Message message)
        {
            this.message = message;
            this.status = status;
        }

        public RequestException(final int status)
        {
            this(status, null);
        }

        public int getStatus()
        {
            return status;
        }

        /**
         * @return  the name of the velocity template that should be rendered
         * when this exception is thrown. When {@code null} is returned, the
         * default template is used.
         */
        public String getTemplate()
        {
            return template;
        }

        public void setTemplate(final String template)
        {
            this.template = template;
        }

        public String getMessage()
        {
            return message == null ? null : message.toString();
        }
    }

    /**
     * Used by subclasses to trigger a 400.
     */
    public static class BadRequestException extends RequestException
    {
        public BadRequestException()
        {
            this(null);
        }

        public BadRequestException(final Message message)
        {
            super(HttpServletResponse.SC_BAD_REQUEST, message);
        }

        public BadRequestException(final Message message, final Throwable cause)
        {
            super(HttpServletResponse.SC_BAD_REQUEST, message, cause);
        }
    }

    /**
     * Used by subclasses to trigger a 404.
     */
    protected static class NotFoundException extends RequestException
    {
        public NotFoundException()
        {
            this(null);
        }

        public NotFoundException(final Message message)
        {
            super(HttpServletResponse.SC_BAD_REQUEST, message);
        }

        public NotFoundException(final Message message, final Throwable cause)
        {
            super(HttpServletResponse.SC_NOT_FOUND, message, cause);
        }
    }

    /**
     * Used by subclasses to trigger a 401.
     */
    public static class UnauthorizedException extends RequestException
    {
        public UnauthorizedException()
        {
            this(null);
        }

        public UnauthorizedException(final Message message)
        {
            super(HttpServletResponse.SC_UNAUTHORIZED, message);
        }

        public UnauthorizedException(final Message message, final Throwable cause)
        {
            super(HttpServletResponse.SC_UNAUTHORIZED, message, cause);
        }

        @Override
        public final String getTemplate()
        {
            return "com/atlassian/applinks/ui/no_admin_privileges.vm";
        }
    }
    
    public static class UnauthorizedBecauseUnauthenticatedException extends RequestException
    {
        public UnauthorizedBecauseUnauthenticatedException()
        {
            super(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
