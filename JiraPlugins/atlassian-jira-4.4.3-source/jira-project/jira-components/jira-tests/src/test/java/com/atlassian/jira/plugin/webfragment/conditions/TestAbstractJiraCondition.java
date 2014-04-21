package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.user.User;

public class TestAbstractJiraCondition extends MockControllerTestCase
{

    @Test
    public void testShouldDisplayWithUser()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User fred = new User("fred", mpa, new MockCrowdService());

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
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User fred = new User("fred", mpa, new MockCrowdService());

        final UserUtil mockUserUtil = mockController.getMock(UserUtil.class);
        mockUserUtil.getUser("fred");
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
