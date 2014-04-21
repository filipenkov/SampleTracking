package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.DefaultFieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;

/**
 * @since v4.0
 */
public class TestConstantsClauseXmlHandler extends MockControllerTestCase
{
    ConstantsClauseXmlHandler constantsClauseXmlHandler;

    @Before
    public void setUp() throws Exception
    {
        final FieldFlagOperandRegistry flagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        constantsClauseXmlHandler = new ConstantsClauseXmlHandler(flagOperandRegistry);
    }

    @After
    public void tearDown() throws Exception
    {
        constantsClauseXmlHandler = null;

    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        assertTrue(constantsClauseXmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testXmlFieldIdSupported() throws Exception
    {
        replay();
        assertTrue(constantsClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forPriority().getIndexField()));
        assertTrue(constantsClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forResolution().getIndexField()));
        assertTrue(constantsClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forStatus().getIndexField()));
        assertTrue(constantsClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forIssueType().getIndexField()));
        assertFalse(constantsClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forProject().getIndexField()));
    }

    @Test
    public void testResolutionUnresolved() throws Exception
    {
        Document document = new Document("<resolution andQuery='false'><value>-1</value></resolution>");
        constantsClauseXmlHandler = new ConstantsClauseXmlHandler(new DefaultFieldFlagOperandRegistry());
        replay();

        final ClauseXmlHandler.ConversionResult result = constantsClauseXmlHandler.convertXmlToClause(document.getRoot());
        final TerminalClauseImpl expected = new TerminalClauseImpl(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), Operator.EQUALS, "Unresolved");
        assertEquals(expected, result.getClause());
    }

    @Test
    public void testIssueTypeAllStandardTypesFlag() throws Exception
    {
        Document document = new Document("<type andQuery='false'><value>-2</value></type>");
        constantsClauseXmlHandler = new ConstantsClauseXmlHandler(new DefaultFieldFlagOperandRegistry());
        replay();

        final ClauseXmlHandler.ConversionResult result = constantsClauseXmlHandler.convertXmlToClause(document.getRoot());
        final TerminalClauseImpl expected = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES));
        assertEquals(expected, result.getClause());
    }

    @Test
    public void testIssueTypeAllSubTaskTypesFlag() throws Exception
    {
        Document document = new Document("<type andQuery='false'><value>-3</value></type>");
        constantsClauseXmlHandler = new ConstantsClauseXmlHandler(new DefaultFieldFlagOperandRegistry());
        replay();

        final ClauseXmlHandler.ConversionResult result = constantsClauseXmlHandler.convertXmlToClause(document.getRoot());
        final TerminalClauseImpl expected = new TerminalClauseImpl(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES));
        assertEquals(expected, result.getClause());
    }
}
