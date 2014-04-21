package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.plugin.jql.function.CascadeOptionFunction;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;
import electric.xml.Document;
import electric.xml.Element;
import electric.xml.Elements;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * @since v4.0
 */
public class TestCascadeSelectParameterClauseXmlHandler extends MockControllerTestCase
{
    String childAndParent = "<searchrequest name='cascade1'>\n"
            + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.StringParameter'>\n"
            + "     <customfield_10060 name='customfield_10060' value='10010'/>\n"
            + "   </parameter>\n"
            + "   <parameter class='com.atlassian.jira.issue.search.parameters.lucene.StringParameter'>\n"
            + "     <customfield_10060 name='customfield_10060:1' value='10011'/>\n"
            + "   </parameter>\n"
            + " </searchrequest> ";

    String parent = "<searchrequest name='cascade1'>\n"
            + "<parameter class='com.atlassian.jira.issue.search.parameters.lucene.StringParameter'>\n"
            + "     <customfield_10060 name='customfield_10060' value='10010'/>\n"
            + "   </parameter>\\r                                                                                              \n"
            + " </searchrequest> ";

    String parentSnippet ="<customfield_10060 name='customfield_10060' value='10010'/>";

    private JqlSelectOptionsUtil jqlSelectOptionsUtil;


    @Before
    public void setUp() throws Exception
    {
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);
        assertFalse(xmlHandler.isSafeToNamifyValue());
    }

    @Test
    public void testGetClauseFromXmlParentAndChildHappyPath() throws Exception
    {
        final MockOption parentOption = new MockOption(null, null, null, "Parent", null, 10010L);
        final MockOption childOption = new MockOption(null, null, null, "Child", null, 10011L);

        jqlSelectOptionsUtil.getOptionById(10011L);
        mockController.setReturnValue(childOption);

        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(parentOption);

        Document doc = new Document(childAndParent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        final Element parentElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result1.getResultType());
        final Clause result = result1.getClause();
        final Clause expectedResult = new TerminalClauseImpl(JqlCustomFieldId.toString(10060), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "10010", "10011"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlParentAndChildInValidParentId() throws Exception
    {
        final MockOption childOption = new MockOption(null, null, null, "Child", null, 10011L);

        jqlSelectOptionsUtil.getOptionById(10011L);
        mockController.setReturnValue(childOption);

        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(null);

        Document doc = new Document(childAndParent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        final Element parentElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result1.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_10060' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result1.getMessage(new MockI18nBean(), "me"));

        mockController.verify();
    }
    
    @Test
    public void testGetClauseFromXmlParentAndChildInValidChildId() throws Exception
    {
        final MockOption parentOption = new MockOption(null, null, null, "Parent", null, 10010L);

        jqlSelectOptionsUtil.getOptionById(10011L);
        mockController.setReturnValue(null);

        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(parentOption);

        Document doc = new Document(childAndParent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        final Element parentElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result1.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_10060' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result1.getMessage(new MockI18nBean(), "me"));

        mockController.verify();
    }
    
    @Test
    public void testGetClauseFromXmlParentHappyPath() throws Exception
    {
        final MockOption parentOption = new MockOption(null, null, null, "Parent", null, 10010L);

        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(parentOption);

        Document doc = new Document(parent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        final Element parentElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result1.getResultType());
        final Clause result = result1.getClause();
        final Clause expectedResult = new TerminalClauseImpl(JqlCustomFieldId.toString(10060), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "10010"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlParentSnippet() throws Exception
    {
        final MockOption parentOption = new MockOption(null, null, null, "Parent", null, 10010L);

        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(parentOption);

        Document doc = new Document(parentSnippet);
        final Element parentElement = doc.getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, result1.getResultType());
        final Clause result = result1.getClause();
        final Clause expectedResult = new TerminalClauseImpl(JqlCustomFieldId.toString(10060), Operator.IN, new FunctionOperand(CascadeOptionFunction.FUNCTION_CASCADE_OPTION, "10010"));

        assertEquals(expectedResult, result);

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlParentInvalidId() throws Exception
    {
        jqlSelectOptionsUtil.getOptionById(10010L);
        mockController.setReturnValue(null);

        Document doc = new Document(parent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        final Element parentElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(parentElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.FAILED_CONVERSION, result1.getResultType());
        assertEquals("We were unable to upgrade the part of the saved filter 'me' that was saved against the field id 'customfield_10060' as we could not interpret the values stored in the XML. This error means that the filter will no longer bring back the same results. Please edit the filter manually to ensure that it is searching what you originally intended.", result1.getMessage(new MockI18nBean(), "me"));

        mockController.verify();
    }

    @Test
    public void testGetClauseFromXmlParentAndChildChildReturnsNull() throws Exception
    {
        Document doc = new Document(childAndParent);
        final Element request = doc.getFirstElement();

        final Elements children = request.getElements();
        children.next();
        final Element childElement = children.next().getFirstElement();

        mockController.replay();
        final CascadeSelectParameterClauseXmlHandler xmlHandler = new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil);

        final ClauseXmlHandler.ConversionResult result1 = xmlHandler.convertXmlToClause(childElement);
        assertEquals(ClauseXmlHandler.ConversionResultType.NOOP_CONVERSION, result1.getResultType());
        assertNull(result1.getMessage(new MockI18nBean(), "me"));

        mockController.verify();
    }
}
