package com.atlassian.jira.user;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.EasyList;
import com.opensymphony.user.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestOfbizUserHistoryStore extends LegacyJiraMockTestCase
{
    private OfBizDelegator ofBizDelegator;
    private User user;
    private User user2;
    private OfBizUserHistoryStore store;

    protected void setUp() throws Exception
    {
        super.setUp();
        ofBizDelegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        user = UtilsForTests.getTestUser("admin");
        user2 = UtilsForTests.getTestUser("test");
        ApplicationProperties properties = new MockApplicationProperties();
        properties.setString(APKeys.JIRA_MAX_HISTORY_ITEMS, "5");
        store = new OfBizUserHistoryStore(ofBizDelegator, properties);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanUsers();
        UtilsForTests.cleanOFBiz();
        ofBizDelegator = null;
        user = null;
        user2 = null;
        store = null;
    }


    public void testAddNoCheck()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 2);

        List<UserHistoryItem> results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());

        store.addHistoryItemNoChecks(user, item1);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(1, results.size());
        assertEquals(item1, results.iterator().next());

        store.addHistoryItemNoChecks(user, item2);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(2, results.size());
        final Iterator<UserHistoryItem> userHistoryItemIterator = results.iterator();
        assertEquals(item2, userHistoryItemIterator.next());
        assertEquals(item1, userHistoryItemIterator.next());

    }

    public void testUpdateNoCheck()
    {
        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item1New = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 3);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 2);

        List<UserHistoryItem> results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());

        store.updateHistoryItemNoChecks(user, item1);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(1, results.size());
        assertEquals(item1, results.iterator().next());

        store.addHistoryItemNoChecks(user, item2);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(2, results.size());
        Iterator<UserHistoryItem> userHistoryItemIterator = results.iterator();
        assertEquals(item2, userHistoryItemIterator.next());
        assertEquals(item1, userHistoryItemIterator.next());

        store.updateHistoryItemNoChecks(user, item1New);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(2, results.size());
        userHistoryItemIterator = results.iterator();
        assertEquals(item1New, userHistoryItemIterator.next());
        assertEquals(item2, userHistoryItemIterator.next());
    }

    public void testAddHistoryItemNullValues()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
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


    }

    public void testHistoryItem()
    {

        assertTrue(store.getHistory(UserHistoryItem.ISSUE, user).isEmpty());

        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        store.addHistoryItem(user, item1);

        List<UserHistoryItem> results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(1, results.size());
        assertEquals(item1, results.iterator().next());

        assertTrue(store.getHistory(UserHistoryItem.ISSUE, user2).isEmpty());
        assertTrue(store.getHistory(UserHistoryItem.PROJECT, user).isEmpty());

        UserHistoryItem item1again = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 2);
        store.addHistoryItem(user, item1again);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(1, results.size());
        assertEquals(item1again, results.iterator().next());

        assertTrue(store.getHistory(UserHistoryItem.ISSUE, user2).isEmpty());
        assertTrue(store.getHistory(UserHistoryItem.PROJECT, user).isEmpty());

        store.addHistoryItem(user2, new UserHistoryItem(UserHistoryItem.ISSUE, "123", 3));
        store.addHistoryItem(user, new UserHistoryItem(UserHistoryItem.PROJECT, "123", 4));


        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "124", 5);
        store.addHistoryItem(user, item2);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(2, results.size());
        Iterator<UserHistoryItem> iterator = results.iterator();
        assertEquals(item2, iterator.next());
        assertEquals(item1again, iterator.next());

        item1again = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 6);
        store.addHistoryItem(user, item1again);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        iterator = results.iterator();
        assertEquals(2, results.size());
        assertEquals(item1again, iterator.next());
        assertEquals(item2, iterator.next());

        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1233", 7);
        store.addHistoryItem(user, item3);

        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234", 8);
        store.addHistoryItem(user, item4);

        UserHistoryItem item5 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235", 9);
        store.addHistoryItem(user, item5);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(5, results.size());
        iterator = results.iterator();
        assertEquals(item5, iterator.next());
        assertEquals(item4, iterator.next());
        assertEquals(item3, iterator.next());
        assertEquals(item1again, iterator.next());
        assertEquals(item2, iterator.next());

        UserHistoryItem item6 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236", 10);
        store.addHistoryItem(user, item6);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(5, results.size());
        iterator = results.iterator();
        assertEquals(item6, iterator.next());
        assertEquals(item5, iterator.next());
        assertEquals(item4, iterator.next());
        assertEquals(item3, iterator.next());
        assertEquals(item1again, iterator.next());


        UserHistoryItem item7 = new UserHistoryItem(UserHistoryItem.ISSUE, "1237", 11);
        store.addHistoryItem(user, item7);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(5, results.size());
        iterator = results.iterator();
        assertEquals(item7, iterator.next());
        assertEquals(item6, iterator.next());
        assertEquals(item5, iterator.next());
        assertEquals(item4, iterator.next());
        assertEquals(item3, iterator.next());

        item5 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235", 12);
        store.addHistoryItem(user, item5);

        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertEquals(5, results.size());
        iterator = results.iterator();
        assertEquals(item5, iterator.next());
        assertEquals(item7, iterator.next());
        assertEquals(item6, iterator.next());
        assertEquals(item4, iterator.next());
        assertEquals(item3, iterator.next());
    }

    public void testRemoveHistoryNullUser()
    {
        try
        {
            Set<UserHistoryItem.Type> types = store.removeHistoryForUser(null);
            fail("User can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
    }

    public void testRemoveHistory()
    {
        Set<UserHistoryItem.Type> oneType = new HashSet<UserHistoryItem.Type>();
        oneType.add(UserHistoryItem.ISSUE);

        Set<UserHistoryItem.Type> twoTypes = new HashSet<UserHistoryItem.Type>();
        twoTypes.add(UserHistoryItem.ISSUE);
        twoTypes.add(UserHistoryItem.PROJECT);

        List<UserHistoryItem> results;
        Set<UserHistoryItem.Type> types = store.removeHistoryForUser(user);

        assertTrue(types.isEmpty());

        UserHistoryItem item1 = new UserHistoryItem(UserHistoryItem.ISSUE, "123", 1);
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "124", 2);
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.PROJECT, "123", 3);
        store.addHistoryItem(user, item1);

        types = store.removeHistoryForUser(user);
        assertEquals(oneType, types);
        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());

        store.addHistoryItem(user, item1);

        types = store.removeHistoryForUser(user);
        assertEquals(oneType, types);
        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());


        store.addHistoryItem(user, item1);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertEquals(oneType, types);
        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());


        store.addHistoryItem(user, item1);
        store.addHistoryItem(user, item3);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertEquals(twoTypes, types);
        results = store.getHistory(UserHistoryItem.ISSUE, user);
        assertTrue(results.isEmpty());
        results = store.getHistory(UserHistoryItem.PROJECT, user);
        assertTrue(results.isEmpty());


        store.addHistoryItem(user, item1);
        store.addHistoryItem(user2, item3);
        store.addHistoryItem(user, item2);

        types = store.removeHistoryForUser(user);
        assertEquals(oneType, types);

        results = store.getHistory(UserHistoryItem.PROJECT, user2);

        assertEquals(EasyList.build(item3), results);


    }

}
