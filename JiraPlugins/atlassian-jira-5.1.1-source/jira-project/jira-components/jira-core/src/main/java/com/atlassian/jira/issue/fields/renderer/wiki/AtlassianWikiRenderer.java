package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2RendererFacade;
import org.springframework.beans.factory.DisposableBean;

/**
 * Implementation of the a renderer plugin that exposes the Wiki renderer within Jira.
 * 
 */
public class AtlassianWikiRenderer implements JiraRendererPlugin, DisposableBean
{
    public static final String ISSUE_CONTEXT_KEY = "jira.issue";
    public static final String RENDERER_TYPE = "atlassian-wiki-renderer";


    private volatile JiraRendererModuleDescriptor jiraRendererModuleDescriptor;
    private final WikiRendererFactory wikiFactory = new WikiRendererFactory();
    private final EventPublisher eventPublisher;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public AtlassianWikiRenderer(EventPublisher eventPublisher, VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.eventPublisher = eventPublisher;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.eventPublisher.register(wikiFactory);
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(wikiFactory);
    }

    public String render(String value, IssueRenderContext context)
    {
        return getRendererFacade().convertWikiToXHtml(getRenderContext(context), value);
    }

    public String renderAsText(String value, IssueRenderContext context)
    {
        return getRendererFacade().convertWikiToText(getRenderContext(context), value);
    }

    public String getRendererType()
    {
        return V2Renderer.RENDERER_TYPE;
    }

    public Object transformForEdit(Object rawValue)
    {
        return rawValue;
    }

    public Object transformFromEdit(Object editValue)
    {
        return editValue;
    }

    public void init(JiraRendererModuleDescriptor jiraRendererModuleDescriptor)
    {
        this.jiraRendererModuleDescriptor = jiraRendererModuleDescriptor;
    }

    public JiraRendererModuleDescriptor getDescriptor()
    {
        return jiraRendererModuleDescriptor;
    }

    private V2RendererFacade getRendererFacade()
    {
        return getWikiRendererFactory().getWikiRenderer();
    }

    RenderContext getRenderContext(IssueRenderContext context)
    {
        RenderContext renderContext = new RenderContext();
        // Add params from the jira render context
        if (context != null)
        {
            renderContext.getParams().putAll(context.getParams());
            renderContext.addParam(ISSUE_CONTEXT_KEY, context.getIssue());
        }
        renderContext.setBaseUrl(velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
        ApplicationProperties applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
        String mimeSniffingPolicy = applicationProperties.getDefaultBackedString(APKeys.JIRA_OPTION_IE_MIME_SNIFFING);
        RenderMode renderMode = null;
        if (mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_OWNED))
        {
            renderMode = RenderMode.suppress(RenderMode.F_MACROS_ERR_MSG);
        }
        else if (mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_PARANOID))
        {
            renderMode = RenderMode.suppress(RenderMode.F_MACROS_ERR_MSG | RenderMode.F_IMAGES | RenderMode.F_EMBEDDED_OBJECTS);
        }
        else if (mimeSniffingPolicy.equalsIgnoreCase(APKeys.MIME_SNIFFING_WORKAROUND))
        {
            renderMode = RenderMode.suppress(RenderMode.F_MACROS_ERR_MSG | RenderMode.F_EMBEDDED_OBJECTS);
        }
        renderContext.pushRenderMode(renderMode);

        return renderContext;
    }

    private WikiRendererFactory getWikiRendererFactory()
    {
        return wikiFactory;
    }
}
