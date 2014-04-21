package com.atlassian.jira.workflow;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.operation.WorkflowIssueOperationImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class WorkflowTransitionUtilImpl implements WorkflowProgressAware, WorkflowTransitionUtil
{
    private final ErrorCollection errorCollection;
    private final JiraAuthenticationContext authenticationContext;
    private final WorkflowManager workflowManager;
    private final PermissionManager permissionManager;

    private MutableIssue issue;
    private GenericValue project;
    private int actionId;
    private ActionDescriptor actionDescriptor;

    private Map params;

    // The username of the user who the workflow transition will be executed as
    private String username;
    private FieldScreenRenderer fieldScreenRenderer;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private final CommentService commentService;
    private Map additionalInputs = new HashMap();

    public WorkflowTransitionUtilImpl(JiraAuthenticationContext authenticationContext, WorkflowManager workflowManager, PermissionManager permissionManager, FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService)
    {
        this.authenticationContext = authenticationContext;
        this.workflowManager = workflowManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.commentService = commentService;
        errorCollection = new SimpleErrorCollection();
        params = new HashMap();
        username = (getRemoteUser() == null ? null : getRemoteUser().getName());
    }

    public MutableIssue getIssue()
    {
        return issue;
    }

    public void setIssue(MutableIssue issue)
    {
        this.issue = issue;
    }

    private String getComment()
    {
        return (String) params.get(FIELD_COMMENT);
    }

    // This is really getGroupCommentLevel
    private String getCommentLevel()
    {
        return (String) params.get(FIELD_COMMENT_LEVEL);
    }

    private String getCommentRoleLevel()
    {
        return (String) params.get(FIELD_COMMENT_ROLE_LEVEL);
    }

    public GenericValue getProject()
    {
        if (project == null)
        {
            project = issue.getProject();
        }

        return project;
    }

    public User getRemoteUser()
    {
        return authenticationContext.getLoggedInUser();
    }

    public int getAction()
    {
        return actionId;
    }

    public void setAction(int action)
    {
        actionId = action;
    }

    public ActionDescriptor getActionDescriptor()
    {
        if (actionDescriptor == null)
        {
            try
            {
                actionDescriptor = workflowManager.getWorkflow(getIssue()).getDescriptor().getAction(actionId);
            }
            catch (WorkflowException e)
            {
                throw new IllegalArgumentException("Cannot find workflow transition with id '" + actionId + "'.");
            }
        }
        if (actionDescriptor == null)
            throw new IllegalArgumentException("No workflow action with id '" + actionId + "' available for issue " + getIssue().getKey());

        return actionDescriptor;
    }

    public void addErrorMessage(String error)
    {
        errorCollection.addErrorMessage(error);
    }

    public void addError(String name, String error)
    {
        errorCollection.addError(name, error);
    }

    public Map getAdditionalInputs()
    {
        final Map map = new HashMap(additionalInputs.size() + 3);
        map.putAll(additionalInputs);

        // Only supply fields that have been updated
        if (fieldUpdated(FIELD_COMMENT))
        {
            map.put(FIELD_COMMENT, getComment());
            map.put(FIELD_COMMENT_LEVEL, getCommentLevel());
            map.put(CommentSystemField.PARAM_ROLE_LEVEL, getCommentRoleLevel());
        }

        map.put("username", getUsername());

        return map;
    }

    public void addAdditionalInput(Object key, Object value)
    {
        additionalInputs.put(key, value);
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public ErrorCollection validate()
    {
        validateComment();

        if (errorCollection.hasAnyErrors())
            return errorCollection;

        for (FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(getIssue()))
                {
                    OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                    // JRA-16112 - This is a hack that is here because the resolution field is "special". You can not
                    // make the resolution field required and therefore by default the FieldLayoutItem for resolution
                    // returns false for the isRequired method. This is so that you can not make the resolution field
                    // required for issue creation. HOWEVER, whenever the resolution system field is shown it is
                    // required because the edit template does not provide a none option and indicates that it is
                    // required. THEREFORE, when the field is included on a transition screen we will do a special
                    // check to make the FieldLayoutItem claim it is required IF we run into the resolution field.
                    if (IssueFieldConstants.RESOLUTION.equals(orderableField.getId()))
                    {
                        fieldScreenRenderLayoutItem =
                                new FieldScreenRenderLayoutItemImpl(fieldScreenRenderLayoutItem.getFieldScreenLayoutItem(), fieldScreenRenderLayoutItem.getFieldLayoutItem())
                                {
                                    public boolean isRequired()
                                    {
                                        return true;
                                    }
                                };
                    }
                    orderableField.validateParams(getOperationContext(), errorCollection, authenticationContext.getI18nHelper(), getIssue(), fieldScreenRenderLayoutItem);
                }
            }
        }

        return errorCollection;
    }

    public FieldScreenRenderer getFieldScreenRenderer()
    {
        if (fieldScreenRenderer == null)
        {
            fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(getRemoteUser(), getIssue(), getActionDescriptor());
        }

        return fieldScreenRenderer;
    }


    private void validateComment()
    {
        // Check if we have a comment
        if (fieldUpdated(FIELD_COMMENT))
        {
            // If so if the user can comment on the issue
            if (permissionManager.hasPermission(Permissions.COMMENT_ISSUE, getProject(), getRemoteUser()))
            {
                commentService.isValidCommentData(getRemoteUser(), getIssue(), getCommentLevel(), getCommentRoleLevel(), errorCollection);
                if (!fieldUpdated(FIELD_COMMENT_LEVEL) && !fieldUpdated(FIELD_COMMENT_ROLE_LEVEL))
                {
                    setCommentLevel(null);
                }
            }
            else
            {
                errorCollection.addErrorMessage(authenticationContext.getI18nHelper().getText("admin.errors.user.does.not.have.permission", (getRemoteUser() != null ? authenticationContext.getI18nHelper().getText("admin.errors.user", "'" + getRemoteUser().getName() + "'") : authenticationContext.getI18nHelper().getText("admin.errors.anonymous.user"))));
            }
        }
    }

    private void setCommentLevel(String commentLevel)
    {
        params.put(FIELD_COMMENT_LEVEL, commentLevel);
    }

    public ErrorCollection progress()
    {
        // Only update issue if transition has a screen
        if (hasScreen())
        {
            for (FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer().getFieldScreenRenderTabs())
            {
                for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
                {
                    if (fieldScreenRenderLayoutItem.isShow(getIssue()))
                    {
                        fieldScreenRenderLayoutItem.getOrderableField().updateIssue(fieldScreenRenderLayoutItem.getFieldLayoutItem(), getIssue(), params);
                    }
                }
            }
        }

        workflowManager.doWorkflowAction(this);

        return errorCollection;
    }

    public void setParams(Map params)
    {
        this.params = params;
    }

    public boolean hasScreen()
    {
        return StringUtils.isNotBlank(getActionDescriptor().getView());
    }

    private boolean fieldUpdated(String fieldId)
    {
        return params.containsKey(fieldId);
    }

    private OperationContext getOperationContext()
    {
        return new OperationContextImpl(new WorkflowIssueOperationImpl(getActionDescriptor()), params);
    }
}
