package com.atlassian.jira.issue.transitions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Simple LinkFactory for generating Issue Transitions.
 *
 * @since v4.1
 */
public class TransitionLinkFactory implements SimpleLinkFactory
{
    private static final Logger log = Logger.getLogger(TransitionLinkFactory.class);
    private final WorkflowManager workflowManager;
    private final VelocityRequestContextFactory requestContextFactory;

    public TransitionLinkFactory(WorkflowManager workflowManager, VelocityRequestContextFactory requestContextFactory)
    {
        this.workflowManager = workflowManager;
        this.requestContextFactory = requestContextFactory;
    }


    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    public List<SimpleLink> getLinks(com.opensymphony.user.User user, Map<String, Object> params)
    {
        final Issue issue = (Issue) params.get("issue");
        return getAvailableActions(user, issue);
    }

    /**
     * Get available Workflow transitions for the current issue/current user
     *
     * @return a list containing all available actions for an issue.
     */
    private List<SimpleLink> getAvailableActions(User user, Issue issue)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final List<ActionDescriptor> actions = loadAvailableActions(user, issue);

        final List<SimpleLink> returnList = new ArrayList<SimpleLink>(actions.size());

        Collections.sort(actions, new Comparator<ActionDescriptor>(){
            public int compare(ActionDescriptor o1, ActionDescriptor o2)
            {
                return getSequenceFromAction(o1).compareTo(getSequenceFromAction(o2));
            }
        });

        for (ActionDescriptor action : actions)
        {

            final String url = requestContext.getBaseUrl() + "/secure/WorkflowUIDispatcher.jspa?"
                    + "id=" + issue.getId() + ""
                    + "&action=" + action.getId()
                    + "&atl_token=" + getXsrfToken();

            final String transitionDisplayName = getWorkflowTransitionDisplayName(action);
            final String description = getWorkflowTransitionDescription(action);
            returnList.add(new SimpleLinkImpl("action_id_" + action.getId(),
                    transitionDisplayName, (StringUtils.isBlank(description) ? null : transitionDisplayName + " - " + description),
                    null, "issueaction-workflow-transition", null, url, null));
        }
        return returnList;
    }

    private Integer getSequenceFromAction(ActionDescriptor action)
    {
        if (action == null)
        {
            return Integer.MAX_VALUE;
        }

        final Map metaAttributes = action.getMetaAttributes();
        if (metaAttributes == null)
        {
            return Integer.MAX_VALUE;
        }

        final String value = (String) metaAttributes.get("opsbar-sequence");

        if (value == null || StringUtils.isBlank(value) || !StringUtils.isNumeric(value))
        {
            return Integer.MAX_VALUE;
        }

        return Integer.valueOf(value);
    }

    private List<ActionDescriptor> loadAvailableActions(User user, Issue issueObject)
    {
        final Project project = issueObject.getProjectObject();
        final List<ActionDescriptor> availableActions = new ArrayList<ActionDescriptor>();

        if (issueObject.getWorkflowId() == null)
        {
            log.warn("!!! Issue " + issueObject.getKey() + " has no workflow ID !!! ");
            return availableActions;
        }

        try
        {
            final Workflow wf = workflowManager.makeWorkflow(user != null ? user.getName() : null);
            final WorkflowDescriptor wd = workflowManager.getWorkflow(issueObject).getDescriptor();

            final HashMap<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("pkey", project.getKey()); // Allows ${project.key} in condition args
            inputs.put("issue", issueObject);
            // The condition should examine the original issue object - put this in the transientvars
            // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, issueObject);
            int[] actionIds = wf.getAvailableActions(issueObject.getWorkflowId(), inputs);

            for (int actionId : actionIds)
            {
                final ActionDescriptor action = wd.getAction(actionId);
                if (action == null)
                {
                    log.error("State of issue [" + issueObject + "] has an action [id=" + actionId +
                            "] which cannot be found in the workflow descriptor");
                }
                else
                {
                    availableActions.add(action);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception thrown while gettig avilable actions", e);
        }

        return availableActions;
    }

    String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        return WorkflowUtil.getWorkflowTransitionDisplayName(descriptor);
    }

    String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return StringUtils.trimToNull(WorkflowUtil.getWorkflowTransitionDescription(descriptor));
    }

    String getXsrfToken()
    {
        final HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            return getXsrfTokenGenerator().generateToken(request);
        }
        return "";
    }

    XsrfTokenGenerator getXsrfTokenGenerator()
    {
        return ComponentManager.getComponentInstanceOfType(XsrfTokenGenerator.class);
    }

}
