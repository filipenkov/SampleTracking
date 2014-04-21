package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
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

    public AtlassianWikiRenderer(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
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
        // JRA-17418 - now that we have changed the style of P in JIRA we are cool to have a leading P
        RenderMode allWithNoMacroErrors = RenderMode.suppress(RenderMode.F_MACROS_ERR_MSG );
        renderContext.pushRenderMode(allWithNoMacroErrors);

        return renderContext;
    }

    private WikiRendererFactory getWikiRendererFactory()
    {
        return wikiFactory;
    }
}
