package com.atlassian.jira.upgrade.tasks.jql;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v4.0
 */
public class TestSimpleCustomFieldClauseXmlHandler extends ListeningTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        final SimpleCustomFieldClauseXmlHandler handler = new SimpleCustomFieldClauseXmlHandler("monkey", Operator.GREATER_THAN);
        assertTrue(handler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlHappyPath() throws Exception
    {
        Document doc = new Document("<customfield_100 monkey='bob'/>");
        Element el = doc.getElement("customfield_100");
        final TerminalClauseImpl expected = new TerminalClauseImpl("cf[100]", Operator.GREATER_THAN, "bob");
        final SimpleCustomFieldClauseXmlHandler handler = new SimpleCustomFieldClauseXmlHandler("monkey", Operator.GREATER_THAN);
        assertEquals(expected, handler.convertXmlToClause(el).getClause());
    }

    @Test
    public void testGetClauseFromXmlNoValue() throws Exception
    {
        Document doc = new Document("<customfield_100/>");
        Element el = doc.getElement("customfield_100");
        final SimpleCustomFieldClauseXmlHandler handler = new SimpleCustomFieldClauseXmlHandler("monkey", Operator.GREATER_THAN);
        assertNull(handler.convertXmlToClause(el).getClause());
    }

    @Test
    public void testGetClauseFromXmlEmptyValue() throws Exception
    {
        Document doc = new Document("<customfield_100 monkey=''/>");
        Element el = doc.getElement("customfield_100");
        final SimpleCustomFieldClauseXmlHandler handler = new SimpleCustomFieldClauseXmlHandler("monkey", Operator.GREATER_THAN);
        assertNull(handler.convertXmlToClause(el).getClause());
    }

}
