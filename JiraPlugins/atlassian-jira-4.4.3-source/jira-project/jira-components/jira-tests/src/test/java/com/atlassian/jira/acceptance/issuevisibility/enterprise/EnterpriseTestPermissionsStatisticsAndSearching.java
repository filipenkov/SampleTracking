/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.issuevisibility.enterprise;

import com.atlassian.jira.acceptance.issuevisibility.AbstractTestPermissionsStatisticsAndSearching;


public abstract class EnterpriseTestPermissionsStatisticsAndSearching extends AbstractTestPermissionsStatisticsAndSearching
{
    public EnterpriseTestPermissionsStatisticsAndSearching(String s)
    {
        super(s);
    }

    protected String getJellyDataScriptName()
    {
        return super.getJellyDataScriptName() + "test-permissions-statistics-and-searching.data.enterprise.jelly";
    }
}
