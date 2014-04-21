package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import webwork.dispatcher.ActionResult;

import java.util.List;
import java.util.Map;

/**
 * Bean to help with updating issues only for the fields in the action params. That is, no attempt is made to update
 * fields that are not explicitly passed in the action params map. This way, you can use this bean to update a single, or
 * a small number of fields without having to recreate the entire object.
 *
 * @deprecated Use {@link com.atlassian.jira.bc.issue.IssueService} or {@link com.atlassian.jira.issue.IssueManager} instead. Since v5.0.
 */
public interface UpdateFieldsHelperBean
{
    /**
     * @deprecated Use {@link com.atlassian.jira.bc.issue.IssueService#update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult)} instead. Since v5.0.
     */
    ActionResult updateIssue(MutableIssue issueObject,
                             OperationContext operationContext,
                             User user,
                             ErrorCollection errors,
                             I18nHelper i18n) throws Exception;

    /**
     * @deprecated Use {@link com.atlassian.jira.bc.issue.IssueService#validateUpdate(com.atlassian.crowd.embedded.api.User, Long, com.atlassian.jira.issue.IssueInputParameters)} instead. Since v5.0.
     */
    void validate(Issue issueObject,
                  OperationContext operationContext,
                  Map actionParams,
                  User user,
                  ErrorCollection errors,
                  I18nHelper i18n);

    /**
     * @deprecated Use {@link com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory} instead. Since v5.0.
     */
    List getFieldsForEdit(User user, Issue issueObject);

    /**
     * @deprecated Use {@link com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory} instead. Since v5.0.
     */
    boolean isFieldValidForEdit(User user, String fieldId, Issue issueObject);
}
