/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.workflow.condition;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.dynamic.Mock;
import com.opensymphony.workflow.spi.WorkflowEntry;

import java.util.Map;

public class TestSubTaskBlockingCondition extends LegacyJiraMockTestCase
{
    SubTaskBlockingCondition stbc;
    private Map args;
    private Map transientVars;
    private Mock wfe;
    private Mock mockSubTaskManager;


    protected void setUp() throws Exception
    {
        super.setUp();
        stbc = new SubTaskBlockingCondition();
        wfe = new Mock(WorkflowEntry.class);

        mockSubTaskManager = new Mock(SubTaskManager.class);
        mockSubTaskManager.setStrict(true);
    }

    public void testSubTaskBlockingConditionNoIssue()
    {
        args = EasyMap.build("statuses", "1");
        transientVars = EasyMap.build("entry", wfe.proxy());

        try
        {
            assertFalse(stbc.passesCondition(transientVars, args, null));
            fail("No issue; should have thrown a DataAccessException");
        } catch (DataAccessException dae) {}
    }
}
