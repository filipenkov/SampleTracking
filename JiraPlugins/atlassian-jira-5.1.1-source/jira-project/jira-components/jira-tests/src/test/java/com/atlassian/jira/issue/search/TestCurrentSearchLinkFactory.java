package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestCurrentSearchLinkFactory extends MockControllerTestCase
{

    private VelocityRequestContext requestContext;
    private VelocityRequestSession requestSession;
    private VelocityRequestContextFactory requestContextFactory;
    private I18nHelper.BeanFactory i18nFactory;
    private I18nHelper i18n;
    private SearchProvider searchProvider;

    private User user;

    private CurrentSearchLinkFactory linkFactory;
    private SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory;
    private SessionSearchRequestManager sessionSearchRequestManager;

    @Before
    public void setUp() throws Exception
    {
        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestSession = mockController.getMock(VelocityRequestSession.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);
        searchProvider = mockController.getMock(SearchProvider.class);
        sessionSearchObjectManagerFactory = mockController.getMock(SessionSearchObjectManagerFactory.class);
        sessionSearchRequestManager = mockController.getMock(SessionSearchRequestManager.class);

        user = new MockUser("admin");

        linkFactory = new CurrentSearchLinkFactory(requestContextFactory, i18nFactory, searchProvider, sessionSearchObjectManagerFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        requestContext = null;
        requestSession = null;
        requestContextFactory = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        i18n = null;
        searchProvider = null;
    }

    @Test
    public void testNullSession()
    {
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testNullFilterInSession()
    {
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(null);

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testErrorThrownDuringSearchSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "Filter 1", "Filter 1 - Filter Description", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setThrowable(new SearchException());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18n.getText("menu.issues.current.search.title", "Filter 1", "Filter Description");
        mockController.setReturnValue("Filter 1 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testErrorThrownDuringSearchUnSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", null, null, null, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_unsaved", "Unsaved", "Unsaved", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setThrowable(new SearchException());

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        i18n.getText("menu.issues.current.search.unsaved");
        mockController.setReturnValue("Unsaved");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testNoResultsSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "Filter 1 (No issues)", "Filter 1 - Filter Description", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(0L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.no.issues", "Filter 1");
        mockController.setReturnValue("Filter 1 (No issues)");

        i18n.getText("menu.issues.current.search.title", "Filter 1", "Filter Description");
        mockController.setReturnValue("Filter 1 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testNoResultsUnSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", null, null, null, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_unsaved", "Unsaved (No issues)", "Unsaved", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(0L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.unsaved");
        mockController.setReturnValue("Unsaved");

        i18n.getText("menu.issues.current.search.no.issues", "Unsaved");
        mockController.setReturnValue("Unsaved (No issues)");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


    @Test
    public void testOneResultsSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "Filter 1 (One issue)", "Filter 1 - Filter Description", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(1L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.one.issue", "Filter 1");
        mockController.setReturnValue("Filter 1 (One issue)");

        i18n.getText("menu.issues.current.search.title", "Filter 1", "Filter Description");
        mockController.setReturnValue("Filter 1 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneResultsUnSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", null, null, null, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_unsaved", "Unsaved (One issue)", "Unsaved", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(1L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.unsaved");
        mockController.setReturnValue("Unsaved");

        i18n.getText("menu.issues.current.search.one.issue", "Unsaved");
        mockController.setReturnValue("Unsaved (One issue)");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


    @Test
    public void testManyResultsSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", null, 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "Filter 1 (666 issues)", "Filter 1", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(666L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.issues", "Filter 1", "666");
        mockController.setReturnValue("Filter 1 (666 issues)");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testManyResultsSavedFitlerWithLongName() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "123456789012345678901234567890", "Description of filter with long name", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "123456789012345678901234567890 (666 issues)", "123456789012345678901234567890 - Description of filter with long name", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(666L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.issues", "123456789012345678901234567890", "666");
        mockController.setReturnValue("123456789012345678901234567890 (666 issues)");

        i18n.getText("menu.issues.current.search.title", "123456789012345678901234567890", "Description of filter with long name");
        mockController.setReturnValue("123456789012345678901234567890 - Description of filter with long name");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testManyResultsSavedFitlerWithTooLongName() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", "1234567890123456789012345678901234567890", "Description of filter with long name", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_1", "123456789012345678901234567890... (666 issues)", "1234567890123456789012345678901234567890 - Description of filter with long name", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(666L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.issues", "123456789012345678901234567890...", "666");
        mockController.setReturnValue("123456789012345678901234567890... (666 issues)");

        i18n.getText("menu.issues.current.search.title", "1234567890123456789012345678901234567890", "Description of filter with long name");
        mockController.setReturnValue("1234567890123456789012345678901234567890 - Description of filter with long name");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testManyResultsUnSavedFitler() throws SearchException
    {
        SearchRequest sr = new SearchRequest(null, "admin", null, null, null, 1);
        SimpleLink sl = new SimpleLinkImpl("curr_search_lnk_unsaved", "Unsaved (666 issues)", "Unsaved", null, null, "/secure/IssueNavigator.jspa?mode=hide", null);
        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getSession();
        mockController.setReturnValue(requestSession);

        setupSessionSearchRequestManager(sr);

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        searchProvider.searchCount(null, user);
        mockController.setReturnValue(666L);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        i18n.getText("menu.issues.current.search.unsaved");
        mockController.setReturnValue("Unsaved");

        i18n.getText("menu.issues.current.search.issues", "Unsaved", "666");
        mockController.setReturnValue("Unsaved (666 issues)");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    private void setupSessionSearchRequestManager(final SearchRequest sr)
    {
        expect(sessionSearchObjectManagerFactory.createSearchRequestManager(requestSession))
                .andReturn(sessionSearchRequestManager);

        expect(sessionSearchRequestManager.getCurrentObject())
                .andReturn(sr);
    }
}
