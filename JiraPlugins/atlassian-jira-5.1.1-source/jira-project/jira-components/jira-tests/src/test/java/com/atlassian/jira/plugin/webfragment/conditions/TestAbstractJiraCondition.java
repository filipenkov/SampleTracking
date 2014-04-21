package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAbstractJiraCondition extends MockControllerTestCase
{

    @Test
    public void testShouldDisplayWithUser()
    {
        final User fred = new MockUser("fred");

        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);

        mockController.replay();

        AbstractJiraCondition condition = new AbstractJiraCondition()
        {
            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            public boolean shouldDisplay(final User user, final JiraHelper jiraHelper)
            {
                assertEquals(fred, user);
                return true;
            }
        };

        boolean shouldDisplay = condition.shouldDisplay(EasyMap.build("user", fred));
        assertTrue(shouldDisplay);        
    }

     @Test
     public void testShouldDisplayWithUsername()
    {
        final User fred = new MockUser("fred");

        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUserObject("fred");
        mockController.setReturnValue(fred);

        mockController.replay();

        AbstractJiraCondition condition = new AbstractJiraCondition()
        {
            @Override
            UserUtil getUserUtil()
            {
                return mockUserUtil;
            }

            public boolean shouldDisplay(final User user, final JiraHelper jiraHelper)
            {
                assertEquals(fred, user);
                return true;
            }
        };

        boolean shouldDisplay = condition.shouldDisplay(EasyMap.build("username", "fred"));
        assertTrue(shouldDisplay);
    }

}
