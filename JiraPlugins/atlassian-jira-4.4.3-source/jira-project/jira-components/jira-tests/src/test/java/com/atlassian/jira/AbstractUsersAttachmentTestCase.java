/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;

public abstract class AbstractUsersAttachmentTestCase extends AbstractUsersIndexingTestCase
{
    private static String attachmentDirectory = null;

    public AbstractUsersAttachmentTestCase(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        if (attachmentDirectory == null)
        {
            attachmentDirectory = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "jira-testrun-attachments";
        }

        PropertiesManager.getInstance().getPropertySet().setString(APKeys.JIRA_PATH_ATTACHMENTS, attachmentDirectory);
    }
}
