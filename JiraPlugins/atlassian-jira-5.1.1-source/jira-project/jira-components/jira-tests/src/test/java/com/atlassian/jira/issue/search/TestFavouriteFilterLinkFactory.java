package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestFavouriteFilterLinkFactory extends MockControllerTestCase
{
    private SearchRequestService searchRequestService;
    private VelocityRequestContext requestContext;
    private VelocityRequestContextFactory requestContextFactory;
    private ApplicationProperties applicationProperties;
    private I18nHelper.BeanFactory i18nFactory;
    private I18nHelper i18n;

    private User user;

    private FavouriteFilterLinkFactory linkFactory;

    @Before
    public void setUp() throws Exception
    {
        searchRequestService = mockController.getMock(SearchRequestService.class);
        requestContext = mockController.getMock(VelocityRequestContext.class);
        requestContextFactory = mockController.getMock(VelocityRequestContextFactory.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        i18nFactory = mockController.getMock(I18nHelper.BeanFactory.class);
        i18n = mockController.getMock(I18nHelper.class);

        user = new MockUser("admin");

        linkFactory = new FavouriteFilterLinkFactory(searchRequestService, requestContextFactory, applicationProperties, i18nFactory);
    }

    @After
    public void tearDown() throws Exception
    {
        searchRequestService = null;
        requestContext = null;
        requestContextFactory = null;
        applicationProperties = null;
        linkFactory = null;
        user = null;
        i18nFactory = null;
        i18n = null;

    }

    @Test
    public void testNullFiltersNullUser()
    {
        searchRequestService.getFavouriteFilters(null);
        mockController.setReturnValue(null);

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }


    @Test
    public void testEmptyFiltersNullUser()
    {
        searchRequestService.getFavouriteFilters(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        assertTrue(linkFactory.getLinks(null, null).isEmpty());

        mockController.verify();
    }

    @Test
    public void testOneFilterWithBaseUrl()
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1 - Filter Description", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        i18n.getText("menu.issues.filter.title", "Filter 1", "Filter Description");
        mockController.setReturnValue("Filter 1 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneFilterWithLongLabel()
    {
        SearchRequest sr = new SearchRequest(null, "admin", "123456789012345678901234567890", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("filter_lnk_1", "123456789012345678901234567890", "123456789012345678901234567890 - Filter Description", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        i18n.getText("menu.issues.filter.title", "123456789012345678901234567890", "Filter Description");
        mockController.setReturnValue("123456789012345678901234567890 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneFilterWithTooLongLabel()
    {
        SearchRequest sr = new SearchRequest(null, "admin", "12345678901234567890123456789012345678901234567890", "Filter Description", 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("filter_lnk_1", "123456789012345678901234567890...", "12345678901234567890123456789012345678901234567890 - Filter Description", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        i18n.getText("menu.issues.filter.title", "12345678901234567890123456789012345678901234567890", "Filter Description");
        mockController.setReturnValue("12345678901234567890123456789012345678901234567890 - Filter Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testOneFilterWithNoBaseUrl()
    {
        SearchRequest sr = new SearchRequest(null, "admin", "Filter 1", null, 1L, 1);
        SimpleLink sl = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(sl).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testMultipleFiltersWithNoBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, "admin", "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, "admin", "Filter 2", null, 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, "admin", "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, "admin", "Filter 4", null, 4L, 4);
        SimpleLink s1 = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1 - Filter Description1", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);
        SimpleLink s2 = new SimpleLinkImpl("filter_lnk_2", "Filter 2", "Filter 2", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=2", null);
        SimpleLink s3 = new SimpleLinkImpl("filter_lnk_3", "Filter 3", "Filter 3 - Filter Description3", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=3", null);
        SimpleLink s4 = new SimpleLinkImpl("filter_lnk_4", "Filter 4", "Filter 4", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=4", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr1, sr2, sr3, sr4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("10");

        i18n.getText("menu.issues.filter.title", "Filter 1", "Filter Description1");
        mockController.setReturnValue("Filter 1 - Filter Description1");

        i18n.getText("menu.issues.filter.title", "Filter 3", "Filter Description3");
        mockController.setReturnValue("Filter 3 - Filter Description3");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(s1, s2, s3, s4).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testMultipleFiltersWithNoBaseUrlInvalidMaxProperty()
    {
        SearchRequest sr1 = new SearchRequest(null, "admin", "Filter 1", null, 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, "admin", "Filter 2", "Filter Description2", 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, "admin", "Filter 3", null, 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, "admin", "Filter 4", "Filter Description4", 4L, 4);
        SimpleLink s1 = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);
        SimpleLink s2 = new SimpleLinkImpl("filter_lnk_2", "Filter 2", "Filter 2 - Filter Description2", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=2", null);
        SimpleLink s3 = new SimpleLinkImpl("filter_lnk_3", "Filter 3", "Filter 3", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=3", null);
        SimpleLink s4 = new SimpleLinkImpl("filter_lnk_4", "Filter 4", "Filter 4 - Filter Description4", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=4", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr1, sr2, sr3, sr4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("Nick Rocks");

        i18n.getText("menu.issues.filter.title", "Filter 2", "Filter Description2");
        mockController.setReturnValue("Filter 2 - Filter Description2");

        i18n.getText("menu.issues.filter.title", "Filter 4", "Filter Description4");
        mockController.setReturnValue("Filter 4 - Filter Description4");


        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(s1, s2, s3, s4).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


    @Test
    public void testTooManyFiltersWithNoBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, "admin", "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, "admin", "Filter 2", "Filter Description2", 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, "admin", "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, "admin", "Filter 4", "Filter Description4", 4L, 4);
        SimpleLink s1 = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1 - Filter Description1", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);
        SimpleLink s2 = new SimpleLinkImpl("filter_lnk_2", "Filter 2", "Filter 2 - Filter Description2", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=2", null);
        SimpleLink s3 = new SimpleLinkImpl("filter_lnk_3", "Filter 3", "Filter 3 - Filter Description3", null, null, "/secure/IssueNavigator.jspa?mode=hide&requestId=3", null);
        SimpleLink sMore = new SimpleLinkImpl("filter_lnk_more", "more...", "More Description", null, null, "/secure/ManageFilters.jspa?filterView=favourites", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr1, sr2, sr3, sr4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("3");

        i18n.getText("menu.issues.filter.title", "Filter 1", "Filter Description1");
        mockController.setReturnValue("Filter 1 - Filter Description1");

        i18n.getText("menu.issues.filter.title", "Filter 2", "Filter Description2");
        mockController.setReturnValue("Filter 2 - Filter Description2");

        i18n.getText("menu.issues.filter.title", "Filter 3", "Filter Description3");
        mockController.setReturnValue("Filter 3 - Filter Description3");


        i18n.getText("menu.issues.filter.more");
        mockController.setReturnValue("more...");
        i18n.getText("menu.issues.filter.more.desc");
        mockController.setReturnValue("More Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(s1, s2, s3, sMore).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }

    @Test
    public void testTooManyFiltersWithBaseUrl()
    {
        SearchRequest sr1 = new SearchRequest(null, "admin", "Filter 1", "Filter Description1", 1L, 1);
        SearchRequest sr2 = new SearchRequest(null, "admin", "Filter 2", "Filter Description2", 2L, 2);
        SearchRequest sr3 = new SearchRequest(null, "admin", "Filter 3", "Filter Description3", 3L, 3);
        SearchRequest sr4 = new SearchRequest(null, "admin", "Filter 4", "Filter Description4", 4L, 4);
        SimpleLink s1 = new SimpleLinkImpl("filter_lnk_1", "Filter 1", "Filter 1 - Filter Description1", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=1", null);
        SimpleLink s2 = new SimpleLinkImpl("filter_lnk_2", "Filter 2", "Filter 2 - Filter Description2", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=2", null);
        SimpleLink s3 = new SimpleLinkImpl("filter_lnk_3", "Filter 3", "Filter 3 - Filter Description3", null, null, "/jira/secure/IssueNavigator.jspa?mode=hide&requestId=3", null);
        SimpleLink sMore = new SimpleLinkImpl("filter_lnk_more", "more...", "More Description", null, null, "/jira/secure/ManageFilters.jspa?filterView=favourites", null);

        searchRequestService.getFavouriteFilters(user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(sr1, sr2, sr3, sr4).asList());

        requestContextFactory.getJiraVelocityRequestContext();
        mockController.setReturnValue(requestContext);
        i18nFactory.getInstance(user);
        mockController.setReturnValue(i18n);

        requestContext.getBaseUrl();
        mockController.setReturnValue("/jira");

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_FILTER_DROPDOWN_ITEMS);
        mockController.setReturnValue("3");

        i18n.getText("menu.issues.filter.title", "Filter 1", "Filter Description1");
        mockController.setReturnValue("Filter 1 - Filter Description1");

        i18n.getText("menu.issues.filter.title", "Filter 2", "Filter Description2");
        mockController.setReturnValue("Filter 2 - Filter Description2");

        i18n.getText("menu.issues.filter.title", "Filter 3", "Filter Description3");
        mockController.setReturnValue("Filter 3 - Filter Description3");

        i18n.getText("menu.issues.filter.more");
        mockController.setReturnValue("more...");
        i18n.getText("menu.issues.filter.more.desc");
        mockController.setReturnValue("More Description");

        mockController.replay();

        List<SimpleLink> returnList = linkFactory.getLinks(user, null);

        List<SimpleLink> expectedList = CollectionBuilder.newBuilder(s1, s2, s3, sMore).asList();

        assertEquals(expectedList, returnList);

        mockController.verify();
    }


}
