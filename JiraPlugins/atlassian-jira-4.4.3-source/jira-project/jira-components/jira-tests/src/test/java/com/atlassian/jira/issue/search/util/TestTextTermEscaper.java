package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestTextTermEscaper extends ListeningTestCase
{
    @Test
    public void testEscapesColon() throws Exception
    {
        assertEquals("priority\\:1", new TextTermEscaper().get("priority:1"));
    }

    @Test
    public void testEscapesBackslash() throws Exception
    {
        assertEquals("priority\\\\1", new TextTermEscaper().get("priority\\1"));
    }

    @Test
    public void testNotEscapesRabbitEars() throws Exception
    {
        assertEquals("priority\"1", new TextTermEscaper().get("priority\"1"));
    }

    @Test
    public void testNotEscapesWildCards() throws Exception
    {
        assertEquals("priority*1?test", new TextTermEscaper().get("priority*1?test"));
    }
}
