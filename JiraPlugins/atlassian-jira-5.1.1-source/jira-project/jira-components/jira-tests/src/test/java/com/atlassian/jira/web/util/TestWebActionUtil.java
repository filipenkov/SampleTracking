/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.core.util.collection.EasyList;
import mock.action.MockAction;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestWebActionUtil extends LegacyJiraMockTestCase
{
    public TestWebActionUtil(String s)
    {
        super(s);
    }

    public void testNoErrors()
    {
        MockAction action = new MockAction();
        WebActionUtil.addDependentVersionErrors(action, EasyList.build(new Long(4), new Long(5)), "field");
        assertEquals(0, action.getErrors().size());
    }

    public void testErrors()
    {
        MockAction action = new MockAction();
        WebActionUtil.addDependentVersionErrors(action, EasyList.build(new Long(-2)), "field");
        assertEquals(1, action.getErrors().size());
        assertEquals("You cannot specify \"Unreleased\" or \"Released\".", action.getErrors().get("field"));
    }
}
