/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.portal;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.opensymphony.util.TextUtils;

public class TestPortletConfigurationException extends ListeningTestCase
{
    @Test
    public void testConstuct()
    {
        PortletConfigurationException pce = new PortletConfigurationException(new Exception("This exception"));
        assertTrue(TextUtils.stringSet(pce.getMessage()));
    }
}
