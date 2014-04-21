package com.atlassian.jira.bc.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * This class implements constant-related business cases.
 *
 * @since v4.2
 */
public class DefaultConstantsService implements ConstantsService
{
    private final I18nHelper i18n;
    private final ConstantsManager constantsMgr;
    private final PermissionManager permissionMgr;
    private final WorkflowSchemeManager workflowSchemeMgr;
    private final WorkflowManager workflowMgr;
    private final IssueTypeSchemeManager issueTypeSchemeMgr;

    /**
     * Creates a new DefaultConstantsService.
     *
     * @param i18n a I18nHelper
     * @param constantsManager a ConstantsManager
     * @param permissionManager a PermissionManager
     * @param workflowSchemeMgr a WorkflowSchemeManager
     * @param workflowMgr a WorkflowManager
     * @param issueTypeSchemeMgr an IssueTypeSchemeManager
     */
    public DefaultConstantsService(I18nHelper i18n, ConstantsManager constantsManager, PermissionManager permissionManager, WorkflowSchemeManager workflowSchemeMgr, WorkflowManager workflowMgr, IssueTypeSchemeManager issueTypeSchemeMgr)
    {
        this.constantsMgr = constantsManager;
        this.permissionMgr = permissionManager;
        this.workflowSchemeMgr = workflowSchemeMgr;
        this.workflowMgr = workflowMgr;
        this.i18n = i18n;
        this.issueTypeSchemeMgr = issueTypeSchemeMgr;
    }

    @Override
    public ServiceOutcome<Status> getStatusById(com.opensymphony.user.User user, String statusId)
    {
        return getStatusById((User) user, statusId);
    }

    public ServiceOutcome<Status> getStatusById(User user, String statusId)
    {
        Status status = constantsMgr.getStatusObject(statusId);
        if (status != null)
        {
            // check if the status is visible via the project's workflows. there must be a better way to do this!
            Collection<Project> visibleProjects = permissionMgr.getProjectObjects(Permissions.BROWSE, user);
            for (Project project : visibleProjects)
            {
                List<JiraWorkflow> workflows = getWorkflows(project);
                for (JiraWorkflow workflow : workflows)
                {
                    @SuppressWarnings ("unchecked")
                    List<StepDescriptor> steps = workflow.getDescriptor().getSteps();
                    for (StepDescriptor step : steps)
                    {
                        String linkedStatusId = (String) step.getMetaAttributes().get("jira.status.id");
                        if (statusId.equals(linkedStatusId))
                        {
                            return ServiceOutcomeImpl.ok(status);
                        }
                    }
                }
            }
        }

        return ServiceOutcomeImpl.error(i18n.getText("constants.service.status.not.found", statusId));
    }

    @Override
    public ServiceOutcome<IssueType> getIssueTypeById(com.opensymphony.user.User user, String issueTypeId)
    {
        return getIssueTypeById((User) user, issueTypeId);
    }

    public ServiceOutcome<IssueType> getIssueTypeById(User user, String issueTypeId)
    {
        IssueType issueType = constantsMgr.getIssueTypeObject(issueTypeId);
        if (issueType != null)
        {
            // check if the issue type is visible via a project. needs improvement...
            Collection<Project> visibleProjects = permissionMgr.getProjectObjects(Permissions.BROWSE, user);
            for (Project project : visibleProjects)
            {
                for (IssueType visibleType : issueTypeSchemeMgr.getIssueTypesForProject(project))
                {
                    if (issueTypeId.equals(visibleType.getId()))
                    {
                        return ServiceOutcomeImpl.ok(issueType);
                    }
                }
            }
        }

        return ServiceOutcomeImpl.error(i18n.getText("constants.service.issuetype.not.found", issueTypeId));
    }

    /**
     * Returns the JiraWorkflow instances that are active for a given project.
     *
     * @param project a Project
     * @return a List of JiraWorkflow
     */
    @SuppressWarnings ("unchecked")
    private List<JiraWorkflow> getWorkflows(Project project)
    {
        try
        {
            GenericValue workflowScheme = workflowSchemeMgr.getWorkflowScheme(project.getGenericValue());
            if (workflowScheme == null)
            {
                return singletonList(workflowMgr.getDefaultWorkflow());
            }

            List<GenericValue> workflows = new ArrayList<GenericValue>();
            workflows.add(workflowSchemeMgr.getDefaultEntity(workflowScheme));
            workflows.addAll(workflowSchemeMgr.getNonDefaultEntities(workflowScheme));
            if (workflows.isEmpty())
            {
                return emptyList();
            }

            List<JiraWorkflow> result = new ArrayList<JiraWorkflow>(workflows.size());
            for (GenericValue workflow : workflows)
            {
                result.add(workflowMgr.getWorkflow(workflow.getString("workflow")));
            }

            return result;
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }
}
