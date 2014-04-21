package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstantsWithEmpty;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.plugin.jql.function.MembersOfFunction;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @since v4.0
 */
public class TestUserClauseXmlHandler extends MockControllerTestCase
{
    private UserFieldSearchConstantsWithEmpty searchConstants;
    private String userElementName = "issue_assignee";
    private String groupElementName = userElementName + "_group";
    private UserClauseXmlHandler xmlHandler;

    @Before
    public void setUp() throws Exception
    {
        searchConstants = SystemSearchConstants.forAssignee();
        xmlHandler = new UserClauseXmlHandler(searchConstants);
    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        assertFalse(xmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testElementHasNoSuchField() throws Exception
    {
        mockController.replay();
        // Redefine the element to be one we don't support for this test
        String userElementName = "userElement";
        Document doc = new Document("<"+userElementName+"/>");
        Element el = doc.getElement(userElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'userElement'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementHasNoValues() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+userElementName+"/>");
        Element el = doc.getElement(userElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'issue_assignee' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementSpecificUser() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+userElementName+" value='bob'/>");
        Element el = doc.getElement(userElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        final Clause fromXml = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertNotNull(fromXml);
        assertEquals(new TerminalClauseImpl("assignee", Operator.EQUALS, "bob"), fromXml);
    }

    @Test
    public void testElementSpecificUserBestGuess() throws Exception
    {
        final Field field = mockController.getMock(Field.class);
        field.getNameKey();
        mockController.setReturnValue("My Cust Field");
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.getField("customfield_12345");
        mockController.setReturnValue(field);

        mockController.replay();
        Document doc = new Document("<customfield_12345 value='bob'/>");
        Element el = doc.getElement("customfield_12345");
        final ClauseXmlHandler.BestGuessConversionResult result = (ClauseXmlHandler.BestGuessConversionResult) xmlHandler.convertXmlToClause(el);
        result.setFieldManager(fieldManager);
        final Clause fromXml = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, result.getResultType());
        assertNotNull(fromXml);
        assertEquals(new TerminalClauseImpl("cf[12345]", Operator.EQUALS, "bob"), fromXml);
        assertEquals("We found a part of the saved filter 'me' related to field name 'My Cust Field', which was likely saved by a plugin. We made our best guess to convert this part of the query so that it is now searching 'cf[12345]'. However, the filter may no longer bring back the correct results. Please manually check the filter to ensure it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testElementCurrentUser() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+userElementName+" value='"+ searchConstants.getCurrentUserSelectFlag() +"'/>");
        Element el = doc.getElement(userElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        final Clause fromXml = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertNotNull(fromXml);
        assertEquals(new TerminalClauseImpl("assignee", Operator.EQUALS, new FunctionOperand(CurrentUserFunction.FUNCTION_CURRENT_USER)), fromXml);
    }

    @Test
    public void testElementEmpty() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+userElementName+" value='"+ searchConstants.getEmptySelectFlag() +"'/>");
        Element el = doc.getElement(userElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        final Clause fromXml = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertNotNull(fromXml);
        assertEquals(new TerminalClauseImpl("assignee", Operator.IS, new EmptyOperand()), fromXml);
    }

    @Test
    public void testElementSpecificGroup() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+groupElementName+" groupName='bob'/>");
        Element el = doc.getElement(groupElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        final Clause fromXml = result.getClause();
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result.getResultType());
        assertNotNull(fromXml);
        assertEquals(new TerminalClauseImpl("assignee", Operator.IN, new FunctionOperand(MembersOfFunction.FUNCTION_MEMBERSOF, "bob")), fromXml);
    }

    @Test
    public void testElementSpecificGroupNoValue() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<"+groupElementName+"/>");
        Element el = doc.getElement(groupElementName);
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'issue_assignee_group' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }

    @Test
    public void testInvalidElement() throws Exception
    {
        mockController.replay();
        Document doc = new Document("<dogpoo value='bob'/>");
        Element el = doc.getElement("dogpoo");
        final ClauseXmlHandler.ConversionResult result = xmlHandler.convertXmlToClause(el);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'dogpoo'. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result.getMessage(new MockI18nBean(), "me"));
    }
    
}
