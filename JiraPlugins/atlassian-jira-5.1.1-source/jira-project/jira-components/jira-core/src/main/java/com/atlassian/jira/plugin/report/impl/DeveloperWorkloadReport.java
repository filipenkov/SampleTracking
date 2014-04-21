/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 2, 2004
 * Time: 8:18:19 PM
 */
package com.atlassian.jira.plugin.report.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeveloperWorkloadReport extends AbstractReport
{
    private final Logger log = Logger.getLogger(DeveloperWorkloadReport.class);

    private final ProjectManager projectManager;
    private final ApplicationProperties applicationProperties;
    private final JiraDurationUtils jiraDurationUtils;
    private final SearchProvider searchProvider;

    public DeveloperWorkloadReport(ProjectManager projectManager, ApplicationProperties applicationProperties, JiraDurationUtils jiraDurationUtils)
    {
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
        this.jiraDurationUtils = jiraDurationUtils;
        searchProvider = ComponentManager.getInstance().getSearchProvider();
    }

    public boolean showReport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    public void validate(ProjectActionSupport action, Map params)
    {
        super.validate(action, params);
        String developer = (String) params.get("developer");
        if (!TextUtils.stringSet(developer))
        {
            action.addError("developer", action.getText("report.developerworkload.developer.is.required"));
        }
        else
        {
            if (!UserUtils.userExists(developer))
            {
                action.addError("developer", action.getText("report.developerworkload.developer.does.not.exist"));
            }
        }
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        User remoteUser = action.getLoggedInUser();
        User developer = UserUtils.getUser((String) reqParams.get("developer"));
        String subtaskInclusion = (String) reqParams.get("subtaskInclusion");
        if (subtaskInclusion == null)
        {
            // default to behaviour as before this option was added to support legacy URLs            
            subtaskInclusion = UserSubTaskIncludeValuesGenerator.Options.ONLY_ASSIGNED;
        }
        List<Issue> result = new ArrayList<Issue>();
        {
            List<Issue> assignedIssues = initAssignedIssues(remoteUser, developer);
            result.addAll(assignedIssues);
        }
        {
            List<Issue> subTasks = new SubTaskFetcher(searchProvider).getSubTasksForUser(remoteUser, result, subtaskInclusion, true);
            result.addAll(subTasks);
        }
        Map countMap = initCountMap(result);
        Map workloadMap = initWorkloadMap(result);

        Map velocityParams = new HashMap();
        velocityParams.put("report", this);
        velocityParams.put("action", action);
        velocityParams.put("developer", developer);
        velocityParams.put("assignedIssues", result);
        velocityParams.put("countMap", countMap);
        velocityParams.put("workloadMap", workloadMap);
        velocityParams.put("totalCount", getTotalIssuesCount(countMap));
        velocityParams.put("totalWorkload", getTotalWorkload(workloadMap));
        return descriptor.getHtml("view", velocityParams);
    }

    /**
     * this formatting function is shared by the full view for navigator as well as view issue.
     *
     * @param v duration in seconds
     *
     * @return formatted duration
     */
    public String formatPrettyDuration(Long v)
    {
        return jiraDurationUtils.getFormattedDuration(v, descriptor.getI18nBean().getLocale());
    }

    /**
     * Retrieves the list of the issues that are currently assigned to the specified developer issue must be unresolved.
     * Never returns null.
     *
     * @param remoteUser current user
     * @param developer  user to find the assigned issues for
     *
     * @return list of issues
     */
    List<Issue> initAssignedIssues(User remoteUser, User developer)
    {
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where().assigneeUser(developer.getName()).and().unresolved();
        try
        {
            SearchResults searchResults = searchProvider.search(builder.buildQuery(), remoteUser, PagerFilter.getUnlimitedFilter());
            return searchResults.getIssues();
        }
        catch (SearchException e)
        {
            log.error("Error executing Search Request in DeveloperWorkloadReport (remoteUser=" + remoteUser + ", developer=" + developer + "): " + e, e);
        }
        return Collections.emptyList();
    }

    Map initCountMap(List<Issue> assignedIssues)
    {
        Map countMap = new HashMap();

        for (final Issue assignedIssue : assignedIssues)
        {
            Long estimate = assignedIssue.getEstimate();

            if (estimate == null)
            {
                continue; // drop issues that have not been assigned an estimate
            }

            //TODO do we have to convert to a string here?
            String pid = String.valueOf(assignedIssue.getProjectObject().getId());
            if (countMap.containsKey(pid))
            {
                long current = ((Long) countMap.get(pid)).longValue();
                countMap.put(pid, new Long(current + 1));
            }
            else
            {
                countMap.put(pid, new Long(1));
            }
        }

        return countMap;
    }

    Map initWorkloadMap(List<Issue> assignedIssues)
    {
        Map workloadMap = new HashMap();

        for (final Issue assignedIssue : assignedIssues)
        {
            String pid = String.valueOf(assignedIssue.getProjectObject().getId());
            Long estimate = assignedIssue.getEstimate();

            if (estimate == null)
            {
                continue; // drop issues that have not been assigned an estimate
            }

            if (workloadMap.containsKey(pid))
            {
                long currentEstimate = ((Long) workloadMap.get(pid)).longValue();
                workloadMap.put(pid, new Long(currentEstimate + estimate.longValue()));
            }
            else
            {
                workloadMap.put(pid, estimate);
            }
        }

        return workloadMap;
    }

    public Long getTotalIssuesCount(Map countMap)
    {
        long total = 0;
        for (Iterator i = countMap.values().iterator(); i.hasNext();)
        {
            Long count = (Long) i.next();
            total += count.longValue();
        }
        return new Long(total);
    }

    public Long getTotalWorkload(Map workloadMap)
    {
        long totalWorkload = 0;
        for (Iterator i = workloadMap.values().iterator(); i.hasNext();)
        {
            Long workload = (Long) i.next();
            totalWorkload += workload.longValue();
        }
        return new Long(totalWorkload);
    }

    public String getProjectName(String pid)
    {
        return projectManager.getProjectObj(new Long(pid)).getName();
    }
}
