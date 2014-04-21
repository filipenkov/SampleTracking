package com.atlassian.gadgets.dashboard.internal;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;

import com.atlassian.gadgets.dashboard.internal.util.JavaScript;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRendererFactory;

import static com.google.common.collect.Maps.newHashMap;

public class DashboardTemplateRenderer implements TemplateRenderer
{
    private final TemplateRenderer renderer;
    private final DashboardUrlBuilder urlBuilder;
    private final DashboardWebItemFinder webItemFinder;
    private final ApplicationProperties applicationProperties;

    public DashboardTemplateRenderer(VelocityTemplateRendererFactory factory,
            DashboardUrlBuilder urlBuilder,
            DashboardWebItemFinder webItemFinder,
            ApplicationProperties applicationProperties)
    {
        this.urlBuilder = urlBuilder;
        this.webItemFinder = webItemFinder;
        this.applicationProperties = applicationProperties;
        renderer = factory.getInstance(Collections.<String, String>emptyMap());
    }
    
    public void render(String templateName, Writer writer) throws RenderingException, IOException
    {
        render(templateName, Collections.<String, Object>emptyMap(), writer);
    }

    public void render(String templateName, Map<String, Object> context, Writer writer) throws RenderingException, IOException
    {
        renderer.render(templateName, createContext(context, writer), writer);
    }
    
    public String renderFragment(String fragment, Map<String, Object> context)
    {
        return renderer.renderFragment(fragment, createContext(context, null));
    }

    public boolean resolve(String templateName)
    {
        return renderer.resolve(templateName);
    }

    private Map<String, Object> createContext(Map<String, Object> contextParams, Writer writer)
    {
        Map<String, Object> context = newHashMap(contextParams);
        context.put("urlBuilder", urlBuilder);
        context.put("writer", writer);
        context.put("webItemFinder", webItemFinder);
        context.put("applicationProperties", applicationProperties);
        context.put("js", new JavaScript());
        return context;
    }
}
