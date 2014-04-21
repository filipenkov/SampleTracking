package com.atlassian.jira.user;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestCachingUserHistoryStore extends MockControllerTestCase
{
    private CachingUserHistoryStore store;
    private OfBizUserHistoryStore delegateStore;
    private ApplicationProperties applicationProperties;
    private User user;
    private User user2;

    @Before
    public void setUp() throws Exception
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());
        user2 = new User("test", mpa, new MockCrowdService());
        delegateStore = mockController.getMock(OfBizUserHistoryStore.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);


        store = new CachingUserHistoryStore(delegateStore, applicationProperties, null);
    }

    @After
    public void tearDown() throws Exception
    {

        delegateStore = null;
        user = null;
        user2 = null;
        store = null;
        applicationProperties = null;
    }


    @Test
    public void testHistoryNullParams()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        mockController.replay();
        try
        {
            store.addHistoryItem(null, item);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.addHistoryItem(user, null);
            fail("history item can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.getHistory(UserHistoryItem.ISSUE, null);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            store.getHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        mockController.verify();
    }

    @Test
    public void testAddUserHistoryNullHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();
    }

    @Test
    public void testAddUserHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem itemNew = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(new ArrayList<UserHistoryItem>());

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");


        // adding another one, but should just get history from cache
        delegateStore.addHistoryItemNoChecks(user, item2);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        // Adding first one again, should just update the record.  And no check to expire old ones
        delegateStore.updateHistoryItemNoChecks(user, itemNew);

        mockController.replay();

        store.addHistoryItem(user, item);
        store.addHistoryItem(user, item2);
        store.addHistoryItem(user, itemNew);

        mockController.verify();
    }

    @Test
    public void testAddUserHistoryExpireOldOnes()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, null, 0);

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(item2, item3, item4).asMutableList());

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        delegateStore.expireOldHistoryItems(user, UserHistoryItem.ISSUE, CollectionBuilder.newBuilder("1235", "1236").asList());

        mockController.replay();

        store.addHistoryItem(user, item);
        final List<UserHistoryItem> resultList = store.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> expectedList = CollectionBuilder.newBuilder(item, item2).asMutableList();

        assertEquals(expectedList, resultList);

        mockController.verify();
    }

    @Test
    public void testAddUserHistoryExpireOldOnesWitThreshold()
    {
        store = new CachingUserHistoryStore(delegateStore, applicationProperties, null, 2);

        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(item2, item3, item4).asMutableList());

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();
    }

    @Test
    public void testAddUserHistoryNoExpireAsReplacing()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList());

        delegateStore.updateHistoryItemNoChecks(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        mockController.verify();
    }

    @Test
    public void testGetHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> list = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        mockController.setReturnValue(list);

        mockController.replay();

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(4, history.size());
        assertEquals(list, history);

        mockController.verify();

    }

    @Test
    public void testGetHistoryAfterAdd()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> list = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        mockController.setReturnValue(list);

        delegateStore.updateHistoryItemNoChecks(user, item);

        mockController.replay();

        store.addHistoryItem(user, item);

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(4, history.size());
        assertEquals(list, history);

        mockController.verify();

    }

    @Test
    public void testGetHistoryOrderOfAdds()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> list = CollectionBuilder.newBuilder(item3, item4).asMutableList();
        mockController.setReturnValue(list);

        delegateStore.addHistoryItemNoChecks(user, item2);
        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        delegateStore.addHistoryItemNoChecks(user, item);
        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");

        delegateStore.updateHistoryItemNoChecks(user, item2);

        mockController.replay();

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(list, history);

        store.addHistoryItem(user, item2);

        history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(CollectionBuilder.newBuilder(item2, item3, item4).asMutableList(), history);

        store.addHistoryItem(user, item);
        store.addHistoryItem(user, item2);

        history = store.getHistory(UserHistoryItem.ISSUE, user);

        assertEquals(CollectionBuilder.newBuilder(item2, item, item3, item4).asMutableList(), history);

        mockController.verify();

    }

    @Test
    public void testGetHistoryDifferentTypes()
    {
        UserHistoryItem issueItem = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem issueItem2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 1);

        UserHistoryItem projectItem = new UserHistoryItem(UserHistoryItem.PROJECT, "123", 1);
        UserHistoryItem projectItem2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234", 1);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> issueList = CollectionBuilder.newBuilder(issueItem, issueItem2).asMutableList();
        mockController.setReturnValue(issueList);

        delegateStore.getHistory(UserHistoryItem.PROJECT, user);
        final List<UserHistoryItem> projectList = CollectionBuilder.newBuilder(projectItem, projectItem2).asMutableList();
        mockController.setReturnValue(projectList);

        mockController.replay();

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        mockController.verify();
    }

    @Test
    public void testRemoveHistory()
    {
        UserHistoryItem issueItem = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem issueItem2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 1);

        UserHistoryItem projectItem = new UserHistoryItem(UserHistoryItem.PROJECT, "123", 1);
        UserHistoryItem projectItem2 = new UserHistoryItem(UserHistoryItem.PROJECT, "1234", 1);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        final List<UserHistoryItem> issueList = CollectionBuilder.newBuilder(issueItem, issueItem2).asMutableList();
        mockController.setReturnValue(issueList);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user2);
        final List<UserHistoryItem> user2issueList = CollectionBuilder.newBuilder(issueItem, issueItem2).asMutableList();
        mockController.setReturnValue(user2issueList);

        delegateStore.getHistory(UserHistoryItem.PROJECT, user);
        final List<UserHistoryItem> projectList = CollectionBuilder.newBuilder(projectItem, projectItem2).asMutableList();
        mockController.setReturnValue(projectList);

        delegateStore.removeHistoryForUser(user);
        final Set<UserHistoryItem.Type> types = CollectionBuilder.newBuilder(UserHistoryItem.ISSUE, UserHistoryItem.PROJECT).asSet();
        mockController.setReturnValue(types);

        // it should hit the db again
        delegateStore.getHistory(UserHistoryItem.PROJECT, user);
        mockController.setReturnValue(projectList);
        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(issueList);

        mockController.replay();

        List<UserHistoryItem> history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user2);
        assertEquals(user2issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        store.removeHistoryForUser(user);

        // this wont hit the db
        history = store.getHistory(UserHistoryItem.ISSUE, user2);
        assertEquals(user2issueList, history);

        history = store.getHistory(UserHistoryItem.PROJECT, user);
        assertEquals(projectList, history);

        history = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(issueList, history);

        mockController.verify();

    }

    @Test
    public void testMaxEntries()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(new ArrayList<UserHistoryItem>());

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("2");
        delegateStore.removeHistoryForUser(user);
        Set<UserHistoryItem.Type> types = CollectionBuilder.newBuilder(UserHistoryItem.ISSUE, UserHistoryItem.PROJECT).asSet();
        mockController.setReturnValue(types);


        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(new ArrayList<UserHistoryItem>());


        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString("jira.max.history.items");
        mockController.setReturnValue("2");
        delegateStore.removeHistoryForUser(user);
        types = CollectionBuilder.newBuilder(UserHistoryItem.ISSUE, UserHistoryItem.PROJECT).asSet();
        mockController.setReturnValue(types);


        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(new ArrayList<UserHistoryItem>());

        delegateStore.addHistoryItemNoChecks(user, item);

        applicationProperties.getDefaultBackedString("jira.max.Issue.history.items");
        mockController.setReturnValue("");

        applicationProperties.getDefaultBackedString("jira.max.history.items");
        mockController.setReturnValue("");

        mockController.replay();

        store.addHistoryItem(user, item);
        store.removeHistoryForUser(user);

        store.addHistoryItem(user, item);
        store.removeHistoryForUser(user);

        store.addHistoryItem(user, item);


        mockController.verify();
    }

}
