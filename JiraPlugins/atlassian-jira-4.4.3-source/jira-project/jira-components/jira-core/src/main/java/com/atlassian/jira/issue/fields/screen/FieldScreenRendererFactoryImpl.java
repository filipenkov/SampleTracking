package com.atlassian.jira.issue.fields.screen;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.opensymphony.workflow.loader.ActionDescriptor;

import java.util.Collection;
import java.util.List;

/**
 * Default implementation of the FieldScreenRendererFactory.
 */
public class FieldScreenRendererFactoryImpl implements FieldScreenRendererFactory
{
    private final BulkFieldScreenRendererFactory bulkRendererFactory;
    private final StandardFieldScreenRendererFactory rendererFactory;

    public FieldScreenRendererFactoryImpl(FieldManager fieldManager, FieldLayoutManager fieldLayoutManager,
            IssueTypeScreenSchemeManager issueTypeScreenSchemeManager, FieldScreenManager fieldScreenManager, HackyFieldRendererRegistry hackyFieldRendererRegistry)
    {
        this (new BulkFieldScreenRendererFactory(fieldManager, fieldLayoutManager, hackyFieldRendererRegistry),
                new StandardFieldScreenRendererFactory(fieldManager, fieldLayoutManager, issueTypeScreenSchemeManager, fieldScreenManager));
    }

    FieldScreenRendererFactoryImpl(BulkFieldScreenRendererFactory bulkRendererFactory, StandardFieldScreenRendererFactory rendererFactory)
    {
        this.bulkRendererFactory = bulkRendererFactory;
        this.rendererFactory = rendererFactory;
    }

    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields)
    {
        return getFieldScreenRenderer(remoteUser, issue, issueOperation, onlyShownCustomFields ? FieldPredicates.isCustomField() : Predicates.<Field>truePredicate());
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, ActionDescriptor actionDescriptor)
    {
        return getFieldScreenRenderer((User) remoteUser, issue, actionDescriptor);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
    {
        return getFieldScreenRenderer((User) remoteUser, issue, issueOperation, predicate);
    }

    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
    {
        return rendererFactory.createFieldScreenRenderer(issue, issueOperation, predicate);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation, boolean onlyShownCustomFields)
    {
        return getFieldScreenRenderer((User) remoteUser, issue, issueOperation, onlyShownCustomFields);
    }

    public FieldScreenRenderer getFieldScreenRenderer(User remoteUser, Issue issue, ActionDescriptor actionDescriptor)
    {
        return rendererFactory.createFieldScreenRenderer(issue, actionDescriptor);
    }

    public FieldScreenRenderer getFieldScreenRenderer(Issue issue)
    {
        return rendererFactory.createFieldScreenRenderer(issue);
    }

    public FieldScreenRenderer getFieldScreenRenderer(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        return bulkRendererFactory.createRenderer(issues, actionDescriptor);
    }

    @Override
    public FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, com.opensymphony.user.User remoteUser, Issue issue, IssueOperation issueOperation)
    {
        return getFieldScreenRenderer(fieldIds, (User) remoteUser, issue, issueOperation);
    }

    public FieldScreenRenderer getFieldScreenRenderer(List<String> fieldIds, User remoteUser, Issue issue, IssueOperation issueOperation)
    {
        return rendererFactory.createFieldScreenRenderer(fieldIds, issue, issueOperation);
    }
}
