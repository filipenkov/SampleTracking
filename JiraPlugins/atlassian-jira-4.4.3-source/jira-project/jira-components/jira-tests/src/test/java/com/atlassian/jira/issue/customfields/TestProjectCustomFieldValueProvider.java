package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v4.0
 */
public class TestProjectCustomFieldValueProvider extends MockControllerTestCase
{
    private ProjectConverter projectConverter;
    private CustomField customField;

    @Before
    public void setUp() throws Exception
    {
        projectConverter = mockController.getMock(ProjectConverter.class);
        customField = mockController.getMock(CustomField.class);
    }

    @Test
    public void testGetStringValueNoProject() throws Exception
    {
        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter)
        {
            @Override
            public Object getValue(final CustomField customField, final FieldValuesHolder fieldValuesHolder)
            {
                return null;
            }
        };

        FieldValuesHolder holder = new FieldValuesHolderImpl();

        assertNull(valueProvider.getStringValue(customField, holder));

        mockController.verify();
    }

    @Test
    public void testGetStringValue() throws Exception
    {
        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter)
        {
            @Override
            public Object getValue(final CustomField customField, final FieldValuesHolder fieldValuesHolder)
            {
                return new MockGenericValue("project", MapBuilder.newBuilder().add("id", 12345L).toHashMap());
            }
        };
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        assertEquals("12345", valueProvider.getStringValue(customField, holder));
    }

    @Test
    public void testGetValueSingleStringIdExists() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        GenericValue project = new MockGenericValue("project");

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue("id");

        projectConverter.getProject("id");
        mockController.setReturnValue(project);

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertEquals(project, valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueListIdExists() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        GenericValue project = new MockGenericValue("project");

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder("id", "id1").asList());

        projectConverter.getProject("id");
        mockController.setReturnValue(project);

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertEquals(project, valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueListIdNotString() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder(10L, "id1").asList());

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertNull(valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueEmptyList() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertNull(valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueSingleIdNotString() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue(10L);

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertNull(valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueSingleStringIdDoesNotExistsException() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue("id");

        projectConverter.getProject("id");
        mockController.setThrowable(new FieldValidationException("blarg!"));

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertNull(valueProvider.getValue(customField, holder));
        mockController.verify();
    }

    @Test
    public void testGetValueSingleStringIdDoesNotExistsNull() throws Exception
    {
        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        FieldValuesHolder holder = new FieldValuesHolderImpl();

        customField.getCustomFieldValues(holder);
        mockController.setReturnValue(null);

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);

        customFieldType.getStringValueFromCustomFieldParams(null);
        mockController.setReturnValue("id");

        projectConverter.getProject("id");
        mockController.setReturnValue(null);

        mockController.replay();

        final ProjectCustomFieldValueProvider valueProvider = new ProjectCustomFieldValueProvider(projectConverter);

        assertNull(valueProvider.getValue(customField, holder));
        mockController.verify();
    }
    
}
