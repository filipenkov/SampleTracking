package com.atlassian.jira.rest.v1.issues;

import com.atlassian.jira.issue.DocumentIssueImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.user.User;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * Rest end point for IssuePicker searching
 *
 * @since v4.0
 */
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
@Produces ({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class IssueActionsAndOperationsResource
{
    private static final Logger log = Logger.getLogger(IssueActionsAndOperationsResource.class);
    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final WorkflowManager workflowManager;
    private final I18nHelper i18n;

    private final String issueIdOrKey;
    private Issue issue;
    private final SimpleLinkManager simpleLinkManager;
    private final XsrfTokenGenerator xsrfTokenGenerator;

    public IssueActionsAndOperationsResource(
            final JiraAuthenticationContext authContext, final IssueManager issueManager,
            final I18nHelper.BeanFactory i18nBeanFactory, final WorkflowManager workflowManager,
            final String issueIdOrKey, final SimpleLinkManager simpleLinkManager,
            final XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.authContext = authContext;
        this.issueManager = issueManager;
        this.workflowManager = workflowManager;
        this.simpleLinkManager = simpleLinkManager;
        this.xsrfTokenGenerator = xsrfTokenGenerator;
        i18n = i18nBeanFactory.getInstance(authContext.getUser());
        this.issueIdOrKey = issueIdOrKey;
    }

    @GET
    public Response getActionsAndOperationsResponse(@Context HttpServletRequest request)
    {
        return getActionsAndOperations(request);
    }

    private Response getActionsAndOperations(HttpServletRequest request)
    {
        Response.ResponseBuilder responseBuilder;
        if (!isAuthenticated())
        {
            responseBuilder = Response.serverError().status(Response.Status.UNAUTHORIZED);
        }
        else
        {
            responseBuilder = Response.ok(new AvailableActionsAndOperationsWrapper(getIssue().getId().toString(),
                    getIssue().getKey(), i18n.getText("admin.issue.operations.view"), getCurrentAtlToken(request),
                    getAvailableActions(), getIssueOperations(request)));
        }
        return responseBuilder.cacheControl(NO_CACHE).build();
    }

    /**
     * Get available Workflow transitions for the current issue/current user
     *
     * @return a list containing all available actions for an issue.
     */
    private List<AvailableAction> getAvailableActions()
    {
        final Map<Integer, ActionDescriptor> actions = loadAvailableActions(getIssue());

        final List<AvailableAction> returnList = new ArrayList<AvailableAction>(actions.size());

        for (Map.Entry<Integer, ActionDescriptor> entry : actions.entrySet())
        {
            returnList.add(new AvailableAction(entry.getKey().toString(),
                    getWorkflowTransitionDisplayName(entry.getValue()), getWorkflowTransitionDescription(entry.getValue())));
        }
        return returnList;
    }

    private Map<Integer, ActionDescriptor> loadAvailableActions(Issue issueObject)
    {
        final Project project = issueObject.getProjectObject();
        final Map<Integer, ActionDescriptor> availableActions = new LinkedHashMap<Integer, ActionDescriptor>();
        final Issue originalIssue;

        if (issueObject instanceof DocumentIssueImpl)
        {
            issueObject = issueManager.getIssueObject(issueObject.getId());
            originalIssue = issueObject;

        }
        else
        {
            if (issueObject.getWorkflowId() == null)
            {
                log.warn("!!! Issue " + issueObject.getKey() + " has no workflow ID !!! ");
                return availableActions;
            }
            originalIssue = issueManager.getIssueObject(issueObject.getId());
        }

        try
        {
            final Workflow wf = getWorkflow();
            final WorkflowDescriptor wd = workflowManager.getWorkflow(issueObject).getDescriptor();

            final HashMap<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("pkey", project.getKey()); // Allows ${project.key} in condition args
            inputs.put("issue", issueObject);
            // The condition should examine the original issue object - put this in the transientvars
            // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssue);
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
                    availableActions.put(actionId, action);
                }
            }
        }
        catch (Exception e)
        {
            log.error("Exception thrown while gettig avilable actions", e);
        }

        return availableActions;
    }

    private Workflow getWorkflow()
    {
        final User user = authContext.getUser();
        return workflowManager.makeWorkflow(user != null ? user.getName() : null);
    }

    /**
     * Gets a list of available operations for the curernt issue/current user
     *
     * @return a List of Display Beans for Operations
     */
    private List<AvailableOperation> getIssueOperations(HttpServletRequest request)
    {
        final User currentUser = authContext.getUser();
        final List<AvailableOperation> operations = new ArrayList<AvailableOperation>();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("issue", getIssue());
        params.put("issueId", getIssue().getId());
        final JiraHelper helper = new JiraHelper(request, getIssue().getProjectObject(), params);
        final List<SimpleLinkSection> sections = simpleLinkManager.getSectionsForLocation("opsbar-operations", currentUser, helper);
        for (SimpleLinkSection section : sections)
        {
            final List<SimpleLink> links = simpleLinkManager.getLinksForSection(section.getId(), currentUser, helper);

            for (SimpleLink link : links)
            {
                final AvailableOperation operation = new AvailableOperation(link.getLabel(), link.getUrl(), link.getTitle(), link.getStyleClass());
                operations.add(operation);
            }

        }

        return operations;
    }

    private Issue getIssue()
    {
        if (issue == null)
        {
            try
            {
                issue = issueManager.getIssueObject(Long.valueOf(issueIdOrKey));
            }
            catch (NumberFormatException nfe)
            {
                issue = issueManager.getIssueObject(issueIdOrKey);
            }
        }
        return issue;
    }

    /**
     * Get the i18n'ed name of a workflow action (eg. 'Start Progress').
     *
     * @param descriptor Descriptor eg. from {@link com.atlassian.jira.workflow.JiraWorkflow#getDescriptor()}
     */
    private String getWorkflowTransitionDisplayName(final ActionDescriptor descriptor)
    {
        return WorkflowUtil.getWorkflowTransitionDisplayName(descriptor);
    }

    private String getWorkflowTransitionDescription(final ActionDescriptor descriptor)
    {
        return StringUtils.trimToNull(WorkflowUtil.getWorkflowTransitionDescription(descriptor));
    }


    private boolean isAuthenticated()
    {
        return authContext.getUser() != null;
    }

    private String getCurrentAtlToken(final HttpServletRequest httpRequest)
    {
        return xsrfTokenGenerator.generateToken(httpRequest);
    }

    @XmlRootElement
    public static class AvailableAction
    {
        @XmlElement
        private String action;
        @XmlElement
        private String name;
        @XmlElement
        private String desc;

        private AvailableAction()
        {
        }

        private AvailableAction(final String action, final String name, final String desc)
        {
            this.action = action;
            this.name = name;
            this.desc = desc;
        }
    }

    @XmlRootElement
    public static class AvailableOperation
    {
        @XmlElement
        private String name;
        @XmlElement
        private String url;
        @XmlElement
        private String desc;
        @XmlElement
        private String styleClass;

        private AvailableOperation()
        {
        }

        private AvailableOperation(final String name, final String url, final String desc, final String styleClass)
        {
            this.name = name;
            this.url = url;
            this.desc = desc;
            this.styleClass = styleClass;
        }
    }

    @XmlRootElement
    public static class AvailableActionsAndOperationsWrapper
    {
        @XmlElement
        private String id;
        @XmlElement
        private String key;
        @XmlElement
        private String viewIssue;
        @XmlElement
        private String atlToken;
        @XmlElement
        private List<AvailableAction> actions;
        @XmlElement
        private List<AvailableOperation> operations;

        private AvailableActionsAndOperationsWrapper()
        {
        }

        private AvailableActionsAndOperationsWrapper(final String id, final String key, final String viewIssue, final String atlToken, final List<AvailableAction> actions, final List<AvailableOperation> operations)
        {
            this.id = id;
            this.key = key;
            this.actions = actions;
            this.operations = operations;
            this.viewIssue = viewIssue;
            this.atlToken = atlToken;
        }
    }
}
