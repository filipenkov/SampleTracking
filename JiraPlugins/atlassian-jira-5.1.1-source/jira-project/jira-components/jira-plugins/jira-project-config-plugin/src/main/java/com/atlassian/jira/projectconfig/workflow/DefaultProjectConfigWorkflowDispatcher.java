package com.atlassian.jira.projectconfig.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.jira.workflow.migration.WorkflowMigrationHelper;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.text.Normalizer;
import java.util.Collections;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * @since v5.1
 */
public class DefaultProjectConfigWorkflowDispatcher implements ProjectConfigWorkflowDispatcher
{
    private final static Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private final static int MAX_WORKFLOW_NAME_LENGTH = 255;
    private final static int MAX_WORKFLOW_SCHEME_NAME_LENGTH = 255;

    private final WorkflowSchemeManager workflowSchemeManager;
    private final ProjectService projectService;
    private final JiraAuthenticationContext authCtx;
    private final PermissionManager permissionManager;
    private final WorkflowService workflowService;
    private final MigrationHelperFactory migrationFactory;

    public DefaultProjectConfigWorkflowDispatcher(WorkflowSchemeManager workflowSchemeManager,
            ProjectService projectService, JiraAuthenticationContext authCtx, PermissionManager permissionManager,
            WorkflowService workflowService, MigrationHelperFactory migrationFactory)
    {
        this.workflowSchemeManager = workflowSchemeManager;
        this.projectService = projectService;
        this.authCtx = authCtx;
        this.permissionManager = permissionManager;
        this.workflowService = workflowService;
        this.migrationFactory = migrationFactory;
    }

    @Override
    public ServiceOutcome<Pair<String, Long>> editWorkflow(long projectId)
    {
        final User user = authCtx.getLoggedInUser();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return error("admin.project.workflow.no.edit.permission");
        }

        final Project project = project(projectId, user);
        if (project == null)
        {
            return error("admin.project.project.no.edit.permission");
        }

        if (!workflowSchemeManager.isUsingDefaultScheme(project))
        {
            return error("admin.project.workflow.not.default.scheme");
        }

        final ServiceOutcome<String> copyWfOutcome = copySystemWorkflow(project);
        if (!copyWfOutcome.isValid())
        {
            return error(copyWfOutcome);
        }

        ServiceOutcome<Long> createSchemeeOutcome = createNewSchemeFromWorkflow(copyWfOutcome.getReturnedValue(), project);
        if (!createSchemeeOutcome.isValid())
        {
            return error(createSchemeeOutcome);
        }

        ServiceOutcome<Long> migrateOutcome = migrateWorkflow(project, createSchemeeOutcome.getReturnedValue());
        if (migrateOutcome.isValid())
        {
            return ServiceOutcomeImpl.ok(Pair.nicePairOf(copyWfOutcome.getReturnedValue(), migrateOutcome.getReturnedValue()));
        }

