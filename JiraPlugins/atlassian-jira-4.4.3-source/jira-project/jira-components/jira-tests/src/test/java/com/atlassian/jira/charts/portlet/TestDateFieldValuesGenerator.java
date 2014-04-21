package com.atlassian.jira.charts.portlet;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DateField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

/**
 * @since v4.0
 */
public class TestDateFieldValuesGenerator extends MockControllerTestCase
{
    @Test
    public void testGetValuesNoFields() throws FieldException
    {
        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.getAllAvailableNavigableFields();
        mockController.setReturnValue(Collections.emptySet());

        mockController.replay();
        DateFieldValuesGenerator dateFieldValuesGenerator = new DateFieldValuesGenerator()
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };

        final Map map = dateFieldValuesGenerator.getValues(null);
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    @Test
    public void testGetSystemFields() throws FieldException
    {
        final SearchableField mockCommentSystemField = mockController.getMock(SearchableField.class);
        final MockDateField mockCreatedSystemField = new MockDateField("created", "Created");
        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.getAllAvailableNavigableFields();
        final Set<Field> fields = new LinkedHashSet<Field>();
        fields.add(mockCreatedSystemField);
        fields.add(mockCommentSystemField);
        mockController.setReturnValue(fields);
        mockFieldManager.isCustomField(mockCreatedSystemField);
        mockController.setReturnValue(false);
        mockFieldManager.isCustomField(mockCommentSystemField);
        mockController.setReturnValue(false);

        mockController.replay();
        DateFieldValuesGenerator dateFieldValuesGenerator = new DateFieldValuesGenerator()
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };

        final Map map = dateFieldValuesGenerator.getValues(null);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("created", map.keySet().iterator().next());
        assertEquals("Created", map.values().iterator().next());
    }

    @Test
    public void testGetSystemAndTextCustomFields() throws FieldException
    {
        final SearchableField mockCommentSystemField = mockController.getNiceMock(SearchableField.class);
        final MockDateField mockCreatedSystemField = new MockDateField("created", "Created");

        final CustomFieldType mockTextCustomFieldType = mockController.getMock(CustomFieldType.class);
        final CustomField mockTextCustomField = mockController.getMock(CustomField.class);
        mockTextCustomField.getCustomFieldType();
        mockController.setReturnValue(mockTextCustomFieldType);

        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.getAllAvailableNavigableFields();
        final Set<Field> fields = new LinkedHashSet<Field>();
        fields.add(mockCreatedSystemField);
        fields.add(mockCommentSystemField);
        fields.add(mockTextCustomField);
        mockController.setReturnValue(fields);
        mockFieldManager.isCustomField(mockCreatedSystemField);
        mockController.setReturnValue(false);
        mockFieldManager.isCustomField(mockCommentSystemField);
        mockController.setReturnValue(false);
        mockFieldManager.isCustomField(mockTextCustomField);
        mockController.setReturnValue(true);

        mockController.replay();
        DateFieldValuesGenerator dateFieldValuesGenerator = new DateFieldValuesGenerator()
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };

        final Map map = dateFieldValuesGenerator.getValues(null);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("created", map.keySet().iterator().next());
        assertEquals("Created", map.values().iterator().next());
    }

    @Test
    public void testGetSystemAndDateCustomFields() throws FieldException
    {
        final SearchableField mockCommentSystemField = mockController.getNiceMock(SearchableField.class);
        final MockDateField mockCreatedSystemField = new MockDateField("created", "Created");
        final CustomField mockDateCustomField = mockController.getMock(CustomField.class);
        mockDateCustomField.getCustomFieldType();
        mockController.setReturnValue(new MockDateCustomFieldType());
        mockDateCustomField.getId();
        mockController.setReturnValue("customfield_10023");
        mockDateCustomField.getName();
        mockController.setReturnValue("My Date CF");

        final FieldManager mockFieldManager = mockController.getMock(FieldManager.class);
        mockFieldManager.getAllAvailableNavigableFields();
        final Set<Field> fields = new LinkedHashSet<Field>();
        fields.add(mockCreatedSystemField);
        fields.add(mockCommentSystemField);
        fields.add(mockDateCustomField);
        mockController.setReturnValue(fields);
        mockFieldManager.isCustomField(mockCreatedSystemField);
        mockController.setReturnValue(false);
        mockFieldManager.isCustomField(mockCommentSystemField);
        mockController.setReturnValue(false);
        mockFieldManager.isCustomField(mockDateCustomField);
        mockController.setReturnValue(true);

        mockController.replay();
        DateFieldValuesGenerator dateFieldValuesGenerator = new DateFieldValuesGenerator()
        {
            @Override
            FieldManager getFieldManager()
            {
                return mockFieldManager;
            }
        };

        final Map map = dateFieldValuesGenerator.getValues(null);
        assertNotNull(map);
        assertEquals(2, map.size());
        final Iterator iterator = map.keySet().iterator();
        assertEquals("created", iterator.next());
        final Iterator valuesIterator = map.values().iterator();
        assertEquals("Created", valuesIterator.next());
        assertEquals("customfield_10023", iterator.next());
        assertEquals("My Date CF", valuesIterator.next());
    }

    private class MockDateCustomFieldType implements CustomFieldType, DateField
    {

        public void init(final CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor)
        {
        }

        public String getKey()
        {
            return null;
        }

        public String getName()
        {
            return null;
        }

        public String getDescription()
        {
            return null;
        }

        public CustomFieldTypeModuleDescriptor getDescriptor()
        {
            return null;
        }

        public String getStringFromSingularObject(final Object singularObject)
        {
            return null;
        }

        public Object getSingularObjectFromString(final String string) throws FieldValidationException
        {
            return null;
        }

        public Set remove(final CustomField field)
        {
            return null;
        }

        public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
        {
        }

        public void createValue(final CustomField field, final Issue issue, final Object value)
        {
        }

        public void updateValue(final CustomField field, final Issue issue, final Object value)
        {
        }

        public Object getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
        {
            return null;
        }

        public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
        {
            return null;
        }

        public Object getValueFromIssue(final CustomField field, final Issue issue)
        {
            return null;
        }

        public Object getDefaultValue(final FieldConfig fieldConfig)
        {
            return null;
        }

        public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
        {
        }

        public String getChangelogValue(final CustomField field, final Object value)
        {
            return null;
        }

        public String getChangelogString(final CustomField field, final Object value)
        {
            return null;
        }

        public Map getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
        {
            return null;
        }

        public List getConfigurationItemTypes()
        {
            return null;
        }

        public List getRelatedIndexers(final CustomField customField)
        {
            return null;
        }

        public boolean isRenderable()
        {
            return false;
        }

        public boolean valuesEqual(final Object v1, final Object v2)
        {
            return false;
        }

        public String availableForBulkEdit(final BulkEditBean bulkEditBean)
        {
            return null;
        }
    }

    private class MockDateField implements Field, DateField
    {
        private final String id;
        private final String name;

        private MockDateField(final String id, final String name)
        {
            this.id = id;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getNameKey()
        {
            return null;
        }

        public String getName()
        {
            return name;
        }

        public int compareTo(final Object o)
        {
            return 0;
        }
    }
}
