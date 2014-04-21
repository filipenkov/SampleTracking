package com.atlassian.jira.issue.customfields.impl;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v4.0
 */
public class TestTextCFType extends ListeningTestCase
{

    @Test
    public void testgetStringFromSingularObject() throws Exception
    {
        TextCFType textCFType = new TextCFType(null, null);

        // Writing this test for refactoring. I am just asserting that I don't change the current behaviour.
        assertEquals("", textCFType.getStringFromSingularObject(null));
        assertEquals("", textCFType.getStringFromSingularObject(""));
        assertEquals("Hello World", textCFType.getStringFromSingularObject("Hello World"));
    }

    @Test
    public void testGetSingularObjectFromString() throws Exception
    {
        TextCFType textCFType = new TextCFType(null, null);

        // Writing this test for refactoring. I am just asserting that I don't change the current behaviour.
        assertEquals(null, textCFType.getSingularObjectFromString(null));
        assertEquals("", textCFType.getSingularObjectFromString(""));
        assertEquals("Hello World", textCFType.getSingularObjectFromString("Hello World"));
    }


}
