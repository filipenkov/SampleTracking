package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.imports.project.customfield.CascadingSelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.LegacyReplayVerifyTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.collections.MultiHashMap;
import org.easymock.MockControl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class TestCascadingSelectCFType extends LegacyReplayVerifyTestCase
{
    private static final Long CFC_ID = new Long(1);

    // ------------------------------------------------------------------------------------------------ Class Properties

    CascadingSelectCFType testObject;

    // Level 1 Dependencies
    private CustomFieldValuePersister mockCustomFieldValuePersister;
    private MockControl ctrlCustomFieldPersister;

    private OptionsManager mockOptionsManager;
    private MockControl ctrlOptionsManager;

    private GenericConfigManager mockGenericConfigManager;
    private MockControl ctrlGenericConfigManager;


    // Level 2 Dependencies
    private CustomField mockCustomField;
    private MockControl ctrlCustomField;

    private FieldConfig mockFieldConfig;
    private MockControl ctrlFieldConfig;

    private CustomFieldParams mockCustomFieldParams;
    private MockControl ctrlCustomFieldParams;

    private Options mockOptions;
    private MockControl ctrlOptions;

    private JiraContextNode mockJiraContextNode;
    private MockControl ctrlProjectIssueType;

    private ErrorCollection mockErrorCollection;
    private MockControl ctrlErrorCollection;

    private Issue mockIssue;

    // ------------------------------------------------------------------------------------------------- Class Constants

    private static final Long ISSUE_ID = new Long(1);
    private static final PersistenceFieldType CASCADE_VALUE_TYPE = CascadingSelectCFType.CASCADE_VALUE_TYPE;

    // ------------------------------------------------------------------------------------------ Initialisation Methods

    public void testGetProjectImporter() throws Exception
    {
        assertTrue(testObject.getProjectImporter() instanceof CascadingSelectCustomFieldImporter);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // SetUp Level 1 Dependencies
        ctrlCustomFieldPersister = MockControl.createControl(CustomFieldValuePersister.class);
        mockCustomFieldValuePersister = (CustomFieldValuePersister) ctrlCustomFieldPersister.getMock();

        ctrlOptionsManager = MockControl.createControl(OptionsManager.class);
        mockOptionsManager = (OptionsManager) ctrlOptionsManager.getMock();

        ctrlGenericConfigManager = MockControl.createControl(GenericConfigManager.class);
        mockGenericConfigManager = (GenericConfigManager) ctrlGenericConfigManager.getMock();

        // SetUp Level 2 Dependencies
        ctrlCustomField = MockControl.createControl(CustomField.class);
        mockCustomField = (CustomField) ctrlCustomField.getMock();

        ctrlFieldConfig = MockControl.createControl(FieldConfig.class);
        mockFieldConfig = (FieldConfig) ctrlFieldConfig.getMock();

        ctrlCustomFieldParams = MockControl.createControl(CustomFieldParams.class);
        mockCustomFieldParams = (CustomFieldParams) ctrlCustomFieldParams.getMock();

        ctrlOptions = MockControl.createControl(Options.class);
        mockOptions = (Options) ctrlOptions.getMock();

        ctrlProjectIssueType = MockControl.createControl(JiraContextNode.class);
        mockJiraContextNode = (JiraContextNode) ctrlProjectIssueType.getMock();

        ctrlErrorCollection = MockControl.createControl(ErrorCollection.class);
        mockErrorCollection = (ErrorCollection) ctrlErrorCollection.getMock();

        mockIssue = new MockIssue()
        {
            public Long getId()
            {
                return ISSUE_ID;
            }
        };

        // Reset states
        _reset();

        // Instantiate
        testObject = new CascadingSelectCFType(mockOptionsManager, mockCustomFieldValuePersister, mockGenericConfigManager);
    }


    // ---------------------------------------------------------------------------------------------- JUnit Test Methods


    public void testRemove() throws Exception
    {
        // Initialise
        mockOptionsManager.removeCustomFieldOptions(mockCustomField);
        mockCustomFieldValuePersister.removeAllValues("10001");
        ctrlCustomFieldPersister.setReturnValue(new HashSet(EasyList.build("1")));
        ctrlCustomField.expectAndReturn(mockCustomField.getId(), "10001");

        _startTestPhase();

        // Execute
        Set o = testObject.remove(mockCustomField);
        assertNotNull(o);

        _verifyAll();
    }

    public void testRemoveValue() throws Exception
    {
        // Initialise
        mockCustomFieldValuePersister.removeValue(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, MockOption.PARENT_OPTION_ID.toString());
        ctrlCustomFieldPersister.setReturnValue(new HashSet());

        _startTestPhase();

        // Execute
        testObject.removeValue(mockCustomField, mockIssue, MockOption._getMockParentOption());

        _verifyAll();

    }

    public void testGetDefaultValue() throws Exception
    {
        // Initialise
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getId(), CFC_ID);
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);
        ctrlGenericConfigManager.expectAndReturn(mockGenericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString()), new CustomFieldParamsImpl(mockCustomField));

        _startTestPhase();

        // Execute
        Object o = testObject.getDefaultValue(mockFieldConfig);

        assertNotNull(o);
        assertTrue(o instanceof CustomFieldParams);

        _verifyAll();
    }

    public void testSetDefaultValueToNone() throws Exception
    {
        // Initialise
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getId(), CFC_ID);
        mockGenericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString(), null);

        _startTestPhase();

        // Execute
        testObject.setDefaultValue(mockFieldConfig, null);

        _verifyAll();
    }

    public void testSetDefaultValue() throws Exception
    {
        // Initialise
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getId(), CFC_ID);
        mockGenericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString(), mockCustomFieldParams);

        mockCustomFieldParams.transformObjectsToStrings();
        mockCustomFieldParams.setCustomField(null);
        _startTestPhase();

        // Execute
        testObject.setDefaultValue(mockFieldConfig, mockCustomFieldParams);

        _verifyAll();
    }

    public void testCreateValue() throws Exception
    {
        // Initialise
        mockCustomFieldParams.getAllValues();
        ctrlCustomFieldParams.setReturnValue(EasyList.build(MockOption._getMockParentOption(), MockOption._getMockChild1Option()));

        mockCustomFieldValuePersister.createValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1000"), null);
        mockCustomFieldValuePersister.createValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1002"), "1000");

        _startTestPhase();

        // Execute
        testObject.createValue(mockCustomField, mockIssue, mockCustomFieldParams);

        _verifyAll();
    }

    public void testGetOptions() throws Exception
    {
        // Initialise
        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        _startTestPhase();

        // Execute
        Options o = testObject.getOptions(mockFieldConfig, mockJiraContextNode);
        assertNotNull(o);

        _verifyAll();
    }

    public void testGetIssueIdsWithValue() throws Exception
    {
        // Initialise
        mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1000");
        ctrlCustomFieldPersister.setReturnValue(new HashSet(EasyList.build("1")));

        mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1002");
        ctrlCustomFieldPersister.setReturnValue(new HashSet(EasyList.build("2")));

        mockCustomFieldValuePersister.getIssueIdsWithValue(mockCustomField, CASCADE_VALUE_TYPE, "1003");
        ctrlCustomFieldPersister.setReturnValue(new HashSet(EasyList.build("3", "4")));

        _startTestPhase();

        // Execute
        Set o = testObject.getIssueIdsWithValue(mockCustomField, MockOption._getMockParentOption());
        assertNotNull(o);
        assertEquals(4, o.size());

        _verifyAll();
    }

    public void testGetStringFromSingularObject() throws Exception
    {
        // Initialise

        _startTestPhase();

        // Execute
        String o = testObject.getStringFromSingularObject(MockOption._getMockParentOption());
        assertNotNull(o);
        assertEquals(MockOption.PARENT_OPTION_ID.toString(), o);

        _verifyAll();
    }

    public void testGetSingularObjectFromString() throws Exception
    {
        // Initialise
        mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID);
        ctrlOptionsManager.setReturnValue(MockOption._getMockParentOption());

        _startTestPhase();

        // Execute
        Object o = testObject.getSingularObjectFromString(MockOption.PARENT_OPTION_ID.toString());

        assertNotNull(o);

        _verifyAll();
    }

    public void testValidateFromParamsNoErrors() throws Exception
    {
        // Initialise
        ctrlCustomField.expectAndReturn(mockCustomField.getId(), "10001");
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParentOption() }));

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParentOption() }));

        mockOptions.getOptionById(new Long(1003));
        ctrlOptions.setReturnValue(MockOption._getMockChild2Option());

        Map m = new MultiHashMap();
        m.put(null, MockOption._getMockParentOption());
        m.put("1", MockOption._getMockChild2Option());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        _startTestPhase();

        // Execute
        testObject.validateFromParams(cfp, mockErrorCollection, mockFieldConfig);

        _verifyAll();
    }

    public void testValidateFromParamsErrorsForContextBecauseBadRoot() throws Exception
    {
        // Initialise
        ctrlCustomField.expectAndReturn(mockCustomField.getId(), "10001");
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockFieldConfig.getName();
        ctrlFieldConfig.setReturnValue("FAKE NAME");

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParent2Option() }));

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParentOption() }));

        mockOptions.getOptionById(new Long(1002));
        ctrlOptions.setReturnValue(null);

        Map m = new MultiHashMap();
        m.put(null, MockOption._getMockParent2Option());
        m.put("1", MockOption._getMockChild1Option());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        _startTestPhase();

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);
        assertTrue(errorCollectionToAddTo.hasAnyErrors());

        _verifyAll();
    }

    public void testValidateFromParamsErrorsForContextBecauseBadChild() throws Exception
    {
        // Initialise
        ctrlCustomField.expectAndReturn(mockCustomField.getId(), "10001");
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockFieldConfig.getName();
        ctrlFieldConfig.setReturnValue("FAKE NAME");

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParent2Option() }));

        Map m = new MultiHashMap();
        m.put(null, MockOption._getMockParentOption());
        m.put("1", MockOption._getMockChild1Option());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        _startTestPhase();

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);
        assertTrue(errorCollectionToAddTo.hasAnyErrors());

        _verifyAll();
    }

    public void testValidateFromParamsErrors() throws Exception
    {
        // Initialise
        ctrlCustomField.expectAndReturn(mockCustomField.getId(), "10001");
        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParentOption() }));

        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions);

        mockOptions.getRootOptions();
        ctrlOptions.setReturnValue(Arrays.asList(new Object[] { MockOption._getMockParentOption() }));

        mockOptions.getOptionById(new Long(1003));
        ctrlOptions.setReturnValue(MockOption._getMockChild2Option());

        Map m = new MultiHashMap();
        m.put(null, MockOption._getMockParentOption());
        final MockOption child = MockOption._getMockChild2Option();
        child.setParentOption(MockOption._getMockChild1Option());
        m.put("1", child);

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        _startTestPhase();

        // Execute
        final SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);

        assertTrue(errorCollectionToAddTo.hasAnyErrors());

        _verifyAll();
    }

    public void testUpdateValue() throws Exception
    {
        // Initialise
        mockCustomFieldValuePersister.updateValues(mockCustomField, mockIssue.getId(), CASCADE_VALUE_TYPE, null);

        mockCustomFieldParams.getAllValues();
        ctrlCustomFieldParams.setReturnValue(EasyList.build(MockOption._getMockParentOption(), MockOption._getMockChild1Option()));

        mockCustomFieldValuePersister.updateValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1000"), null);
        mockCustomFieldValuePersister.updateValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, EasyList.build("1002"), "1000");

        _startTestPhase();

        // Execute
        testObject.updateValue(mockCustomField, mockIssue, mockCustomFieldParams);

        _verifyAll();
    }

    public void testGetValueFromCustomFieldParams() throws Exception
    {
        // Initialise
        Map m = new MultiHashMap();
        m.put(null, MockOption.PARENT_OPTION_ID.toString());
        m.put("1", MockOption.CHILD_1_ID.toString());
        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, m);

        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID), MockOption._getMockParentOption());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(MockOption.CHILD_1_ID), MockOption._getMockChild1Option());

        _startTestPhase();

        // Execute
        Object o = testObject.getValueFromCustomFieldParams(cfp);
        assertNotNull(o);
        assertTrue(o instanceof CustomFieldParams);
        CustomFieldParams customFieldParams = (CustomFieldParams) o;
        assertEquals(2, customFieldParams.getAllValues().size());

        _verifyAll();
    }

    public void testGetValueFromIssue() throws Exception
    {
        // Initialise
        mockCustomFieldValuePersister.getValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, null);
        ctrlCustomFieldPersister.setReturnValue(EasyList.build(MockOption.PARENT_OPTION_ID.toString()));

        mockCustomFieldValuePersister.getValues(mockCustomField, ISSUE_ID, CASCADE_VALUE_TYPE, MockOption.PARENT_OPTION_ID.toString());
        ctrlCustomFieldPersister.setReturnValue(null);

        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(MockOption.PARENT_OPTION_ID), MockOption._getMockParentOption());

        _startTestPhase();

        // Execute
        Object o = testObject.getValueFromIssue(mockCustomField, mockIssue);
        assertNotNull(o);
        CustomFieldParams customFieldParams = (CustomFieldParams) o;
        assertEquals(1, customFieldParams.getAllValues().size());

        _verifyAll();
    }

    public void testGetChangelogValue() throws Exception
    {
        // Initialise
        Map m = new MultiHashMap();
        m.put(null, MockOption._getMockParentOption());
        final MockOption child = MockOption._getMockChild2Option();
        child.setParentOption(MockOption._getMockChild1Option());
        m.put("1", child);

        _startTestPhase();

        // Execute
        String o = testObject.getChangelogValue(mockCustomField, new CustomFieldParamsImpl(mockCustomField, m));
        assertNotNull(o);

        _verifyAll();
    }

    public MockControl[] _getRegisteredMockControllers()
    {
        return new MockControl[] { ctrlCustomFieldPersister, ctrlOptionsManager, ctrlCustomField,
                ctrlCustomFieldParams, ctrlOptions, ctrlProjectIssueType, ctrlErrorCollection,
                ctrlGenericConfigManager, ctrlFieldConfig };
    }
}