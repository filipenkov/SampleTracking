package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.util.collect.CollectionBuilder;
import net.jcip.annotations.ThreadSafe;
import org.easymock.EasyMock;
import org.easymock.internal.matchers.Equals;

import java.util.List;
import java.util.Set;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
@ThreadSafe
public class TestDefaultUserHistoryManager extends MockControllerTestCase
{

    private UserHistoryStore delegateStore;
    private UserHistoryManager historyManager;
    private ApplicationUser user;

    private static final class IGNORE_TIMESTAMP_ARGUMENTS_MATCHER extends Equals
    {
        private final Object expected;

        public IGNORE_TIMESTAMP_ARGUMENTS_MATCHER(Object expected)
        {
            super(expected);
            this.expected = expected;
        }


        @Override
        public void appendTo(StringBuffer buffer)
        {
        }

        @Override
        public boolean matches(Object argument)
        {
            if (argument instanceof UserHistoryItem && expected instanceof UserHistoryItem)
            {
                final UserHistoryItem item1 = (UserHistoryItem) argument;
                final UserHistoryItem item2 = (UserHistoryItem) expected;

                return item1.getType().equals(item2.getType()) && item1.getEntityId().equals(item2.getEntityId());
            }
            return super.matches(argument);
        }
    }


    @Before
    public void setUp() throws Exception
    {
        user = new DelegatingApplicationUser("admin", new MockUser("admin"));

        delegateStore = mockController.getMock(UserHistoryStore.class);

        historyManager = new DefaultUserHistoryManager(delegateStore);
    }

    @After
    public void tearDown() throws Exception
    {
        delegateStore = null;
        historyManager = null;

    }

    @Test
    public void testNullArgs()
    {
        mockController.replay();

        try
        {
            historyManager.addItemToHistory(null, user, "123");
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }
        try
        {
            historyManager.addItemToHistory(UserHistoryItem.ISSUE, user, null);
            fail("entity can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.hasHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.getHistory(null, user);
            fail("type can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        try
        {
            historyManager.removeHistoryForUser(null);
            fail("user can not be null");
        }
        catch (IllegalArgumentException e)
        {
            //pass
        }

        mockController.verify();

    }

    public static <T extends UserHistoryItem> T eqUHI(T in) {
        EasyMock.reportMatcher(new IGNORE_TIMESTAMP_ARGUMENTS_MATCHER(in));
        return in;
    }
    
    @Test
    public void testAddItemToHistory()
    {

        final UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        delegateStore.addHistoryItem(eq(user), eqUHI(item));
        expectLastCall();
        replay();

        historyManager.addItemToHistory(UserHistoryItem.ISSUE, user, "123");
    }


    @Test
    public void testHasHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertTrue(historyManager.hasHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testHasHistoryEmptyHistory()
    {
        final List<UserHistoryItem> history = CollectionBuilder.<UserHistoryItem>newBuilder().asMutableList();

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testHasHistoryNoHistory()
    {
        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(historyManager.hasHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistory()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, historyManager.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistoryEmptyHistory()
    {
        final List<UserHistoryItem> history = CollectionBuilder.<UserHistoryItem>newBuilder().asMutableList();

        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, historyManager.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testGetHistoryNoHistory()
    {
        delegateStore.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertEquals(null, historyManager.getHistory(UserHistoryItem.ISSUE, user));

        mockController.verify();
    }

    @Test
    public void testRemoveHistoryForUser()
    {
        final Set<UserHistoryItem.Type> typesRemoved = CollectionBuilder.<UserHistoryItem.Type>newBuilder().asSet();

        delegateStore.removeHistoryForUser(user);
        mockController.setReturnValue(typesRemoved);
        mockController.replay();

        historyManager.removeHistoryForUser(user);

        mockController.verify();
    }
}
