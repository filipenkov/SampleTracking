/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.services.vcs;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.vcs.RepositoryManager;
import com.mockobjects.dynamic.Mock;

public class TestVcsService extends LegacyJiraMockTestCase
{
    public TestVcsService(String s)
    {
        super(s);
    }

    public void testIsUnique()
    {
        VcsService vcsService = new VcsService();

        assertTrue(vcsService.isUnique());
    }

    public void testIsInternal()
    {
        VcsService vcsService = new VcsService();

        assertTrue(vcsService.isInternal());
    }

    public void testRun()
    {
        Mock repositoryManager = new Mock(RepositoryManager.class);

        repositoryManager.setStrict(true);

        repositoryManager.expectAndReturn("updateRepositories", Boolean.TRUE);

        VcsService vcsService = new VcsService((RepositoryManager) repositoryManager.proxy());

        vcsService.run();

        repositoryManager.verify();
    }
}
