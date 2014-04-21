package com.atlassian.jira.imports.project.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssueLinkType;
import com.atlassian.core.util.map.EasyMap;

/**
 * @since v3.13
 */
public class TestIssueLinkTypeParserImpl extends ListeningTestCase
{
    @Test
    public void testParse() throws ParseException
    {
        IssueLinkTypeParserImpl issueLinkTypeParser = new IssueLinkTypeParserImpl();
        //     <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        ExternalIssueLinkType externalIssueLinkType = issueLinkTypeParser.parse(EasyMap.build("id", "10", "linkname", "jira_subtask_link", "inward", "jira_subtask_inward", "outward", "jira_subtask_outward", "style", "jira_subtask"));
        assertEquals("10", externalIssueLinkType.getId());
        assertEquals("jira_subtask_link", externalIssueLinkType.getLinkname());
        assertEquals("jira_subtask", externalIssueLinkType.getStyle());
    }

    @Test
    public void testParseNoId()
    {
        IssueLinkTypeParserImpl issueLinkTypeParser = new IssueLinkTypeParserImpl();
        //     <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        try
        {
            issueLinkTypeParser.parse(EasyMap.build("linkname", "jira_subtask_link", "inward", "jira_subtask_inward", "outward", "jira_subtask_outward", "style", "jira_subtask"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
            assertEquals("No 'id' field for IssueLinkType.", e.getMessage());
        }
    }

    @Test
    public void testParseNoLinkname()
    {
        IssueLinkTypeParserImpl issueLinkTypeParser = new IssueLinkTypeParserImpl();
        //     <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        try
        {
            issueLinkTypeParser.parse(EasyMap.build("id", "10", "inward", "jira_subtask_inward", "outward", "jira_subtask_outward", "style", "jira_subtask"));
            fail("Should throw ParseException.");
        }
        catch (ParseException e)
        {
            // Expected.
            assertEquals("No 'linkname' field for IssueLinkType 10.", e.getMessage());
        }
    }

    @Test
    public void testParseNoStyle() throws ParseException
    {
        IssueLinkTypeParserImpl issueLinkTypeParser = new IssueLinkTypeParserImpl();
        //     <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        ExternalIssueLinkType externalIssueLinkType = issueLinkTypeParser.parse(EasyMap.build("id", "10", "linkname", "jira_subtask_link", "inward", "jira_subtask_inward", "outward", "jira_subtask_outward", "style", "jira_subtask"));
        // This is allowed - style is optional.
        assertEquals("10", externalIssueLinkType.getId());
        assertEquals("jira_subtask_link", externalIssueLinkType.getLinkname());
    }

    @Test
    public void testParseNull() throws ParseException
    {
        try
        {
            new IssueLinkTypeParserImpl().parse(null);
            fail("Uncool.");
        }
        catch (IllegalArgumentException e)
        {
            // Cool.
        }
    }

}
