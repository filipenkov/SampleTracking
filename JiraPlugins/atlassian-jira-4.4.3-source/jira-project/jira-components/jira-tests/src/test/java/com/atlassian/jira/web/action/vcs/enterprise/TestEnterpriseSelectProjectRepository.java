/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.vcs.enterprise;

import com.atlassian.jira.local.LegacyJiraMockTestCase;

import com.atlassian.jira.web.action.admin.vcs.enterprise.EnterpriseSelectProjectRepository;

public class TestEnterpriseSelectProjectRepository extends LegacyJiraMockTestCase
{
    private EnterpriseSelectProjectRepository espr;

    public TestEnterpriseSelectProjectRepository(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        espr = new EnterpriseSelectProjectRepository(null);
    }

    public void testGetSetMultipleRepositoryIds()
    {
        String[] expectedrepositoryIds = new String[] { "1", "2", "3" };
        espr.setMultipleRepositoryIds(expectedrepositoryIds);
        assertEquals(expectedrepositoryIds, espr.getMultipleRepositoryIds());
    }
}
