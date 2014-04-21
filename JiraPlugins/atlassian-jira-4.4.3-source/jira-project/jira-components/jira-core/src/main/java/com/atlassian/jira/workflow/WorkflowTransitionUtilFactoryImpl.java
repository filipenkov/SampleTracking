package com.atlassian.jira.workflow;

import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;

/**
 * @since v4.4
 */
public class WorkflowTransitionUtilFactoryImpl implements WorkflowTransitionUtilFactory
{
    private final JiraAuthenticationContext authenticationContext;
    private final WorkflowManager workflowManager;
    private final PermissionManager permissionManager;
    private final FieldScreenRendererFactory fieldScreenRendererFactory;
    private final CommentService commentService;

    public WorkflowTransitionUtilFactoryImpl(JiraAuthenticationContext authenticationContext, WorkflowManager workflowManager,
            PermissionManager permissionManager, FieldScreenRendererFactory fieldScreenRendererFactory, CommentService commentService)
    {
        this.authenticationContext = authenticationContext;
        this.workflowManager = workflowManager;
        this.permissionManager = permissionManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.commentService = commentService;
    }


    @Override
    public WorkflowTransitionUtil create()
    {
        return new WorkflowTransitionUtilImpl(authenticationContext, workflowManager, permissionManager, fieldScreenRendererFactory, commentService);
    }
}
