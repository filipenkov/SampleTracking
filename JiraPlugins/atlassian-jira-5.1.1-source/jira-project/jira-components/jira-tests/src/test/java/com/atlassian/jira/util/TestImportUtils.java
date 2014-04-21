/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestImportUtils extends ListeningTestCase
{
    @After
    public void tearDown() throws Exception
    {
        ImportUtils.setSubvertSecurityScheme(false);
        ImportUtils.setIndexIssues(true);
    }

    @Test
    public void testSubvertSecurityScheme()
    {
        assertFalse("Default to not subverting security scheme", ImportUtils.isSubvertSecurityScheme());
        ImportUtils.setSubvertSecurityScheme(true);
        assertTrue("Subvert Security Scheme should be true", ImportUtils.isSubvertSecurityScheme());
    }

    @Test
    public void testIndexIssues()
    {
        assertTrue("Default to indexing issues", ImportUtils.isIndexIssues());
        ImportUtils.setIndexIssues(false);
        assertFalse("Indexing Issues should be false", ImportUtils.isIndexIssues());
    }
}
