package com.atlassian.jira.plugin.issuenav.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.rest.FieldHtmlFactory;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Base action for retrieving field edit html
 *
 * @since 5.0
 */
public abstract class BaseEditAction extends JiraWebActionSupport implements OperationContext
{
    protected final IssueService issueService;
    protected final FieldHtmlFactory fieldHtmlFactory;

    private final Map<String, Object> fieldValuesHolder = new HashMap<String, Object>();
    protected Long issueId;

    public BaseEditAction(final IssueService issueService, final FieldHtmlFactory fieldHtmlFactory)
    {
        this.issueService = issueService;
        this.fieldHtmlFactory = fieldHtmlFactory;
    }

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    @Override
    public Map getFieldValuesHolder()
    {
        return fieldValuesHolder;
    }

    public void setFieldValuesHolder(final Map<String, Object> fieldValuesHolder)
    {
        this.fieldValuesHolder.clear();
        this.fieldValuesHolder.putAll(fieldValuesHolder);
    }

    @Override
    public IssueOperation getIssueOperation()
    {
        return IssueOperations.EDIT_ISSUE_OPERATION;
    }
}
