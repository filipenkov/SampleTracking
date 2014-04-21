/*
 * Copyright (c) 2002-2004 All rights reserved.
 */
package com.atlassian.jira.plugin.report.impl;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.KeyComparator;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.issue.util.AggregateTimeTrackingCalculator;
import com.atlassian.jira.issue.util.IssueImplAggregateTimeTrackingCalculator;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.portal.FilterValuesGenerator;
import com.atlassian.jira.portal.SortingValuesGenerator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.PermissionCheckBean;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.collections.set.ListOrderedSet;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class TimeTrackingReport extends AbstractReport
{
    private static final Long LONG_MAX = Long.MAX_VALUE;

    private final Totals totals = new Totals();
    private final VersionManager versionManager;
    private final ApplicationProperties applicationProperties;
    private final ConstantsManager constantsManager;
    private final AccuracyCalculator accuracyCalculator = new AccuracyCalculatorImpl();
    private final SearchProvider searchProvider;
    private final DurationFormatter defaultDurationFormatter;
    private final BuildUtilsInfo buildUtilsInfo;

    // set to different ones for excel or html views
    private DurationFormatter durationFormatter;

    private TimeTrackingSummaryBean summaryBean;

    TimeTrackingReport(final VersionManager versionManager, final ApplicationProperties applicationProperties, final ConstantsManager constantsManager, final DurationFormatter durationFormatter, final SearchProvider searchProvider, final BuildUtilsInfo buildUtilsInfo)
    {
        this.versionManager = versionManager;
        this.applicationProperties = applicationProperties;
        this.constantsManager = constantsManager;
        this.defaultDurationFormatter = durationFormatter;
        this.durationFormatter = defaultDurationFormatter;
        this.searchProvider = searchProvider;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
    }

    public TimeTrackingReport(final VersionManager versionManager, final ApplicationProperties applicationProperties, final ConstantsManager constantsManager, final JiraDurationUtils jiraDurationUtils, final SearchProvider searchProvider, final BuildUtilsInfo buildUtilsInfo)
    {
        this(versionManager, applicationProperties, constantsManager, new DurationFormatterImpl(new I18nBean(), jiraDurationUtils), searchProvider, buildUtilsInfo);
    }

    @Override
    public boolean showReport()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
    }

    // Return a map of parameters to pass through to the velocity template for this report
    Map<String, Object> getParams(final ProjectActionSupport action, final Map reqParams) throws PermissionException, GenericEntityException, SearchException
    {
        final User remoteUser = action.getRemoteUser();
        final Long projectId = action.getSelectedProject().getLong("id");
        final String versionIdString = (String) reqParams.get("versionId");
        Version version = null;
        if (!versionIdString.equals("-1"))
        {
            final Long versionId = new Long(versionIdString);
            version = versionManager.getVersion(versionId);
        }

        final TextUtils textUtils = new TextUtils();

        final String sortingOrder = (String) reqParams.get("sortingOrder");
        final String completedFilter = (String) reqParams.get("completedFilter");
        final String subtaskInclusion = (String) reqParams.get("subtaskInclusion");

        final Collection issues = getReportIssues(remoteUser, projectId, new Long(versionIdString), sortingOrder, completedFilter, subtaskInclusion);
        summaryBean = new TimeTrackingSummaryBean(issues);

        final Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put("report", this);
        velocityParams.put("action", action);
        velocityParams.put("version", version);
        velocityParams.put("textUtils", textUtils);
        velocityParams.put("issues", issues);
        velocityParams.put("summaryBean", summaryBean);
        velocityParams.put("sortingOrder", sortingOrder);
        velocityParams.put("completedFilter", completedFilter);
        velocityParams.put("versionIdString", versionIdString);
        velocityParams.put("constantsManager", constantsManager);
        velocityParams.put("remoteUser", remoteUser);
        velocityParams.put("totals", totals);
        velocityParams.put("subtasksEnabled", new SubTasksEnabledCondition().isEnabled());
        velocityParams.put("subtaskDescription", SubTaskIncludeValuesGenerator.Options.getDescription(subtaskInclusion, getI18nBean()));
        velocityParams.put("permissionCheck", new PermissionCheckBean());

        // Excel view params
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);

        final VelocityRequestContextFactory contextFactory = new DefaultVelocityRequestContextFactory(applicationProperties);

        String jiraLogo = lookAndFeelBean.getLogoUrl();
        final String jiraBaseUrl = contextFactory.getJiraVelocityRequestContext().getBaseUrl();
        if ((jiraLogo != null) && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = jiraBaseUrl + jiraLogo;
        }
        velocityParams.put("jiraLogo", jiraLogo);
        velocityParams.put("jiraLogoWidth", lookAndFeelBean.getLogoWidth());
        velocityParams.put("jiraLogoHeight", lookAndFeelBean.getLogoHeight());
        velocityParams.put("jiraTitle", applicationProperties.getString(APKeys.JIRA_TITLE));
        velocityParams.put("topBgColor", lookAndFeelBean.getTopBackgroundColour());
        velocityParams.put("buildInfo", buildUtilsInfo.getBuildInformation());
        velocityParams.put("buildNumber", buildUtilsInfo.getCurrentBuildNumber());
        velocityParams.put("createDate", new Date());
        velocityParams.put("jiraBaseUrl", jiraBaseUrl);

        return velocityParams;
    }

    // Generate a HTML view of report
    public String generateReportHtml(final ProjectActionSupport action, final Map reqParams) throws Exception
    {
        durationFormatter = defaultDurationFormatter;
        return descriptor.getHtml("view", getParams(action, reqParams));
    }

    // Generate an EXCEL view of report
    @Override
    public String generateReportExcel(final ProjectActionSupport action, final Map reqParams) throws Exception
    {
        final StringBuffer contentDispositionValue = new StringBuffer(50);
        contentDispositionValue.append("attachment;filename=\"");
        contentDispositionValue.append(getDescriptor().getName()).append(".xls\";");

        // Add header to fix JRA-8484
        final HttpServletResponse response = ActionContext.getResponse();
        response.addHeader("content-disposition", contentDispositionValue.toString());
        durationFormatter = new MinutesDurationFormatter();
        return descriptor.getHtml("excel", getParams(action, reqParams));
    }

    @Override
    public boolean isExcelViewSupported()
    {
        return true;
    }

    @Override
    public void validate(final ProjectActionSupport action, final Map params)
    {
        super.validate(action, params);

        final String sortingOrder = (String) params.get("sortingOrder");
        final String completedFilter = (String) params.get("completedFilter");
        final String versionId = (String) params.get("versionId");

        if ((sortingOrder == null) || (!sortingOrder.equalsIgnoreCase(SortingValuesGenerator.SORT_BY_MOST_COMPLETED) && !sortingOrder.equalsIgnoreCase(SortingValuesGenerator.SORT_BY_LEAST_COMPLETED)))
        {
            action.addError("sortingOrder", getI18nBean().getText("admin.errors.timetracking.invalid.sorting.order"));
        }

        if ((completedFilter == null) || (!completedFilter.equalsIgnoreCase(FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES) && !completedFilter.equalsIgnoreCase(FilterValuesGenerator.FILTER_ALL_ISSUES)))
        {
            action.addError("completedFilter", getI18nBean().getText("admin.errors.timetracking.invalid.filter"));
        }

        try
        {
            final Project project = action.getSelectedProjectObject();
            if (project == null)
            {
                action.addErrorMessage(getI18nBean().getText("admin.errors.timetracking.no.project"));
                return;
            }

            if ((versionId == null) || versionId.equals("-2") || versionId.equals("-3") || ((!versionId.equals("-1")) && !getProjectVersionIds(
                project).contains(versionId)))
            {
                action.addError("versionId", getI18nBean().getText("admin.errors.timetracking.no.version"));
            }
        }
        catch (final Exception e)
        {
            action.addErrorMessage(getI18nBean().getText("admin.errors.timetracking.versions.error"));
        }
    }

    /**
     * Get a collection of all version ids in the selected project
     *
     * @param project project to get the version ids for
     * @return collection of version ids
     */
    public Collection<String> getProjectVersionIds(final Project project)
    {
        final Collection<String> versionIds = new ArrayList<String>();
        for (final Version version : versionManager.getVersions(project.getId()))
        {
            versionIds.add(version.getId().toString());
        }
        return versionIds;
    }

    /**
     * Get the list of issues to be displayed in the report.
     *
     * @param user             user
     * @param projectId        project id
     * @param versionId        version id
     * @param sortingOrder     sorting order
     * @param completedFilter  completed filter, e.g. {@link com.atlassian.jira.portal.FilterValuesGenerator#FILTER_INCOMPLETE_ISSUES}
     * @param subtaskInclusion whether to include subtasks with null or any fixfor version
     * @return collection of issues
     * @throws SearchException if error occurs
     */
    Collection /* <ReportIssue> */getReportIssues(final User user, final Long projectId, final Long versionId, final String sortingOrder, final String completedFilter, final String subtaskInclusion) throws SearchException
    {
        final Set /* <Issue> */issuesFound = searchIssues(user, projectId, versionId, sortingOrder, subtaskInclusion);
        final Predicate reportIssueFilter = getCompletionFilter(completedFilter);
        final Set /* <SubTaskingIssueDecorator> */transformedIssues = new IssueSubTaskTransformer(reportIssueFilter).getIssues(issuesFound);

        final Comparator completionComparator = getCompletionComparator(sortingOrder);

        return new Processor(durationFormatter, accuracyCalculator, completionComparator, reportIssueFilter).getDecoratedIssues(transformedIssues);
    }

    /**
     * Creates a filter that only lets issues that match the given completion criterion. Valid values for
     * completedFilter are FilterValuesGenerator.FILTER_ALL_ISSUES (which means do not filter any issues out) and
     * FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES (which means only include issues that are incomplete or that have
     * at least one incomplete subtask).
     *
     * @param completedFilter designation for filtering issues based on whether they are completed.
     * @return a {@link Predicate} to filter issues.
     */
    private Predicate getCompletionFilter(final String completedFilter)
    {
        Predicate reportIssueFilter;
        if (completedFilter.equals(FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES))
        {
            // include only issues that are incomplete or that have incomplete subtasks
            reportIssueFilter = new Predicate()
            {
                public boolean evaluate(final Object object)
                {
                    if (object instanceof ReportIssue)
                    {
                        return !((ReportIssue) object).isAggregateComplete();
                    }
                    final Issue issue = (Issue) object;
                    return (issue.getEstimate() != null) && (issue.getEstimate().longValue() != 0);
                }
            };
        }
        else
        {
            // include all
            reportIssueFilter = PredicateUtils.truePredicate();
        }
        return reportIssueFilter;
    }

    private Comparator getCompletionComparator(final String sortingOrder)
    {
        final Comparator comparator = new Comparator()
        {
            public int compare(final Object arg0, final Object arg1)
            {
                final ReportIssue reportIssue1 = (ReportIssue) arg0;
                final ReportIssue reportIssue2 = (ReportIssue) arg1;
                int result = reportIssue1.getAggregateRemainingEstimateLong(LONG_MAX).compareTo(
                    reportIssue2.getAggregateRemainingEstimateLong(LONG_MAX));
                if (result == 0)
                {
                    // they are the same, so fall back to largest original estimate
                    result = reportIssue2.getAggregateRemainingEstimateLong(LONG_MAX).compareTo(
                        reportIssue1.getAggregateRemainingEstimateLong(LONG_MAX));
                }
                if (result == 0)
                {
                    // still the same so sort by issue key
                    return KeyComparator.COMPARATOR.compare(reportIssue1.getIssue().getKey(), reportIssue2.getIssue().getKey());
                }
                return result;
            }
        };
        if (sortingOrder.equals(SortingValuesGenerator.SORT_BY_MOST_COMPLETED))
        {
            return comparator;
        }

        return new ReverseComparator(comparator);
    }

    private Set /* <Issue> */searchIssues(final User user, final Long projectId, final Long versionId, final String sortingOrder, final String subtaskInclusion) throws SearchException
    {
        final Query parentQuery = getSearchForParents(projectId, versionId, sortingOrder);
        final SearchResults parentSearchResults = searchProvider.search(parentQuery, user, new PagerFilter(Integer.MAX_VALUE));

        final Set result = new ListOrderedSet();
        final List parentIssues = parentSearchResults.getIssues();
        result.addAll(parentIssues);

        // search for subtasks if necessary
        final List subtasks = new SubTaskFetcher(searchProvider).getSubTasks(user, parentIssues, subtaskInclusion, false);
        result.addAll(subtasks);

        return result;
    }

    /*
     * get the initial search request that gets the parent issues and subtasks of the same version
     */
    private Query getSearchForParents(final Long projectId, final Long versionId, final String sortingOrder)
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        final JqlClauseBuilder builder = queryBuilder.where().project(projectId);

        if (versionId != null)
        {
            if (VersionManager.NO_VERSIONS.equals(versionId.toString()))
            {
                builder.and().fixVersionIsEmpty();
            }
            else
            {
                builder.and().fixVersion().eq(versionId);
            }
        }

        if (sortingOrder.equals(SortingValuesGenerator.SORT_BY_MOST_COMPLETED))
        {
            queryBuilder.orderBy().currentEstimate(SortOrder.ASC);
        }
        else
        {
            queryBuilder.orderBy().currentEstimate(SortOrder.DESC);
        }
        return queryBuilder.buildQuery();
    }

    public int getCompletionPercentage()
    {
        return (int) (((float) summaryBean.getTimeSpent() / (float) (summaryBean.getTimeSpent() + summaryBean.getRemainingEstimate())) * 100F);
    }

    public int getAccuracyPercentage()
    {
        return AccuracyCalculator.Percentage.calculate(summaryBean.getOriginalEstimate(), summaryBean.getTimeSpent(),
            summaryBean.getRemainingEstimate());
    }

    /* for unit testing only! */
    Totals getTotals()
    {
        return totals;
    }

    private I18nHelper getI18nBean()
    {
        return descriptor.getI18nBean();
    }

    /*
     * accessor for tests
     */
    DurationFormatter getDurationFormatter()
    {
        return durationFormatter;
    }

    /** Responsible for taking a List of Issues and creating a List of {@link ReportIssue ReportIssues} for our report */
    static class Processor
    {
        private final DurationFormatter durationFormatter;
        private final AccuracyCalculator accuracyCalculator;
        private final Comparator comparator;
        private final Predicate reportIssueFilter;

        Processor(final DurationFormatter durationFormatter, final AccuracyCalculator accuracyCalculator, final Comparator comparator)
        {
            this(durationFormatter, accuracyCalculator, comparator, PredicateUtils.truePredicate());
        }

        /**
         * Constructs a Processor that can also filter returned results using the reportIssueFilter.
         *
         * @param durationFormatter  for making the durations pretty
         * @param accuracyCalculator to work out the accuracy
         * @param comparator         to sort the issues
         * @param reportIssueFilter  to remove unwanted issues
         */
        Processor(final DurationFormatter durationFormatter, final AccuracyCalculator accuracyCalculator, final Comparator comparator, final Predicate reportIssueFilter)
        {
            this.durationFormatter = durationFormatter;
            this.accuracyCalculator = accuracyCalculator;
            this.comparator = comparator;
            this.reportIssueFilter = reportIssueFilter;
        }

        List /* <ReportIssue> */getDecoratedIssues(final Collection /* <SubTaskingIssueDecorator> */issues)
        {
            final AggregateTimeTrackingCalculator timeTrackingCalculator = new IssueImplAggregateTimeTrackingCalculator(
                new IssueImplAggregateTimeTrackingCalculator.PermissionChecker()
                {
                    public boolean hasPermission(final Issue subTask)
                    {
                        // permission checks should have already been done.
                        return true;
                    }
                });
            final List decoratedIssues = new ArrayList(issues.size());
            for (final Iterator iterator = issues.iterator(); iterator.hasNext();)
            {
                final Issue issue = (Issue) iterator.next();
                final ReportIssue reportIssue = new ReportIssue(issue, timeTrackingCalculator, durationFormatter, accuracyCalculator, comparator,
                    reportIssueFilter);
                // add those that match the filter only
                if (reportIssueFilter.evaluate(reportIssue))
                {
                    decoratedIssues.add(reportIssue);
                }
            }
            Collections.sort(decoratedIssues, comparator);
            return Collections.unmodifiableList(decoratedIssues);
        }
    }

    final class AccuracyCalculatorImpl implements AccuracyCalculator
    {
        public String calculateAndFormatAccuracy(final Long originalEstimate, final Long remainingEstimate, final Long timeSpent)
        {
            if (accuracyIncalculable(originalEstimate, remainingEstimate, timeSpent))
            {
                return getI18nBean().getText("viewissue.timetracking.unknown");
            }
            final Long accuracy = getAccuracy(originalEstimate.longValue(), remainingEstimate.longValue(), timeSpent.longValue());
            return durationFormatter.shortFormat(accuracy);
        }

        public Long calculateAccuracy(final Long originalEstimate, final Long remainingEstimate, final Long timeSpent)
        {
            if (accuracyIncalculable(originalEstimate, remainingEstimate, timeSpent))
            {
                return null;
            }
            return getAccuracy(originalEstimate.longValue(), remainingEstimate.longValue(), timeSpent.longValue());
        }

        public int onSchedule(final Long originalEstimate, final Long remainingEstimate, final Long timeSpent)
        {
            if ((originalEstimate == null) || (remainingEstimate == null) || (timeSpent == null))
            {
                return 0;
            }
            final long accuracy = getAccuracy(originalEstimate.longValue(), remainingEstimate.longValue(), timeSpent.longValue()).longValue();
            return accuracy == 0 ? 0 : ((accuracy > 0) ? 1 : -1);
        }

        private Long getAccuracy(final long originalEst, final long timeEst, final long timeSpent)
        {
            return new Long(originalEst - timeEst - timeSpent);
        }

        private boolean accuracyIncalculable(final Long originalEstimate, final Long remainingEstimate, final Long timeSpent)
        {
            return (originalEstimate == null) || (remainingEstimate == null) || (timeSpent == null);
        }
    }

    public class Totals
    {
        public String getOriginalEstimate()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getOriginalEstimate()));
        }

        public String getTimeSpent()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getTimeSpent()));
        }

        public String getRemainingEstimate()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getRemainingEstimate()));
        }

        public String getAccuracyNice()
        {
            return accuracyCalculator.calculateAndFormatAccuracy(new Long(summaryBean.getOriginalEstimate()), new Long(
                summaryBean.getRemainingEstimate()), new Long(summaryBean.getTimeSpent()));
        }

        public String getAccuracy()
        {
            final Long accuracy = accuracyCalculator.calculateAccuracy(new Long(summaryBean.getOriginalEstimate()), new Long(
                summaryBean.getRemainingEstimate()), new Long(summaryBean.getTimeSpent()));
            return durationFormatter.format(accuracy);
        }

        public String getAccuracyPercentage()
        {
            return "" + AccuracyCalculator.Percentage.calculate(summaryBean.getOriginalEstimate(), summaryBean.getRemainingEstimate(),
                summaryBean.getTimeSpent());
        }

        public int onSchedule()
        {
            return accuracyCalculator.onSchedule(new Long(summaryBean.getOriginalEstimate()), new Long(summaryBean.getRemainingEstimate()), new Long(
                summaryBean.getTimeSpent()));
        }

        /* used in bars.vm */
        public String getTotalCurrentEstimate()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getRemainingEstimate() + summaryBean.getTimeSpent()));
        }

        // //
        public String getAggregateOriginalEstimate()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getAggregateOriginalEstimate()));
        }

        public String getAggregateTimeSpent()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getAggregateTimeSpent()));
        }

        public String getAggregateRemainingEstimate()
        {
            return durationFormatter.shortFormat(new Long(summaryBean.getAggregateRemainingEstimate()));
        }

        public String getAggregateAccuracyNice()
        {
            return accuracyCalculator.calculateAndFormatAccuracy(new Long(summaryBean.getAggregateOriginalEstimate()), new Long(
                summaryBean.getAggregateRemainingEstimate()), new Long(summaryBean.getAggregateTimeSpent()));
        }

        public String getAggregateAccuracy()
        {
            final Long accuracy = accuracyCalculator.calculateAccuracy(new Long(summaryBean.getAggregateOriginalEstimate()), new Long(
                summaryBean.getAggregateRemainingEstimate()), new Long(summaryBean.getAggregateTimeSpent()));
            return durationFormatter.format(accuracy);
        }

        public String getAggregateAccuracyPercentage()
        {
            return "" + AccuracyCalculator.Percentage.calculate(summaryBean.getAggregateOriginalEstimate(),
                summaryBean.getAggregateRemainingEstimate(), summaryBean.getAggregateTimeSpent());
        }

        public int isAggregateOnSchedule()
        {
            return accuracyCalculator.onSchedule(new Long(summaryBean.getOriginalEstimate()), new Long(summaryBean.getRemainingEstimate()), new Long(
                summaryBean.getTimeSpent()));
        }
    }
}
