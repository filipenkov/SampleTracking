package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import electric.xml.Document;
import electric.xml.ParseException;

/**
 * A test for {@link com.atlassian.jira.upgrade.tasks.jql.JqlXmlSupport}.
 *
 * @since v4.0
 */
public class TestJqlXmlSupport extends ListeningTestCase
{
    @Test
    public void testGetNameWithAttribute() throws ParseException
    {
        Document document = new Document("<name name=\"myname\"/>");
        assertEquals("myname", JqlXmlSupport.getName(document.getFirstElement()));
    }

    @Test
    public void testGetNameWithoutAttribute() throws ParseException
    {
        Document document = new Document("<returnme name2=\"myname\"/>");
        assertEquals("returnme", JqlXmlSupport.getName(document.getFirstElement()));
    }

    @Test
    public void testGetNameNullElement() throws ParseException
    {
        try
        {
            JqlXmlSupport.getName(null);
            fail("Exception was expected.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetTextFromSubElementNoSubElement() throws Exception
    {
        Document document = new Document("<name><ab>ab</ab></name>");
        assertNull(JqlXmlSupport.getTextFromSubElement(document.getFirstElement(), "cd"));
    }

    @Test
    public void testGetTextFromSubElementNoText() throws Exception
    {
        Document document = new Document("<name><ab></ab></name>");
        assertNull(JqlXmlSupport.getTextFromSubElement(document.getFirstElement(), "ab"));
    }

    @Test
    public void testGetTextFromSubElement() throws Exception
    {
        Document document = new Document("<name><ab>def</ab></name>");
        assertEquals("def", JqlXmlSupport.getTextFromSubElement(document.getFirstElement(), "ab"));
    }

    @Test
    public void testGetTextFromSubElementBadArgs() throws Exception
    {
        Document document = new Document("<name><ab>def</ab></name>");

        try
        {
            JqlXmlSupport.getTextFromSubElement(document.getFirstElement(), "");
            fail("Expected exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            JqlXmlSupport.getTextFromSubElement(document.getFirstElement(), null);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }

        try
        {
            JqlXmlSupport.getTextFromSubElement(null, "abc");
            fail("Expected exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }
}