        return error(migrateOutcome);
    }

    ServiceOutcome<Long> migrateWorkflow(Project project, Long targetSchemeId)
    {
        try
        {
            GenericValue scheme = workflowSchemeManager.getScheme(targetSchemeId);
            WorkflowMigrationHelper migrationHelper = migrationFactory.createMigrationHelper(project.getGenericValue(),
                    scheme);

            if (!migrationHelper.doQuickMigrate())
            {
                TaskDescriptor<WorkflowMigrationResult> returnedValue = migrationHelper.migrateAsync();
                return ServiceOutcomeImpl.ok(returnedValue.getTaskId());
            }
            else
            {
                return ServiceOutcomeImpl.ok(null);
            }
        }
        catch (GenericEntityException e)
        {
            return error("admin.project.workflow.scheme.unable.to.migrate.workflow.scheme");
        }
        catch (RejectedExecutionException e)
        {
            return error("admin.project.workflow.scheme.unable.to.migrate.workflow.scheme");
        }
    }

    ServiceOutcome<String> copySystemWorkflow(Project project)
    {
        final JiraServiceContext context = createContext();
        final JiraWorkflow newWorkflow = workflowService.copyWorkflow(context, getWorkflowNameForProject(project), 
                null, getDefaultWorkflow());

        if (newWorkflow == null || context.getErrorCollection().hasAnyErrors())
        {
            return error("admin.project.workflow.unable.to.copy.workflow");
        }
        else
        {
            return ServiceOutcomeImpl.ok(newWorkflow.getName());
        }
    }

    ServiceOutcome<Long> createNewSchemeFromWorkflow(String workflowName, Project project)
    {
        SchemeEntity entity = new SchemeEntity(workflowName, "0");
        try
        {
            Scheme scheme = new Scheme(null, null, getWorkflowSchemeNameForProject(project), Collections.singletonList(entity));
            scheme = workflowSchemeManager.createSchemeAndEntities(scheme);
            return ServiceOutcomeImpl.ok(scheme.getId());
        }
        catch (DataAccessException e)
        {
            return error("admin.project.workflow.scheme.unable.to.copy.workflow.scheme");
        }
    }

    private String getWorkflowSchemeNameForProject(Project project)
    {
        String name = generateWfSchemeNameForPrefix(project.getName());
        if (name == null)
        {
            name = generateWfSchemeNameForPrefix(project.getKey());
            if (name == null)
            {
                name = generateWfSchemeName();
            }
        }
        return name;
    }

    private String generateWfSchemeNameForPrefix(String workflowSchemeNamePrefix)
    {
        if (workflowSchemeNamePrefix == null)
        {
            return null;
        }

        I18nHelper i18nHelper = authCtx.getI18nHelper();
        String newName = i18nHelper.getText("admin.project.workflow.scheme.name.template", workflowSchemeNamePrefix);
        int i = 2;

        while (!validateWorkflowSchemeName(newName) && newName.length() <= MAX_WORKFLOW_SCHEME_NAME_LENGTH)
        {
            newName = i18nHelper.getText("admin.project.workflow.scheme.name.template.with.number",
                    workflowSchemeNamePrefix, String.valueOf(i++));
        }

        return (newName.length() > MAX_WORKFLOW_SCHEME_NAME_LENGTH) ? null : newName;
    }

    private boolean validateWorkflowSchemeName(String newName)
    {
        try
        {
            return !workflowSchemeManager.schemeExists(newName);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private String generateWfSchemeName()
    {
        for (int i = 1; i < Integer.MAX_VALUE; i++)
        {
            String newName = authCtx.getI18nHelper().getText("admin.project.workflow.scheme.name.template.generic", i);
            if (validateWorkflowSchemeName(newName))
            {
                return newName;
            }
        }
        throw new RuntimeException("Really you have this many workflow schemes?");
    }

    private String getWorkflowNameForProject(Project project)
    {
        String name = generateWfNameForPrefix(convertToValidWorkflowName(project.getName()));
        if (name == null)
        {
            name = generateWfNameForPrefix(convertToValidWorkflowName(project.getKey()));
            if (name == null)
            {
                name = generateWfName();
            }
        }
        return name;
    }

    private String generateWfName()
    {
        for (int i = 1; i < Integer.MAX_VALUE; i++)
        {
            String newName = String.format("Workflow %d", i);
            if (validateWorkflowName(newName))
            {
                return newName;
            }
        }
        throw new RuntimeException("Really you have this many workflows?");
    }

    private String generateWfNameForPrefix(String workflowNamePrefix)
    {
        if (workflowNamePrefix == null)
        {
            return null;
        }

        String newName = String.format("%s Workflow", workflowNamePrefix);
        int i = 2;

        while (!validateWorkflowName(newName) && newName.length() <= MAX_WORKFLOW_NAME_LENGTH)
        {
            newName = String.format("%s Workflow %d", workflowNamePrefix, i++);
        }

        return (newName.length() > MAX_WORKFLOW_NAME_LENGTH) ? null : newName;
    }

    private JiraWorkflow getDefaultWorkflow()
    {
        JiraServiceContext context = createContext();
        return workflowService.getWorkflow(context, JiraWorkflow.DEFAULT_WORKFLOW_NAME);
    }

    private boolean validateWorkflowName(String newName)
    {
        JiraServiceContext context = createContext();
        workflowService.validateCopyWorkflow(context, newName);
        return !context.getErrorCollection().hasAnyErrors();
    }

    private static String convertToValidWorkflowName(String str)
    {
        String name = trimToNull(str);
        if (name == null)
        {
            return null;
        }

        if (WorkflowUtil.isAcceptableName(name))
        {
            return name;
        }

        String normalize = Normalizer.normalize(name, Normalizer.Form.NFD);
        normalize = DIACRITICAL_MARKS.matcher(normalize).replaceAll("");

        return WorkflowUtil.isAcceptableName(normalize) ? normalize : null;
    }

    private Project project(long projectId, User user)
    {
        final ProjectService.GetProjectResult projectByIdForAction = projectService.getProjectByIdForAction(user, projectId,
                ProjectAction.EDIT_PROJECT_CONFIG);

        if (projectByIdForAction.isValid())
        {
            return projectByIdForAction.getProject();
        }
        else
        {
            return null;
        }
    }

    private JiraServiceContext createContext()
    {
        return new JiraServiceContextImpl(authCtx.getLoggedInUser(), new SimpleErrorCollection(), authCtx.getI18nHelper());
    }

    private <T> ServiceOutcome<T> error(String...keys)
    {
        ErrorCollection errors = new SimpleErrorCollection();
        for (String key : keys) {
            errors.addErrorMessage(authCtx.getI18nHelper().getText(key));
        }
        return new ServiceOutcomeImpl(errors);
    }

    private static ServiceOutcome<Pair<String, Long>> error(ServiceOutcome<?> key)
    {
        return ServiceOutcomeImpl.from(key.getErrorCollection(), null);
    }
}
