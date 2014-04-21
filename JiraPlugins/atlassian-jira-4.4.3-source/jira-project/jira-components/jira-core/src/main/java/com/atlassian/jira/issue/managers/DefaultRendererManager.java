package com.atlassian.jira.issue.managers;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer;
import com.atlassian.jira.issue.renderers.FieldRenderContext;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The default implementation of a RendererManager.
 */
public class DefaultRendererManager implements RendererManager
{
    private static final Logger log = Logger.getLogger(DefaultRendererManager.class);
    private static final String PLUGIN_TYPE = "com.atlassian.jira.plugin.system.jirarenderers";

    private final PluginAccessor pluginAccessor;

    public DefaultRendererManager(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public List<JiraRendererPlugin> getAllActiveRenderers()
    {
        final List<JiraRendererModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(JiraRendererModuleDescriptor.class);
        final List<JiraRendererPlugin> renderers = new ArrayList<JiraRendererPlugin>(descriptors.size());
        for (final JiraRendererModuleDescriptor descriptor : descriptors)
        {
            renderers.add(descriptor.getModule());
        }
        return renderers;
    }

    public JiraRendererPlugin getRendererForType(String rendererType)
    {
        JiraRendererPlugin renderer = getRendererPluginForType(rendererType);

        if (renderer == null)
        {
            // fall back and get the text renderer
            renderer = getRendererPluginForType(DefaultTextRenderer.RENDERER_TYPE);
            if(renderer == null)
            {
                throw new IllegalStateException("The default text renderer for Jira has been disabled, please re-enable the text renderer for correct funtioning to resume.");
            }
        }
        return renderer;
    }

    public JiraRendererPlugin getRendererForField(FieldLayoutItem fieldConfig)
    {
        String rendererType = (fieldConfig != null) ? fieldConfig.getRendererType() : null;
        return getRendererForType(rendererType);
    }

    public String getRenderedContent(FieldLayoutItem fieldConfig, Issue issue)
    {
        if (!(fieldConfig.getOrderableField() instanceof RenderableField))
        {
            log.warn("Attempting to get renderered content from a field that is not of type RendererableField, offending field is: " + fieldConfig.getOrderableField().getId());
            throw new IllegalArgumentException("Attempting to get renderered content from a field that is not of type RendererableField, offending field is: " + fieldConfig.getOrderableField().getId());
        }

        IssueRenderContext renderContext = issue.getIssueRenderContext();

        RenderableField renderField = (RenderableField) fieldConfig.getOrderableField();

        JiraRendererPlugin rendererPlugin = getRendererForField(fieldConfig);

        return rendererPlugin.render(renderField.getValueFromIssue(issue), renderContext);
    }

    public String getRenderedContent(String rendererType, String value, IssueRenderContext renderContext)
    {
        JiraRendererPlugin rendererPlugin = getRendererForType(rendererType);

        return rendererPlugin.render(value, renderContext);
    }

    public String getRenderedContent(FieldRenderContext fieldRenderContext)
    {
        Issue issue = fieldRenderContext.getIssue();
        String fieldId = fieldRenderContext.getFieldId();
        FieldLayoutManager fieldLayoutManager = ComponentManager.getComponentInstanceOfType(FieldLayoutManager.class); // can't put into constructor due to cyclic dependency

        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(fieldId);
        if (fieldLayoutItem != null)
        {
            return getRenderedContent(fieldLayoutItem.getRendererType(), fieldRenderContext.getBody(), issue.getIssueRenderContext());
        }
        return null;
    }

    /**
     * Returns the renderer plugin of the specified renderer type.
     * <p>
     * Note that in the case where there are multiple renderers with the same type (for example if an external plugin
     * uses the same name as another plugin), then the first result will be returned.
     *
     * @param rendererType the name of the renderer type
     * @return the renderer plugin; null if none was found.
     */
    private JiraRendererPlugin getRendererPluginForType(String rendererType)
    {
        final List<JiraRendererPlugin> rendererPlugins = pluginAccessor.getEnabledModulesByClass(JiraRendererPlugin.class);
        for (JiraRendererPlugin rendererPlugin : rendererPlugins)
        {
            if (rendererPlugin.getRendererType().equals(rendererType))
            {
                return rendererPlugin;
            }
        }

        return null;
    }
}
