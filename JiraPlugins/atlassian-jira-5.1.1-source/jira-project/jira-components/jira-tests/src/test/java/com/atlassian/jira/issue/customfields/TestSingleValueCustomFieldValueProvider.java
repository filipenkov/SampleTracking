package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.mock.controller.MockController;

import java.util.Collections;
import java.lang.reflect.Method;

/**
 * @since v4.0
 */
public class TestSingleValueCustomFieldValueProvider extends MockControllerTestCase
{

    @Test
    public void testGetStringValue() throws Exception
    {
        final Method method = SingleValueCustomFieldValueProvider.class.getMethod("getStringValue", CustomField.class, FieldValuesHolder.class);
        _testGetSingleString(method);
        _testGetList(method);
        _testGetEmptyList(method);
        _testGetNull(method);
    }
    
    @Test
    public void testGetValue() throws Exception
    {
        final Method method = SingleValueCustomFieldValueProvider.class.getMethod("getValue", CustomField.class, FieldValuesHolder.class);
        _testGetSingleString(method);
        _testGetList(method);
        _testGetEmptyList(method);
        _testGetNull(method);
    }

    private void _testGetSingleString(Method method) throws Exception
    {

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue("string");

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(null);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();

        assertEquals("string", method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }    

    private void _testGetList(Method method) throws Exception
    {

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(Collections.singletonList("string"));

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(null);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();

        assertEquals("string", method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }

    private void _testGetEmptyList(Method method) throws Exception
    {

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(Collections.emptyList());

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(null);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();

        assertNull(method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }
    
    private void _testGetNull(Method method) throws Exception
    {

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(null);

        final CustomField customField = mockController.getMock(CustomField.class);
        customField.getCustomFieldValues(null);
        mockController.setReturnValue(null);
        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        mockController.replay();

        assertNull(method.invoke(new SingleValueCustomFieldValueProvider(), customField,  null));

        mockController.verify();
        mockController.onTestEnd();
        mockController = new MockController();
    }
}
