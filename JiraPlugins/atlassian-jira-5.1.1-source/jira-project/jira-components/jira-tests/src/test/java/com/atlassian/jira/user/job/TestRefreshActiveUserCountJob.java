package com.atlassian.jira.user.job;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.user.util.UserUtil;
import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.local.ListeningTestCase;
import org.quartz.JobExecutionException;

/**
 * Tests the background job to re-calculate the active user count.
 */
public class TestRefreshActiveUserCountJob extends ListeningTestCase
{

    @Test
    public void testExecuteWithLicenseWithNoUserLimit() throws JobExecutionException
    {
        RefreshActiveUserCountJob refreshActiveUserCountJob = new RefreshActiveUserCountJob()
        {
            @Override
            boolean requiresUserLimit()
            {
                return Boolean.FALSE;
            }

            UserUtil getUserUtil()
            {
                fail("Should not need the userUtil as the license does not have a user limit!");
                return null;
            }
        };

        refreshActiveUserCountJob.execute(null);
    }

    @Test
    public void testExecute() throws JobExecutionException
    {
        final Mock mockUserUtil = new Mock(UserUtil.class);
        mockUserUtil.setStrict(true);
        mockUserUtil.expectVoid("clearActiveUserCount");
        mockUserUtil.expectAndReturn("getActiveUserCount", new Integer(53));

        RefreshActiveUserCountJob refreshActiveUserCountJob = new RefreshActiveUserCountJob()
        {
            @Override
            boolean requiresUserLimit()
            {
                return Boolean.TRUE;
            }

            UserUtil getUserUtil()
            {

                return (UserUtil) mockUserUtil.proxy();
            }
        };

        refreshActiveUserCountJob.execute(null);

        mockUserUtil.verify();
    }

}
