/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.issuefields.AbstractConfigureFieldLayout;
import com.atlassian.jira.web.action.admin.issuefields.AbstractTestViewIssueFields;
import org.ofbiz.core.entity.GenericValue;

public class TestEditFieldLayout extends AbstractTestViewIssueFields
{
    private ConfigureFieldLayout editFieldLayout;
    private GenericValue testEntity;

    public TestEditFieldLayout(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // Create a scheme
        testEntity = UtilsForTests.getTestEntity("FieldLayoutScheme", EasyMap.build("name", "Name"));
        UtilsForTests.cleanWebWork();
    }

    public void setNewVif()
    {
        editFieldLayout = new ConfigureFieldLayout(null, null, reindexMessageManager, fieldLayoutSchemeHelper, null, null);
        editFieldLayout.setId(testEntity.getLong("id"));
    }

    public AbstractConfigureFieldLayout getVif()
    {
        if (editFieldLayout == null)
        {
            setNewVif();
        }
        return editFieldLayout;
    }

    public void refreshVif()
    {
        editFieldLayout = null;
    }
}
