package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.DefaultFieldFlagOperandRegistry;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;

/**
 * @since v4.0
 */
public class TestAffectedVersionClauseXmlHandler extends MockControllerTestCase
{
    AffectedVersionClauseXmlHandler affectedVersionClauseXmlHandler;

    @Before
    public void setUp() throws Exception
    {
        final FieldFlagOperandRegistry flagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        affectedVersionClauseXmlHandler = new AffectedVersionClauseXmlHandler(flagOperandRegistry);
    }

    @After
    public void tearDown() throws Exception
    {
        affectedVersionClauseXmlHandler = null;

    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        assertTrue(affectedVersionClauseXmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testXmlFieldIdSupported() throws Exception
    {
        replay();
        assertTrue(affectedVersionClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forAffectedVersion().getIndexField()));
        assertFalse(affectedVersionClauseXmlHandler.xmlFieldIdSupported(SystemSearchConstants.forProject().getIndexField()));
    }

    @Test
    public void testReleasedVersionsFlag() throws Exception
    {
        Document document = new Document("<version andQuery='false'><value>-3</value></version>");
        affectedVersionClauseXmlHandler = new AffectedVersionClauseXmlHandler(new DefaultFieldFlagOperandRegistry());
        replay();

        final ClauseXmlHandler.ConversionResult result = affectedVersionClauseXmlHandler.convertXmlToClause(document.getRoot());
        final TerminalClauseImpl expected = new TerminalClauseImpl(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS));
        assertEquals(expected, result.getClause());
    }

    @Test
    public void testUnreleasedVersionsFlag() throws Exception
    {
        Document document = new Document("<version andQuery='false'><value>-2</value></version>");
        affectedVersionClauseXmlHandler = new AffectedVersionClauseXmlHandler(new DefaultFieldFlagOperandRegistry());
        replay();

        final ClauseXmlHandler.ConversionResult result = affectedVersionClauseXmlHandler.convertXmlToClause(document.getRoot());
        final TerminalClauseImpl expected = new TerminalClauseImpl(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), Operator.IN, new FunctionOperand(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS));
        assertEquals(expected, result.getClause());
    }
}
