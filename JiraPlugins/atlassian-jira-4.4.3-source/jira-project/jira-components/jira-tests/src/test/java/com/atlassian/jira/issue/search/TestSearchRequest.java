/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSearchRequest extends ListeningTestCase
{
    @Test
    public void testBlankConstructor()
    {
        SearchRequest sr = new SearchRequest();
        assertNotNull(sr.getQuery());
    }
}
