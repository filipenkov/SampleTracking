/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.plugin.report.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VersionWorkloadReport extends AbstractReport
{
    private static final Logger log = Logger.getLogger(VersionWorkloadReport.class);

    private final VersionManager versionManager;
    private final SearchProvider searchProvider;
    private final ApplicationProperties applicationProperties;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final UserManager userManager;

    // comes in via request parameters and gets set during generateReportHtml. Is used in methods
    // called by velocity.
    private String displayUnknown;

    final Map<String, String> user2FullNames = new HashMap<String, String>();
    final Map<String, List<Issue>> user2Issues = new HashMap<String, List<Issue>>();
    final Map<String, Long> user2RemainingTime = new HashMap<String, Long>();
    final Map<String, Map<String, Long>> userSummaryTotals = new HashMap<String, Map<String, Long>>();
    final Map<String, Long> issueTypeTotals = new HashMap<String, Long>();
    long grandTotal = 0L;

    public VersionWorkloadReport(final VersionManager versionManager, final SearchProvider searchProvider,
            final ConstantsManager constantsManager,
            final ApplicationProperties applicationProperties,
            final IssueTypeSchemeManager issueTypeSchemeManager,
            final JiraDurationUtils jiraDurationUtils, UserManager userManager)
    {
        this.versionManager = versionManager;
        this.searchProvider = searchProvider;
        this.applicationProperties = applicationProperties;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.jiraDurationUtils = jiraDurationUtils;
        this.userManager = userManager;
    }

    public boolean showReport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    public String generateReportHtml(ProjectActionSupport action, Map reqParams) throws Exception
    {
        final User remoteUser = action.getLoggedInUser();
        this.displayUnknown = (String) reqParams.get("displayUnknown");
        final String versionId = (String) reqParams.get("versionId");

        final String subtaskInclusion = (String) reqParams.get("subtaskInclusion");
        final Collection<Issue> issues = loadIssues(remoteUser, versionId, subtaskInclusion);
        // All the 'work' is done in the following call to build the required Map's used in the display
        calculateSummaryTotals(issues);
        final Collection assigneeUserNames = getAssigneeUserNames();
        final Version version = versionManager.getVersion(new Long(versionId));
        final TextUtils textUtils = new TextUtils();

        final Map<String, Object> velocityParams = new HashMap<String, Object>();
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
            final Map<String, Long> userTotals = userSummaryTotals.get(user);
            time = userTotals.get(type);
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
        final String versionId = (String) params.get("versionId");
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

    List<Issue> loadIssues(User remoteUser, String versionId, String subtaskInclusion) throws Exception
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        final JqlClauseBuilder whereBuilder = queryBuilder.where().unresolved();

        if (versionId != null)
        {
            whereBuilder.and().fixVersion(versionId);
        }
        queryBuilder.orderBy().priority(SortOrder.DESC);

        final List<Issue> issues = searchProvider.search(queryBuilder.buildQuery(), remoteUser, PagerFilter.getUnlimitedFilter()).getIssues();
        final ListOrderedSet result = new ListOrderedSet();
        result.addAll(issues);
        final List<Issue> subTasks = new SubTaskFetcher(searchProvider).getSubTasks(remoteUser, issues, subtaskInclusion, true);
        Collections.sort(subTasks, new Comparator<Issue>()
        {
            @Override
            public int compare(Issue issue, Issue issue1)
            {
                return issue.getCreated().compareTo(issue1.getCreated());
            }
        });
        result.addAll(subTasks);
        return Collections.<Issue>unmodifiableList(result.asList());
    }

    /**
     * Returns a collection of {@link com.atlassian.jira.issue.issuetype.IssueType}'s associated with the
     * project.
     *
     * @param project currently selected {@link com.atlassian.jira.project.Project}
     * @return Collection of {@link com.atlassian.jira.issue.issuetype.IssueType}'s
     */
    protected List<IssueType> loadIssueTypes(Project project)
    {
        return new ArrayList<IssueType>(issueTypeSchemeManager.getIssueTypesForProject(project));
    }

    protected void calculateSummaryTotals(Collection<Issue> issues)
    {
        String unassignedKey = getI18nBean().getText("common.concepts.unassigned");

        for (final Issue issue : issues)
        {
            // get user from issue
            final User assigneeUser = issue.getAssignee();
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
                userSummaryTotals.put(assignee, new HashMap<String, Long>());
            }

            // get the issue type for the issue
            final String issueTypeId = issue.getIssueTypeObject().getId();

            // get the time estimate for the current issue
            final Long issueEstimate = issue.getEstimate();

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
        final Map<String, Long> userTotalMap = userSummaryTotals.get(assignee);
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
            final Long estimate = userTotalMap.get(issueType);
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
        Long lRemainingTime = user2RemainingTime.get(assignee);

        if (issueEstimate != null)
        {
            if (lRemainingTime == null)
            {
                lRemainingTime = 0L;
            }
            long remainingTime = lRemainingTime.longValue();
            remainingTime += issueEstimate.longValue();
            user2RemainingTime.put(assignee, new Long(remainingTime));
        }
    }

    private void calculateTotalTime(String issueType, Long issueEstimate)
    {
        // Does the issue type already exist?
        final Long currentTypeTotal = issueTypeTotals.get(issueType);
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
        List<Issue> unassignedList = user2Issues.get(unassignedKey);
        if (assignee == null)
        {
            if (unassignedList == null)
            {
                unassignedList = new ArrayList<Issue>();
                user2Issues.put(unassignedKey, unassignedList);
            }
            unassignedList.add(issue);
        }
        else
        {
            List<Issue> assigneeList = user2Issues.get(assignee);
            if (assigneeList == null)
            {
                assigneeList = new ArrayList<Issue>();
                user2Issues.put(assignee, assigneeList);
            }
            assigneeList.add(issue);
        }
    }

    private Collection getAssigneeUserNames()
    {
        final List<String> result = new ArrayList<String>(user2Issues.keySet());
        Collections.sort(result, new Comparator<String>()
        {
            public int compare(final String l1, final String l2)
            {
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
                    User user1 = userManager.getUserEvenWhenUnknown(l1);
                    if (user1 == null)
                    {
                        String errMsg = "Could not find user " + l1;
                        throw new IllegalArgumentException(errMsg);
                    }
                    User user2 = userManager.getUserEvenWhenUnknown(l2);
                    if (user2 == null)
                    {
                        String errMsg = "Could not find user " + l2;
                        throw new IllegalArgumentException(errMsg);
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
        final List<Issue> result = new ArrayList<Issue>();
        for (final Issue issue : user2Issues.get(user))
        {
            if (issue.getIssueTypeObject().getId().equals(typeId))
            {
                result.add(issue);
            }
        }

        return result;
    }

    // We need to provide velocity a way to know how many table rows there will eventually be so the markup
    // can generate correct rowspans for us.
    public Collection<Issue> getDisplayableIssues(final String user, final String typeId)
    {
        final List<Issue> result = new ArrayList<Issue>();
        for (final Issue issue : user2Issues.get(user))
        {
            if (issue.getIssueTypeObject().getId().equals(typeId))
            {
                if (!(getNiceTimeEstimateDuration(issue).equals(getI18nBean().getText("report.versionworkload.no.estimate")) && displayUnknown != null && displayUnknown.equals("no")))
                {
                    result.add(issue);
                }
            }
        }

        return result;

    }

    public String getRemainingTime(String assignee)
    {
        final Long time = user2RemainingTime.get(assignee);
        return getNiceTimeDuration(time);
    }

    public String getIssueTypeTotal(String issueType)
    {
        final Long time = issueTypeTotals.get(issueType);
        return getNiceTimeDuration(time);
    }

    public String getNiceTimeEstimateDuration(Issue issue)
    {
        return getNiceTimeDuration(issue.getEstimate());
    }

    public String getAssigneeFullName(String username)
    {
        final String userFullName = user2FullNames.get(username);
        return userFullName == null ? username : userFullName;
    }
}
