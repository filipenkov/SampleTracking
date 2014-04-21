package com.atlassian.jira.collector.plugin.components;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class ScriptletRendererImpl implements ScriptletRenderer
{
    private static final Logger log = Logger.getLogger(ScriptletRendererImpl.class);
    private static final String SRC_ATTR = "src=\"";

    private final WebResourceManager webResourceManager;
    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext authenticationContext;

    public ScriptletRendererImpl(final WebResourceManager webResourceManager, final TemplateRenderer templateRenderer,
            final JiraAuthenticationContext authenticationContext)
    {
        this.webResourceManager = webResourceManager;
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
    }

    @Override
    public String render(final Collector collector)
    {
        return renderTemplate(collector, "templates/rest/scriptlet-source.vm","issuecollector");
    }

    @Override
    public String renderJavascript(final Collector collector)
    {
        return renderTemplate(collector, "templates/rest/scriptlet-source-javascript.vm","issuecollector-embededjs");
    }


    private String renderTemplate(final Collector collector, final String template, final String resource)
    {
        final StringWriter out = new StringWriter();
        final Map<String, Object> context = JiraVelocityUtils.createVelocityParams(authenticationContext);

        context.put("collector", collector);
        context.put("triggerTextHtml", StringEscapeUtils.escapeJavaScript(collector.getTrigger().getText()));
        if (collector.getTrigger().getPosition().equals(Trigger.Position.CUSTOM))
        {
            context.put("customFunctionHtml", collector.getTrigger().getCustomFunction());
        }
        
        final String resourceTag = webResourceManager.getResourceTags(String.format("com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:%s",resource), UrlMode.ABSOLUTE);

        if(resourceTag.contains(SRC_ATTR)) 
        {
            final String resourceUrl = resourceTag.substring(resourceTag.indexOf(SRC_ATTR) + SRC_ATTR.length(), resourceTag.lastIndexOf("\""));
            context.put("bootstrapUrl", resourceUrl);
        }

        try
        {
            templateRenderer.render(template, context, out);
        }
        catch (IOException e)
        {
            log.error("Unknown error rendering template scriptlet-source.vm", e);
            return "Error rendering script!";
        }

        return out.toString();
    }
}
