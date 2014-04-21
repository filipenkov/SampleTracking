package com.atlassian.plugins.rest.module.template;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.plugins.rest.common.template.Renderer;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.sun.jersey.api.core.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RendererImpl implements Renderer
{
    private final TemplateRenderer templateRenderer;
    private final OutputStreamWriter writer;
    private final HttpContext httpContext;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    RendererImpl(final TemplateRenderer templateRenderer, final OutputStreamWriter writer,
        final HttpContext httpContext, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        this.templateRenderer = Preconditions.checkNotNull(templateRenderer);
        this.writer = Preconditions.checkNotNull(writer);
        this.httpContext = Preconditions.checkNotNull(httpContext);
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    public void render(final Object model, final String template) throws IOException
    {
        final Map<String, Object> context = Maps.newHashMap();
        context.put("it", model);
        context.put("renderer", this);
        context.put("httpContext", httpContext);
        context.put("request", httpServletRequest);
        context.put("response", httpServletResponse);

        templateRenderer.render(getAbsolutePath(model.getClass(), template), context, writer);
    }

    private String getAbsolutePath(final Class<?> resourceClass, String path)
    {
        // absolute path
        if (StringUtils.startsWith(path, "/"))
        {
            return path;
        }

        // undefined, defaults to index
        if (StringUtils.isEmpty(path) || StringUtils.equals(path, "/"))
        {
            path = "index";
        }

        return getAbsolutePath(resourceClass) + '/' + path;
    }

    private String getAbsolutePath(final Class<?> resourceClass)
    {
        return '/' + resourceClass.getName().replace('.', '/').replace('$', '/');
    }
}