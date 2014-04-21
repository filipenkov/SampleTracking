package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestTextSystemFieldClauseXmlHandler extends MockControllerTestCase
{
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;

    @Before
    public void setUp() throws Exception
    {
        fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        final TextSystemFieldClauseXmlHandler handler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testElementMultipleFields() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>environment</field><field>body</field><field>summary</field><field>description</field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        final Clause clause = result.getClause();
        final List<Clause> clauses = clause.getClauses();
        assertEquals(4, clauses.size());
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertTrue(clauses.contains(new TerminalClauseImpl("summary", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("environment", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("comment", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("description", Operator.LIKE, "monkeys")));
    }
    
    @Test
    public void testElementWithUnexpectedName() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<blorg><field>environment</field><field>body</field><field>summary</field><field>description</field><query>monkeys</query></blorg>");
        Element el = doc.getElement("blorg");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final Clause clause = textSystemFieldClauseXmlHandler.convertXmlToClause(el).getClause();
        final List<Clause> clauses = clause.getClauses();
        assertEquals(4, clauses.size());
        assertTrue(clauses.contains(new TerminalClauseImpl("summary", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("environment", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("comment", Operator.LIKE, "monkeys")));
        assertTrue(clauses.contains(new TerminalClauseImpl("description", Operator.LIKE, "monkeys")));
    }
    
    @Test
    public void testElementMultipleQueries() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>environment</field><field>body</field><field>summary</field><field>description</field><query>elephants</query><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'multifieldlucene' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementWithAllBadField() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>animal</field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'multifieldlucene'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
    
    @Test
    public void testElementWithOneBadField() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>summary</field><field>animal</field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'animal'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertEquals(new TerminalClauseImpl("summary", Operator.LIKE, "monkeys"), result.getClause());
    }

    @Test
    public void testElementWithNoField() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'multifieldlucene' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementWithEmptyField() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<multifieldlucene><field></field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.ConversionResult result = textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'multifieldlucene' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
    
    @Test
    public void testElementGoodAndBestGuessFields() throws Exception
    {
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("My Cust Field");

        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("customfield_12345");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>summary</field><field>customfield_12345</field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.BestGuessConversionResult result = (ClauseXmlHandler.BestGuessConversionResult) textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        result.setFieldManager(fieldManager);
        final Clause clause = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertEquals("We found a part of the saved filter 'me' related to field name 'My Cust Field', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'cf[12345]'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertEquals(JqlQueryBuilder.newBuilder().where().defaultOr().summary("monkeys").addStringCondition("cf[12345]", Operator.LIKE, "monkeys").buildClause(), clause);
    }

    @Test
    public void testElementGoodAndBestGuessAndNotFoundFields() throws Exception
    {
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("My Cust Field");

        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("customfield_12345");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<multifieldlucene><field>dogpoo</field><field>customfield_12345</field><query>monkeys</query></multifieldlucene>");
        Element el = doc.getElement("multifieldlucene");
        final TextSystemFieldClauseXmlHandler textSystemFieldClauseXmlHandler = new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry);
        final ClauseXmlHandler.BestGuessConversionResult result = (ClauseXmlHandler.BestGuessConversionResult) textSystemFieldClauseXmlHandler.convertXmlToClause(el);
        result.setFieldManager(fieldManager);
        final Clause clause = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.\n"
                + "We found a part of the saved filter 'me' related to field name 'My Cust Field', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'cf[12345]'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertEquals(JqlQueryBuilder.newBuilder().where().defaultOr().addStringCondition("cf[12345]", Operator.LIKE, "monkeys").buildClause(), clause);
    }

}
