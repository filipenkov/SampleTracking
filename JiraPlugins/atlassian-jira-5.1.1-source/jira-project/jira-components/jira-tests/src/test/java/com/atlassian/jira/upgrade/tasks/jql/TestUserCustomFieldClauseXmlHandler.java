package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.0
 */
public class TestUserCustomFieldClauseXmlHandler extends ListeningTestCase
{
    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        final UserParameterCustomFieldClauseXmlHandler handler = new UserParameterCustomFieldClauseXmlHandler();
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlHappyPath() throws Exception
    {
        Document doc = new Document("<customfield_100 value='bob'/>");
        Element el = doc.getElement("customfield_100");
        final TerminalClauseImpl expected = new TerminalClauseImpl("cf[100]", Operator.EQUALS, "bob");
        final UserParameterCustomFieldClauseXmlHandler handler = new UserParameterCustomFieldClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertEquals(expected, result.getClause());
    }

    @Test
    public void testGetClauseFromXmlCurrentUser() throws Exception
    {
        Document doc = new Document("<customfield_100 value='"+ DocumentConstants.ISSUE_CURRENT_USER+"'/>");
        Element el = doc.getElement("customfield_100");
        final TerminalClauseImpl expected = new TerminalClauseImpl("cf[100]", Operator.EQUALS, new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER));
        final UserParameterCustomFieldClauseXmlHandler handler = new UserParameterCustomFieldClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertEquals(expected, result.getClause());
    }

    @Test
    public void testGetClauseFromXmlNoValue() throws Exception
    {
        Document doc = new Document("<customfield_100/>");
        Element el = doc.getElement("customfield_100");
        final UserParameterCustomFieldClauseXmlHandler handler = new UserParameterCustomFieldClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_100' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testGetClauseFromXmlEmptyValue() throws Exception
    {
        Document doc = new Document("<customfield_100 value=''/>");
        Element el = doc.getElement("customfield_100");
        final UserParameterCustomFieldClauseXmlHandler handler = new UserParameterCustomFieldClauseXmlHandler();
        final ClauseXmlHandler.ConversionResult result = handler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
    }
}
