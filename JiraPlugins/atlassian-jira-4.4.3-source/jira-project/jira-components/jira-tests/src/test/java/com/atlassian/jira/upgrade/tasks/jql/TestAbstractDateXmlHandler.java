package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Simple test for {@link com.atlassian.jira.upgrade.tasks.jql.AbstractDateXmlHandler}.
 *
 * @since v4.0
 */
public class TestAbstractDateXmlHandler extends MockControllerTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created");
        assertFalse(dateXmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlNullElement() throws Exception
    {
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created");
        try
        {
            dateXmlHandler.convertXmlToClause(null);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testGetClauseFromXmlNoSupportedClauseNameMapping() throws Exception
    {
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created");

        Document doc = new Document("<dogpoo></dogpoo>");
        Element el = doc.getElement("dogpoo");
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlNoDates() throws Exception
    {
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created");

        Document doc = new Document("<created></created>");
        Element el = doc.getElement("created");
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'created' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlOnlyFrom() throws Exception
    {
        final String expectedDate = "1981-01-12";
        Document doc = new Document("<created></created>");
        Element el = doc.getElement("created");
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created", expectedDate, null);
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(el);
        final Clause actualClause = result.getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.GREATER_THAN_EQUALS, expectedDate);
        assertEquals(expectedClause, actualClause);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
    }

    @Test
    public void testGetClauseFromXmlOnlyTo() throws Exception
    {
        Document doc = new Document("<created></created>");
        Element el = doc.getElement("created");

        final String expectedDate = "52667263763873";
        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created", null, expectedDate);
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(el);
        final Clause actualClause = result.getClause();
        final Clause expectedClause = new TerminalClauseImpl("created", Operator.LESS_THAN_EQUALS, expectedDate);
        assertEquals(expectedClause, actualClause);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
    }

    @Test
    public void testGetClauseFromXml() throws Exception
    {
        Document doc = new Document("<created></created>");
        Element el = doc.getElement("created");
        final String fromDate = "3/4/0001";
        final String toDate = "15/6/2007";
        final String fieldName = "created";

        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler(fieldName, fromDate, toDate);
        final ClauseXmlHandler.ConversionResult result = dateXmlHandler.convertXmlToClause(el);
        final Clause actualClause = result.getClause();
        final Clause expectedClause = new AndClause(
                new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, fromDate),
                new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, toDate));

        assertEquals(expectedClause, actualClause);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
    }

    @Test
    public void testGetClauseFromXmlBestGuess() throws Exception
    {
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("issue.field.description");
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("description");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<description></description>");
        Element el = doc.getElement("description");
        final String fromDate = "3/4/0001";
        final String toDate = "15/6/2007";
        final String fieldName = "description";

        final AbstractDateXmlHandler dateXmlHandler = new DumbAbstractDateXmlHandler("created", fromDate, toDate);
        final ClauseXmlHandler.BestGuessConversionResult result = (ClauseXmlHandler.BestGuessConversionResult) dateXmlHandler.convertXmlToClause(el);
        result.setFieldManager(fieldManager);
        final Clause actualClause = result.getClause();
        final Clause expectedClause = new AndClause(
                new TerminalClauseImpl(fieldName, Operator.GREATER_THAN_EQUALS, fromDate),
                new TerminalClauseImpl(fieldName, Operator.LESS_THAN_EQUALS, toDate));

        assertEquals(expectedClause, actualClause);
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertEquals("We found a part of the saved filter 'me' related to field name 'Description', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'description'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    private static class DumbAbstractDateXmlHandler extends AbstractDateXmlHandler
    {
        private final String fromDate;
        private final String toDate;

        private DumbAbstractDateXmlHandler(final String fieldName)
        {
            this(fieldName, null, null);
        }

        private DumbAbstractDateXmlHandler(final String fieldName, final String fromDate, final String toDate)
        {
            super(Collections.singletonList(fieldName));
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        protected String getLowerBound(final String fieldName, final Element element)
        {
            return fromDate;
        }

        protected String getUpperBound(final String fieldName, final Element element)
        {
            return toDate;
        }
    }
}
