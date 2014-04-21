package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.easymock.IMocksControl;

public class TestTimeTrackingEnabledCondition extends ListeningTestCase
{
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final WorklogService worklogService = mocksControl.createMock(WorklogService.class);

        expect(worklogService.isTimeTrackingEnabled()).andReturn(true);

        final TimeTrackingEnabledCondition condition = new TimeTrackingEnabledCondition(worklogService);

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final WorklogService worklogService = mocksControl.createMock(WorklogService.class);

        expect(worklogService.isTimeTrackingEnabled()).andReturn(false);

        final TimeTrackingEnabledCondition condition = new TimeTrackingEnabledCondition(worklogService);

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, null));
        mocksControl.verify();

    }

}
