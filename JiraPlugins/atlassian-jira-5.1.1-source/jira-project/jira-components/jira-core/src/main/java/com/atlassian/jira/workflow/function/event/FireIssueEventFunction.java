/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.event;

import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class FireIssueEventFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(FireIssueEventFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        Long eventTypeId = null;
        Object object = args.get("eventTypeId");

        // AbstractWorkflow can pass the value as a string 
        if (object instanceof Long)
        {
            eventTypeId = (Long) args.get("eventTypeId");
        }
        else if (object instanceof String)
        {
            eventTypeId = new Long((String) object);
        }

        if (eventTypeId != null)
        {
            // this should fire events, and also look for comments and change logs
            Issue updatedIssue = (Issue) transientVars.get("issue");
            Comment comment = (Comment)transientVars.get("commentValue");
            GenericValue changeGroup = (GenericValue) transientVars.get("changeGroup");

            Map params = new HashMap(4);
            params.put("eventsource", IssueEventSource.WORKFLOW);

            // Check if mail notifications are disabled for bulk operations - default behaviour is to send mail
            boolean sendMail = true;
            if (transientVars.get("sendBulkNotification") != null)
                sendMail = ((Boolean)transientVars.get("sendBulkNotification")).booleanValue();

            IssueEventDispatcher.dispatchEvent(eventTypeId, updatedIssue, WorkflowUtil.getCaller(transientVars), comment, null, changeGroup, params, sendMail);
        }
    }

    public static FunctionDescriptor makeDescriptor(Long eventTypeId)
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", FireIssueEventFunction.class.getName());
        descriptor.getArgs().put("eventTypeId", eventTypeId);
        return descriptor;
    }
}
