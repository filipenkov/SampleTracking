package com.atlassian.jira.action.issue.customfields.option;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.manager.DefaultOptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestOfBizOptionsManager extends MockControllerTestCase
{
    // ------------------------------------------------------------------------------------------------ Class Properties

    DefaultOptionsManager testObject;

    // Level 1 Dependencies
    private MockOfBizDelegator mockOfBizDelegator;

    private CustomFieldManager mockCustomFieldManager;

    // Delgator objects
    private Map options;
    private MockGenericValue parent;
    private MockGenericValue child1;
    private MockGenericValue child2;


    // Level 2 Dependencies
    private FieldConfig mockFieldConfig;

    private Options mockOptions;

    private Option mockOption;
    public static final Long LONG_ID = new Long(10001);
    private static final String CF_ID = "customfield_10001";

    // ------------------------------------------------------------------------------------------ Initialisation Methods
    public TestOfBizOptionsManager()
    {
        // SetUp Level 1 Dependencies
        mockCustomFieldManager = getMock(CustomFieldManager.class);


        // SetUp Level 2 Dependencies
        mockFieldConfig = getMock(FieldConfig.class);

        mockOptions = getMock(Options.class);

        mockOption = getMock(Option.class);

        _setUpDelgator();
    }

    @Before
    public void setUp() throws Exception
    {
        reset();

        mockOfBizDelegator = new MockOfBizDelegator(new ArrayList(options.values()), EasyList.build(parent));

        // Instantiate
        testObject = new DefaultOptionsManager(mockOfBizDelegator, null, null);

    }




    // ---------------------------------------------------------------------------------------------- JUnit Test Methods


    @Test
    public void testgetOptions() throws Exception
    {
        // Initialise
        expect(mockFieldConfig.getId()).andReturn(LONG_ID);

        replay();

        // Execute
        Options options = testObject.getOptions(mockFieldConfig);
        assertNotNull(options);
        assertEquals(options.size(), 1);
    }

    @Test
    public void testsetOptions() throws Exception
    {
        // Initialise
        expect(mockOptions.iterator()).andReturn(EasyList.build(new MockOption(parent)).iterator());

        CustomField mockCustomField;
        mockCustomField = getMock(CustomField.class);

        expect(mockCustomField.getId()).andStubReturn(CF_ID);
        expect(mockCustomField.getIdAsLong()).andStubReturn(LONG_ID);


        expect(mockFieldConfig.getCustomField()).andReturn(mockCustomField);
        expect(mockFieldConfig.getId()).andStubReturn(LONG_ID);

        replay();

        // Execute
        testObject.setRootOptions(mockFieldConfig, mockOptions);
        mockOfBizDelegator.verifyAll();
    }

    @Test
    public void testupdateOptions() throws Exception
    {
        // Initialise
        mockOption.store();
        expectLastCall().times(3);

        replay();

        // Execute
        testObject.updateOptions(EasyList.build(mockOption, mockOption, mockOption));
    }

    @Test
    public void testcreateOption() throws Exception
    {
        // Initialise
        CustomField mockCustomField;
        mockCustomField = getMock(CustomField.class);

        expect(mockCustomField.getIdAsLong()).andStubReturn(LONG_ID);

        expect(mockFieldConfig.getId()).andStubReturn(LONG_ID);
        expect(mockFieldConfig.getCustomField()).andStubReturn(mockCustomField);

        replay();

        // Execute
        Option newOption = testObject.createOption(mockFieldConfig, null, new Long(0), "Falcon");
        assertNotNull(newOption);
        assertEquals(newOption.getValue(), "Falcon");
        assertEquals(newOption.getOptionId(), MockOption.PARENT_OPTION_ID);
    }

    @Test
    public void testdeleteOptionAndChildren() throws Exception
    {
        // Initialise
        expect(mockOption.retrieveAllChildren(null)).andReturn(EasyList.build(new MockOption(child1), new MockOption(child2)));
        expect(mockOption.getGenericValue()).andReturn(parent);

        replay();

        // Execute
        assertEquals(3, mockOfBizDelegator.findAll("CustomFieldOption").size());
        testObject.deleteOptionAndChildren(mockOption);
        assertEquals(0, mockOfBizDelegator.findAll("CustomFieldOption").size());
    }


    @Test
    public void testChildrenOnly() throws Exception
    {
        // Initialise
        expect(mockOption.retrieveAllChildren(null)).andReturn(null);
        expect(mockOption.getGenericValue()).andReturn(child1);

        replay();

        // Execute
        assertEquals(3, mockOfBizDelegator.findAll("CustomFieldOption").size());
        testObject.deleteOptionAndChildren(mockOption);
        assertEquals(2, mockOfBizDelegator.findAll("CustomFieldOption").size());
    }

    @Test
    public void testfindByOptionId() throws Exception
    {
        replay();
        // Execute
        Option o = testObject.findByOptionId(MockOption.PARENT_OPTION_ID);
        assertNotNull(o);
        assertEquals(MockOption.PARENT_OPTION_ID, o.getOptionId());
    }


    @Test
    public void testfindByParentId() throws Exception
    {
        replay();
        // Execute
        List l = testObject.findByParentId(MockOption.PARENT_OPTION_ID);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.get(0) instanceof Option);

        l = testObject.findByParentId(new Long(1002));
        assertNotNull(l);
        assertEquals(0, l.size());
    }


    public void _setUpDelgator()
    {
        parent = MockOption._newMockParentOptionGV();
        child1 = MockOption._newMockChild1GV();
        child2 = MockOption._newMockChild2GV();
        options = new HashMap();
        options.put(new Long(1), parent);
        options.put(new Long(2), child1);
        options.put(new Long(3), child2);
    }

}

