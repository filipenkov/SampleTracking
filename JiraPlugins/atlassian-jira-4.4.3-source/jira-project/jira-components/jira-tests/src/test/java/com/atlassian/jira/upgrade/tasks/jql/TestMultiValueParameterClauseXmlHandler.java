package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.query.clause.TerminalClauseImpl;
import electric.xml.Document;
import electric.xml.Element;
import org.easymock.EasyMock;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestMultiValueParameterClauseXmlHandler extends MockControllerTestCase
{
    private MultiValueParameterClauseXmlHandler handler;
    private CustomFieldManager customFieldManager;

    @Before
    public void setUp() throws Exception
    {
        final FieldFlagOperandRegistry flagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
        flagOperandRegistry.getOperandForFlag(EasyMock.isA(String.class), EasyMock.isA(String.class));
        mockController.setDefaultReturnValue(null);
        customFieldManager = mockController.getMock(CustomFieldManager.class);
        handler = new MultiValueParameterClauseXmlHandler(flagOperandRegistry, customFieldManager);
    }

    @After
    public void tearDown() throws Exception
    {
        handler = null;

    }

    @Test
    public void testIsSafeToNamifyValue() throws Exception
    {
        replay();
        assertFalse(handler.isSafeToNamifyValue());
    }

    @Test
    public void testXmlFieldIdSupported() throws Exception
    {
        mockController.replay();
        assertTrue(handler.xmlFieldIdSupported(SystemSearchConstants.forPriority().getIndexField()));
        assertTrue(handler.xmlFieldIdSupported("customfield_12345"));
    }

    @Test
    public void testMultiIssueKeyHackHappyPath() throws Exception
    {
        final Document doc = new Document("<key andQuery='false'><value>HSP-1</value></key>");
        final Element element = doc.getElement("key");

        final CustomField customField = mockController.getMock(CustomField.class);
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);

        EasyMock.expect(customFieldType.getKey())
                .andReturn("com.atlassian.jira.toolkit:multikeyfield");

        EasyMock.expect(customField.getCustomFieldType())
                .andReturn(customFieldType);

        EasyMock.expect(customField.getId())
                .andReturn("customfield_10000");

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(CollectionBuilder.newBuilder(customField).asList());

        mockController.replay();

        final ClauseXmlHandler.ConversionResult conversionResult = handler.convertXmlToClause(element);

        assertEquals(ClauseXmlHandler.ConversionResultType.FULL_CONVERSION, conversionResult.getResultType());
        assertEquals(new TerminalClauseImpl("cf[10000]", "HSP-1") , conversionResult.getClause());

        mockController.verify();
    }

    @Test
    public void testMultiIssueKeyHackNoCustomFields() throws Exception
    {
        final Document doc = new Document("<key andQuery='false'><value>HSP-1</value></key>");
        final Element element = doc.getElement("key");

        EasyMock.expect(customFieldManager.getCustomFieldObjects())
                .andReturn(Collections.<CustomField>emptyList());

        mockController.replay();

        final ClauseXmlHandler.ConversionResult conversionResult = handler.convertXmlToClause(element);

        assertEquals(ClauseXmlHandler.ConversionResultType.BEST_GUESS_CONVERSION, conversionResult.getResultType());
        assertEquals(new TerminalClauseImpl("key", "HSP-1") , conversionResult.getClause());

        mockController.verify();
    }
}
