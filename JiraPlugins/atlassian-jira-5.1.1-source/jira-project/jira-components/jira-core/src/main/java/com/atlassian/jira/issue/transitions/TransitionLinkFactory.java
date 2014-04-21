package com.atlassian.jira.issue.transitions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.workflow.IssueWorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
    private final VelocityRequestContextFactory requestContextFactory;
    private final IssueWorkflowManager issueWorkflowManager;

    public TransitionLinkFactory(VelocityRequestContextFactory requestContextFactory, IssueWorkflowManager issueWorkflowManager)
    {
        this.requestContextFactory = requestContextFactory;
        this.issueWorkflowManager = issueWorkflowManager;
    }


    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
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
        final List<ActionDescriptor> actions = issueWorkflowManager.getSortedAvailableActions(issue);

        final List<SimpleLink> returnList = new ArrayList<SimpleLink>(actions.size());

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
