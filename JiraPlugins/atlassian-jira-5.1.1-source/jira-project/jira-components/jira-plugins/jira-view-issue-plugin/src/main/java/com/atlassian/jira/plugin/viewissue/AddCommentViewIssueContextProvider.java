package com.atlassian.jira.plugin.viewissue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import webwork.action.Action;

import java.util.Map;

/**
 * Context Provider for Add Comment block on View issue page.
 *
 * @since v4.4
 */
public class AddCommentViewIssueContextProvider implements CacheableContextProvider
{
    private final FieldLayoutManager fieldLayoutManager;

    public AddCommentViewIssueContextProvider(FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException
    {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final Action action = (Action) context.get("action");

        final MapBuilder<String, Object> paramsBuilder = MapBuilder.newBuilder(context);

        paramsBuilder.add("commentHtml", getCommentHtml(issue, action));

        return paramsBuilder.toMap();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        final Issue issue = (Issue) context.get("issue");
        final User user = (User) context.get("user");

        return issue.getId() + "/" + (user == null ? "" : user.getName());
    }

    private String getCommentHtml(Issue issue, Action action)
    {
        final OperationContext context = (OperationContext) action;

        final MapBuilder<String, Object> displayParams = MapBuilder.newBuilder();
        displayParams.add("theme", "aui");
        displayParams.add("noHeader", true);
        final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);

        final FieldLayoutItem commentFieldLayoutItem = fieldLayout.getFieldLayoutItem("comment");

        final OrderableField commentField = commentFieldLayoutItem.getOrderableField();

        return commentField.getCreateHtml(commentFieldLayoutItem, context, action, issue, displayParams.toMap());


    }
}
