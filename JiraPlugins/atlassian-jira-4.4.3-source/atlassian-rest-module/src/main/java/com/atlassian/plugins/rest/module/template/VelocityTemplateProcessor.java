package com.atlassian.plugins.rest.module.template;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sun.jersey.spi.template.TemplateProcessor;
import com.sun.jersey.api.core.HttpContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.osgi.util.tracker.ServiceTracker;
import org.apache.log4j.Logger;

import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Template processor that uses velocity and locates template within the bundle that uses it.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class VelocityTemplateProcessor implements TemplateProcessor
{
    private static final Logger log = Logger.getLogger(VelocityTemplateProcessor.class);
    private static final String VM = ".vm";

    private final ServiceTracker templateRendererServiceTracker;
    private @Context HttpContext httpContext;
    private @Context HttpServletRequest httpServletRequest;
    private @Context HttpServletResponse httpServletResponse;

    VelocityTemplateProcessor(ServiceTracker templateRendererServiceTracker)
    {
        this.templateRendererServiceTracker = Preconditions.checkNotNull(templateRendererServiceTracker);
    }

    public String resolve(final String path)
    {
        return executeOnTemplateRenderer(new TemplateRendererCommand<String>()
        {
            public String execute(TemplateRenderer renderer)
            {
                String resolvedPath = path + VM;
                if (renderer.resolve(resolvedPath))
                {
                    return resolvedPath;
                }
                else
                {
                    return null;
                }
            }
        });
    }

    public void writeTo(final String resolvedPath, final Object model, final OutputStream out) throws IOException
    {
        try
        {
            executeOnTemplateRenderer(new TemplateRendererCommand<Object>()
            {
                public Object execute(TemplateRenderer renderer)
                {
                    final OutputStreamWriter writer = new OutputStreamWriter(out);
                    final Map<String, Object> context = Maps.newHashMap();
                    context.put("renderer", new RendererImpl(renderer, writer, httpContext,
                        httpServletRequest, httpServletResponse));
                    context.put("it", model);
                    context.put("httpContext", httpContext);
                    context.put("request", httpServletRequest);
                    context.put("response", httpServletResponse);
                    try
                    {
                        renderer.render(resolvedPath, context, writer);
                    }
                    catch (IOException ioe)
                    {
                        throw new RuntimeException(ioe);
                    }
                    return null;
                }
            });
        }
        catch (RuntimeException re)
        {
            if (re.getCause() instanceof IOException)
            {
                throw (IOException) re.getCause();
            }
            else
            {
                throw re;
            }
        }
    }

    /**
     * Close the template renderer service tracker.  This should be called when the service is unimported.
     */
    public void closeTemplateRendererServiceTracker()
    {
        templateRendererServiceTracker.close();
    }

    /**
     * Command so calls to the template renderer don't need to check if the template renderer service is available
     * first
     */
    private interface TemplateRendererCommand<T>
    {
        T execute(TemplateRenderer renderer);
    }

    private <T> T executeOnTemplateRenderer(TemplateRendererCommand<T> templateRendererCommand)
    {
        TemplateRenderer renderer = (TemplateRenderer) templateRendererServiceTracker.getService();
        if (renderer != null)
        {
            return templateRendererCommand.execute(renderer);
        }
        else
        {
            log.warn("No template renderer service available, not executing command");
            return null;
        }

    }
}
