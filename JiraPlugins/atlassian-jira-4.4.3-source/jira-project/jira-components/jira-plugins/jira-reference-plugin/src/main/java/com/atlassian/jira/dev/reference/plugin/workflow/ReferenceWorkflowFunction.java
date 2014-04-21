/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.dev.reference.plugin.workflow;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowContext;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Creates a reference comment on the transitioned issue.
 *
 */
public class ReferenceWorkflowFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(ReferenceWorkflowFunction.class);

    private final CommentManager commentManager;

    public ReferenceWorkflowFunction(CommentManager commentManager)
    {
        this.commentManager = commentManager;
    }

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        try
        {
            WorkflowContext ctx = getContext(transientVars);
            Issue issue = getIssue(transientVars);
            String newComment = "[ReferenceWorkflowFunction] transition ID " + getActionId(transientVars)
                    + " of issue " + issue.getKey() + " by " + ctx.getCaller();
            Comment comment = commentManager.create(issue, ctx.getCaller(), newComment, null, null, false);
            transientVars.put("commentValue", comment);
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
        }
    }

    private WorkflowContext getContext(Map transientVars)
    {
        return (WorkflowContext) transientVars.get("context");
    }

    private Issue getIssue(Map transientVars)
    {
        return (Issue) transientVars.get("issue");
    }

    private Integer getActionId(Map transientVars)
    {
        return (Integer) transientVars.get("actionId");
    }

}
