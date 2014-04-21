/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import org.easymock.EasyMock;
import webwork.action.Action;

public class TestCreateCustomField extends AbstractWebworkTestCase
{
    CreateCustomField customField;
    private ReindexMessageManager reindexMessageManager;
    private CustomFieldContextConfigHelper customFieldContextConfigHelper;

    public TestCreateCustomField(String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        final CustomFieldValidatorImpl customFieldValidator = new CustomFieldValidatorImpl(new MockCustomFieldManager());
        reindexMessageManager = EasyMock.createMock(ReindexMessageManager.class);
        customFieldContextConfigHelper = EasyMock.createMock(CustomFieldContextConfigHelper.class);
        EasyMock.replay(reindexMessageManager, customFieldContextConfigHelper);
        customField = new CreateCustomField(null, customFieldValidator,  null, new MockCustomFieldManager(), null, null, reindexMessageManager, customFieldContextConfigHelper);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        EasyMock.verify(reindexMessageManager, customFieldContextConfigHelper);
    }

    public void testDoChooseType1() throws Exception
    {
        customField.setFieldType("");

        String returnValue = customField.doCustomFieldType();
        assertEquals(Action.INPUT, returnValue);
        assertEquals(1, customField.getErrorMessages().size());
        assertEquals("Invalid field type specified.", customField.getErrorMessages().iterator().next());
    }
}

