/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class TestViewSchemes extends LegacyJiraMockTestCase
{
    public TestViewSchemes(String s)
    {
        super(s);
    }

    public void testGetSchemes() throws GenericEntityException
    {
        GenericValue scheme1 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "Name 1"));
        GenericValue scheme2 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "Name 2"));
        ViewSchemes vs = new ViewSchemes();
        List schemes = vs.getSchemes();
        assertEquals(2, schemes.size());
        assertTrue(schemes.contains(scheme1));
        assertTrue(schemes.contains(scheme2));
    }
}
