package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.timezone.TimeZoneManager;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;

/**
 * @since v4.0
 */
public class TestDefaultClauseXmlHandlerRegistry extends MockControllerTestCase
{
    private JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private CustomFieldManager customFieldManager;
    private FieldFlagOperandRegistry fieldFlagOperandRegistry;

    @Before
    public void setUp() throws Exception
    {
        jqlSelectOptionsUtil = mockController.getMock(JqlSelectOptionsUtil.class);
        customFieldManager = mockController.getMock(CustomFieldManager.class);
        fieldFlagOperandRegistry = mockController.getMock(FieldFlagOperandRegistry.class);
    }

    @Test
    public void testParameterPriority() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.PriorityParameter", "priority");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }

    @Test
    public void testParameterResolution() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.ResolutionParameter", "resolution");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }

    @Test
    public void testParameterStatus() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.StatusParameter", "status");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }
    
    @Test
    public void testIssueConstantsParameterPriority() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueConstantsParameter", "priority");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }

    @Test
    public void testIssueConstantsParameterResolution() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueConstantsParameter", "resolution");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }

    @Test
    public void testIssueConstantsParameterStatus() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.IssueConstantsParameter", "status");
        assertNotNull(handler);
        assertTrue(handler instanceof ConstantsClauseXmlHandler);
    }

    @Test
    public void testDateParameterCreatedAbsolute() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter", "created");
        assertNotNull(handler);
        assertTrue(handler instanceof AbsoluteDateXmlHandler);
    }

    @Test
    public void testDateParameterCreatedRelative() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "created");
        assertNotNull(handler);
        assertTrue(handler instanceof RelativeDateXmlHandler);
    }

    @Test
    public void testDateParameterUpdatedAbsolute() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter", "updated");
        assertNotNull(handler);
        assertTrue(handler instanceof AbsoluteDateXmlHandler);
    }

    @Test
    public void testDateParameterUpdatedRelative() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "updated");
        assertNotNull(handler);
        assertTrue(handler instanceof RelativeDateXmlHandler);
    }

    @Test
    public void testDateParameterDueDateAbsolute() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter", "duedate");
        assertNotNull(handler);
        assertTrue(handler instanceof AbsoluteDateXmlHandler);
    }

    @Test
    public void testDateParameterDueDateRelative() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "duedate");
        assertNotNull(handler);
        assertTrue(handler instanceof RelativeDateXmlHandler);
    }

    @Test
    public void testDateParameterResolutionDateAbsolute() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter", "resolutiondate");
        assertNotNull(handler);
        assertTrue(handler instanceof AbsoluteDateXmlHandler);
    }

    @Test
    public void testDateParameterResolutionDateRelative() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", "resolutiondate");
        assertNotNull(handler);
        assertTrue(handler instanceof RelativeDateXmlHandler);
    }

    @Test
    public void testWorkRatioParameter() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.WorkRatioParameter", "workratio");
        assertNotNull(handler);
        assertTrue(handler instanceof WorkRatioClauseXmlHandler);
    }

    @Test
    public void testNoSuchHandler() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        assertNull(handlerRegistry.getClauseXmlHandler("I.Am.a.class.that.will.never.exist", "blah"));
    }

    @Test
    public void testMultiVersionCustomField() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getKey();
        mockController.setReturnValue("akey");

        CustomField field = mockController.getMock(CustomField.class);
        field.getCustomFieldType();
        mockController.setDefaultReturnValue(customFieldType);

        customFieldManager.getCustomFieldObject(100L);
        mockController.setReturnValue(field);

        TimeZoneManager timeZoneManager = mockController.createMock(TimeZoneManager.class);

        mockController.replay();

        DefaultClauseXmlHandlerRegistry handlerRegistry = new DefaultClauseXmlHandlerRegistry(customFieldManager, jqlSelectOptionsUtil, fieldFlagOperandRegistry, timeZoneManager);
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter", "customfield_100");

        assertNotNull(handler);
        assertTrue(handler instanceof MultiValueParameterClauseXmlHandler);
        mockController.verify();
    }

    @Test
    public void testCustomFieldSpecificType() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getKey();
        mockController.setReturnValue("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect");

        CustomField field = mockController.getMock(CustomField.class);
        field.getCustomFieldType();
        mockController.setDefaultReturnValue(customFieldType);

        customFieldManager.getCustomFieldObject(100L);
        mockController.setReturnValue(field);

        TimeZoneManager timeZoneManager = mockController.createMock(TimeZoneManager.class);
        mockController.replay();

        DefaultClauseXmlHandlerRegistry handlerRegistry = new DefaultClauseXmlHandlerRegistry(customFieldManager, jqlSelectOptionsUtil, fieldFlagOperandRegistry, timeZoneManager);
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.StringParameter", "customfield_100");

        assertNotNull(handler);
        assertTrue(handler instanceof CascadeSelectParameterClauseXmlHandler);
        mockController.verify();
    }

    @Test
    public void testCustomFieldBadIdFoundAnyway() throws Exception
    {
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter", "customfield_poo");

        assertTrue(handler instanceof MultiValueParameterClauseXmlHandler);
    }

    @Test
    public void testCustomFieldIdWithGroupSuffix() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getKey();
        mockController.setReturnValue("cascadingselect");

        CustomField field = mockController.getMock(CustomField.class);
        field.getCustomFieldType();
        mockController.setDefaultReturnValue(customFieldType);

        customFieldManager.getCustomFieldObject(12345L);
        mockController.setReturnValue(field);

        TimeZoneManager timeZoneManager = mockController.createMock(TimeZoneManager.class);

        mockController.replay();

        DefaultClauseXmlHandlerRegistry handlerRegistry = new DefaultClauseXmlHandlerRegistry(customFieldManager, jqlSelectOptionsUtil, fieldFlagOperandRegistry, timeZoneManager);
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter", "customfield_12345_group");

        assertTrue(handler instanceof MultiValueParameterClauseXmlHandler);
    }

    @Test
    public void testCustomFieldIdNoLongerExists() throws Exception
    {
        customFieldManager.getCustomFieldObject(99999L);
        mockController.setReturnValue(null);
        DefaultClauseXmlHandlerRegistry handlerRegistry = createRegistry();
        final ClauseXmlHandler handler = handlerRegistry.getClauseXmlHandler("com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter", "customfield_99999");

        assertNotNull(handler);
    }

    private DefaultClauseXmlHandlerRegistry createRegistry() throws Exception
    {
        TimeZoneManager timeZoneManager = mockController.createMock(TimeZoneManager.class);
        mockController.replay();
        return new DefaultClauseXmlHandlerRegistry(customFieldManager, jqlSelectOptionsUtil, fieldFlagOperandRegistry, timeZoneManager);
    }
}
