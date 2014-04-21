/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.project;

import com.atlassian.jira.local.AbstractWebworkTestCase;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.action.project.AbstractProjectEntityCreate;

import webwork.action.Action;

public class TestAbstractProjectEntityCreate extends AbstractWebworkTestCase
{
    public TestAbstractProjectEntityCreate(String s)
    {
        super(s);
    }

    public void testErrorMessages() throws Exception
    {
        AbstractProjectEntityCreate action = new AbstractProjectEntityCreate();
        String result = action.execute();
        assertEquals(Action.ERROR, result);
        assertEquals(2, action.getErrorMessages().size());
        assertTrue(action.getErrorMessages().contains("You must specify a name for this entity"));
        assertTrue(action.getErrorMessages().contains("You must specify a project for this entity"));
    }

    public void testNoErrorMessages() throws Exception
    {
        AbstractProjectEntityCreate action = new AbstractProjectEntityCreate();
        action.setName("foo");
        action.setProject(UtilsForTests.getTestEntity("Project", null));

        String result = action.execute();
        assertEquals(Action.SUCCESS, result);
        assertEquals(0, action.getErrorMessages().size());
    }
}
