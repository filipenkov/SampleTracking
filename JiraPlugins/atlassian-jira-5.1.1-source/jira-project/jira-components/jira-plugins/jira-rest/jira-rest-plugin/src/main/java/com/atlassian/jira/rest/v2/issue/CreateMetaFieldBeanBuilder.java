package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.ProjectJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Builder for {@link FieldMetaBean} instances, in the context of meta data for creating issues.
 *
 * @since v5.0
 */
public class CreateMetaFieldBeanBuilder extends AbstractMetaFieldBeanBuilder
{
    private final OperationContext operationContext = new CreateIssueOperationContext();
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final JiraAuthenticationContext authContext;

    public CreateMetaFieldBeanBuilder(final FieldLayoutManager fieldLayoutManager, final Project project, final Issue issue, final IssueType issueType, final User user, VersionBeanFactory versionBeanFactory, VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo, ProjectBeanFactory projectBeanFactory, JiraBaseUrls baseUrls, PermissionManager permissionManager, FieldScreenRendererFactory fieldScreenRendererFactory,
            JiraAuthenticationContext authContext)
    {
        super(fieldLayoutManager, project, issue, issueType, user, versionBeanFactory, velocityRequestContextFactory, contextUriInfo, baseUrls);
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.authContext = authContext;
    }

    @Override
    public OperationContext getOperationContext()
    {
        return operationContext;
    }

    @Override
    public Map<String, FieldMetaBean> build()
    {
        Map<String, FieldMetaBean> fields = super.build();

        // Add 'Project' to the CREATE fields list, since it isn't in the field layout, but in REST it is "required" for create
        FieldMetaBean projectFieldMetaBean = new FieldMetaBean(true, ProjectSystemField.getJsonType(),
                authContext.getI18nHelper().getText(ProjectSystemField.PROJECT_NAME_KEY), null,
                Collections.singletonList(StandardOperation.SET.getName()), Collections.singletonList(ProjectJsonBean.shortBean(project, baseUrls)));
        fields.put(IssueFieldConstants.PROJECT, projectFieldMetaBean);

        // If this is for a subtask then add the "parent" pseudo field
        if (issueType.isSubTask())
        {
            FieldMetaBean parentFieldMetaBean = new FieldMetaBean(true, JsonTypeBuilder.system("issuelink", "parent"),
                    authContext.getI18nHelper().getText("issue.field.parent"), null,
                    Collections.singletonList(StandardOperation.SET.getName()), null);
            fields.put("parent", parentFieldMetaBean);
        }

        return fields;
    }

    @Override
    public boolean hasPermissionToPerformOperation()
    {
        return permissionManager.hasPermission(Permissions.CREATE_ISSUE, issue, user);
    }

    @Override
    FieldScreenRenderer getFieldScreenRenderer(User user, Issue issue)
    {
        return fieldScreenRendererFactory.getFieldScreenRenderer(user, issue, IssueOperations.CREATE_ISSUE_OPERATION, false);
    }
}
