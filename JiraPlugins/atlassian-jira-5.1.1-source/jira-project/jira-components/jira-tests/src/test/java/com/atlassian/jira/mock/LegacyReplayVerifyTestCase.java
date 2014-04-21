package com.atlassian.jira.mock;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.easymock.MockControl;

/**
 * deprecated since v4.0. Use {@link com.atlassian.jira.local.MockControllerTestCase} instead.
 */
public abstract class LegacyReplayVerifyTestCase extends LegacyJiraMockTestCase
{
    // ------------------------------------------------------------------------------------------ Replay & Reset Methods

    protected void _startTestPhase()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.replay();
        }
    }

    protected void _reset()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.reset();
        }
    }

    protected void _verifyAll()
    {
        for (int i = 0; i < _getRegisteredMockControllers().length; i++)
        {
            MockControl mockControl = _getRegisteredMockControllers()[i];
            mockControl.verify();
        }
    }

    public abstract MockControl[] _getRegisteredMockControllers();
}
