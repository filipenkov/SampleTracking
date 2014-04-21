package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestStringRangeParameterClauseXmlHandler extends ListeningTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlLessThan() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob' operator='&lt;='/>");
        Element el = doc.getElement("customfield_100");

        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final Clause result = handler.convertXmlToClause(el).getClause();
        final Clause expectedResult = new TerminalClauseImpl("cf[100]", Operator.LESS_THAN_EQUALS, "bob");
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetClauseFromXmlGreaterThan() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob' operator='&gt;='/>");
        Element el = doc.getElement("customfield_100");

        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final Clause result = handler.convertXmlToClause(el).getClause();
        final Clause expectedResult = new TerminalClauseImpl("cf[100]", Operator.GREATER_THAN_EQUALS, "bob");
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetClauseFromXmlEmptyValue() throws Exception
    {
        Document doc = new Document("<customfield_100 value='' operator='&lt;='/>");
        Element el = doc.getElement("customfield_100");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
    
    @Test
    public void testGetClauseFromXmlEmptyOperator() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob' operator=''/>");
        Element el = doc.getElement("customfield_100");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlInvalidOperator() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob' operator='sdf'/>");
        Element el = doc.getElement("customfield_100");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
    
    @Test
    public void testGetClauseFromXmlNoOperator() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob'/>");
        Element el = doc.getElement("customfield_100");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlInvalidId() throws Exception
    {
        Document doc = new Document("<customfield_poo value='bob' operator='&lt;='/>");
        Element el = doc.getElement("customfield_poo");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();

        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_poo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlInvalidName() throws Exception
    {
        Document doc = new Document("<dogpoo value='bob' operator='&lt;='/>");
        Element el = doc.getElement("dogpoo");
        final StringRangeParameterClauseXmlHandler handler = new StringRangeParameterClauseXmlHandler();

        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
}
