package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ProjectSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.velocity.VelocityManager;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;

/**
 * JIRA's project suystem field.
 */
public class ProjectSystemField extends AbstractOrderableNavigableFieldImpl implements ProjectField
{
    private static final String PROJECT_NAME_KEY = "issue.field.project";
    private static final String FIELD_PARAMETER_NAME = "pid";

    private final ProjectManager projectManager;
    private final ProjectStatisticsMapper projectStatisticsMapper;

    public ProjectSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, ProjectManager projectManager,
            PermissionManager permissionManager, ProjectStatisticsMapper projectStatisticsMapper,
            final ProjectSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.PROJECT, PROJECT_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.projectManager = projectManager;
        this.projectStatisticsMapper = projectStatisticsMapper;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        Long projectId = (Long) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), projectId);
        Collection allowedProjects = getAllowedProjects();

        velocityParams.put("projects", allowedProjects);
        return renderTemplate("project-edit.vm", velocityParams);
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Project field cannot be edited.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(null, action, null, displayParameters);
        Long projectId = (Long) operationContext.getFieldValuesHolder().get(getId());
        velocityParams.put(getId(), projectId);
        Collection allowedProjects = getAllowedProjects();

        velocityParams.put("projects", allowedProjects);
        return renderTemplate("project-edit.vm", velocityParams);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        try
        {
            Map fieldValuesHolder = operationContext.getFieldValuesHolder();
            Long projectId = (Long) fieldValuesHolder.get(getId());
            if (projectId != null)
            {
                GenericValue project = getProject(projectId);
                if (project == null)
                {
                    errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.invalidproject"));
                    errorCollectionToAddTo.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
                }
                else if (!getAllowedProjects().contains(project))
                {
                    if (authenticationContext.getUser() != null)
                    {
                        errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.projectnopermission"));
                        errorCollectionToAddTo.addReason(ErrorCollection.Reason.FORBIDDEN);
                    }
                    else
                    {
                        errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.projectnopermission.notloggedin"));
                        errorCollectionToAddTo.addReason(ErrorCollection.Reason.FORBIDDEN);
                    }
                }
            }
            else
            {
                errorCollectionToAddTo.addError(FIELD_PARAMETER_NAME, i18n.getText("createissue.noproject"));
                errorCollectionToAddTo.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        catch (NumberFormatException e)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidproject"));
            errorCollectionToAddTo.addReason(ErrorCollection.Reason.VALIDATION_FAILED);
        }

    }

    protected Object getRelevantParams(Map params)
    {
        String[] value = (String[]) params.get(FIELD_PARAMETER_NAME);
        if (value != null && value.length > 0)
        {
            return new Long(value[0]);
        }
        else
        {
            return null;
        }
    }

    protected GenericValue getProject(Long projectId)
    {
        return projectManager.getProject(projectId);
    }

    public Object getValueFromParams(Map params)
    {
        return getProject((Long) params.get(getId()));
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getAllowedProjects()
    {
        return getPermissionManager().getProjects(Permissions.CREATE_ISSUE, getAuthenticationContext().getUser());
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getProject().getLong("id"));
    }

    public Object getDefaultValue(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void createValue(Issue issue, Object value)
    {
        // Do not do anything the value is recorded on the issue itself
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        throw new UnsupportedOperationException("Project field cannot be changed.");
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setProject((GenericValue) getValueFromParams(fieldValueHolder));
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("This method should never be called.");
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getProject() != null);
    }

    ///////////////////////////////////////////// NavigableField implementation //////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.project";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return projectStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("project", issue.getProject());
        return renderTemplate("project-columnview.vm", velocityParams);
    }
}
