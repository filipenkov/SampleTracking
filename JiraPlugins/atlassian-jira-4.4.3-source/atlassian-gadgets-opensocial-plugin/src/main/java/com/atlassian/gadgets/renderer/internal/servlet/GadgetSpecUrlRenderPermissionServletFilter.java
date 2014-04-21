package com.atlassian.gadgets.renderer.internal.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetSpecUrlChecker;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.annotations.HtmlSafe;
import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRenderer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Filter that returns appropriate HTTP errors if the requested gadget is not
 * allowed to be rendered.
 */
public class GadgetSpecUrlRenderPermissionServletFilter implements Filter
{
    private static final String URL_PARAM = "url";

    private final GadgetSpecUrlChecker gadgetChecker;
    private final I18nResolver i18n;
    private final UserManager userManager;
    private final Log log = LogFactory.getLog(GadgetSpecUrlRenderPermissionServletFilter.class);
    private final VelocityTemplateRenderer renderer;
    
    /**
     * Constructor.
     * @param gadgetChecker used to determine if a gadget is renderable (i.e., the
     * gadget has not been removed)
     */ 
    public GadgetSpecUrlRenderPermissionServletFilter(
            GadgetSpecUrlChecker gadgetChecker,
            I18nResolver i18nResolver,
            UserManager userManager,
            VelocityTemplateRenderer renderer)
    {
        checkNotNull(gadgetChecker, "gadgetChecker");
        checkNotNull(i18nResolver, "i18nResolver");
        checkNotNull(userManager, "userManager");
        this.gadgetChecker = gadgetChecker;
        this.i18n = i18nResolver;
        this.userManager = userManager;
        this.renderer = renderer;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getParameter(URL_PARAM);
        if (StringUtils.isBlank(uri))
        {
            // forward without comment to the rendering servlet; we don't want
            // to block its error handling
            chain.doFilter(request, response);
            return;
        }

        URI gadgetSpecUri;
        try
        {
            gadgetSpecUri = new URI(uri);
        }
        catch (URISyntaxException urise)
        {
            // shouldn't happen, theoretically
            log.warn("GadgetSpecUrlRenderPermissionServletFilter: couldn't parse URI from request", urise);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "couldn't parse '" + uri + "' as a URI");
            return;
        }

        try
        {
            gadgetChecker.assertRenderable(gadgetSpecUri.toString());
        }
        catch (GadgetSpecUriNotAllowedException igsue)
        {
            resp.setStatus(HttpServletResponse.SC_GONE);
            resp.setContentType("text/html");
            if(resp.getWriter() != null) {
                final String userName = userManager.getRemoteUsername(req);
                final boolean isAdmin = (userName != null && userManager.isSystemAdmin(userName));
                final Map<String, Object> context = new HashMap<String, Object>();
                context.put("reason", igsue.getMessage());
                context.put("message", i18n.getText("error.gadget.gone"));
                context.put("gadgetUrl", isAdmin ? gadgetSpecUri : "");
                context.put("unescaper", Unescaper.getSingleton());
                renderer.render("/gadgetRemovedError.vm", context, resp.getWriter());
            }
            return;
        }
        chain.doFilter(request, response);
    }    

    public void init(FilterConfig filterConfig) throws ServletException{}
    public void destroy() {}

    public static class Unescaper {

        private static Unescaper singleton = new Unescaper();
        
        /**
         * Just returns the {@code text} parameter.  Because this method is annotated with {@code @HtmlSafe} the returned
         * value will not be escaped when it is inserted into the rendered template.
         *
         * @param text Text that we don't want escaped
         * @return {@code text} unmodified
         */
        @HtmlSafe
        public String html(String text)
        {
            return text;
        }

        public static Unescaper getSingleton()
        {
            return singleton;
        }

    }
}
