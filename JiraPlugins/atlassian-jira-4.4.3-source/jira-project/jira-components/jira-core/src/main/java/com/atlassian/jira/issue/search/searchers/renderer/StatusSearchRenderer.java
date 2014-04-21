package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comparator.ConstantsComparator;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
            final ApplicationProperties applicationProperties, final VelocityManager velocityManager,
            final FieldVisibilityManager fieldVisibilityManager, final WorkflowManager workflowManager,
            final ProjectManager projectManager)
    {
        super(SystemSearchConstants.forStatus(), searcherNameKey, constantsManager, velocityRequestContextFactory,
                applicationProperties, velocityManager, fieldVisibilityManager);
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
    }

    public Collection<Status> getSelectListOptions(SearchContext searchContext)
    {
        List projectIds = searchContext.getProjectIds();
        if (projectIds == null || projectIds.isEmpty())
        {
            try
            {
                Set uniqueStatusGv = new HashSet();
                Collection workflows = workflowManager.getActiveWorkflows();
                for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
                {
                    JiraWorkflow jiraWorkflow = (JiraWorkflow) iterator.next();
                    List statuses = jiraWorkflow.getLinkedStatuses();
                    uniqueStatusGv.addAll(statuses);
                }

                List sortedList = new ArrayList(uniqueStatusGv);
                Collections.sort(sortedList, ConstantsComparator.COMPARATOR);

                return constantsManager.convertToConstantObjects(ConstantsManager.STATUS_CONSTANT_TYPE, sortedList);
            }
            catch (WorkflowException e)
            {
                log.warn("Workflow exception occurred trying to get a workflow statuses. Returning all statuses", e);
                return constantsManager.getStatusObjects();
            }
        }
        else
        {
            Set uniqueStatusGv = new HashSet();
            List issueTypeIds = searchContext.getIssueTypeIds();

            // If no issue type ids, then we want it all!
            if (issueTypeIds == null || issueTypeIds.isEmpty())
            {
                issueTypeIds = constantsManager.getAllIssueTypeIds();
            }

            for (Iterator iterator = projectIds.iterator(); iterator.hasNext();)
            {
                Long projectId = (Long) iterator.next();
                if (projectManager.getProjectObj(projectId) != null)
                {
                    for (Iterator iterator1 = issueTypeIds.iterator(); iterator1.hasNext();)
                    {
                        String issueTypeId =  (String) iterator1.next();
                        try
                        {
                            JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
                            List linkedStatusGvs = workflow.getLinkedStatuses();
                            uniqueStatusGv.addAll(linkedStatusGvs);
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

            // Sort the found issue types
            List sortedList = new ArrayList(uniqueStatusGv);
            Collections.sort(sortedList, ConstantsComparator.COMPARATOR);

            return constantsManager.convertToConstantObjects(ConstantsManager.STATUS_CONSTANT_TYPE, sortedList);
        }
    }
}
