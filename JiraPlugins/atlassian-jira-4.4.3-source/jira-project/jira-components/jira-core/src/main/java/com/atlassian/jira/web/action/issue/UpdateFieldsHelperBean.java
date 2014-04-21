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
 */
public interface UpdateFieldsHelperBean
{

    ActionResult updateIssue(MutableIssue issueObject,
                             OperationContext operationContext,
                             com.opensymphony.user.User user,
                             ErrorCollection errors,
                             I18nHelper i18n) throws Exception;

    ActionResult updateIssue(MutableIssue issueObject,
                             OperationContext operationContext,
                             User user,
                             ErrorCollection errors,
                             I18nHelper i18n) throws Exception;

    void validate(Issue issueObject,
                  OperationContext operationContext,
                  Map actionParams,
                  com.opensymphony.user.User user,
                  ErrorCollection errors,
                  I18nHelper i18n);

    void validate(Issue issueObject,
                  OperationContext operationContext,
                  Map actionParams,
                  User user,
                  ErrorCollection errors,
                  I18nHelper i18n);

    List getFieldsForEdit(com.opensymphony.user.User user, Issue issueObject);

    List getFieldsForEdit(User user, Issue issueObject);

    boolean isFieldValidForEdit(com.opensymphony.user.User user, String fieldId, Issue issueObject);

    boolean isFieldValidForEdit(User user, String fieldId, Issue issueObject);
}
