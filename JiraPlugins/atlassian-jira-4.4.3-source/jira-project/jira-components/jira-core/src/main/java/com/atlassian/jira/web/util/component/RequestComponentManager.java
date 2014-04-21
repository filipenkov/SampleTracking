/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util.component;

import com.atlassian.jira.config.component.ProfilingComponentAdapterFactory;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.DefaultPicoContainer;
import webwork.action.ServletActionContext;
import webwork.action.factory.SessionMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

public class RequestComponentManager
{
    private static final Logger log = Logger.getLogger(RequestComponentManager.class);

    /**
     * Get a container that contains the 'request' level information.  This container should only be used on a
     * per-request basis (ie destroyed after the request).  Any attempt to use this container after the
     * end of the request will result in indeterminate behaviour.
     * <p>
     * So far - the following components are registered:
     * <ul>
     * <li>{@link SessionMap}
     * </ul>
     *
     * @return A Pico container that contains the 'request' level information.
     */
    public DefaultPicoContainer getContainer(PicoContainer parent)
    {
        //register any request level components that the webwork actions needs to use.
        try
        {
            HttpServletRequest request = ServletActionContext.getRequest();

            if (request != null)
            {
                return injectWorkflow(parent, request);
            }
        }
        catch (PicoRegistrationException e)
        {
            log.error(e, e);
            if (e.getCause() != null)
            {
                log.error("Cause: " + e.getCause(), e.getCause());
            }
            throw e;
        }
        catch (Exception e)
        {
            // We need to catch this exception, as there are a lot of times
            log.error(e, e);

            //do nothing
        }
        return new DefaultPicoContainer(new ProfilingComponentAdapterFactory(), parent);
    }

    DefaultPicoContainer injectWorkflow(PicoContainer parent, HttpServletRequest request)
    {
        DefaultPicoContainer requestContainer = new DefaultPicoContainer(new ProfilingComponentAdapterFactory(), parent);
        // Extract the workflow name from the request parameters
        String workflowName = request.getParameter("workflowName");
        if (StringUtils.isEmpty(workflowName))
        {
            // nothing to do
            return requestContainer;
        }
        // Check if we want to edit the draft copy of an active workflow, or view/edit the "live" copy.
        // We will strictly expect a valid value for the "workflowMode" parameter, so that when it is missing we know
        // we have a page that hasn't been updated to handle draft workflows yet.
        String workflowMode = request.getParameter("workflowMode");
        if (workflowMode == null)
        {
            throw new IllegalStateException("Found a 'workflow' in the request parameters, but there is no "
                                            + "'workflowMode'. " + getFullRequestUrl(request));
        }
        JiraWorkflow workflow = getWorkflow(parent, workflowMode, workflowName, request);
        if (workflow == null)
        {
            throw new IllegalStateException("No " + workflowMode + " workflow was found for '" + workflowName + "'.");
        }

        requestContainer.registerComponentInstance(workflow);

        String workflowStep = request.getParameter("workflowStep");

        if (TextUtils.stringSet(workflowStep))
        {
            StepDescriptor step = workflow.getDescriptor().getStep(Integer.parseInt(workflowStep));

            if (step == null)
            {
                log.warn("No workflow step found for '" + workflowStep + "'");
                // We should return now to avoid NullPointerExceptions
                return requestContainer;
            }
            requestContainer.registerComponentInstance(step);

            String workflowTransition = request.getParameter("workflowTransition");

            if (TextUtils.stringSet(workflowTransition))
            {
                ActionDescriptor transition = step.getAction(Integer.parseInt(workflowTransition));

                if (transition != null)
                {
                    requestContainer.registerComponentInstance(transition);
                }
            }
        }
        else
        {
            // If step id is not supplied but the workflowTransition id is given, then
            // this must be a global action.
            String globalWorkflowTransition = request.getParameter("workflowTransition");
            if (TextUtils.stringSet(globalWorkflowTransition))
            {
                boolean actionFound = false;

                final List globalActions = workflow.getDescriptor().getGlobalActions();
                int globalActionId = Integer.parseInt(globalWorkflowTransition);
                for (Iterator iterator = globalActions.iterator(); iterator.hasNext();)
                {
                    ActionDescriptor globalAction = (ActionDescriptor) iterator.next();
                    if (globalAction.getId() == globalActionId)
                    {
                        requestContainer.registerComponentInstance(globalAction);
                        actionFound = true;
                    }
                }

                if (!actionFound) // look in initial actions if we didn't find it yet
                {
                    final List initialActions = workflow.getDescriptor().getInitialActions();
                    int initialActionId = Integer.parseInt(globalWorkflowTransition);
                    for (Iterator iterator = initialActions.iterator(); iterator.hasNext();)
                    {
                        ActionDescriptor initialAction = (ActionDescriptor) iterator.next();
                        if (initialAction.getId() == initialActionId)
                        {
                            requestContainer.registerComponentInstance(initialAction);
                            actionFound = true;
                        }
                    }
                }

                if (!actionFound)
                {
                    log.error("Could not find any actions matching this workflow transition: " + globalWorkflowTransition);
                }
            }
        }
        return requestContainer;
    }

    private JiraWorkflow getWorkflow(PicoContainer parent, String workflowMode, String workflowName, HttpServletRequest request)
    {
        // Use the WorkflowManager to get the required workflow
        WorkflowManager workflowManager = (WorkflowManager) parent.getComponentInstanceOfType(WorkflowManager.class);
        JiraWorkflow workflow;
        if (workflowMode.equals(JiraWorkflow.LIVE))
        {
            // get the "live" workflow
            workflow = workflowManager.getWorkflowClone(workflowName);
        }
        else if (workflowMode.equals(JiraWorkflow.DRAFT))
        {
            // get the draft workflow
            workflow = workflowManager.getDraftWorkflow(workflowName);
        }
        else
        {
            throw new IllegalStateException("Invalid workflow mode '" + workflowMode + "'. " + getFullRequestUrl(request));
        }

        return workflow;
    }

    private String getFullRequestUrl(HttpServletRequest request)
    {
        StringBuffer url = new StringBuffer(request.getRequestURL().toString());
        if (!StringUtils.isEmpty(request.getQueryString()))
        {
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }
}
