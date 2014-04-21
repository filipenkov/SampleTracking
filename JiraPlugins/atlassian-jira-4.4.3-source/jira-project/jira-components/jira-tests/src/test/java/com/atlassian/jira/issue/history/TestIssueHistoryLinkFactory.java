package com.atlassian.jira.issue.history;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.query.Query;
import mock.user.MockOSUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestIssueHistoryLinkFactory extends MockControllerTestCase
{

    private UserIssueHistoryManager historyManager;
    private VelocityRequestContext requestContext;
    private VelocityRequestSession requestSession;
    private VelocityRequestContextFactory requestContextFactory;
    private ApplicationProperties applicationProperties;
    private I18nHelper.BeanFactory i18nFactory;
    private SearchService searchService;
    private I18nHelper i18n;

    private com.opensymphony.user.User user;

    private IssueHistoryLinkFactory linkFactory;

    @Before
    public void setUp() throws Exception
    {
        historyManager = mockController.getMock(UserIssueHistoryManager.class);
        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestSession = mockController.getMock(VelocityRequestSession.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);
        searchService = mockController.getMock(SearchService.class);

        user = new MockOSUser("admin");

        linkFactory = new IssueHistoryLinkFactory(requestContextFactory, historyManager, applicationProperties, searchService, i18nFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        historyManager = null;
        searchService = null;
        requestContext = null;
        requestSession = null;
        requestContextFactory = null;
        applicationProperties = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        i18n = null;

    }

    @Test
    public void testNullUserNullHistory()
    {
        historyManager.getShortIssueHistory(null);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testNullUserEmptyHistory()
    {
        historyManager.getShortIssueHistory(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testEmptyHistory()
    {
        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        assertTrue(linkFactory.getLinks(user, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testOneIssueHistory()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1", "/jira/images/bug.gif", null,
                null, "/jira/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testOneIssueHistoryWithLongSummary()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("12345678901234567890123");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 12345678901234567890123", "TEST-1 12345678901234567890123", "/jira/images/bug.gif",
                null, null, "/jira/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testOneIssueHistoryWithTooLongSummary()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("123456789012345678901234567890");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 12345678901234567890123...",
                "TEST-1 123456789012345678901234567890", "/jira/images/bug.gif", null, null, "/jira/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testOneIssueHistoryWithAbsoluteIconUrl()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("http://images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1", "http://images/bug.gif",
                null, null, "/jira/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testOneIssueHistoryWithAbsoluteSecureIconUrl()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("https://images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1",
                "https://images/bug.gif", null, null, "/jira/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testOneIssueHistoryNoContext()
    {
        MockIssue issue = new MockIssue(1L);
        issue.setKey("TEST-1");
        issue.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue.setIssueTypeObject(mockIssueType);

        SimpleLink link = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1", "/images/bug.gif",
                null, null, "/browse/TEST-1", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testManyIssuesHistory()
    {
        MockIssue issue1 = new MockIssue(1L);
        issue1.setKey("TEST-1");
        issue1.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue1.setIssueTypeObject(mockIssueType);

        MockIssue issue2 = new MockIssue(2L);
        issue2.setKey("TEST-2");
        issue2.setSummary("Summary 2");
        final MockIssueType mockIssueType2 = new MockIssueType("type2", "type2name");
        mockIssueType2.setIconUrl("/images/task.gif");
        issue2.setIssueTypeObject(mockIssueType2);

        MockIssue issue3 = new MockIssue(3L);
        issue3.setKey("TEST-3");
        issue3.setSummary("Summary 3");
        final MockIssueType mockIssueType3 = new MockIssueType("type3", "type3name");
        mockIssueType3.setIconUrl("/images/feature.gif");
        issue3.setIssueTypeObject(mockIssueType3);

        MockIssue issue4 = new MockIssue(4L);
        issue4.setKey("TEST-4");
        issue4.setSummary("Summary 4");
        final MockIssueType mockIssueType4 = new MockIssueType("type4", "type4name");
        mockIssueType4.setIconUrl("/images/improv.gif");
        issue4.setIssueTypeObject(mockIssueType4);

        SimpleLink link1 = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1",
                "/jira/images/bug.gif", null, null, "/jira/browse/TEST-1", null);
        SimpleLink link2 = new SimpleLinkImpl("issue_lnk_2", "TEST-2 Summary 2", "TEST-2 Summary 2",
                "/jira/images/task.gif", null, null, "/jira/browse/TEST-2", null);
        SimpleLink link3 = new SimpleLinkImpl("issue_lnk_3", "TEST-3 Summary 3", "TEST-3 Summary 3",
                "/jira/images/feature.gif", null, null, "/jira/browse/TEST-3", null);
        SimpleLink link4 = new SimpleLinkImpl("issue_lnk_4", "TEST-4 Summary 4", "TEST-4 Summary 4",
                "/jira/images/improv.gif", null, null, "/jira/browse/TEST-4", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue1, issue2, issue3, issue4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link1, link2, link3, link4).asList();

        assertEquals(returnedList, expectedList);

        mockController.verify();
    }

    @Test
    public void testTooManyIssuesHistory()
    {
        MockIssue issue1 = new MockIssue(1L);
        issue1.setKey("TEST-1");
        issue1.setSummary("Summary 1");
        final MockIssueType mockIssueType = new MockIssueType("type1", "type1name");
        mockIssueType.setIconUrl("/images/bug.gif");
        issue1.setIssueTypeObject(mockIssueType);

        MockIssue issue2 = new MockIssue(2L);
        issue2.setKey("TEST-2");
        issue2.setSummary("Summary 2");
        final MockIssueType mockIssueType2 = new MockIssueType("type2", "type2name");
        mockIssueType2.setIconUrl("/images/task.gif");
        issue2.setIssueTypeObject(mockIssueType2);

        MockIssue issue3 = new MockIssue(3L);
        issue3.setKey("TEST-3");
        issue3.setSummary("Summary 3");

        MockIssue issue4 = new MockIssue(4L);
        issue4.setKey("TEST-4");
        issue4.setSummary("Summary 4");

        SimpleLink link1 = new SimpleLinkImpl("issue_lnk_1", "TEST-1 Summary 1", "TEST-1 Summary 1",
                "/jira/images/bug.gif", null, null, "/jira/browse/TEST-1", null);
        SimpleLink link2 = new SimpleLinkImpl("issue_lnk_2", "TEST-2 Summary 2", "TEST-2 Summary 2",
                "/jira/images/task.gif", null, null, "/jira/browse/TEST-2", null);
        SimpleLink linkMore = new SimpleLinkImpl("issue_lnk_more", "More...", "More Description", null, null,
                null, "/jira/secure/IssueNavigator.jspa?reset=true&mode=hide&jql=Nick-Rocks", null);

        historyManager.getShortIssueHistory(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(issue1, issue2, issue3, issue4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("3");

        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        final Query query = JqlQueryBuilder.newBuilder().where().issueInHistory().buildQuery();
        searchService.getQueryString(user, query);
        mockController.setReturnValue("&jql=Nick-Rocks");

        i18n.getText("menu.issues.history.more");
        mockController.setReturnValue("More...");
        i18n.getText("menu.issues.history.more.desc");
        mockController.setReturnValue("More Description");

        mockController.replay();

        final List<SimpleLink> returnedList = linkFactory.getLinks(user, null);

        // We actually expect 1 less than the number defined.  This allows us to have a more link.
        final List<SimpleLink> expectedList = CollectionBuilder.newBuilder(link1, link2, linkMore).asList();

        assertEquals(expectedList, returnedList);

        mockController.verify();
    }


}
