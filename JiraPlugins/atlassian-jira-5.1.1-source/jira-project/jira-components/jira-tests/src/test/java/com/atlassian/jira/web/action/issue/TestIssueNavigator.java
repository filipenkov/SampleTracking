package com.atlassian.jira.web.action.issue;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.issue.fields.DefaultFieldManager;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.pager.NextPreviousPager;
import com.atlassian.jira.issue.pager.PagerManager;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.plugin.webresource.JiraWebResourceManager;
import com.atlassian.jira.util.index.MockIndexLifecycleManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionNextPreviousPagerManager;
import com.atlassian.jira.web.session.SessionPagerFilterManager;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import com.atlassian.jira.web.session.SessionSelectedIssueManager;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpSession;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TestIssueNavigator extends AbstractUsersTestCase
{
    private FieldManager oldFieldManager;
    private IssueNavigator in;
    private MockHttpSession session;
    private HttpServletRequest previousRequest;
    private SessionSearchRequestManager sessionSearchRequestManager;
    private SessionSelectedIssueManager sessionSelectedIssueManager;
    private SessionPagerFilterManager sessionPagerFilterManager;
    private SessionNextPreviousPagerManager sessionNextPreviousPagerManager;
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;

    @com.atlassian.jira.easymock.Mock
    private JiraWebResourceManager webResourceManager;

    @com.atlassian.jira.easymock.Mock
    private AvatarService avatarService;

    @com.atlassian.jira.easymock.Mock
    private EventPublisher eventPublisher;

    public TestIssueNavigator(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        EasyMockAnnotations.initMocks(this);

        // Stub out any calls.  we are not interested in testing it
        //
        sessionSearchObjectManagerFactory = EasyMock.createMock(SessionSearchObjectManagerFactory.class);
        sessionSearchRequestManager = EasyMock.createMock(SessionSearchRequestManager.class);
        sessionPagerFilterManager = EasyMock.createMock(SessionPagerFilterManager.class);
        sessionSelectedIssueManager = EasyMock.createMock(SessionSelectedIssueManager.class);
        sessionNextPreviousPagerManager = EasyMock.createMock(SessionNextPreviousPagerManager.class);
        EasyMock.expect(sessionSearchObjectManagerFactory.createSearchRequestManager()).andStubReturn(sessionSearchRequestManager);
        EasyMock.expect(sessionSearchObjectManagerFactory.createSelectedIssueManager()).andStubReturn(sessionSelectedIssueManager);
        EasyMock.expect(sessionSearchObjectManagerFactory.createPagerFilterManager()).andStubReturn(sessionPagerFilterManager);
        EasyMock.expect(sessionSearchObjectManagerFactory.createNextPreviousPagerManager()).andStubReturn(sessionNextPreviousPagerManager);
        sessionNextPreviousPagerManager.setCurrentObject(EasyMock.<NextPreviousPager>anyObject()); EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(sessionNextPreviousPagerManager.getCurrentObject()).andReturn(null).anyTimes();
        sessionPagerFilterManager.setCurrentObject(EasyMock.<PagerFilter>anyObject()); EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(sessionPagerFilterManager.getCurrentObject()).andReturn(null).anyTimes();
        EasyMock.replay(sessionSearchObjectManagerFactory, sessionSearchRequestManager, sessionNextPreviousPagerManager, sessionPagerFilterManager, sessionSelectedIssueManager, webResourceManager, avatarService);
        
        oldFieldManager = ComponentAccessor.getFieldManager();

//        ManagerFactory.addService(IssueIndexManager.class, new MemoryIndexManager());
        ManagerFactory.addService(FieldVisibilityBean.class, new FieldVisibilityBean()
        {
            @Override
            public boolean isFieldHiddenInAllSchemes(final String fieldId, final SearchContext context, final com.atlassian.crowd.embedded.api.User user)
            {
                return false;
            }
        });
        ManagerFactory.addService(FieldManager.class, new DefaultFieldManager(null)
        {
            @Override
            public boolean isFieldHidden(final com.atlassian.crowd.embedded.api.User remoteUser, final Field field)
            {
                return false;
            }
        });

        final User u1 = createMockUser("Test User 1");
        createMockUser("Test Reporter 1");
        createMockUser("Test Assignee 1");

        final Group g1 = createMockGroup("Test Group");
        addUserToGroup(u1, g1);

        final GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "ABC-7348", "workflowId", new Long(
            1000), "priority", "C", "project", new Long(10)));

        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug"));
        in = getIsseueNavigator();

        previousRequest = ActionContext.getRequest();

        session = new MockHttpSession();
        final MockHttpServletRequest servletRequest = new MockHttpServletRequest(session);
        ActionContext.setRequest(servletRequest);
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.refreshIssueManager();
//        ManagerFactory.removeService(IssueIndexManager.class);
        ManagerFactory.removeService(FieldVisibilityBean.class);
        ManagerFactory.removeService(FieldManager.class);
        ManagerFactory.addService(FieldManager.class, oldFieldManager);
        ActionContext.getSession().remove(SessionKeys.SEARCH_PAGER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_SORTER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_REQUEST);
        ActionContext.setRequest(previousRequest);

        in = null;

        super.tearDown();
    }

    public void testDoValidation1() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("reset", new String[] { "true" });

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.SUCCESS, result);
        assertTrue(in.getErrors().isEmpty());
    }

    public void testDoValidation2() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("reset", new String[] { "true" });
        map.put("reporter", new String[] { "Test Reporter 1" });
        map.put("assignee", new String[] { "Test Assignee 1" });

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.SUCCESS, result);
        assertTrue(in.getErrors().isEmpty());
    }

    public void testDoValidation3() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("reset", new String[] { "true" });
        map.put("reporterSelect", new String[] { DocumentConstants.SPECIFIC_USER });
        map.put("reporter", new String[] { "Test Reporter 1000" });
        map.put("assigneeSelect", new String[] { DocumentConstants.SPECIFIC_USER });
        map.put("assignee", new String[] { "Test Assignee 1000" });

        ActionContext.setParameters(map);
        final String result = in.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(!in.getErrors().isEmpty());
        assertEquals(2, in.getErrors().size());
        assertEquals("Could not find username: Test Reporter 1000", in.getErrors().get("reporter"));
        assertEquals("Could not find username: Test Assignee 1000", in.getErrors().get("assignee"));
    }

    public void testDoValidation4() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("reset", new String[] { "true" });
        map.put("reporterSelect", new String[] { DocumentConstants.SPECIFIC_USER });
        map.put("reporter", new String[] { "Test Reporter 1000" });
        map.put("assigneeSelect", new String[] { DocumentConstants.SPECIFIC_GROUP });
        map.put("assignee", new String[] { "Test Group 1000" });

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(!in.getErrors().isEmpty());
        assertEquals(2, in.getErrors().size());
        assertEquals("Could not find username: Test Reporter 1000", in.getErrors().get("reporter"));
        assertEquals("Could not find group: Test Group 1000", in.getErrors().get("assignee"));
    }

    public void testValidationIssueTypes() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("reset", new String[] { "true" });
        map.put("type", new String[] { ConstantsManager.ALL_STANDARD_ISSUE_TYPES, "1" });
        ActionContext.setParameters(map);

        String result = in.execute();
        assertEquals(Action.SUCCESS, result);
        assertTrue(in.getErrors().isEmpty());
    }

    public void testDoExecute1() throws Exception
    {
        final HashMap map = new HashMap();
        map.put("createNew", "");

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.SUCCESS, result);
    }

    public void testDoExecute3() throws Exception
    {
        final User u = createMockUser("Test User 1");
        JiraTestUtil.loginUser(u);

        final SearchRequest sr = createBlankSR(u);
        sr.setName("Name");
        sr.setDescription("Description");
        sr.setOwnerUserName("Test User 1");

        final SearchRequest searchRequest = ManagerFactory.getSearchRequestManager().create(sr);
        final HashMap map = new HashMap();
        map.put("requestId", new String[] { searchRequest.getId().toString() });

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.SUCCESS, result);
    }

    private IssueNavigator getIsseueNavigator()
    {
        final MockController controller = MockController.createNiceContoller();
        final SearchService searchService = controller.getMock(SearchService.class);
        searchService.doesQueryFitFilterForm(EasyMock.<User>anyObject(), EasyMock.<Query>anyObject());
        controller.setDefaultReturnValue(true);
        controller.replay();

        return new IssueNavigator(ComponentManager.getComponentInstanceOfType(SearchProvider.class),
            ComponentAccessor.getFieldManager().getColumnLayoutManager(), ComponentManager.getComponentInstanceOfType(IssueSearcherManager.class),
            ComponentManager.getInstance().getSearchRequestFactory(), ComponentManager.getInstance().getSearchRequestService(), null, null, null, new PagerManager(null, sessionSearchObjectManagerFactory), searchService, ComponentManager.getComponentInstanceOfType(ApplicationProperties.class), new MockIndexLifecycleManager(), null, null, null, null, null, null, webResourceManager, avatarService, eventPublisher);
    }

    private SearchRequest createBlankSR(final User u)
    {
        final SearchRequest request = new SearchRequest(new QueryImpl());
        request.setQuery(new QueryImpl());
        return request;
    }

    public void testIsHasSearchRequestColumnLayoutNullSearchRequest() throws Exception
    {

        assertFalse(in.isHasSearchRequestColumnLayout());
    }

    public void testIsHasSearchRequestColumnLayoutSearchRequest() throws Exception
    {
        // Create search request
        final User u = createMockUser("Test User 1");
        JiraTestUtil.loginUser(u);

        final SearchRequest sr = createBlankSR(u);
        session.setAttribute(SessionKeys.SEARCH_REQUEST, sr);

        // Create Field Manager mock
        final Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.setStrict(true);

        // Create Column Layout Manager Mock
        final Mock mockColumnLayoutManager = new Mock(ColumnLayoutManager.class);
        mockColumnLayoutManager.setStrict(true);
        mockColumnLayoutManager.expectAndReturn("hasColumnLayout", P.args(new IsEqual(sr)), Boolean.TRUE);
        mockFieldManager.expectAndReturn("getColumnLayoutManager", mockColumnLayoutManager.proxy());
        ManagerFactory.addService(FieldManager.class, (FieldManager) mockFieldManager.proxy());

        // Test for true
        in = getIsseueNavigator();
        assertTrue(in.isHasSearchRequestColumnLayout());
        mockFieldManager.verify();
        mockColumnLayoutManager.verify();

        // Test for false
        mockColumnLayoutManager.expectAndReturn("hasColumnLayout", P.args(new IsEqual(sr)), Boolean.FALSE);
        mockFieldManager.expectAndReturn("getColumnLayoutManager", mockColumnLayoutManager.proxy());
        ManagerFactory.addService(FieldManager.class, (FieldManager) mockFieldManager.proxy());

        assertFalse(in.isHasSearchRequestColumnLayout());
        mockColumnLayoutManager.verify();
    }

    public void testIsShowConfigureUserColumnLayoutNullSearchRequest() throws ColumnLayoutStorageException
    {

        try
        {
            in.isShowOverrideColumnLayout();
            fail("IllegalStateException should have been thrown.");
        }
        catch (final IllegalStateException e)
        {
            assertEquals("Search Request does not exist.", e.getMessage());
        }
    }

    public void testIsShowConfigureUserColumnLayoutNoColumnLayout()
            throws ColumnLayoutStorageException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        // Create search request
        final SearchRequest sr = setupSearchRequest();

        setupColumnLayoutMock(sr, false);
        sr.setUseColumns(true);

        assertFalse(in.isShowOverrideColumnLayout());
    }

    public void testIsShowConfigureUserColumnLayoutColumnLayout()
            throws ColumnLayoutStorageException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        // Create search request
        final SearchRequest sr = setupSearchRequest();

        setupColumnLayoutMock(sr, true);
        sr.setUseColumns(true);

        in = getIsseueNavigator();
        assertTrue(in.isShowOverrideColumnLayout());
    }

    public void testIsShowConfigureUserColumnLayoutOverriddenColumnLayout()
            throws ColumnLayoutStorageException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        // Create search request
        final SearchRequest sr = setupSearchRequest();

        setupColumnLayoutMock(sr, true);
        sr.setUseColumns(false);

        assertFalse(in.isShowOverrideColumnLayout());
    }

    public void testIsShowConfigureUserColumnLayoutOverriddenNoColumnLayout()
            throws ColumnLayoutStorageException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        // Create search request
        final SearchRequest sr = setupSearchRequest();

        setupColumnLayoutMock(sr, false);
        sr.setUseColumns(false);

        assertFalse(in.isShowOverrideColumnLayout());
    }

    private SearchRequest setupSearchRequest()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User u = createMockUser("Test User 1");

        // ActionContext.getSession().put(DefaultAuthenticator.LOGGED_IN_KEY, u);
        final SearchRequest sr = createBlankSR(u);
        session.setAttribute(SessionKeys.SEARCH_REQUEST, sr);
        return sr;
    }

    private void setupColumnLayoutMock(final SearchRequest sr, final boolean hasColumnLayout)
    {
        // Create Field Manager mock
        final Mock mockFieldManager = new Mock(FieldManager.class);
        mockFieldManager.setStrict(true);

        // Create Column Layout Manager Mock
        final Mock mockColumnLayoutManager = new Mock(ColumnLayoutManager.class);
        mockColumnLayoutManager.setStrict(true);
        mockColumnLayoutManager.expectAndReturn("hasColumnLayout", P.args(new IsEqual(sr)), Boolean.valueOf(hasColumnLayout));
        mockFieldManager.expectAndReturn("getColumnLayoutManager", mockColumnLayoutManager.proxy());
        ManagerFactory.addService(FieldManager.class, (FieldManager) mockFieldManager.proxy());
    }

    public void testDoColumnOverrideNoSerachRequest() throws Exception
    {

        try
        {
            in.doColumnOverride();
            fail("IllegalStateException should have been thrown");
        }
        catch (final IllegalStateException e)
        {
            assertEquals("Search Request does not exist.", e.getMessage());
        }
    }

    public void testDoColumnOverrideSearchRequestOn() throws Exception
    {
        final SearchRequest searchRequest = setupSearchRequest();

        // Ensure useColumns is true
        searchRequest.setUseColumns(true);

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IssueNavigator.jspa");

        final String result = in.doColumnOverride();
        assertEquals(Action.NONE, result);
        assertFalse(in.getSearchRequest().useColumns());
        response.verify();
    }

    public void testDoColumnOverrideSearchRequestOff() throws Exception
    {
        final SearchRequest searchRequest = setupSearchRequest();

        // Ensure useColumns is false
        searchRequest.setUseColumns(false);

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("IssueNavigator.jspa");

        final String result = in.doColumnOverride();
        assertEquals(Action.NONE, result);
        assertTrue(in.getSearchRequest().useColumns());
        response.verify();
    }

    public void testDateValidationsCorrectFormat() throws Exception
    {
        int i;
        final HashMap map = new HashMap();
        final String[] correctDateStrs = { "01-Jan-2000", "1-jan-2005", "1-JAN-2005", "31-jaN-1999", "31-december-2004" };
        final String[] dateParamNames = { "createdAfter", "createdBefore", "updatedAfter", "updatedBefore", "duedateAfter", "duedateBefore" };

        map.put("reset", new String[] { "true" });
        for (i = 0; i < correctDateStrs.length; i++)
        {
            map.put(dateParamNames[0], correctDateStrs[i]);

            ActionContext.setParameters(map);

            assertEquals(Action.SUCCESS, in.execute());
        }
    }

    public void testDateValidationsWrongFormat() throws Exception
    {
        int i;
        final HashMap map = new HashMap();
        final String[] correctDateStrs = { "01-01x2000", "1/jan-2005", "1-JAN/2005", "31-jaN-1999", "31-december-2004", "31dec2004" };
        final String[] dateParamNames = { "created:after", "created:before", "updated:after", "updated:before", "duedate:after", "duedate:before" };

        map.put("reset", new String[] { "true" });
        for (i = 0; i < dateParamNames.length; i++)
        {
            map.put(dateParamNames[i], new String[] { correctDateStrs[i] });
        }

        ActionContext.setParameters(map);

        final String result = in.execute();
        assertEquals(Action.ERROR, result);
        assertEquals(in.getErrors().size(), i);
        for (i = 0; i < dateParamNames.length; i++)
        {
            assertTrue(in.getErrors().get(dateParamNames[i]).equals("Invalid date format. Please enter the date in the format \"d/MMM/yy\"."));
        }
    }

    public void testDatesPriorTo1970() throws Exception
    {
        final User u = createMockUser("Test User 1");
        JiraTestUtil.loginUser(u);

        final String[] yearEarly = { "0", "1", "100", "1035", "1969" };

        int i, j;
        final HashMap map = new HashMap();
        final String[] dateParamNames = { "created:after", "created:before", "updated:after", "updated:before", "duedate:after", "duedate:before" };

        for (i = 0; i < yearEarly.length; i++)
        {
            String dateStr = "01/Jan/" + yearEarly[i];

            // Need to format the string in the system locale format, so that this test
            // passes on systems where the locale is not US
            final SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", Locale.US);
            final Date d = sdf.parse(dateStr);
            final SimpleDateFormat systemLocaleFormat = new SimpleDateFormat("dd/MMM/yyyy");
            dateStr = systemLocaleFormat.format(d);

            map.put("reset", new String[] { "true" });
            for (j = 0; j < dateParamNames.length; j++)
            {
                map.put(dateParamNames[j], new String[] { dateStr });
            }

            in = getIsseueNavigator();
            ActionContext.setParameters(map);

            final String result = in.execute();
            assertEquals(Action.SUCCESS, result);
        }
    }


    public void testGetBulkEditMax()
    {
        IssueNavigator in = new IssueNavigator(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, avatarService, eventPublisher);
        assertEquals(5, in.getBulkEditMax(5, "500"));
        assertEquals(5, in.getBulkEditMax(5, "5"));
        assertEquals(7, in.getBulkEditMax(7, null));
        assertEquals(35, in.getBulkEditMax(35, "-1"));
        assertEquals(225, in.getBulkEditMax(225, "0"));
        assertEquals(50, in.getBulkEditMax(50, "-100"));
        assertEquals(5, in.getBulkEditMax(5, ""));
        assertEquals(5, in.getBulkEditMax(5, "foo"));
        assertEquals(1234, in.getBulkEditMax(55555, "1234"));
    }
}
