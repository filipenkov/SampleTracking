package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.action.issue.customfields.persistence.MockCustomFieldValuePersister;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.LegacyReplayVerifyTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TestMultiSelectCFType extends LegacyReplayVerifyTestCase
{
    private static final Long CFC_ID = new Long(1);


    // ------------------------------------------------------------------------------------------------ Class Properties

    private MultiSelectCFType testObject;

    // Level 1 Dependencies

    private MockCustomFieldValuePersister mockCustomFieldPersister;

    private OptionsManager mockOptionsManager;
    private MockControl ctrlOptionsManager;

    private GenericConfigManager mockGenericConfigManager;
    private MockControl ctrlGenericConfigManager;

    // Level 2 Dependencies
    private CustomField mockCustomField;
    private MockControl ctrlCustomField;

    private FieldConfig mockFieldConfig;
    private MockControl ctrlFieldConfig;

    private MockControl ctrlOption;

    private CustomFieldParams mockCustomFieldParams;
    private MockControl ctrlCustomFieldParams;

    private Options mockOptions;
    private MockControl ctrlOptions;

    private MockControl ctrlProjectIssueType;

    private MockControl ctrlErrorCollection;

    private Issue mockIssue;

    // ------------------------------------------------------------------------------------------------- Class Constants

    private static final Long ISSUE_ID = new Long(1);

    // ------------------------------------------------------------------------------------------ Initialisation Methods

    protected void setUp() throws Exception
    {
        super.setUp();

        // SetUp Level 1 Dependencies
        ctrlOptionsManager = MockClassControl.createControl(OptionsManager.class);
        mockOptionsManager = (OptionsManager) ctrlOptionsManager.getMock();

        ctrlGenericConfigManager = MockControl.createControl(GenericConfigManager.class);
        mockGenericConfigManager = (GenericConfigManager) ctrlGenericConfigManager.getMock();

        // SetUp Level 2 Dependencies
        ctrlCustomField = MockClassControl.createControl(CustomField.class);
        mockCustomField = (CustomField) ctrlCustomField.getMock();

        ctrlFieldConfig = MockControl.createControl(FieldConfig.class);
        mockFieldConfig = (FieldConfig) ctrlFieldConfig.getMock();

        ctrlOption = MockClassControl.createControl(Option.class);

        ctrlCustomFieldParams = MockClassControl.createControl(CustomFieldParams.class);
        mockCustomFieldParams = (CustomFieldParams) ctrlCustomFieldParams.getMock();

        ctrlOptions = MockClassControl.createControl(Options.class);
        mockOptions = (Options) ctrlOptions.getMock();

        ctrlProjectIssueType = MockClassControl.createControl(JiraContextNode.class);

        ctrlErrorCollection = MockClassControl.createControl(ErrorCollection.class);

        mockIssue = new MockIssue()
        {
            public Long getId()
            {
                return ISSUE_ID;
            }
        };

        // Reset states
        _reset();
        mockCustomFieldPersister = _setUpDelgator();

        // Instantiate
        testObject = new MultiSelectCFType(mockOptionsManager, mockCustomFieldPersister, mockGenericConfigManager);

        ctrlCustomField.reset();
        mockCustomField.getId();
        ctrlCustomField.setDefaultReturnValue("customfield_10001");
        ctrlCustomField.replay();
    }


    // ---------------------------------------------------------------------------------------------- JUnit Test Methods


    public void testProjectImportableUsesSelectImporter() throws Exception
    {
        assertTrue(testObject.getProjectImporter() instanceof SelectCustomFieldImporter);
    }

    public void testRemove() throws Exception
    {
        // Initialise
        mockOptionsManager.removeCustomFieldOptions(mockCustomField);

        _startTestPhase();

        // Execute
        Set o = testObject.remove(mockCustomField);
        assertNotNull(o);
        assertEquals(3, o.size());

        _verifyAll();
    }

    public void testRemoveValue() throws Exception
    {
        // Initialise

        _startTestPhase();

        // Execute
        int n = mockCustomFieldPersister.findAll().size();
        testObject.removeValue(mockCustomField, mockIssue, MockOption._getMockParentOption());
        assertEquals(n - 1, mockCustomFieldPersister.findAll().size());

        _verifyAll();
    }


    public void testDefaultValues() throws Exception
    {
        // Initialise
        List object = EasyList.build(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        List ids = EasyList.build(Long.valueOf(1000), Long.valueOf(1001));

        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getId(), CFC_ID, 2);
        mockGenericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString(), ids);
        ctrlGenericConfigManager.expectAndReturn(mockGenericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, CFC_ID.toString()), ids);
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1000")), MockOption._getMockParentOption());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1001")), MockOption._getMockParent2Option());

        _startTestPhase();

        // Execute
        testObject.setDefaultValue(mockFieldConfig, object);
        Object o = testObject.getDefaultValue(mockFieldConfig);
        assertNotNull(o);
        assertEquals(2, ((Collection) o).size());

        _verifyAll();
    }

    public void testCreateUpdateValue() throws Exception
    {
        // Initialise
        List object = EasyList.build(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        List o2 = EasyList.build(MockOption._getMockChild1Option(), MockOption._getMockChild2Option());

        mockCustomFieldPersister.getDelegator().removeAll(mockCustomFieldPersister.findAll());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1000")), MockOption._getMockParentOption());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1001")), MockOption._getMockParent2Option());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1002")), MockOption._getMockChild1Option());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1003")), MockOption._getMockChild2Option());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1002")), MockOption._getMockChild1Option());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1003")), MockOption._getMockChild2Option());

        _startTestPhase();

        // Execute
        testObject.createValue(mockCustomField, mockIssue, object);
        List expected = EasyList.build(MockOption._getMockParentOption(), MockOption._getMockParent2Option());
        assertEquals(expected, testObject.getValueFromIssue(mockCustomField, mockIssue));

        testObject.updateValue(mockCustomField, mockIssue, o2);
        List expected2 = EasyList.build(MockOption._getMockChild1Option(), MockOption._getMockChild2Option());
        assertNotSame(expected, testObject.getValueFromIssue(mockCustomField, mockIssue));
        assertEquals(expected2, testObject.getValueFromIssue(mockCustomField, mockIssue));

        _verifyAll();
    }


    public void testGetIssueIdsWithValue() throws Exception
    {
        // Initialise

        _startTestPhase();

        // Execute
        Set o = testObject.getIssueIdsWithValue(mockCustomField, MockOption._getMockParentOption());
        assertNotNull(o);
        assertEquals(1, o.size());

        _verifyAll();
    }

    public void testGetStringFromSingularObject() throws Exception
    {
        // Initialise
        _startTestPhase();

        // Execute
        String o = testObject.getStringFromSingularObject(MockOption._getMockParentOption());
        assertNotNull(o);
        assertEquals("1000", o);
        _verifyAll();
    }

    public void testGetSingularObjectFromString() throws Exception
    {
        // Initialise
        String string = "1000";
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1000")), MockOption._getMockParentOption());
        _startTestPhase();

        // Execute
        Object o = testObject.getSingularObjectFromString(string);
        assertNotNull(o);
        assertEquals(MockOption._getMockParentOption(), o);
        _verifyAll();
    }

    public void testExtractTransferObjectFromString()
    {
        // null String
        assertEquals(null, MultiSelectCFType.extractTransferObjectFromString(null));
        // Empty String
        Collection expected = new ArrayList();
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString(""));
        // Single value
        expected.add("blue");
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue"));
        // two values
        expected.add("red");
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,red"));
        // empty values
        // still expect two values as others are ignored.
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,,red,,"));
        // trim whitespace
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("   , ,\tblue, \n,   red  \r \n  \t   ,\t,"));

        // Use an escape character to allow a comma in a value:
        expected.add("red,white, and blue");
        // Note that we had to use java escaping for the back slash.
        // the real text would look like  "blue,red,red\,white\, and blue"
        assertEquals(expected, MultiSelectCFType.extractTransferObjectFromString("blue,red,red\\,white\\, and blue"));
    }

    public void testGetStringFromTransferObject()
    {
        // null String
        assertEquals(null, MultiSelectCFType.getStringFromTransferObject(null));
        // Empty List
        Collection values = new ArrayList();
        assertEquals("", MultiSelectCFType.getStringFromTransferObject(values));
        // Single value
        values.add("blue");
        assertEquals("blue", MultiSelectCFType.getStringFromTransferObject(values));
        // two values
        values.add("red");
        assertEquals("blue,red", MultiSelectCFType.getStringFromTransferObject(values));

        // Use an escape character to allow a comma in a value:
        values.add("red,white, and blue");
        // Note that we had to use java escaping for the back slash.
        // the real text would look like  "blue,red,red\,white\, and blue"
        assertEquals("blue,red,red\\,white\\, and blue", MultiSelectCFType.getStringFromTransferObject(values));
    }

    public void testValidateFromParams() throws Exception
    {
        // Initialise
        mockOptionsManager.getOptions(mockFieldConfig);
        ctrlOptionsManager.setReturnValue(mockOptions, MockControl.ONE_OR_MORE);

        ctrlOptions.expectAndReturn(mockOptions.getOptionById(Long.valueOf("1000")), MockOption._getMockParentOption());
        ctrlOptions.expectAndReturn(mockOptions.getOptionById(Long.valueOf("2")), MockOption._getMockParent2Option());

        CustomFieldParams cfp = new CustomFieldParamsImpl(mockCustomField, EasyList.build("1000", "2"));

        ctrlFieldConfig.expectAndReturn(mockFieldConfig.getCustomField(), mockCustomField);
        _startTestPhase();

        // Execute
        SimpleErrorCollection errorCollectionToAddTo = new SimpleErrorCollection();
        testObject.validateFromParams(cfp, errorCollectionToAddTo, mockFieldConfig);
        assertTrue(!errorCollectionToAddTo.hasAnyErrors());

        _verifyAll();
    }


    public void testGetValueFromCustomFieldParams() throws Exception
    {
        // Initialise
        List list = EasyList.build("1002", "2");
        ctrlCustomFieldParams.expectAndReturn(mockCustomFieldParams.getAllValues(), list);

        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("1002")), MockOption._getMockParentOption());
        ctrlOptionsManager.expectAndReturn(mockOptionsManager.findByOptionId(Long.valueOf("2")), MockOption._getMockParent2Option());

        _startTestPhase();

        // Execute
        Object o = testObject.getValueFromCustomFieldParams(mockCustomFieldParams);
        assertNotNull(o);
        assertEquals(EasyList.build(MockOption._getMockParentOption(), MockOption._getMockParent2Option()), o);

        _verifyAll();
    }


    public void testGetChangelogValue() throws Exception
    {
        // Initialise
        Object object = EasyList.build(MockOption._getMockParentOption(), MockOption._getMockParent2Option());

        _startTestPhase();

        // Execute
        String o = testObject.getChangelogValue(mockCustomField, object);
        assertNotNull(o);

        _verifyAll();
    }


    // ------------------------------------------------------------------------------------------ Replay & Reset Methods


    public MockControl[] _getRegisteredMockControllers()
    {
        return new MockControl[] {
                ctrlOptionsManager,
                ctrlOption,
                ctrlCustomFieldParams,
                ctrlOptions,
                ctrlProjectIssueType,
                ctrlErrorCollection,
                ctrlGenericConfigManager,
                ctrlFieldConfig
        };
    }

    private MockCustomFieldValuePersister _setUpDelgator()
    {
        return new MockCustomFieldValuePersister(MockCustomFieldValuePersister._getList(), MockCustomFieldValuePersister._getList());
    }

    public void testValuesEqualWithUnEqual1() throws Exception
    {
        final List first = EasyList.build("A", "B");
        final List second = EasyList.build("B", "C");

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);
        assertEquals(false, type.valuesEqual(first, second));
    }

    public void testValuesEqualWithEqualLists1() throws Exception
    {
        final List first = EasyList.build("A", "B");
        final List second = EasyList.build("B", "A");

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);
        assertEquals(true, type.valuesEqual(first, second));
    }

    public void testValuesEqualWithEqualListsWithRepeatedElementValues() throws Exception
    {
        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work this way.
        final List first = EasyList.build("A", "B", "A");
        final List second = EasyList.build("B", "A", "A");

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);
        assertEquals(true, type.valuesEqual(first, second));
    }

    public void testValuesEqualWithUnEqualElementCardinality() throws Exception
    {
        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work this way.
        final List first = EasyList.build("A", "B", "B");
        final List second = EasyList.build("A", "B");

        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);//
        assertEquals(false, type.valuesEqual(first, second));
    }

    public void testValuesEqualWithDifferentStringValuesWhichShouldNeverHappen() throws Exception
    {
        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work this way.
        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);//
        assertEquals(false, type.valuesEqual("first", "second"));
    }

    public void testValuesEqualWithTheSameStringValuesWhichShouldNeverHappen() throws Exception
    {
        // This would actually be invalid data and should never happen if jira is playing nice, but Anton has asked that the fields work this way.
        final MultiSelectCFType type = new MultiSelectCFType(null, null, null);//
        assertEquals(true, type.valuesEqual("some string", "some string"));
    }
}

