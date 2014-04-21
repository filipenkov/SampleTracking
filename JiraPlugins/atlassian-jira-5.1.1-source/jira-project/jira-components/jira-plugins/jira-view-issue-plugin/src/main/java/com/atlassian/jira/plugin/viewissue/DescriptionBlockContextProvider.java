package com.atlassian.jira.plugin.viewissue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Context provider for the description block
 *
 * @since v5.0
 */
public class DescriptionBlockContextProvider implements ContextProvider
{
    final private FieldLayoutManager fieldLayoutManager;
    final private RendererManager rendererManager;

    public DescriptionBlockContextProvider(FieldLayoutManager fieldLayoutManager, RendererManager rendererManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.rendererManager = rendererManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        final Issue issue = (Issue) context.get("issue");

        final FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue).getFieldLayoutItem(IssueFieldConstants.DESCRIPTION);
        if (fieldLayoutItem != null)
        {
            String renderedContent = rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), issue.getDescription(), issue.getIssueRenderContext());
            if (StringUtils.isNotBlank(renderedContent))
            {
                paramsBuilder.add("descriptionHtml", renderedContent);
            }
        }
        else
        {
            if (StringUtils.isNotBlank(issue.getDescription()))
            {
                paramsBuilder.add("descriptionHtml", issue.getDescription());
            }
        }

        return paramsBuilder.toMap();
    }
}
