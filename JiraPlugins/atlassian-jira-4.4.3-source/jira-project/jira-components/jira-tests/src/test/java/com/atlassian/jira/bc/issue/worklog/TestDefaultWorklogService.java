package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.ErrorCollectionAssert;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestDefaultWorklogService extends LegacyJiraMockTestCase
{
    private ErrorCollection errorCollection;
    private JiraServiceContextImpl serviceContext;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        errorCollection = new SimpleErrorCollection();
        serviceContext = new JiraServiceContextImpl(null, errorCollection);
    }

    public void testValidateDeleteNoPermission()
    {
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        assertNull(worklogService.validateDelete(serviceContext, null));
        mockWorklogManager.verify();
    }

    public void testValidateDeleteWithNewEstimateNoPermission()
    {
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        assertNull(worklogService.validateDeleteWithNewEstimate(serviceContext, null, null));
        mockWorklogManager.verify();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testValidateDeleteWithNewEstimateInvalidNewEstimate()
    {
        final Mock mockWorklog = new Mock(WorklogResult.class);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                return false;
            }

            @Override
            public WorklogResult validateDelete(final JiraServiceContext jiraServiceContext, final Long worklogId)
            {
                return (WorklogResult) mockWorklog.proxy();
            }
        };

        assertNull(worklogService.validateDeleteWithNewEstimate(serviceContext, null, null));
    }

    public void testValidateDeleteWithNewEstimateHappyPath()
    {
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        final Mock mockWorklog = new Mock(Worklog.class);

        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, mockWorklog.proxy());
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean isValidCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                hasPermCalled.set(true);
                return true;
            }

            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                isValidCalled.set(true);
                return true;
            }
        };

        assertEquals(mockWorklog.proxy(), worklogService.validateDeleteWithNewEstimate(serviceContext, null, null).getWorklog());
        assertTrue(hasPermCalled.get());
        assertTrue(isValidCalled.get());
        assertFalse(errorCollection.hasAnyErrors());
        mockWorklogManager.verify();
    }

    public void testGetByIssueVisibleToUserGroupRestrictionNotVisible()
    {
        final AtomicBoolean isGroupCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, new Long(1234), null, null, null, "testgroup", null, new Long(1));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return EasyList.build(worklog);
            }

            @Override
            protected boolean isUserInGroup(final User user, final String groupLevel)
            {
                isGroupCalled.set(true);
                return false;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(isGroupCalled.get());
        assertEquals(0, visibleIssues.size());
    }

    public void testGetByIssueVisibleToUserGroupRestrictionVisible()
    {
        final AtomicBoolean isGroupCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, new Long(1234), null, null, null, "testgroup", null, new Long(1));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return EasyList.build(worklog);
            }

            @Override
            protected boolean isUserInGroup(final User user, final String groupLevel)
            {
                isGroupCalled.set(true);
                return true;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(isGroupCalled.get());
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    public void testGetByIssueVisibleToUserRoleRestrictionNotVisible()
    {
        final AtomicBoolean isInRoleCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, new Long(1234), null, null, null, null, new Long(1234), new Long(1));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return EasyList.build(worklog);
            }

            @Override
            protected boolean isUserInRole(final Long roleLevel, final User user, final Issue issue)
            {
                isInRoleCalled.set(true);
                return false;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(isInRoleCalled.get());
        assertEquals(0, visibleIssues.size());
    }

    public void testGetByIssueVisibleToUserRoleRestrictionVisible()
    {
        final AtomicBoolean isInRoleCalled = new AtomicBoolean(false);
        final Worklog worklog = new WorklogImpl(null, null, new Long(1234), null, null, null, null, new Long(1234), new Long(1));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return EasyList.build(worklog);
            }

            @Override
            protected boolean isUserInRole(final Long roleLevel, final User user, final Issue issue)
            {
                isInRoleCalled.set(true);
                return true;
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertTrue(isInRoleCalled.get());
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    public void testGetByIssueVisibleToUserNoRestriction()
    {
        final Worklog worklog = new WorklogImpl(null, null, new Long(1234), null, null, null, null, null, new Long(1));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            public List getByIssue(final JiraServiceContext jiraServiceContext, final Issue issue)
            {
                return EasyList.build(worklog);
            }
        };

        final List visibleIssues = worklogService.getByIssueVisibleToUser(serviceContext, null);
        assertEquals(1, visibleIssues.size());
        assertEquals(worklog, visibleIssues.get(0));
    }

    public void testValidateParamsAndCreateWorklog() throws DuplicateEntityException, ImmutableException
    {
        final User testUser = UserUtils.createUser("testValidateCreateUser", "test@atlassian.com", "Test User");
        final Long timeSpentLong = new Long(12345);
        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.TRUE);
        final AtomicBoolean isValidFieldsCalled = new AtomicBoolean(false);
        final AtomicBoolean getDurationCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, (VisibilityValidator) mockVisibilityValidator.proxy(),
                null, null, null, null)
        {
            @Override
            protected boolean isValidWorklogInputFields(final JiraServiceContext jiraServiceContext, final Issue issue, final String timeSpent, final Date startDate, final String errorFieldPrefix)
            {
                isValidFieldsCalled.set(true);
                return true;
            }

            @Override
            protected long getDurationForFormattedString(final String timeSpent, final JiraServiceContext jiraServiceContext)
            {
                getDurationCalled.set(true);
                return timeSpentLong.longValue();
            }
        };

        final Date startDate = new Date();
        final String comment = "test comment";
        final String groupLevel = "testgroup";
        final Worklog worklog = worklogService.validateParamsAndCreateWorklog(new JiraServiceContextImpl(testUser, errorCollection), null,
            testUser.getName(), groupLevel, null, "2d", startDate, null, comment, null, null, null, null);
        assertNotNull(worklog);
        assertEquals(timeSpentLong, worklog.getTimeSpent());
        assertEquals(startDate, worklog.getStartDate());
        assertEquals(comment, worklog.getComment());
        assertEquals(groupLevel, worklog.getGroupLevel());
        assertEquals(testUser.getName(), worklog.getAuthor());
        assertNull("The worklog role level should be null.", worklog.getRoleLevelId());
        assertTrue(isValidFieldsCalled.get());
        assertTrue(getDurationCalled.get());
        mockVisibilityValidator.verify();
    }

    public void testValidateCreateWithNewEstimateInvalidNewEstimate() throws DuplicateEntityException, ImmutableException
    {
        final AtomicBoolean isValidNewEstCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidNewEstimate(final JiraServiceContext jiraServiceContext, final String newEstimate, final String errorFieldPrefix)
            {
                isValidNewEstCalled.set(true);
                return false;
            }

            @Override
            public WorklogResult validateCreate(final JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
            {
                return WorklogResultFactory.create(null);
            }
        };

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl.builder().buildNewEstimate();
        final WorklogResult worklogNewEstimateResult = worklogService.validateCreateWithNewEstimate(serviceContext, params);
        assertNull(worklogNewEstimateResult);
        assertTrue(isValidNewEstCalled.get());
    }

    public void testValidateCreate() throws DuplicateEntityException, ImmutableException
    {
        final AtomicBoolean hasPermCalled = new AtomicBoolean(false);
        final AtomicBoolean validateAndCreateCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue, final String author, final String groupLevel, final String roleLevelId, final String timeSpent, final Date startDate, final Long worklogId, final String comment, final Date created, final Date updated, final String updateAuthor, final String errorFieldPrefix)
            {
                validateAndCreateCalled.set(true);
                return null;
            }

            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                hasPermCalled.set(true);
                return true;
            }
        };

        final WorklogInputParameters params = WorklogInputParametersImpl.timeSpent("2d").build();
        worklogService.validateCreate(new JiraServiceContextImpl(null, errorCollection), params);
        assertTrue(hasPermCalled.get());
        assertTrue(validateAndCreateCalled.get());
    }

    public void testIsValidWorklogInputFieldsInvalidTimeSpent()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return false;
            }
        };
        assertFalse(worklogService.isValidWorklogInputFields(serviceContext, null, "INVALID", new Date(), null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "Invalid time duration entered.");
    }

    public void testIsValidWorklogInputFieldsInvalidTimeSpentZero()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            @Override
            protected long getDurationForFormattedString(final String timeSpent, final JiraServiceContext jiraServiceContext)
            {
                return 0;
            }
        };
        assertFalse(worklogService.isValidWorklogInputFields(serviceContext, null, "0", new Date(), null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "Time Spent can not be zero.");
    }

    public void testIsValidWorklogInputFieldsNullTimeSpentNullStartDate()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertFalse(worklogService.isValidWorklogInputFields(serviceContext, null, null, null, null));
        ErrorCollectionAssert.assertFieldError(errorCollection, "timeLogged", "You must indicate the time spent working.");
        ErrorCollectionAssert.assertFieldError(errorCollection, "startDate", "You must specify a date on which the work occurred.");
    }

    public void testIsValidNewEstimate()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean isValidDuration(final String duration, final JiraServiceContext jiraServiceContext)
            {
                return false;
            }

            @Override
            boolean hasEditIssuePermission(final User user, final Issue issue)
            {
                return true;
            }
        };

        assertFalse(worklogService.isValidNewEstimate(serviceContext, "Blah"));
        ErrorCollectionAssert.assertFieldError(errorCollection, "newEstimate", "Invalid new estimate entered.");
    }

    public void testReduceEstimateDoesNotGoBelowZero()
    {
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getEstimate", new Long(1000));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertEquals(new Long(0), worklogService.reduceEstimate((Issue) mockIssue.proxy(), new Long(1500)));
    }

    public void testUpdateNullWorklog()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertNull("Worklog should be null", worklogService.update(serviceContext, null, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Worklog must not be null.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testUpdateNullWorklogIssue()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertNull("Worklog should be null", worklogService.update(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());
        mockWorklog.verify();
    }

    public void testUpdate()
    {
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("update", P.ANY_ARGS, null);
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        final AtomicBoolean hasEditPermCalled = new AtomicBoolean(false);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                hasEditPermCalled.set(true);
                return true;
            }
        };
        assertNull("Worklog should be null", worklogService.update(serviceContext, worklogResult, null, true));
        assertTrue(hasEditPermCalled.get());
        assertFalse(errorCollection.hasAnyErrors());
        mockWorklog.verify();
    }

    public void testUpdateWithNewRemainingEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        final Worklog worklog = new WorklogImpl(null, (Issue) mockIssue.proxy(), new Long(1), null, null, null, null, null, new Long(1000));
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        final Long newEstimate = new Long(12345);
        mockWorklogManager.expectAndReturn("update", new Constraint[] { new IsNull(), new IsEqual(worklog), new IsEqual(newEstimate), new IsEqual(
            Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        worklogService.updateWithNewRemainingEstimate(serviceContext, WorklogResultFactory.createNewEstimate(worklog, newEstimate), true);
        mockWorklogManager.verify();
    }

    public void testUpdateAndRetainRemainingEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        final Worklog worklog = new WorklogImpl(null, (Issue) mockIssue.proxy(), new Long(1), null, null, null, null, null, new Long(1000));
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("update",
            new Constraint[] { new IsNull(), new IsEqual(worklog), new IsNull(), new IsEqual(Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        worklogService.updateAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        mockWorklogManager.verify();
    }

    public void testUpdateAndAutoAdjustRemainingEstimate()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final Mock mockOriginalWorklog = new Mock(Worklog.class);
        final Long origTimeSpent = new Long(2323);
        mockOriginalWorklog.expectAndReturn("getTimeSpent", origTimeSpent);

        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, mockOriginalWorklog.proxy());

        final Long newEstimate = new Long(12345);
        mockWorklogManager.expectAndReturn("update",
            new Constraint[] { new IsNull(), new IsEqual(mockWorklog.proxy()), new IsEqual(newEstimate), new IsEqual(Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToUpdate(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }

            @Override
            protected Long getAutoAdjustNewEstimateOnUpdate(final Issue issue, final Long newTimeSpent, final Long originalTimeSpent)
            {
                assertEquals(origTimeSpent, originalTimeSpent);
                return newEstimate;
            }

        };

        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());
        worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        mockWorklogManager.verify();
        mockWorklog.verify();
        mockOriginalWorklog.verify();
    }

    public void testReduceEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getEstimate", new Long(1000));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertEquals(new Long(500), worklogService.reduceEstimate((Issue) mockIssue.proxy(), new Long(500)));
    }

    public void testGetAutoAdjustNewEstimateOnUpdateDoesNotGoBelowZero()
    {
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getEstimate", new Long(1000));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertEquals(new Long(0), worklogService.getAutoAdjustNewEstimateOnUpdate((Issue) mockIssue.proxy(), new Long(2500), new Long(1000)));
    }

    public void testGetAutoAdjustNewEstimateOnUpdate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getEstimate", new Long(1000));
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertEquals(new Long(1500), worklogService.getAutoAdjustNewEstimateOnUpdate((Issue) mockIssue.proxy(), new Long(500), new Long(1000)));
    }

    public void testCreateAndAutoAdjustRemainingEstimate()
    {
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        final Mock mockIssue = new Mock(Issue.class);
        final Worklog worklog = new WorklogImpl(null, (Issue) mockIssue.proxy(), new Long(1), null, null, null, null, null, new Long(1000));
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final Long newEstimate = new Long(12345);
        mockWorklogManager.expectAndReturn("create", new Constraint[] { new IsNull(), new IsEqual(worklog), new IsEqual(newEstimate), new IsEqual(
            Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }

            @Override
            protected Long reduceEstimate(final Issue issue, final Long timeSpent)
            {
                return newEstimate;
            }
        };

        worklogService.createAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true);
        mockWorklogManager.verify();
    }

    public void testCreateAndRetainRemainingEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        final Worklog worklog = new WorklogImpl(null, (Issue) mockIssue.proxy(), null, null, null, null, null, null, new Long(1000));
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("create",
            new Constraint[] { new IsNull(), new IsEqual(worklog), new IsNull(), new IsEqual(Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }
        };

        worklogService.createAndRetainRemainingEstimate(serviceContext, worklogResult, true);
        mockWorklogManager.verify();
    }

    public void testCreateWithNewRemainingEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        final Worklog worklog = new WorklogImpl(null, (Issue) mockIssue.proxy(), null, null, null, null, null, null, new Long(1000));
        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        final Long newEstimate = new Long(12345);
        final WorklogNewEstimateResult worklogResult = WorklogResultFactory.createNewEstimate(worklog, newEstimate);
        mockWorklogManager.expectAndReturn("create", new Constraint[] { new IsNull(), new IsEqual(worklog), new IsEqual(newEstimate), new IsEqual(
            Boolean.TRUE) }, null);
        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            @Override
            public boolean hasPermissionToCreate(final JiraServiceContext jiraServiceContext, final Issue issue, final boolean isEditableCheckRequired)
            {
                return true;
            }
        };

        worklogService.createWithNewRemainingEstimate(serviceContext, worklogResult, true);
        mockWorklogManager.verify();
    }

    public void testCreateWithNullIssue()
    {
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(1000));
        final WorklogResult worklogResult = WorklogResultFactory.create(worklog);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        worklogService.create(serviceContext, worklogResult, null, true);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testCreateWithNullWorklog()
    {
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        final WorklogResult worklogResult = WorklogResultFactory.create(null);
        worklogService.create(serviceContext, worklogResult, null, true);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Worklog must not be null.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToCreateWithUser() throws EntityNotFoundException, DuplicateEntityException, ImmutableException
    {
        final User testUser = UserUtils.createUser("testHasPermissionToCreateWithUser", "test@atlassian.com", "Test User");
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORK_ISSUE)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(worklogService.hasPermissionToCreate(new JiraServiceContextImpl(testUser, errorCollection), new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Test User, you do not have the permission to associate a worklog to this issue.",
            errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToCreateNonEditableWorkflowState() throws EntityNotFoundException, DuplicateEntityException, ImmutableException
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORK_ISSUE)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            @Override
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            @Override
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return false;
            }
        };

        assertFalse(worklogService.hasPermissionToCreate(serviceContext, new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not edit the issue as it is in a non-editable workflow state.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToCreateWithNullUser()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORK_ISSUE)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);
        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(worklogService.hasPermissionToCreate(serviceContext, new MockIssue(), true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have the permission to associate a worklog to this issue.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasEditOwnPermission()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_EDIT_OWN)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));
        assertTrue(worklogService.hasEditOwnPermission(null, worklog));
    }

    public void testHasEditOwnPermissionUnauthorised()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_EDIT_OWN)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));
        assertFalse(worklogService.hasEditOwnPermission(null, worklog));
    }

    public void testHasEditOwnPermissionNotSameAuthor()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_EDIT_OWN)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return false;
            }
        };

        assertFalse(worklogService.hasEditOwnPermission(null, null));
    }

    public void testHasEditAllPermission()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_EDIT_ALL)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null);

        assertTrue(worklogService.hasEditAllPermission(null, null));
    }

    public void testHasEditAllPermissionUnauthorised()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_EDIT_ALL)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null);

        assertFalse(worklogService.hasEditAllPermission(null, null));
    }

    public void testHasDeleteOwnPermission()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_DELETE_OWN)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));
        assertTrue(worklogService.hasDeleteOwnPermission(null, worklog));
    }

    public void testHasDeleteOwnPermissionUnauthorised()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_DELETE_OWN)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return true;
            }
        };

        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));
        assertFalse(worklogService.hasDeleteOwnPermission(null, worklog));
    }

    public void testHasDeleteOwnPermissionNotSameAuthor()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_DELETE_OWN)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null)
        {
            protected boolean isSameAuthor(final User user, final Worklog worklog)
            {
                return false;
            }
        };

        assertFalse(worklogService.hasDeleteOwnPermission(null, null));
    }

    public void testHasDeleteAllPermission()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_DELETE_ALL)), new IsAnything(),
            new IsAnything()), Boolean.TRUE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null);

        assertTrue(worklogService.hasDeleteAllPermission(null, null));
    }

    public void testHasDeleteAllPermissionUnauthorised()
    {
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.WORKLOG_DELETE_ALL)), new IsAnything(),
            new IsAnything()), Boolean.FALSE);

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, (PermissionManager) mockPermissionManager.proxy(), null,
                null, null, null, null);

        assertFalse(worklogService.hasDeleteAllPermission(null, null));
    }

    public void testIsSameAuthorSpecificAuthor()
    {
        final User user = UtilsForTests.getTestUser("testIsSameAuthorSpecificAuthor");
        final Worklog worklog = new WorklogImpl(null, null, null, user.getName(), null, null, null, null, new Long(123));

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertTrue(worklogService.isSameAuthor(user, worklog));
    }

    public void testIsSameAuthorAnonymous()
    {
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertTrue(worklogService.isSameAuthor(null, worklog));
    }

    public void testIsSameAuthorDifferentAuthors()
    {
        final User user1 = UtilsForTests.getTestUser("testIsSameAuthorDifferentAuthorsUserOne");
        final User user2 = UtilsForTests.getTestUser("testIsSameAuthorDifferentAuthorsUserTwo");
        final Worklog worklog = new WorklogImpl(null, null, null, user1.getName(), null, null, null, null, new Long(123));

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertFalse(worklogService.isSameAuthor(user2, worklog));
    }

    public void testIsSameAuthorOneAnonymousAuthor()
    {
        final User user = UtilsForTests.getTestUser("testIsSameAuthorOneAnonymousAuthor");
        final Worklog worklog = new WorklogImpl(null, null, null, null, null, null, null, null, new Long(123));

        final DefaultWorklogService worklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertFalse(worklogService.isSameAuthor(user, worklog));
    }

    public void testHasPermissionToUpdateNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };
        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Worklog must not be null.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToUpdateNullIssueForWorklog()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };
        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateNullWorklogId()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };
        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The worklog must have an id to perform an update/delete.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateNoPermissionAnonymous()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            protected boolean hasEditOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasEditAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have permission to edit the specified worklog.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateNoPermissionSpecificUser() throws DuplicateEntityException, ImmutableException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            protected boolean hasEditOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasEditAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        final User user = UserUtils.createUser("testHasPermissionToUpdateNoPermissionSpecificUser", "yo@yo.net", "Tim Fullname");
        serviceContext = new JiraServiceContextImpl(user, errorCollection);

        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Tim Fullname, you do not have permission to edit the specified worklog.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateHappyPathEditOwn()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.TRUE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            protected boolean hasEditOwnPermission(final User user, final Worklog worklog)
            {
                return true;
            }

            protected boolean hasEditAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertTrue(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertFalse(errorCollection.hasAnyErrors());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateInvalidVisibilityData()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.FALSE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            protected boolean hasEditOwnPermission(final User user, final Worklog worklog)
            {
                return true;
            }

            protected boolean hasEditAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertFalse(errorCollection.hasAnyErrors());

        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateHappyPathEditAll()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.TRUE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            protected boolean hasEditOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasEditAllPermission(final User user, final Issue issue)
            {
                return true;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }

            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        assertTrue(defaultWorklogService.hasPermissionToUpdate(serviceContext, (Worklog) mockWorklog.proxy()));
        assertFalse(errorCollection.hasAnyErrors());

        mockWorklog.verify();
    }

    public void testUpdateAndAutoAdjustRemainingEstimateFailToFindOriginalWorklog()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, null);

        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null);

        assertNull(worklogService.updateAndAutoAdjustRemainingEstimate(serviceContext, worklogResult, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Cannot find worklog with id: '1'.", errorCollection.getErrorMessages().iterator().next());

        mockWorklogManager.verify();
        mockWorklog.verify();
    }

    public void testValidateUpdateWithNewEstimate() throws DuplicateEntityException, ImmutableException
    {
        final Mock mockOrigWorklog = new Mock(Worklog.class);
        final Date createdDate = new Date(1000);
        mockOrigWorklog.expectAndReturn("getCreated", createdDate);

        final User user = UserUtils.createUser("testValidateUpdateWithNewEstimate", "yo@yo.net", "Tim Fullname");
        serviceContext = new JiraServiceContextImpl(user, errorCollection);

        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("getById", P.ANY_ARGS, mockOrigWorklog.proxy());

        final DefaultWorklogService worklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null, null,
            null, null, null)
        {
            // TODO: Was this method changed?
            protected Worklog validateParamsAndCreateWorklog(final JiraServiceContext jiraServiceContext, final Issue issue, final String author, final String groupLevel, final String roleLevelId, final String timeSpent, final Date startDate, final Long worklogId, final String comment, final Date created, final Date updated, final String updateAuthor, final String errorFieldPrefix)
            //protected Worklog validateParamsAndCreateWorklog(JiraServiceContext jiraServiceContext, Issue issue, String groupLevel, String roleLevelId, String timeSpent, Date startDate, String newEstimate, Long worklogId, String comment, Date created, Date updated, String updateAuthor)
            {
                assertEquals(createdDate, created);
                assertEquals("Tim Fullname", updateAuthor);
                assertTrue(updated.getTime() > createdDate.getTime());
                return null;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        WorklogNewEstimateInputParameters params = WorklogInputParametersImpl.builder().buildNewEstimate();
        worklogService.validateUpdateWithNewEstimate(serviceContext, params);
        // TODO: Should we be asserting something here?
    }

    public void testValidateUpdateOrDeletePermissionCheckParamsNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams(null, errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Worklog must not be null.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testValidateUpdateOrDeletePermissionCheckParamsNullIssue()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams((Worklog) mockWorklog.proxy(), errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testValidateUpdateOrDeletePermissionCheckParamsNullWorklogId()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", null);

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams((Worklog) mockWorklog.proxy(), errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The worklog must have an id to perform an update/delete.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testValidateUpdateOrDeletePermissionCheckParamsIssueInNonEditableWorkflowState()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return false;
            }
        };

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams((Worklog) mockWorklog.proxy(), errorCollection, serviceContext);
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You can not edit the issue as it is in a non-editable workflow state.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testValidateUpdateOrDeletePermissionCheckParamsHappyPath()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isIssueInEditableWorkflowState(final Issue issue)
            {
                return true;
            }
        };

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));

        defaultWorklogService.validateUpdateOrDeletePermissionCheckParams((Worklog) mockWorklog.proxy(), errorCollection, serviceContext);
        assertFalse(errorCollection.hasAnyErrors());
    }

    public void testHasPermissionToDeleteNoPermissionAnonymous()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            protected boolean hasDeleteOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasDeleteAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToDelete(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You do not have permission to delete the specified worklog.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToDeleteNoPermissionSpecificUser() throws DuplicateEntityException, ImmutableException
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            protected boolean hasDeleteOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasDeleteAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        final User user = UserUtils.createUser("testHasPermissionToDeleteNoPermissionSpecificUser", "yo@yo.net", "Tim Fullname");
        serviceContext = new JiraServiceContextImpl(user, errorCollection);

        assertFalse(defaultWorklogService.hasPermissionToDelete(serviceContext, (Worklog) mockWorklog.proxy()));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Tim Fullname, you do not have permission to delete the specified worklog.",
            errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testHasPermissionToDeleteInvalidVisibilityData()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.FALSE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            protected boolean hasDeleteOwnPermission(final User user, final Worklog worklog)
            {
                return true;
            }

            protected boolean hasDeleteAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToDelete(serviceContext, (Worklog) mockWorklog.proxy()));

        mockVisibilityValidator.verify();
        mockWorklog.verify();
    }

    public void testHasPermissionToDeleteHappyPathDeleteOwn()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.TRUE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            protected boolean hasDeleteOwnPermission(final User user, final Worklog worklog)
            {
                return true;
            }

            protected boolean hasDeleteAllPermission(final User user, final Issue issue)
            {
                return false;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertTrue(defaultWorklogService.hasPermissionToDelete(serviceContext, (Worklog) mockWorklog.proxy()));
        assertFalse(errorCollection.hasAnyErrors());

        mockVisibilityValidator.verify();
        mockWorklog.verify();
    }

    public void testHasPermissionToDeleteHappyPathDeleteAll()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);

        final Mock mockVisibilityValidator = new Mock(VisibilityValidator.class);
        mockVisibilityValidator.expectAndReturn("isValidVisibilityData", P.ANY_ARGS, Boolean.TRUE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null,
            (VisibilityValidator) mockVisibilityValidator.proxy(), null, null, null, null)
        {
            void validateUpdateOrDeletePermissionCheckParams(final Worklog worklog, final ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
            {}

            protected boolean hasDeleteOwnPermission(final User user, final Worklog worklog)
            {
                return false;
            }

            protected boolean hasDeleteAllPermission(final User user, final Issue issue)
            {
                return true;
            }

            public boolean isTimeTrackingEnabled()
            {
                return true;
            }
        };

        assertTrue(defaultWorklogService.hasPermissionToDelete(serviceContext, (Worklog) mockWorklog.proxy()));
        assertFalse(errorCollection.hasAnyErrors());

        mockVisibilityValidator.verify();
        mockWorklog.verify();
    }

    public void testHasPermissionToUpdateTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToUpdate(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not perform the requested operation, time tracking is disabled in JIRA.",
            errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToDeleteTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToDelete(serviceContext, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not perform the requested operation, time tracking is disabled in JIRA.",
            errorCollection.getErrorMessages().iterator().next());
    }

    public void testHasPermissionToCreateTimeTrackingDisabled()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean isTimeTrackingEnabled()
            {
                return false;
            }
        };

        assertFalse(defaultWorklogService.hasPermissionToCreate(serviceContext, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not perform the requested operation, time tracking is disabled in JIRA.",
            errorCollection.getErrorMessages().iterator().next());
    }

    public void testDeleteNullWorklog()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        assertFalse(defaultWorklogService.delete(serviceContext, null, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Worklog must not be null.", errorCollection.getErrorMessages().iterator().next());
    }

    public void testDeleteNullIssue()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", null);
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        assertFalse(defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testDeleteNullWorklogId()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", null);
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        assertFalse(defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("The worklog must have an id to perform an update/delete.", errorCollection.getErrorMessages().iterator().next());

        mockWorklog.verify();
    }

    public void testDeleteNoPermission()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return false;
            }
        };

        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        assertFalse(defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertFalse(errorCollection.hasAnyErrors());

        mockWorklog.verify();
    }

    public void testDeleteHappyPath()
    {
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", new MockIssue());
        mockWorklog.expectAndReturn("getId", new Long(1));
        final WorklogResult worklogResult = WorklogResultFactory.create((Worklog) mockWorklog.proxy());

        final Mock mockWorklogManager = new Mock(WorklogManager.class);
        mockWorklogManager.expectAndReturn("delete", new Constraint[] { new IsNull(), new IsEqual(mockWorklog.proxy()), new IsNull(), new IsEqual(
            Boolean.TRUE) }, Boolean.TRUE);

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService((WorklogManager) mockWorklogManager.proxy(), null, null,
                null, null, null, null)
        {
            public boolean hasPermissionToDelete(final JiraServiceContext jiraServiceContext, final Worklog worklog)
            {
                return true;
            }
        };

        assertTrue(defaultWorklogService.delete(serviceContext, worklogResult, null, true));
        assertFalse(errorCollection.hasAnyErrors());

        mockWorklogManager.verify();
        mockWorklog.verify();
    }

    public void testIncreaseEstimate()
    {
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getEstimate", new Long(1000));

        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null);
        assertEquals(new Long(1500), defaultWorklogService.increaseEstimate((Issue) mockIssue.proxy(), new Long(500)));
    }

    public void testDeleteWithNewRemainingEstimate()
    {
        final Long newRemainingEstimate = new Long(12345);
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertEquals(newRemainingEstimate, newEstimate);
                return true;
            }
        };

        defaultWorklogService.deleteWithNewRemainingEstimate(serviceContext, WorklogResultFactory.createNewEstimate((Worklog) null, newRemainingEstimate),
            true);
    }

    public void testDeleteAndRetainRemainingEstimate()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertNull(newEstimate);
                return true;
            }
        };

        defaultWorklogService.deleteAndRetainRemainingEstimate(serviceContext, null, true);
    }

    public void testDeleteAndAutoAdjustRemainingEstimate()
    {
        final Long autoEstimate = new Long(12345);
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            @Override
            protected boolean delete(final JiraServiceContext jiraServiceContext, final WorklogResult worklogResult, final Long newEstimate, final boolean dispatchEvent)
            {
                assertEquals(autoEstimate, newEstimate);
                return true;
            }

            protected Long increaseEstimate(final Issue issue, final Long timeSpent)
            {
                return autoEstimate;
            }
        };

        defaultWorklogService.deleteAndAutoAdjustRemainingEstimate(serviceContext, null, true);
        // TODO: Should we be asserting something here?
    }

    public void testIsValidNewEstimateNotSpecified()
    {
        final DefaultWorklogService defaultWorklogService = new DefaultWorklogService(null, null, null, null, null, null, null)
        {
            boolean hasEditIssuePermission(final User user, final Issue issue)
            {
                return true;
            }
        };

        assertFalse(defaultWorklogService.isValidNewEstimate(serviceContext, "")); //empty String fails TextUtils.stringset()
        ErrorCollectionAssert.assertFieldError(errorCollection, "newEstimate", "You must supply a valid new estimate.");
    }
}
