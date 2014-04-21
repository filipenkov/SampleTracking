/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugin.report.impl;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.permission.FieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.MockFieldClausePermissionFactory;
import com.atlassian.jira.local.AbstractIndexingTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.report.ReportModuleDescriptorImpl;
import com.atlassian.jira.portal.FilterValuesGenerator;
import com.atlassian.jira.portal.SortingValuesGenerator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.LocaleParser;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.Assert;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;

public class TestTimeTrackingReport extends AbstractIndexingTestCase
{
    private TimeTrackingReport ttr;
    private Project project;
    private Version version;
    private Issue issue1;
    private Issue issue2;
    private Issue issue3;
    private Issue issue4;
    private Issue issue5;
    private User bob = null;

    private FieldClausePermissionChecker.Factory originalFactory;
    private Mock versionManager;
    private JiraDurationUtils jiraDurationUtils;
    private JiraAuthenticationContext mockAuthenticationContext;
    private ApplicationProperties applicationProperties;
    private BuildUtilsInfo buildUtilsInfo;

    public TestTimeTrackingReport(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        final FieldVisibilityManager fieldVisibilityManager = createMock(FieldVisibilityManager.class);
        expect(fieldVisibilityManager.isFieldHidden((String) anyObject(), (Issue) anyObject())).andReturn(false).anyTimes();

        final FieldVisibilityBean fieldVisibilityBean = createMock(FieldVisibilityBean.class);
        expect(fieldVisibilityBean.isFieldHidden((String) anyObject(), (Issue) anyObject())).andReturn(false).anyTimes();

        replay(fieldVisibilityManager, fieldVisibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, fieldVisibilityManager);
        ManagerFactory.addService(FieldVisibilityBean.class, fieldVisibilityBean);

        originalFactory = ComponentManager.getComponentInstanceOfType(FieldClausePermissionChecker.Factory.class);
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, new MockFieldClausePermissionFactory());

