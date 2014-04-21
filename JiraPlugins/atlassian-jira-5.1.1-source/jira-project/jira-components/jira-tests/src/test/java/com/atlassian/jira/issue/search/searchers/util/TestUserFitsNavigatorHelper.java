package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestUserFitsNavigatorHelper extends MockControllerTestCase
{
    @Test
    public void testCheckUserFoundByUserName() throws Exception
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final UserPickerSearchService service = mockController.getMock(UserPickerSearchService.class);
        mockController.replay();

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return name.toLowerCase();
            }
        };
        
        assertEquals("monkey", helper.checkUser("monkey"));
        assertTrue(calledFindUserName.get());
    }
    
    @Test
    public void testCheckUserFoundByUserNameUpperCase() throws Exception
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final UserPickerSearchService service = mockController.getMock(UserPickerSearchService.class);
        mockController.replay();

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return name.toLowerCase();
            }
        };

        assertEquals("monkey", helper.checkUser("Monkey"));
        assertTrue(calledFindUserName.get());
    }

    @Test
    public void testCheckUserNotFoundFullNameEnabledDoesNotExist() throws Exception
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final AtomicBoolean calledFullNameExists = new AtomicBoolean(false);
        final UserPickerSearchService service = mockController.getMock(UserPickerSearchService.class);
        mockController.replay();

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return null;
            }

            @Override
            boolean userExistsByFullNameOrEmail(final String name)
            {
                calledFullNameExists.set(true);
                return false;
            }
        };
        
        assertEquals("monkey", helper.checkUser("monkey"));
        assertTrue(calledFindUserName.get());
        assertTrue(calledFullNameExists.get());
    }

    @Test
    public void testCheckUserNotFoundFullNameEnabledDoesExist() throws Exception
    {
        final AtomicBoolean calledFindUserName = new AtomicBoolean(false);
        final AtomicBoolean calledFullNameExists = new AtomicBoolean(false);
        final UserPickerSearchService service = mockController.getMock(UserPickerSearchService.class);
        mockController.replay();

        UserFitsNavigatorHelper helper = new UserFitsNavigatorHelper(service)
        {
            @Override
            String findUserName(final String name)
            {
                calledFindUserName.set(true);
                return null;
            }

            @Override
            boolean userExistsByFullNameOrEmail(final String name)
            {
                calledFullNameExists.set(true);
                return true;
            }
        };
        
        assertNull(helper.checkUser("monkey"));
        assertTrue(calledFindUserName.get());
        assertTrue(calledFullNameExists.get());
    }
}
