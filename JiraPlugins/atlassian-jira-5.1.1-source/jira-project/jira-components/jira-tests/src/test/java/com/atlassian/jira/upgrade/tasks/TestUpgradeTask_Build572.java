package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.Map;

public class TestUpgradeTask_Build572 extends MockControllerTestCase
{
    private ApplicationProperties applicationProperties;
    private OfBizDelegator ofBizDelegator;

    @Before
    public void setUp() throws Exception
    {
        applicationProperties = getMock(ApplicationProperties.class);
        ofBizDelegator = getMock(OfBizDelegator.class);
    }

    @Test
    public void testGetBuildNumber() throws Exception
    {
        UpgradeTask_Build572 task_build572 = instantiate(UpgradeTask_Build572.class);
        assertEquals("572", task_build572.getBuildNumber());

    }

    @Test
    public void testDoUpgrade() throws Exception
    {
        expect(applicationProperties.getDefaultBackedString(APKeys.JIRA_CLONE_LINKTYPE_NAME)).andReturn("Cloners");
        for (int i = 0; i < 4; i++)
        {
            expect(ofBizDelegator.findByAnd(eq(OfBizDelegator.ISSUE_LINK_TYPE), EasyMock.<Map<String, Object>>anyObject())).andReturn(Collections.<GenericValue>emptyList());
            expect(ofBizDelegator.createValue(eq(OfBizDelegator.ISSUE_LINK_TYPE), EasyMock.<Map<String, Object>>anyObject())).andReturn(null);
        }
        // just expect 3 entries.  We have a func to test the content!
        applicationProperties.setOption(APKeys.JIRA_OPTION_ISSUELINKING,true); expectLastCall();

        UpgradeTask_Build572 task_build572 = instantiate(UpgradeTask_Build572.class);
        task_build572.doUpgrade(false);
    }
}
