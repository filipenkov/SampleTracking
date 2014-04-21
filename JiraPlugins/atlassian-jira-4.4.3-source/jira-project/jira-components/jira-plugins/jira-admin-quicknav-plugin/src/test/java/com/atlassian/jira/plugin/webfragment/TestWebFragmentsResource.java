package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.admin.quicknav.SimpleLinkAliasProvider;
import com.atlassian.jira.admin.quicknav.WebFragmentsResource;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkImpl;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test case for {@link WebFragmentsResource}.
 *
 * @since v4.2
 */
public class TestWebFragmentsResource
{
    @Mock(MockType.NICE)
    private SimpleLinkAliasProvider keywordsProvider;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);
    }

    private JiraAuthenticationContext newMockAuthenticationContext(boolean authenticated)
    {
        JiraAuthenticationContext newMock = createNiceMock(JiraAuthenticationContext.class);
        if (authenticated)
        {
            expect(newMock.getLoggedInUser()).andReturn(new MockUser("mock")).anyTimes();
        }
        replay(newMock);
        return newMock;
    }


    @Test
    public void testGetAllItemsWithData()
    {
        WebFragmentsResource tested = new WebFragmentsResource(newLinkManagerWithData(),
                newMockAuthenticationContext(true), keywordsProvider);
        replay(keywordsProvider);
        Response result = tested.getWebFragments(null, "system.admin", null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity() instanceof WebFragmentsResource.WebFragmentLocation);
        WebFragmentsResource.WebFragmentLocation resultList = (WebFragmentsResource.WebFragmentLocation) result.getEntity();
        assertEquals(1, resultList.webItems().size());
        checkItem("system.admin", "main", resultList.webItems().get(0));
        assertEquals(2, resultList.webSections().size());
        checkSection("system.admin", "section1", resultList.webSections().get(0));
        checkSection("system.admin", "section2", resultList.webSections().get(1));

        final List<WebFragmentsResource.WebItem> section1Items = resultList.webSections().get(0).webItems();
        assertEquals(3, section1Items.size());
        checkItem("system.admin/section1", "one", section1Items.get(0));
        checkItem("system.admin/section1", "two", section1Items.get(1));
        checkItem("system.admin/section1", "three", section1Items.get(2));

        final List<WebFragmentsResource.WebItem> section2Items = resultList.webSections().get(1).webItems();
        assertEquals(3, section2Items.size());
        checkItem("system.admin/section2", "four", section2Items.get(0));
        checkItem("system.admin/section2", "five", section2Items.get(1));
        checkItem("system.admin/section2", "six", section2Items.get(2));
    }

    @Test
    public void testGetItemsForSection()
    {
        WebFragmentsResource tested = new WebFragmentsResource(newLinkManagerWithData(),
                newMockAuthenticationContext(true), keywordsProvider);
        replay(keywordsProvider);
        Response result = tested.getWebFragments(null, "system.admin", "section1");
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity() instanceof WebFragmentsResource.WebFragmentLocation);
        WebFragmentsResource.WebFragmentLocation resultList = (WebFragmentsResource.WebFragmentLocation) result.getEntity();
        assertEquals(3, resultList.webItems().size());
        checkItem("system.admin/section1", "one", resultList.webItems().get(0));
        checkItem("system.admin/section1", "two", resultList.webItems().get(1));
        checkItem("system.admin/section1", "three", resultList.webItems().get(2));
    }

    @Test
    public void testGetItemsWithoutData()
    {
        WebFragmentsResource tested = new WebFragmentsResource(newLinkManagerWithoutData(),
                newMockAuthenticationContext(true), keywordsProvider);
        Response result = tested.getWebFragments(null, "system.admin", null);
        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        assertTrue(result.getEntity() instanceof WebFragmentsResource.WebFragmentLocation);
        WebFragmentsResource.WebFragmentLocation resultList = (WebFragmentsResource.WebFragmentLocation) result.getEntity();
        assertTrue(resultList.webItems().isEmpty());
    }

    @Test
    public void testNotAuthenticatedRequest()
    {
        WebFragmentsResource tested = new WebFragmentsResource(newLinkManagerWithData(),
                newMockAuthenticationContext(false), keywordsProvider);
        try
        {
            tested.getWebFragments(null, "system.admin/section1", null);
            fail("Expected web application exception");
        }
        catch (WebApplicationException expected)
        {
            Response response = expected.getResponse();
            assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
            assertNull(response.getEntity());
        }
    }

    @Test
    public void testRequestWithoutLocation()
    {
        WebFragmentsResource tested = new WebFragmentsResource(newLinkManagerWithData(),
                newMockAuthenticationContext(true), keywordsProvider);
        try
        {
            tested.getWebFragments(null, null, null);
            fail("Expected web application exception");
        }
        catch (WebApplicationException expected)
        {
            Response response = expected.getResponse();
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals("Parameter 'location' required", response.getEntity());
        }
    }

    private void checkItem(String section, String key, WebFragmentsResource.WebItem webItem)
    {
        assertEquals(linkIdFor(key), webItem.key());
        assertEquals(linkUrlFor(key), webItem.linkUrl());
        assertEquals(labelFor(key), webItem.label());
        assertEquals(section, webItem.section());
        assertEquals(keywordsListFor(key), new ArrayList<String>(webItem.keywords()));
    }


    private void checkSection(String location, String key, WebFragmentsResource.WebSection webSection)
    {
        assertEquals(key, webSection.key());
        assertEquals(location, webSection.location());
        assertEquals(webSection.location(), location);
    }

    private String linkIdFor(String order)
    {
        return String.format("link%s", order);
    }
    private String linkUrlFor(String order)
    {
        return String.format("http://label.%s", order);
    }
    private String labelFor(String order)
    {
        return String.format("label%s", order);
    }
    private List<String> keywordsListFor(final String key)
    {
        return ImmutableList.of(key, "label");
    }

    private SimpleLinkManager newLinkManagerWithoutData()
    {
        SimpleLinkManager mockManager = createNiceMock(SimpleLinkManager.class);
        expect(mockManager.getSectionsForLocation(eq("system.admin"), any(User.class), any(JiraHelper.class)))
                .andReturn(Collections.<SimpleLinkSection>emptyList()).anyTimes();
        expect(mockManager.getLinksForSection(eq("system.admin"), any(User.class), any(JiraHelper.class)))
                .andReturn(Collections.<SimpleLink>emptyList()).anyTimes();
        replay(mockManager);
        return mockManager;
    }

    private SimpleLinkManager newLinkManagerWithData()
    {
        final SimpleLinkManager mockManager = createNiceMock(SimpleLinkManager.class);
        expect(mockManager.getLinksForSection(eq("system.admin/section1"), any(User.class), any(JiraHelper.class)))
                .andReturn(linksInSection1()).anyTimes();
        expect(mockManager.getLinksForSection(eq("system.admin/section2"), any(User.class), any(JiraHelper.class)))
                .andReturn(linksInSection2()).anyTimes();
        expect(mockManager.getLinksForSection(eq("system.admin"), any(User.class), any(JiraHelper.class)))
                .andReturn(linksInMainLocation()).anyTimes();
        expect(mockManager.getSectionsForLocation(eq("system.admin"), any(User.class), any(JiraHelper.class)))
                .andReturn(someSections()).anyTimes();
        expect(mockManager.getSectionsForLocation(eq("section1"),  any(User.class), any(JiraHelper.class)))
                .andReturn(Collections.<SimpleLinkSection>emptyList()).anyTimes();
        expect(mockManager.getSectionsForLocation(eq("section2"),  any(User.class), any(JiraHelper.class)))
                .andReturn(Collections.<SimpleLinkSection>emptyList()).anyTimes();
        replay(mockManager);
        return mockManager;
    }

    private List<SimpleLink> linksInMainLocation()
    {
        return Arrays.asList(
                newLink("labelmain", "linkmain", "http://label.main", "main", "label")
        );
    }

    private List<SimpleLink> linksInSection1()
    {
        return Arrays.asList(
                newLink("labelone", "linkone", "http://label.one", "one", "label"),
                newLink("labeltwo", "linktwo", "http://label.two", "two", "label"),
                newLink("labelthree", "linkthree", "http://label.three", "three", "label")
        );
    }

    private List<SimpleLink> linksInSection2()
    {
        return Arrays.asList(
                newLink("labelfour", "linkfour", "http://label.four", "four", "label"),
                newLink("labelfive", "linkfive", "http://label.five", "five", "label"),
                newLink("labelsix", "linksix", "http://label.six", "six", "label")
        );
    }

    private List<SimpleLinkSection> someSections()
    {
        return Arrays.asList(
                newSection("section1"),
                newSection("section2")
        );
    }

    private SimpleLinkSection newSection(String key)
    {
        return new SimpleLinkSectionImpl(key, null, null, null, null, null);
    }

    private SimpleLink newLink(String label, String linkId, String url, String... keywords)
    {
        final SimpleLink link = new SimpleLinkImpl(linkId, label, null, null, null, null, url, null);
        expect(keywordsProvider.aliasesFor(any(SimpleLinkSection.class), eq(link), any(JiraAuthenticationContext.class)))
                .andStubReturn(ImmutableSet.of(keywords));
        return link;
    }


    private static class MockJiraWebInterfaceManager extends JiraWebInterfaceManager
    {
        public MockJiraWebInterfaceManager(WebInterfaceManager webInterfaceManager)
        {
            super(webInterfaceManager);
        }

        @Override
        protected Map<String, Object> makeContext(User remoteUser, JiraHelper jiraHelper)
        {
            return Collections.emptyMap();
        }
    }
}
