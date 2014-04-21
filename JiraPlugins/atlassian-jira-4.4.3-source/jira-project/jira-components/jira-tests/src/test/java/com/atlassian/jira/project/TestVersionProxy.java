/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractWebworkTestCase;

public class TestVersionProxy extends AbstractWebworkTestCase
{
    public TestVersionProxy(String s)
    {
        super(s);
    }

    public void testGenericValue()
    {
        GenericValue genericValue = new MockGenericValue("Version", EasyMap.build("name", "foo", "id", new Long(4), "sequence", new Long(1), "project", new Long(101)));
        Version version = new VersionImpl(null, genericValue);
        VersionProxy proxy = new VersionProxy(version);

        assertEquals(proxy.getKey(), 4);
        assertEquals(proxy.getValue(), "foo");
    }

    public void testNormal()
    {
        VersionProxy proxy = new VersionProxy(4, "foo");
        assertEquals(proxy.getKey(), 4);
        assertEquals(proxy.getValue(), "foo");
    }
}
