/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.tabpanels;

import com.atlassian.jira.vcs.cvsimpl.CVSCommit;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestCVSAction extends LegacyJiraMockTestCase
{
    private CVSAction commit;

    public TestCVSAction(String s)
    {
        super(s);
    }

    public void testHasRepositoryViewerFalse()
    {
        commit = new CVSAction(null, new CVSCommit(null, null));
        assertFalse(commit.hasRepositoryViewer());
    }

    public void testGetFileLinkRepositoryBrowserNull()
    {
        String filePath = "testfilepath";
        try
        {
            commit = new CVSAction(null, new CVSCommit(null, null));
            commit.getFileLink(filePath);
            fail("IllegalStateException should have been thrown");
        }
        catch (IllegalStateException e)
        {
            assertEquals("Cannot return file link as repositoryBrowser is not set.", e.getMessage());
        }
    }

    public void testGetRevisionLinkRepositoryBrowserNull()
    {
        String filePath = "testfilepath";
        String revision = "testrevision";
        try
        {
            commit = new CVSAction(null, new CVSCommit(null, null));
            commit.getRevisionLink(filePath, revision);
            fail("IllegalStateException should have been thrown");
        }
        catch (IllegalStateException e)
        {
            assertEquals("Cannot return revision link as repositoryBrowser is not set.", e.getMessage());
        }
    }

    public void testGetDiffLinkRepositoryBrowserNull()
    {
        String filePath = "testfilepath";
        String revision = "testrevision";
        try
        {
            commit = new CVSAction(null, new CVSCommit(null, null));
            commit.getDiffLink(filePath, revision);
            fail("IllegalStateException should have been thrown");
        }
        catch (IllegalStateException e)
        {
            assertEquals("Cannot return diff link as repositoryBrowser is not set.", e.getMessage());
        }
    }
}