        GenericValue projectGv = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        GenericValue version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(10), "project", new Long(1)));

        version = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("id", new Long(10), "project", new Long(1))));

        // 1: all completed.
        GenericValue issueGv1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(100), "key", "HSP-1", "project", new Long(1), "timeoriginalestimate", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timeestimate", new Long(0), "timespent", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)));

        // 2: 10 left
        GenericValue issueGv2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(200), "key", "HSP-2", "project", new Long(1), "timeoriginalestimate", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timeestimate", new Long((1 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timespent", new Long((6 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)));

        // 3: 20 left
        GenericValue issueGv3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(300), "key", "HSP-3", "project", new Long(1), "timeoriginalestimate", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timeestimate", new Long((2 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timespent", new Long((5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)));

        // 4: 30 left
        GenericValue issueGv4 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(400), "key", "HSP-4", "project", new Long(1), "timeoriginalestimate", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timeestimate", new Long((3 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timespent", new Long((5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)));

        GenericValue issueGv5 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(500), "key", "HSP-5", "project", new Long(1), "timeoriginalestimate", new Long((7 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timeestimate", new Long((3 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS), "timespent", new Long((5 * DateUtils.DAY_MILLIS) / DateUtils.SECOND_MILLIS)));

        CoreFactory.getAssociationManager().createAssociation(issueGv1, version1, IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issueGv2, version1, IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issueGv3, version1, IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issueGv4, version1, IssueRelationConstants.FIX_VERSION);

        ManagerFactory.getIndexManager().reIndexAll();
        ManagerFactory.getProjectManager().refresh();

        versionManager = new Mock(VersionManager.class);

        JiraTestUtil.setupAndAssociateDefaultPermissionSchemeWithPermission(projectGv, Permissions.BROWSE);
        applicationProperties = ComponentAccessor.getApplicationProperties();
        ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
        Mock ac = new Mock(JiraAuthenticationContext.class);
        ac.expectAndReturn("getI18nHelper", new I18nBean(LocaleParser.parseLocale("en_AU")));
        mockAuthenticationContext = (JiraAuthenticationContext) ac.proxy();

        final TimeTrackingConfiguration trackingConfiguration = new TimeTrackingConfiguration.PropertiesAdaptor(applicationProperties);
        jiraDurationUtils = new JiraDurationUtils(applicationProperties, mockAuthenticationContext, trackingConfiguration, null, new MockI18nBean.MockI18nBeanFactory());
        buildUtilsInfo = createMock(BuildUtilsInfo.class);
        expect(buildUtilsInfo.getBuildInformation()).andStubReturn("Some build information");
        expect(buildUtilsInfo.getCurrentBuildNumber()).andStubReturn("111");
        replay(buildUtilsInfo);

        SearchProvider searchProvider = ComponentManager.getInstance().getSearchProvider();

        ttr = new TimeTrackingReport((VersionManager) versionManager.proxy(), applicationProperties, constantsManager, jiraDurationUtils, searchProvider, buildUtilsInfo);

        ttr.init(new ReportModuleDescriptorImpl(null, null)
        {
            @Override
            public I18nHelper getI18nBean()
            {
                return new MockI18nHelper();
            }
        });

        project = ComponentAccessor.getProjectFactory().getProject(projectGv);
        issue1 = convertToIssueObject(issueGv1);
        issue2 = convertToIssueObject(issueGv2);
        issue3 = convertToIssueObject(issueGv3);
        issue4 = convertToIssueObject(issueGv4);
        issue5 = convertToIssueObject(issueGv5);
    }

    /*
     * Make sure right versionIds in a project are retrieved
     */
    public void testGetProjectVersions() throws Exception
    {
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getId())), EasyList.build(version));

        Collection versions = ttr.getProjectVersionIds(project);
        assertEquals(1, versions.size());
        assertTrue(versions.contains(version.getId().toString()));
    }

    /*
     * Make sure validate catches invalid selections
     * Shouldn't occur in the form, but just as a safeguard.
     */
    public void testValidate() throws Exception
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getId())), Collections.EMPTY_LIST);

        Map params = EasyMap.build("completedFilter", "dud", "sortingOrder", "another dud", "versionId", "500");

        ttr.validate(projectActionSupport, params);

        assertFalse(projectActionSupport.getErrors().isEmpty());

        assertEquals("admin.errors.timetracking.invalid.sorting.order", projectActionSupport.getErrors().get("sortingOrder"));
        assertEquals("admin.errors.timetracking.invalid.filter", projectActionSupport.getErrors().get("completedFilter"));
        assertEquals("admin.errors.timetracking.no.version", projectActionSupport.getErrors().get("versionId"));
        assertEquals(3, projectActionSupport.getErrors().size());
    }

    /*
     * Make sure the right issues are retrieved.
     * <p/>
     * Case 1: uncompleted issues sorted by most completed first
     */
    public void testGetIssues1() throws Exception
    {
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getGenericValue())), EasyList.build(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_MOST_COMPLETED, FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES, SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);
        assertEquals(3, issues.size());

        Iterator issueIterator = issues.iterator();

        assertEquals(issue2, issueIterator.next());
        assertEquals(issue3, issueIterator.next());
        assertEquals(issue4, issueIterator.next());
    }

    /*
     * Case 2: uncompleted issues sorted by least completed first
     */
    public void testGetIssues2() throws Exception
    {
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getGenericValue())), EasyList.build(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_INCOMPLETE_ISSUES, SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        assertEquals(3, issues.size());

        Iterator issueIterator = issues.iterator();

        assertEquals(issue4, issueIterator.next());
        assertEquals(issue3, issueIterator.next());
        assertEquals(issue2, issueIterator.next());
    }

    /*
     * Case 3: completed issues sorted by most completed first
     */
    public void testGetIssues3() throws Exception
    {
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getGenericValue())), EasyList.build(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_MOST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        assertEquals(4, issues.size());

        Iterator issueIterator = issues.iterator();

        assertEquals(issue1, issueIterator.next());
        assertEquals(issue2, issueIterator.next());
        assertEquals(issue3, issueIterator.next());
        assertEquals(issue4, issueIterator.next());
    }

    /*
     * Case 4: completed issues sorted by least completed first.
     */
    public void testGetIssues4() throws Exception
    {
        versionManager.expectAndReturn("getVersions", P.args(P.eq(project.getGenericValue())), EasyList.build(version));

        Collection issues = ttr.getReportIssues(bob, project.getId(), version.getId(), SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        assertEquals(4, issues.size());

        Iterator issueIterator = issues.iterator();

        assertEquals(issue4, issueIterator.next());
        assertEquals(issue3, issueIterator.next());
        assertEquals(issue2, issueIterator.next());
        assertEquals(issue1, issueIterator.next());
    }

    /*
     * Case 5: issue for no version.
     */
    public void testGetIssues5() throws Exception
    {
        Collection issues = ttr.getReportIssues(bob, project.getId(), new Long(-1), SortingValuesGenerator.SORT_BY_LEAST_COMPLETED, FilterValuesGenerator.FILTER_ALL_ISSUES, SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        assertEquals(1, issues.size());

        Iterator issueIterator = issues.iterator();

        assertEquals(issue5, issueIterator.next());
    }

    public void testGetNiceTimeDurationPretty()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        assertNotNull(formatter);
        updateJDU("7", "24", JiraDurationUtils.FORMAT_PRETTY);
        assertEquals("1 week", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("2 days", formatter.format(issue3.getEstimate()));
        assertEquals("5 days", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetNiceTimeDurationDays()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        assertEquals("7d", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("2d", formatter.format(issue3.getEstimate()));
        assertEquals("5d", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetNiceTimeDurationHours()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        assertEquals("168h", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("48h", formatter.format(issue3.getEstimate()));
        assertEquals("120h", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetNiceTimeDurationWithShorterWeeksAndDaysPretty()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);
        assertEquals("4 weeks, 4 days", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("1 week, 1 day, 6 hours", formatter.format(issue3.getEstimate()));
        assertEquals("3 weeks, 2 days, 1 hour", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetNiceTimeDurationWithShorterWeeksAndDaysDays()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        assertEquals("24d", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("6d 6h", formatter.format(issue3.getEstimate()));
        assertEquals("17d 1h", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetNiceTimeDurationWithShorterWeeksAndDaysHours()
    {
        DurationFormatter formatter = ttr.getDurationFormatter();
        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        assertEquals("168h", formatter.format(issue3.getOriginalEstimate()));
        assertEquals("48h", formatter.format(issue3.getEstimate()));
        assertEquals("120h", formatter.format(issue3.getTimeSpent()));
    }

    public void testGetOriginalEstTot() throws Exception
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);
        String originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("4w", originalTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("28d", originalTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("672h", originalTot);
    }

    public void testGetOriginalEstTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("19w 1d", originalTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("96d", originalTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        originalTot = ttr.getTotals().getOriginalEstimate();
        assertEquals("672h", originalTot);
    }

    public void testGetTimeSpentTot() throws PermissionException, GenericEntityException, SearchException
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("3w 2d", timeSpentTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("23d", timeSpentTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("552h", timeSpentTot);
    }

    public void testGetTimeSpentTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("15w 3d 6h", timeSpentTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("78d 6h", timeSpentTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        timeSpentTot = ttr.getTotals().getTimeSpent();
        assertEquals("552h", timeSpentTot);
    }

    public void testGetTimeEstTot() throws PermissionException, GenericEntityException, SearchException
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("6d", timeEstTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("6d", timeEstTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("144h", timeEstTot);
    }

    public void testGetTimeEstTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("4w 4h", timeEstTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("20d 4h", timeEstTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        timeEstTot = ttr.getTotals().getRemainingEstimate();
        assertEquals("144h", timeEstTot);
    }

    public void testGetAccuracyTot() throws PermissionException, GenericEntityException, SearchException
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("1d", accuracyTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_DAYS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("1d", accuracyTot);

        updateJDU("7", "24", JiraDurationUtils.FORMAT_HOURS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("24h", accuracyTot);
    }

    public void testGetAccuracyTotWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        String accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("3d 3h", accuracyTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_DAYS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("3d 3h", accuracyTot);

        updateJDU("5", "7", JiraDurationUtils.FORMAT_HOURS);
        accuracyTot = ttr.getTotals().getAccuracyNice();
        assertEquals("24h", accuracyTot);
    }

    public void testGetCompletionPercentage() throws PermissionException, GenericEntityException, SearchException
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        int completionPercentage = ttr.getCompletionPercentage();
        assertEquals(79, completionPercentage);
    }

    public void testGetCompletionPercentageWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        int completionPercentage = ttr.getCompletionPercentage();
        assertEquals(79, completionPercentage);
    }

    public void testGetAccuracyPercentage() throws PermissionException, GenericEntityException, SearchException
    {
        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        int completionPercentage = ttr.getAccuracyPercentage();
        assertEquals(-3, completionPercentage);
    }

    public void testGetAccuracyPercentageWithShorterWeeksAndDays()
            throws PermissionException, GenericEntityException, SearchException
    {
        updateJDU("5", "7", JiraDurationUtils.FORMAT_PRETTY);

        ProjectActionSupport projectActionSupport = new ProjectActionSupport()
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return project;
            }
        };
        Map params = EasyMap.build("completedFilter", FilterValuesGenerator.FILTER_ALL_ISSUES, "sortingOrder", SortingValuesGenerator.SORT_BY_MOST_COMPLETED, "versionId", version.getId().toString(), "subtaskInclusion", SubTaskIncludeValuesGenerator.Options.ONLY_SELECTED);

        ttr.getParams(projectActionSupport, params);

        int completionPercentage = ttr.getAccuracyPercentage();
        assertEquals(-3, completionPercentage);
    }

    public void tearDown() throws Exception
    {
        super.tearDown();

        // we put a mock visibility manager into the ManagerFactory. put a working one in for the next guy who
        // comes along and expects it to be normal.
        final FieldVisibilityBean visibilityBean = new FieldVisibilityBean();
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);
        ManagerFactory.addService(FieldClausePermissionChecker.Factory.class, originalFactory);

        applicationProperties.refresh();
    }

    private void updateJDU(String days, String hours, String format)
    {
        PropertySet propertySet = ComponentAccessor.getComponent(PropertiesManager.class).getPropertySet();
        propertySet.setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, hours);
        propertySet.setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, days);
        propertySet.setString(APKeys.JIRA_TIMETRACKING_FORMAT, format);
        jiraDurationUtils.updateFormatters(null, null);
    }

    private Issue convertToIssueObject(GenericValue issueGv)
    {
        return ComponentAccessor.getIssueFactory().getIssue(issueGv);
    }

    void assertEquals(Issue issue, Object object)
    {
        if (object instanceof ReportIssue)
        {
            Assert.assertEquals(((ReportIssue) object).getIssue(), issue);
        }
        else
        {
            Assert.assertEquals(issue, object);
        }
    }
}
