package com.atlassian.jira.web.component.issuesummary;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IssueSummaryBlock extends AbstractWebComponent
{
    private static final String TEMPLATE_PATH = "templates/jira/issue/summary/issuesummaryblock.vm";

    private final PluginAccessor pluginAccessor;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectComponentManager projectComponentManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final LabelUtil labelUtil;
    private final FieldManager fieldManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final WorkflowManager workflowManager;

    public IssueSummaryBlock(VelocityManager velocityManager, PluginAccessor pluginAccessor, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, ProjectComponentManager projectComponentManager,
            FieldVisibilityManager fieldVisibilityManager, LabelUtil labelUtil,
            final FieldManager fieldManager, FieldScreenRendererFactory fieldScreenRendererFactory, final IssueManager issueManager,
            final PermissionManager permissionManager, final WorkflowManager workflowManager)
    {
        super(velocityManager, applicationProperties);

        this.pluginAccessor = pluginAccessor;
        this.authenticationContext = authenticationContext;
        this.projectComponentManager = projectComponentManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.labelUtil = labelUtil;
        this.fieldManager = fieldManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.workflowManager = workflowManager;
    }

    public String getHtml(Issue issue, Action action)
    {
        final User user = authenticationContext.getUser();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("i18n", authenticationContext.getI18nHelper());
        params.put("issue", issue);
        params.put("summaryComponent", this);
        params.put("fieldVisibility", fieldVisibilityManager);
        params.put("issueViews", getIssueViews());
        params.put("canEdit", issueManager.isEditable(issue, user));
        params.put("hasViewWorkflowPermission", permissionManager.hasPermission(Permissions.VIEW_WORKFLOW_READONLY, issue, user));
        final JiraWorkflow workflow = workflowManager.getWorkflow(issue);
        if(workflow != null)
        {
            params.put("workflowName", workflow.getName());
            final StepDescriptor step = workflow.getLinkedStep(issue.getStatus());
            if(step != null) {
                params.put("currentWorkflowStep", Integer.toString(step.getId()));
            }
        }

        if (!fieldVisibilityManager.isFieldHidden("components", issue))
        {
            final List<ProjectComponent> components = getComponents(issue);
            if (components != null && !components.isEmpty())
            {
                params.put("components", components);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden("versions", issue))
        {
            final Collection<Version> versions = getAffectsVersions(issue);
            if (versions != null && !versions.isEmpty())
            {
                params.put("versions", versions);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden("fixVersions", issue))
        {
            final Collection<Version> versions = getFixForVersions(issue);
            if (versions != null && !versions.isEmpty())
            {
                params.put("fixVersions", versions);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.SECURITY, issue))
        {
            final GenericValue securityLevel = issue.getSecurityLevel();
            if (securityLevel != null)
            {
                params.put("securitylevel", securityLevel);
            }
        }
        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.LABELS, issue))
        {
            final Set<Label> labels = issue.getLabels();
            params.put("labelUtil", labelUtil);
            params.put("labels", labels);
            params.put("remoteUser", authenticationContext.getUser());
        }

        if (!fieldVisibilityManager.isFieldHidden(IssueFieldConstants.ENVIRONMENT, issue) && StringUtils.isNotBlank(issue.getEnvironment()))
        {
            params.put("renderedEnvironment", getRenderedEnvironmentFieldValue(user, issue, action));
            params.put("environment", issue.getEnvironment());
        }


        return getHtml(TEMPLATE_PATH, getDefaultParams(params));
    }

    private Map<String, Object> getDefaultParams(Map<String, Object> startingParams)
    {
        return JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
    }

    public Collection<IssueViewModuleDescriptor> getIssueViews()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(IssueViewModuleDescriptor.class);
    }


    /**
     * Gets the HTML that shows the environment field. This includes divs and a javascript enabled hide/show toggle
     * button.
     *
     * @param user who is trying to do it
     * @param issue what issue they want to do it to
     * @param action what they want to do
     * @return the HTML that shows the environment field.
     */
    public String getRenderedEnvironmentFieldValue(final User user, final Issue issue, final Action action)
    {
        final OrderableField environmentField = fieldManager.getOrderableField(IssueFieldConstants.ENVIRONMENT);
        final FieldScreenRenderer renderer = fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, IssueOperations.VIEW_ISSUE_OPERATION, false);

        final FieldLayoutItem fieldLayoutItem = renderer.getFieldScreenRenderLayoutItem(environmentField).getFieldLayoutItem();

        // JRA-16224 Cannot call getViewHtml() on FieldScreenRenderLayoutItem, because it will return "" if Environment is not included in the Screen Layout.
        return environmentField.getViewHtml(fieldLayoutItem, action, issue);
    }

    private List<ProjectComponent> getComponents(Issue issue)
    {
        final Collection<GenericValue> componentGVs = issue.getComponents();
        if (componentGVs == null || componentGVs.isEmpty())
        {
            return null;
        }

        final List<Long> componentIds = new ArrayList<Long>(componentGVs.size());
        for (GenericValue componentGV : componentGVs)
        {
            componentIds.add(componentGV.getLong("id"));
        }

        try
        {
            return projectComponentManager.getComponents(componentIds);
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }

    }

    private Collection<Version> getAffectsVersions(Issue issue)
    {
        return issue.getAffectedVersions();
    }

    private Collection<Version> getFixForVersions(Issue issue)
    {
        return issue.getFixVersions();
    }

}
