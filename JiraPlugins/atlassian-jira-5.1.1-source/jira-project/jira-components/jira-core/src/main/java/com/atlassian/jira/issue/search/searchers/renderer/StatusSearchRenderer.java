package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.ConstantsComparator;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A search renderer for the status.
 *
 * @since v4.0
 */
public class StatusSearchRenderer extends IssueConstantsSearchRenderer<Status>
{
    static final Logger log = Logger.getLogger(IssueSearcher.class);

    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;

    public StatusSearchRenderer(String searcherNameKey, final ConstantsManager constantsManager,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine,
            final FieldVisibilityManager fieldVisibilityManager, final WorkflowManager workflowManager,
            final ProjectManager projectManager)
    {
        super(SystemSearchConstants.forStatus(), searcherNameKey, constantsManager, velocityRequestContextFactory,
                applicationProperties, templatingEngine, fieldVisibilityManager);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
    }

    public Collection<Status> getSelectListOptions(SearchContext searchContext)
    {
        Set<Status> uniqueStatus = new TreeSet<Status>(ConstantsComparator.COMPARATOR);
        List<Long> projectIds = searchContext.getProjectIds();
        if (projectIds == null || projectIds.isEmpty())
        {
            try
            {
                for (JiraWorkflow jiraWorkflow : workflowManager.getActiveWorkflows())
                {
                    uniqueStatus.addAll(jiraWorkflow.getLinkedStatusObjects());
                }
            }
            catch (WorkflowException e)
            {
                log.warn("Workflow exception occurred trying to get a workflow statuses. Returning all statuses", e);
                return constantsManager.getStatusObjects();
            }
        }
        else
        {

            List<String> issueTypeIds = searchContext.getIssueTypeIds();

            // If no issue type ids, then we want it all!
            if (issueTypeIds == null || issueTypeIds.isEmpty())
            {
                issueTypeIds = constantsManager.getAllIssueTypeIds();
            }

            for (Long projectId : projectIds)
            {
                if (projectManager.getProjectObj(projectId) != null)
                {
                    for (String issueTypeId : issueTypeIds)
                    {
                        try
                        {
                            JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
                            List<Status> linkedStatuses = workflow.getLinkedStatusObjects();
                            uniqueStatus.addAll(linkedStatuses);
                        }
                        catch (WorkflowException e)
                        {
                            log.warn("Workflow exception occurred trying to get a workflow with issuetype " + issueTypeId + " and projectId " + projectId, e);
                        }
                    }
                }
                else
                {
                    log.debug("Unable to find project with id " + projectId + " when trying to retrieve available statuses");
                }
            }
        }
        return uniqueStatus;
    }
}
