/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class VersionWorkloadReport extends AbstractReport
{
    private static final Logger log = Logger.getLogger(VersionWorkloadReport.class);

    private final VersionManager versionManager;
    private final SearchProvider searchProvider;
    private final ConstantsManager constantsManager;
    private final ApplicationProperties applicationProperties;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final JiraDurationUtils jiraDurationUtils;

    Map user2FullNames = null;
    Map user2Issues = null;
    Map user2RemainingTime = null;
    Map userSummaryTotals = null;
    Map issueTypeTotals = null;
    long grandTotal = 0L;

    public VersionWorkloadReport(VersionManager versionManager, SearchProvider searchProvider, ConstantsManager constantsManager,
            ApplicationProperties applicationProperties, IssueTypeSchemeManager issueTypeSchemeManager,
            JiraDurationUtils jiraDurationUtils)
    {
        this.versionManager = versionManager;
        this.searchProvider = searchProvider;
        this.constantsManager = constantsManager;
        this.applicationProperties = applicationProperties;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.jiraDurationUtils = jiraDurationUtils;
        user2RemainingTime = new HashMap();
        userSummaryTotals = new HashMap();
        issueTypeTotals = new HashMap();
        user2Issues = new HashMap();
        user2FullNames = new HashMap();
    }

    public boolean showReport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        User remoteUser = action.getRemoteUser();
        String displayUnknown = (String) reqParams.get("displayUnknown");
        String versionId = (String) reqParams.get("versionId");

        String subtaskInclusion = (String) reqParams.get("subtaskInclusion");
        Collection issues = loadIssues(remoteUser, versionId, subtaskInclusion);
        // All the 'work' is done in the following call to build the required Map's used in the display
        calculateSummaryTotals(issues);
        Collection assigneeUserNames = getAssigneeUserNames();
        Version version = versionManager.getVersion(new Long(versionId));
        TextUtils textUtils = new TextUtils();

        Map velocityParams = new HashMap();
        velocityParams.put("report", this);
        velocityParams.put("action", action);
        velocityParams.put("version", version);
        velocityParams.put("displayUnknown", displayUnknown);
        velocityParams.put("issueTypes", loadIssueTypes(action.getSelectedProjectObject()));
        velocityParams.put("assigneeUserNames", assigneeUserNames);
        velocityParams.put("textUtils", textUtils);
        velocityParams.put("permissionCheck", new PermissionCheckBean());
        velocityParams.put("subtasksEnabled", Boolean.valueOf(new SubTasksEnabledCondition().isEnabled()));
        velocityParams.put("subtaskDescription", SubTaskIncludeValuesGenerator.Options.getDescription(subtaskInclusion, getI18nBean()));

        return descriptor.getHtml("view", velocityParams);
    }

    public String getTotalTimeForUserByIssueType(String user, String type)
    {
        Long time = null;
        if (userSummaryTotals != null)
        {
            Map userTotals = (Map) userSummaryTotals.get(user);
            time = (Long) userTotals.get(type);
        }
        return getNiceTimeDuration(time);
    }

    public String getGrandTotal()
    {
        return getNiceTimeDuration(new Long(grandTotal));
    }

    public void validate(ProjectActionSupport action, Map params)
    {
        super.validate(action, params);
        String versionId = (String) params.get("versionId");
        if (!TextUtils.stringSet(versionId))
        {
            action.addError("versionId", getI18nBean().getText("report.versionworkload.version.is.required"));
        }
        else
        {
            try
            {
                if (versionId.equals("-1") || versionId.equals("-2"))
                {
                    action.addError("versionId", getI18nBean().getText("report.versionworkload.please.select.an.actual.version"));
                }
                else if (versionManager.getVersion(new Long(versionId)) == null)
                {
                    action.addError("versionId", getI18nBean().getText("report.versionworkload.version.does.not.exist"));
                }
            }
            catch (Exception e)
            {
                action.addError("versionId", getI18nBean().getText("report.versionworkload.version.does.not.exist"));
            }
        }
    }

    List loadIssues(User remoteUser, String versionId, String subtaskInclusion) throws Exception
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        final JqlClauseBuilder whereBuilder = queryBuilder.where().unresolved();

        if (versionId != null)
        {
            whereBuilder.and().fixVersion(versionId);
        }
        queryBuilder.orderBy().priority(SortOrder.DESC);

        final List issues = searchProvider.search(queryBuilder.buildQuery(), remoteUser, PagerFilter.getUnlimitedFilter()).getIssues();
        final ListOrderedSet result = new ListOrderedSet();
        result.addAll(issues);
        final List subTasks = new SubTaskFetcher(searchProvider).getSubTasks(remoteUser, issues, subtaskInclusion, true);
        result.addAll(subTasks);
        return Collections.unmodifiableList(result.asList());
    }

    /**
     * Returns a collection of {@link com.atlassian.jira.issue.issuetype.IssueType}'s associated with the
     * project.
     *
     * @param project currently selected {@link com.atlassian.jira.project.Project}
     * @return Collection of {@link com.atlassian.jira.issue.issuetype.IssueType}'s
     */
    protected List loadIssueTypes(Project project)
    {
        return new ArrayList(issueTypeSchemeManager.getIssueTypesForProject(project));
    }

    protected void calculateSummaryTotals(Collection issues)
    {
        String unassignedKey = getI18nBean().getText("common.concepts.unassigned");

        for (Iterator iterator = issues.iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();

            // get user from issue
            User assigneeUser = issue.getAssignee();
            String assignee;
            if (assigneeUser != null)
            {
                assignee = assigneeUser.getName();
                if (user2FullNames.get(assignee) == null)
                {
                    user2FullNames.put(assignee, assigneeUser.getDisplayName());
                }
            }
            else
            {
                assignee = unassignedKey;
            }

            if (userSummaryTotals.get(assignee) == null)
            {
                userSummaryTotals.put(assignee, new HashMap());
            }

            // get the issue type for the issue
            String issueTypeId = issue.getIssueTypeObject().getId();

            // get the time estimate for the current issue
            Long issueEstimate = issue.getEstimate();

            // While we are iterating over the issues calculate the IssueType 'Total' and Grand Total for the summary table
            calculateTotalTime(issueTypeId, issueEstimate);

            // Calculate the user totals for the summary table
            calcuateUserTotalTime(assignee, issueEstimate);

            // Calculate the total time for the users IssueType
            calculateUserTypeTotalTime(assignee, issueTypeId, issueEstimate);

            // For efficiency build the user to issues map here while iterating over the issues
            buildUser2Issues(issue, assignee, unassignedKey);
        }
    }

    private void calculateUserTypeTotalTime(String assignee, String issueType, Long issueEstimate)
    {
        Map userTotalMap = (Map) userSummaryTotals.get(assignee);
        if (userTotalMap.get(issueType) == null)
        {
            // put the value in the map
            if (issueEstimate != null)
            {
                userTotalMap.put(issueType, issueEstimate);
            }
        }
        else
        {
            // get out the value and update the time
            Long estimate = (Long) userTotalMap.get(issueType);
            // add current estimate
            if (estimate != null && issueEstimate != null)
            {
                Long newEstimate = new Long(estimate.longValue() + issueEstimate.longValue());
                userTotalMap.put(issueType, newEstimate);
            }
        }
    }

    private void calcuateUserTotalTime(String assignee, Long issueEstimate)
    {
        Long lRemainingTime = (Long) user2RemainingTime.get(assignee);

        if (issueEstimate != null)
        {
            if (lRemainingTime == null)
            {
                lRemainingTime = new Long(0);
            }
            long remainingTime = lRemainingTime.longValue();
            remainingTime += issueEstimate.longValue();
            user2RemainingTime.put(assignee, new Long(remainingTime));
        }
    }

    private void calculateTotalTime(String issueType, Long issueEstimate)
    {
        // Does the issue type already exist?
        Long currentTypeTotal = (Long) issueTypeTotals.get(issueType);
        if (currentTypeTotal != null && issueEstimate != null)
        {
            // Add the new time estimate to the Total
            long newTypeTotal = currentTypeTotal.longValue() + issueEstimate.longValue();
            // Put this new value in the map for the issue type
            issueTypeTotals.put(issueType, new Long(newTypeTotal));
        }
        else if (issueEstimate != null)
        {
            // Put the issue type and estimate
            issueTypeTotals.put(issueType, issueEstimate);
        }

        // Finally add the issue estimate total to the grandtotal
        if (issueEstimate != null)
        {
            grandTotal += issueEstimate.longValue();
        }
    }

    private void buildUser2Issues(Issue issue, String assignee, String unassignedKey)
    {
        List unassignedList = (List) user2Issues.get(unassignedKey);
        if (assignee == null)
        {
            if (unassignedList == null)
            {
                unassignedList = new ArrayList();
                user2Issues.put(unassignedKey, unassignedList);
            }
            unassignedList.add(issue);
        }
        else
        {
            List assigneeList = (List) user2Issues.get(assignee);
            if (assigneeList == null)
            {
                assigneeList = new ArrayList();
                user2Issues.put(assignee, assigneeList);
            }
            assigneeList.add(issue);
        }
    }

    private Collection getAssigneeUserNames()
    {
        List result = new ArrayList(user2Issues.keySet());
        Collections.sort(result, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                String l1 = (String) o1;
                String l2 = (String) o2;

                // Unassigned issues are shown last in the list
                if (l1.equals(getI18nBean().getText("common.concepts.unassigned")))
                {
                    return 1;
                }
                if (l2.equals(getI18nBean().getText("common.concepts.unassigned")))
                {
                    return -1;
                }
                else
                {
                    User user1;
                    try
                    {
                        user1 = UserUtils.getUser(l1);
                    }
                    catch (EntityNotFoundException e)
                    {
                        String errMsg = "Could not find user " + l1;
                        log.error(errMsg, e);
                        throw new IllegalArgumentException(errMsg + ": " + e);
                    }
                    User user2;
                    try
                    {
                        user2 = UserUtils.getUser(l2);
                    }
                    catch (EntityNotFoundException e)
                    {
                        String errMsg = "Could not find user " + l2;
                        log.error(errMsg, e);
                        throw new IllegalArgumentException(errMsg + ": " + e);
                    }
                    return user1.getDisplayName().compareTo(user2.getDisplayName());
                }
            }
        });
        return result;
    }

    private String getNiceTimeDuration(long duration)
    {
        return jiraDurationUtils.getFormattedDuration(new Long(duration), descriptor.getI18nBean().getLocale());
    }

    private String getNiceTimeDuration(Long duration)
    {
        if (duration == null)
        {
            return getI18nBean().getText("report.versionworkload.no.estimate");
        }
        return getNiceTimeDuration(duration.longValue());
    }

    private I18nHelper getI18nBean()
    {
        return getDescriptor().getI18nBean();
    }

    //-- Helper methods used by velocity

    public Collection getIssuesByType(String user, String typeId)
    {
        ArrayList result = new ArrayList();
        for (Iterator i = ((Collection) user2Issues.get(user)).iterator(); i.hasNext();)
        {
            Issue issue = (Issue) i.next();
            if (issue.getIssueTypeObject().getId().equals(typeId))
            {
                result.add(issue);
            }
        }

        return result;
    }

    public String getRemainingTime(String assignee)
    {
        Long time = (Long) user2RemainingTime.get(assignee);
        return getNiceTimeDuration(time);
    }

    public String getIssueTypeTotal(String issueType)
    {
        Long time = (Long) issueTypeTotals.get(issueType);
        return getNiceTimeDuration(time);
    }

    public String getNiceTimeEstimateDuration(Issue issue)
    {
        return getNiceTimeDuration(issue.getEstimate());
    }

    public String getAssigneeFullName(String username)
    {
        String userFullName = (String) user2FullNames.get(username);
        return userFullName == null ? username : userFullName;
    }
}
