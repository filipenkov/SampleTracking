package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestAbstractCustomFieldClauseXmlHandler extends MockControllerTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                throw new UnsupportedOperationException();
            }
        };

        assertTrue(handler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlHappyPath() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<customfield_100 monkey='bob'/>");
        Element el = doc.getElement("customfield_100");
        final AtomicBoolean called = new AtomicBoolean(false);
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                called.set(true);
                assertEquals(jqlFieldName, "cf[100]");
                assertEquals(value, "bob");
                return new TerminalClauseImpl("cf[100]", Operator.EQUALS, "bob");
            }
        };
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertTrue(called.get());
    }

    @Test
    public void testGetClauseFromXmlNoValue() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<customfield_100/>");
        Element el = doc.getElement("customfield_100");
        final AtomicBoolean called = new AtomicBoolean(false);
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                called.set(true);
                return null;
            }
        };
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertFalse(called.get());
    }

    @Test
    public void testGetClauseFromXmlEmptyValue() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<customfield_100 monkey=''/>");
        Element el = doc.getElement("customfield_100");
        final AtomicBoolean called = new AtomicBoolean(false);
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                called.set(true);
                return null;
            }
        };
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertFalse(called.get());
    }

    @Test
    public void testGetClauseFromXmlInvalidId() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<customfield_poo monkey='bob'/>");
        Element el = doc.getElement("customfield_poo");
        final AtomicBoolean called = new AtomicBoolean(false);
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                called.set(true);
                return null;
            }
        };
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_poo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertFalse(called.get());
    }

    @Test
    public void testGetClauseFromXmlInvalidName() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<dogpoo monkey='bob'/>");
        Element el = doc.getElement("dogpoo");
        final AtomicBoolean called = new AtomicBoolean(false);
        final AbstractCustomFieldClauseXmlHandler handler = new AbstractCustomFieldClauseXmlHandler("monkey")
        {
            Clause createClause(final String jqlFieldName, final String value)
            {
                called.set(true);
                return null;
            }
        };

        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertFalse(called.get());
    }

}
