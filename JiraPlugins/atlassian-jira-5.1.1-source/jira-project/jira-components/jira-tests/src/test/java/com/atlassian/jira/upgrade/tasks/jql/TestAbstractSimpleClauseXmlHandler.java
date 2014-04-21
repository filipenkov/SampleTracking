package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.MultiValueOperand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestAbstractSimpleClauseXmlHandler extends MockControllerTestCase
{
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;
    final private String jqlName = "jqlName";

    @Before
    public void setUp() throws Exception
    {
        fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testSingleValueNoFlag() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "value");
        mockController.setReturnValue(null);
        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);        
        final Clause result = handler.getClauseForValues(jqlName, CollectionBuilder.newBuilder("value").asList());
        TerminalClause expectedResult = new TerminalClauseImpl(jqlName, Operator.EQUALS, "value");
        assertEquals(expectedResult, result);        
        mockController.verify();
    }

    @Test
    public void testSingleValueLongValue() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "10");
        mockController.setReturnValue(null);
        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        final Clause result = handler.getClauseForValues(jqlName, CollectionBuilder.newBuilder("10").asList());
        TerminalClause expectedResult = new TerminalClauseImpl(jqlName, Operator.EQUALS, 10L);
        assertEquals(expectedResult, result);
        mockController.verify();
    }

     @Test
     public void testSingleValueFlag() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "value");
        mockController.setReturnValue(new SingleValueOperand("blah"));
        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        final Clause result = handler.getClauseForValues(jqlName, CollectionBuilder.newBuilder("value").asList());
        TerminalClause expectedResult = new TerminalClauseImpl(jqlName, Operator.EQUALS, "blah");
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testMultiValueFlagNonFlagAndLong() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "value1");
        mockController.setReturnValue(new SingleValueOperand("blah"));

        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "value2");
        mockController.setReturnValue(null);

        fieldFlagOperandRegistry.getOperandForFlag(jqlName, "10");
        mockController.setReturnValue(null);


        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        final Clause result = handler.getClauseForValues(jqlName, CollectionBuilder.newBuilder("value1", "value2", "10").asList());
        TerminalClause expectedResult = new TerminalClauseImpl(jqlName, Operator.IN, new MultiValueOperand(new SingleValueOperand("blah"), new SingleValueOperand("value2"), new SingleValueOperand(10L)));
        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetValuesFromElement() throws Exception
    {
        Document doc = new Document("<blah><value>12</value><value>22</value></blah>");

        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);

        final List<String> result = handler.getValuesFromElement(doc.getElement("blah"), "blah");
        final List<String> expectedResult = CollectionBuilder.newBuilder("12", "22").asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testGetValuesFromEmpty() throws Exception
    {
        Document doc = new Document("<blah></blah>");

        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);

        final List<String> result = handler.getValuesFromElement(doc.getElement("blah"), "blah");
        final List<String> expectedResult = CollectionBuilder.<String>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }
    
    @Test
    public void testGetValuesFromInvalidElementName() throws Exception
    {
        Document doc = new Document("<blah><monkey>banana</monkey></blah>");

        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);

        final List<String> result = handler.getValuesFromElement(doc.getElement("blah"), "blah");
        final List<String> expectedResult = CollectionBuilder.<String>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }
    
    @Test
    public void testGetValuesFromEmptyElement() throws Exception
    {
        Document doc = new Document("<blah><value></value></blah>");

        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);

        final List<String> result = handler.getValuesFromElement(doc.getElement("blah"), "blah");
        final List<String> expectedResult = CollectionBuilder.<String>newBuilder().asList();

        assertEquals(expectedResult, result);
        mockController.verify();
    }

    @Test
    public void testElementHasNoValues() throws Exception
    {
        mockController.replay();
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        Document doc = new Document("<priority></priority>");
        Element el = doc.getElement("priority");
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'priority' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementOneValue() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag("priority", "12");
        mockController.setReturnValue(null);
        mockController.replay();
        Document doc = new Document("<priority><value>12</value></priority>");
        Element el = doc.getElement("priority");
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        final TerminalClause clauseFromXml = (TerminalClause) handler.convertXmlToClause(el).getClause();
        assertNotNull(clauseFromXml);
        assertEquals("priority", clauseFromXml.getName());
        assertEquals(Operator.EQUALS, clauseFromXml.getOperator());
        assertTrue(clauseFromXml.getOperand() instanceof SingleValueOperand);
        SingleValueOperand operand = (SingleValueOperand)clauseFromXml.getOperand();
        assertEquals(new Long(12), operand.getLongValue());
        assertNull(operand.getStringValue());
    }

    @Test
    public void testElementOneValueBestGuess() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag("cf[12345]", "12");
        mockController.setReturnValue(null);

        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry, false);
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("issue.field.description");
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("customfield_12345");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<customfield_12345><value>12</value></customfield_12345>");
        Element el = doc.getElement("customfield_12345");
        final ClauseXmlHandler.ConversionResult conversionResult = handler.convertXmlToClause(el);
        final TerminalClause clauseFromXml = (TerminalClause) conversionResult.getClause();
        assertNotNull(clauseFromXml);
        assertEquals("cf[12345]", clauseFromXml.getName());
        assertEquals(Operator.EQUALS, clauseFromXml.getOperator());
        assertTrue(clauseFromXml.getOperand() instanceof SingleValueOperand);
        SingleValueOperand operand = (SingleValueOperand)clauseFromXml.getOperand();
        assertEquals(new Long(12), operand.getLongValue());
        assertNull(operand.getStringValue());
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, conversionResult.getResultType());

        ((ClauseXmlHandler.BestGuessConversionResult)conversionResult).setFieldManager(fieldManager);

        assertEquals("We found a part of the saved filter 'me' related to field name 'Description', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'cf[12345]'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", conversionResult.getMessage(new MockI18nBean(), "me"));
        mockController.verify();
    }

    @Test
    public void testElementMultipleValues() throws Exception
    {
        fieldFlagOperandRegistry.getOperandForFlag("priority", "12");
        mockController.setReturnValue(null);
        fieldFlagOperandRegistry.getOperandForFlag("priority", "22");
        mockController.setReturnValue(null);
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        mockController.replay();
        Document doc = new Document("<priority><value>12</value><value>22</value></priority>");
        Element el = doc.getElement("priority");
        final TerminalClause clauseFromXml = (TerminalClause) handler.convertXmlToClause(el).getClause();
        assertNotNull(clauseFromXml);
        assertEquals("priority", clauseFromXml.getName());
        assertEquals(Operator.IN, clauseFromXml.getOperator());
        assertTrue(clauseFromXml.getOperand() instanceof MultiValueOperand);
        MultiValueOperand operand = (MultiValueOperand)clauseFromXml.getOperand();
        assertEquals(2, operand.getValues().size());
        assertTrue(operand.getValues().contains(new SingleValueOperand(12L)));
        assertTrue(operand.getValues().contains(new SingleValueOperand(22L)));
    }

    @Test
    public void testAttemptToConvertUnsupportedConstant() throws Exception
    {
        final MyAbstractSimpleClauseXmlHandler handler = new MyAbstractSimpleClauseXmlHandler(fieldFlagOperandRegistry);
        mockController.replay();
        Document doc = new Document("<dogpoo><value>12</value></dogpoo>");
        Element el = doc.getElement("dogpoo");
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    class MyAbstractSimpleClauseXmlHandler extends AbstractSimpleClauseXmlHandler
    {
        private final boolean supportsId;

        MyAbstractSimpleClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry, final boolean supportsId)
        {
            super(fieldFlagOperandRegistry);
            this.supportsId = supportsId;
        }

        protected MyAbstractSimpleClauseXmlHandler(final FieldFlagOperandRegistry fieldFlagOperandRegistry)
        {
            super(fieldFlagOperandRegistry);
            this.supportsId = true;
        }

        protected boolean xmlFieldIdSupported(final String xmlFieldId)
        {
            return supportsId;
        }
    }
}
