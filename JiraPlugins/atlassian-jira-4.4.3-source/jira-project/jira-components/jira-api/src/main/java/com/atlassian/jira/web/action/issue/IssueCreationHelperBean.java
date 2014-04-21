package com.atlassian.jira.web.action.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IssueCreationHelperBean
{

    void validateCreateIssueFields(JiraServiceContext jiraServiceContext, Collection<String> providedFields, Issue issueObject, FieldScreenRenderer fieldScreenRenderer,
                                   OperationContext operationContext, Map<String, String[]> parameters, I18nHelper i18n);

    void validateLicense(ErrorCollection errors,
                                I18nHelper i18n);

    void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, com.opensymphony.user.User remoteUser, MutableIssue issueObject, Map fieldValuesHolder);

    void updateIssueFromFieldValuesHolder(FieldScreenRenderer fieldScreenRenderer, User remoteUser, MutableIssue issueObject, Map fieldValuesHolder);

    FieldScreenRenderer createFieldScreenRenderer(com.opensymphony.user.User remoteUser, Issue issueObject);

    FieldScreenRenderer createFieldScreenRenderer(User remoteUser, Issue issueObject);

    List<String> getProvidedFieldNames(com.opensymphony.user.User remoteUser, Issue issueObject);

    List<String> getProvidedFieldNames(User remoteUser, Issue issueObject);

    /**
     * Gets the fields that will be shown in the create issue screen for that issues project and issue type
     *
     * @param user the user in play
     * @param issueObject the as yet saved issue object encompassing project and issue type
     * @return the list of fields that will be shown on the create issue screen
     */
    List<OrderableField> getFieldsForCreate(User user, Issue issueObject);


    void validateProject(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors,
                         I18nHelper i18n);

    void validateIssueType(Issue issue, OperationContext operationContext, Map actionParams, ErrorCollection errors,
                           I18nHelper i18n);

}
