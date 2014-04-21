package com.atlassian.jira.issue.fields.screen;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * A factory for obtaining FieldScreenRenderer's.
 */
public interface FieldScreenRendererFactory
{
    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments. Only the fields
     * that match the passed predicate will be included in the returned {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}.
     * This is the preferred way of getting a FieldScreenRenderer.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param predicate only fields that cause ths predicate to return true will be returned in the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments. Only the fields
     * that match the passed predicate will be included in the returned {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}.
     * This is the preferred way of getting a FieldScreenRenderer.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param predicate only fields that cause ths predicate to return true will be returned in the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer}
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param onlyShownCustomFields if true will only return custom fields in the FieldScreenRenderer, otherwise
     * all fields will be returned.
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields for the passed arguments.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     * @param onlyShownCustomFields if true will only return custom fields in the FieldScreenRenderer, otherwise
     * all fields will be returned.
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields when transitioning through the passed workflow.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param actionDescriptor the current workflow action descriptor
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, ActionDescriptor actionDescriptor);

    /**
     * Obtain a field screen renderer that can be used to render JIRA's fields when transitioning through the passed workflow.
     *
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param actionDescriptor the current workflow action descriptor
     *
     * @return a FieldScreenRenderer for the provided context.
     */
    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, ActionDescriptor actionDescriptor);

    /**
     * Used when need to populate a field without showing a screen - e.g. When using a UpdateIssueFieldFunction in workflow
     *
     * @param issue the currentIssue.
     *
     * @return a FieldScreenRenderer without any tabs.
     */
    public FieldScreenRenderer getFieldScreenRenderer(Issue issue);

    /**
     * Get a renderer that can be used to render the fields when transitioning a collection of issues through workflow.
     *
     * @param issues the issues to be rendered.
     * @param actionDescriptor current workflow action descriptor
     *
     * @return a BulkFieldScreenRenderer - aggregates the tabs and fields for the specified collection of issues
     */
    public FieldScreenRenderer getFieldScreenRenderer(Collection<Issue> issues, ActionDescriptor actionDescriptor);

    /**
     * Returns a {@link FieldScreenRenderer} that represents a 'field screen' with the fields the ids of which
     * are in fieldIds. The returned Field Renderer places all given fields on one tab.
     *
     * @param fieldIds the fields to create the renderer for.
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     *
     * @return a FieldScreenRenderer for the passed fields.
     */
    FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation);
    
    /**
     * Returns a {@link FieldScreenRenderer} that represents a 'field screen' with the fields the ids of which
     * are in fieldIds. The returned Field Renderer places all given fields on one tab.
     *
     * @param fieldIds the fields to create the renderer for.
     * @param remoteUser the current user
     * @param issue the issue to be rendered.
     * @param issueOperation the current issue operation.
     *
     * @return a FieldScreenRenderer for the passed fields.
     */
    FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, User remoteUser, Issue issue, IssueOperation issueOperation);
}
