package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestWorkRatioClauseXmlHandler extends MockControllerTestCase
{
    WorkRatioClauseXmlHandler workRatioClauseXmlHandler;

    @Before
    public void setUp() throws Exception
    {
        workRatioClauseXmlHandler = new WorkRatioClauseXmlHandler();
    }

    @After
    public void tearDown() throws Exception
    {
        workRatioClauseXmlHandler = null;

    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        assertFalse(workRatioClauseXmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testElementHasNoNameAttribute() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasEmptyNameAttribute() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name=''></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasTooManyNameParts() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:abc:def'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasUnknownSuffix() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:def'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasNoValueAttribute() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:min'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasBadValueAttribute() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:min' value='abc'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'workratio' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testHappyPathMin() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:min' value='75'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        final TerminalClause clause = (TerminalClause) result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertEquals("workratio", clause.getName());
        assertEquals(Operator.GREATER_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand operand = (SingleValueOperand) clause.getOperand();
        assertEquals(new Long(75), operand.getLongValue());
    }

    @Test
    public void testHappyPathMinBestGuess() throws Exception
    {
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("My Cust Field");
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("customfield_12345");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<customfield_12345 name='customfield_12345:min' value='75'></customfield_12345>");
        Element el = doc.getElement("customfield_12345");
        final ClauseXmlHandler.BestGuessConversionResult result = (ClauseXmlHandler.BestGuessConversionResult) workRatioClauseXmlHandler.convertXmlToClause(el);
        result.setFieldManager(fieldManager);
        final TerminalClause clause = (TerminalClause) result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertEquals("We found a part of the saved filter 'me' related to field name 'My Cust Field', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'cf[12345]'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
        assertEquals("cf[12345]", clause.getName());
        assertEquals(Operator.GREATER_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand operand = (SingleValueOperand) clause.getOperand();
        assertEquals(new Long(75), operand.getLongValue());
    }

    @Test
    public void testHappyPathMax() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<workratio name='workratio:max' value='75'></workratio>");
        Element el = doc.getElement("workratio");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        final TerminalClause clause = (TerminalClause) result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertEquals("workratio", clause.getName());
        assertEquals(Operator.LESS_THAN_EQUALS, clause.getOperator());
        final SingleValueOperand operand = (SingleValueOperand) clause.getOperand();
        assertEquals(new Long(75), operand.getLongValue());
    }

    @Test
    public void testAttemptToConvertUnsupportedConstant() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<dogpoo><value>12</value></dogpoo>");
        Element el = doc.getElement("dogpoo");
        final ClauseXmlHandler.ConversionResult result = workRatioClauseXmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
}
