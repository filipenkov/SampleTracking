package com.atlassian.jira.favourites;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.search.DefaultSearchRequestManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.search.SearchRequestStore;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.index.MockSharedEntityIndexer;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.query.QueryImpl;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import org.easymock.MockControl;

public class TestAdjustFavouriteCountForSearchRequest extends ListeningTestCase
{
    private User user;

    private MockControl searchRequestStoreCtrl;
    private SearchRequestStore searchRequestStore;
    private MockControl shareManagerCtrl;
    private ShareManager shareManager;
    private MockControl subscriptionManagerCtrl;
    private SubscriptionManager subscriptionManager;
    private MockControl colLayoutManagerCtrl;
    private ColumnLayoutManager columnLayoutManager;
    private SearchService searchService;
    private UserUtil userUtil;

    @Before
    public void setUp() throws Exception
    {
        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());

        searchRequestStoreCtrl = MockControl.createStrictControl(SearchRequestStore.class);
        searchRequestStore = (SearchRequestStore) searchRequestStoreCtrl.getMock();
        shareManagerCtrl = MockControl.createStrictControl(ShareManager.class);
        shareManager = (ShareManager) shareManagerCtrl.getMock();
        subscriptionManagerCtrl = MockControl.createStrictControl(SubscriptionManager.class);
        subscriptionManager = (SubscriptionManager) subscriptionManagerCtrl.getMock();
        colLayoutManagerCtrl = MockControl.createStrictControl(ColumnLayoutManager.class);
        columnLayoutManager = (ColumnLayoutManager) colLayoutManagerCtrl.getMock();
        searchService = EasyMock.createMock(SearchService.class);
        userUtil = EasyMock.createMock(UserUtil.class);
    }

    @After
    public void tearDown() throws Exception
    {
        searchRequestStore = null;
        searchRequestStoreCtrl = null;
        shareManager = null;
        shareManagerCtrl = null;
        colLayoutManagerCtrl = null;
        columnLayoutManager = null;
        subscriptionManager = null;
        subscriptionManagerCtrl = null;
    }

    public void validateMocks()
    {
        searchRequestStoreCtrl.verify();
        shareManagerCtrl.verify();
        subscriptionManagerCtrl.verify();
        colLayoutManagerCtrl.verify();
        EasyMock.verify(searchService, userUtil);
    }

    @Test
    public void testHappyValues()
    {
        final SearchRequest searchRequest;
        searchRequest = new SearchRequest(new QueryImpl(), user.getName(), "test", "test desc", 999L, 0L);
        searchRequestStore.adjustFavouriteCount(searchRequest.getId(), 1);
        searchRequestStoreCtrl.setReturnValue(searchRequest);
        shareManager.getSharePermissions(searchRequest);
        shareManagerCtrl.setReturnValue(SharedEntity.SharePermissions.PRIVATE);

        final SearchRequestManager manager = getSearchRequestManager();

        manager.adjustFavouriteCount(searchRequest, 1);

        validateMocks();
    }

    public SearchRequestManager getSearchRequestManager()
    {
        searchRequestStoreCtrl.replay();
        shareManagerCtrl.replay();
        subscriptionManagerCtrl.replay();
        colLayoutManagerCtrl.replay();
        EasyMock.replay(searchService, userUtil);        

        return new DefaultSearchRequestManager(columnLayoutManager, subscriptionManager, shareManager, searchRequestStore, new MockSharedEntityIndexer(), searchService, userUtil);
    }

}
