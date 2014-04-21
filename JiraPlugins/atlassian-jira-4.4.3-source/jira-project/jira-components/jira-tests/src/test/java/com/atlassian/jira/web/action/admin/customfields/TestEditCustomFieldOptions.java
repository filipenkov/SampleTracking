/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.mock.LegacyReplayVerifyTestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;

import java.util.List;

public class TestEditCustomFieldOptions extends LegacyReplayVerifyTestCase
{
    Mock customFieldManagerMock = new Mock(CustomFieldManager.class);
    EditCustomFieldOptions editCustomFieldOptions;

    private IssueManager mockIssueManager;
    private MockControl ctrlIssueManager;

    private FieldConfigManager mockFieldConfigManager;
    private MockControl ctrlFieldConfigManager;


    protected void setUp() throws Exception
    {
        super.setUp();

        ctrlIssueManager = MockControl.createControl(IssueManager.class);
        mockIssueManager = (IssueManager) ctrlIssueManager.getMock();

        ctrlFieldConfigManager = MockControl.createControl(FieldConfigManager.class);
        mockFieldConfigManager = (FieldConfigManager) ctrlFieldConfigManager.getMock();

        editCustomFieldOptions = new EditCustomFieldOptions(mockIssueManager, null);
        UtilsForTests.cleanWebWork();
    }

    public void testAdd() throws Exception
    {
        // Mock Proxy object unable to handle at this stage...
//        setOriginalOptions(EasyList.build("option1", "option2"));
//        setExpectedOptions(EasyList.build("option1", "option2", "option3"));
//
//        editCustomFieldOptions.setAddValue("option3");
//        editCustomFieldOptions.doAdd();
    }

    public void testRemoveWithoutConfirmation() throws Exception
    {
        setOriginalOptions(EasyList.build("option1", "option2", "option3"));
        setExpectedOptions(EasyList.build("option1", "option3"));

        editCustomFieldOptions.setSelectedValue("option2");
        assertEquals("confirmdelete", editCustomFieldOptions.doRemove());
    }

    public void testRemove() throws Exception
    {
        setOriginalOptions(EasyList.build("option1", "option2", "option3"));
        setExpectedOptions(EasyList.build("option1", "option3"));

        editCustomFieldOptions.setSelectedValue("option2");
        editCustomFieldOptions.doRemove();
    }

    public void testMoveUp() throws Exception
    {
        // Mock Proxy object unable to handle at this stage...

//        setOriginalOptions(EasyList.build("option1", "option2", "option3"));
//        setExpectedOptions(EasyList.build("option1", "option3", "option2"));
//
//        editCustomFieldOptions.setSelectedValue("option3");
//        editCustomFieldOptions.doMoveUp();
    }

    public void testMoveDown() throws Exception
    {
        // Mock Proxy object unable to handle at this stage...

//        setOriginalOptions(EasyList.build("option1", "option2", "option3"));
//        setExpectedOptions(EasyList.build("option1", "option3", "option2"));
//
//        editCustomFieldOptions.setSelectedValue("option2");
//        editCustomFieldOptions.doMoveDown();
    }

    private void setExpectedOptions(final List expectedOptions)
    {
        editCustomFieldOptions = new EditCustomFieldOptions(mockIssueManager, null)
        {
            protected void saveOptions(List options)
            {
                assertEquals(expectedOptions, options);
            }
        };
    }

    private void setOriginalOptions(List originalOptions)
    {
        Mock customFieldMock = new Mock(CustomField.class);
        customFieldMock.expectAndReturn("getOptions", P.ANY_ARGS, originalOptions);
        customFieldManagerMock.expectAndReturn("getCustomFieldObject", P.ANY_ARGS, customFieldMock.proxy());
    }

    public MockControl[] _getRegisteredMockControllers()
    {
        return new MockControl[] {ctrlFieldConfigManager, ctrlIssueManager};
    }
}
