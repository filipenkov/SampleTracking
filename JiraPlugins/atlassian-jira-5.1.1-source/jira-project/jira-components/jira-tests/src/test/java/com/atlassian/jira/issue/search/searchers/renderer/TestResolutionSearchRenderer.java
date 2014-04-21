package com.atlassian.jira.issue.search.searchers.renderer;

import org.junit.Test;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;

/**
 * @since v4.0
 */
public class TestResolutionSearchRenderer extends ListeningTestCase
{
    @Test
    public void testGetSelectListOptions() throws Exception
    {
        final MockControl mockConstantsManagerControl = MockControl.createStrictControl(ConstantsManager.class);
        final ConstantsManager mockConstantsManager = (ConstantsManager) mockConstantsManagerControl.getMock();
        mockConstantsManager.getResolutionObjects();
        mockConstantsManagerControl.setReturnValue(null);
        mockConstantsManagerControl.replay();
        final ResolutionSearchRenderer searchRenderer = new ResolutionSearchRenderer("test", mockConstantsManager, null, null, null, null);

        searchRenderer.getSelectListOptions(null);

        mockConstantsManagerControl.verify();
    }
}
