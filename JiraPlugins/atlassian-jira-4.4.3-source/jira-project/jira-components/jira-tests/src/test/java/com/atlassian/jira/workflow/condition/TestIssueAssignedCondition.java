/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.condition;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;

import java.util.Map;

public class TestIssueAssignedCondition extends ListeningTestCase
{
    @Test
    public void testAssigned()
    {
        IssueAssignedCondition condition = new IssueAssignedCondition();

        Map inputs = EasyMap.build("assignee", "yep");
        assertTrue(condition.passesCondition(inputs, null, null));

        inputs = EasyMap.build("uhm", "nope");
        assertTrue(!condition.passesCondition(inputs, null, null));

        inputs = EasyMap.build("assignee", "");
        assertTrue(!condition.passesCondition(inputs, null, null));
    }
}
