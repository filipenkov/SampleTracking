package com.atlassian.jira.bc.filter;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.search.MockSearchRequest;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAdminManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockGroup;
import com.atlassian.jira.util.collect.MockCloseableIterable;
import com.atlassian.jira.web.action.util.SimpleSearchRequestDisplay;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestDefaultSearchRequestAdminService extends MockControllerTestCase
{
    private SearchRequestAdminManager searchRequestAdminManager;

    private FavouritesManager<SearchRequest> favouritesManager;

    private static final Long ID_123 = 123L;

    @Before
    public void setUp() throws Exception
    {
        searchRequestAdminManager = getMock(SearchRequestAdminManager.class);
        favouritesManager = getMock(FavouritesManager.class);

    }

    @Test
    public void test_getFiltersSharedWithGroup()
    {
        final SearchRequest searchRequest = MockSearchRequest.get("userName", ID_123);
        searchRequest.setName("sr1");
        searchRequest.setDescription("sr1Desc");
        final Group group = new MockGroup("admin");

        final MockCloseableIterable<SearchRequest> closeableIterable = new MockCloseableIterable<SearchRequest>(EasyList.build(searchRequest));
        expect(searchRequestAdminManager.getSearchRequests(group)).andReturn(closeableIterable);

        final SearchRequestAdminService service = instantiate(DefaultSearchRequestAdminService.class);
        final Collection<?> result = service.getFiltersSharedWithGroup(group);
        assertNotNull(result);
        assertEquals(1, result.size());
        final SimpleSearchRequestDisplay simpleSearchRequestDisplay = (SimpleSearchRequestDisplay) result.iterator().next();
        assertEquals(IdentifierUtils.toLowerCase("userName"), simpleSearchRequestDisplay.getOwnerUserName());
        assertEquals("sr1", simpleSearchRequestDisplay.getName());
        assertEquals(ID_123, simpleSearchRequestDisplay.getId());
    }

}