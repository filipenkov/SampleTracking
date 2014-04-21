package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldDescription;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.CustomFieldTestImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.stub;

@RunWith (ListeningMockitoRunner.class)
public class TestCustomFieldManager
{

    private DefaultCustomFieldManager customFieldManager;

    // Level 1 Dependencies
    @Mock
    private PluginAccessor mockPluginAccessor;
    @Mock
    private OfBizDelegator mockDelegator;
    @Mock
    private FieldScreenManager mockScreenManager;
    @Mock
    private EventPublisher mockEventPublisher;
    @Mock
    private FieldConfigSchemeManager mockConfigSchemeManager;
    @Mock
    private NotificationSchemeManager mockNotificationSchemeManager;
    @Mock
    private FieldManager mockFieldManager;
    @Mock
    private CustomFieldValuePersister mockFieldValuePersister;

    // Level 2 Dependencies
    @Mock
    private CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
    @Mock
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;
    @Mock
    private List<Plugin> plugins;
    @Mock
    private GenericValue mockGenericValue;
    @Mock
    private CustomFieldType customFieldType;
    @Mock
    private CustomFieldDescription customFieldDescription;
    @Mock
    private I18nHelper.BeanFactory i18nFactory;


    @Before
    public void setUp() throws Exception
    {
        stub(mockPluginAccessor.getPlugins()).toReturn(plugins);
        stub(plugins.isEmpty()).toReturn(false);
        customFieldManager = new DefaultCustomFieldManager(mockPluginAccessor, mockDelegator, mockConfigSchemeManager, null, null, null, null, null, mockScreenManager, null, mockFieldValuePersister, mockNotificationSchemeManager, mockFieldManager, null, mockEventPublisher, customFieldDescription, i18nFactory);
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(EventPublisher.class, mock(EventPublisher.class)));
    }

    @Test
    public void testGetCustomFieldTypesInvalidKey() throws GenericEntityException
    {

        mockPluginAccessor.getEnabledPluginModule("key");

        assertNull(customFieldManager.getCustomFieldType("key"));
        //customFieldManager.createCustomField("newFieldName", "description", cfType, cfSearch, new Long(0), "Bug");
    }

    @Test
    public void testGetCustomFieldTypes() throws GenericEntityException
    {


        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("key");

        stub(customFieldTypeModuleDescriptor.getModule()).toReturn(customFieldType);

        assertNotNull(customFieldManager.getCustomFieldType("key"));
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithNullId() throws Exception
    {
        try
        {
            customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(null);
            fail("Expected exception to be thrown");
        }
        catch (IllegalArgumentException e)
        {
            // yay
        }
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithBadId() throws Exception
    {
        stub(mockDelegator.findById("CustomField", 999L)).toReturn(null);

        try
        {
            customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(999L);
            fail("Expected exception to be thrown");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Tried to remove custom field with id '999' that doesn't exist!", e.getMessage());
        }
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithExistingCustomField() throws Exception
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;
        final String fieldName = "name";
        CustomField mockCustomField = mock(CustomField.class);
        List<GenericValue> genericValues = Collections.singletonList(mockGenericValue);

        stub(mockGenericValue.getLong("id")).toReturn(999L);
        stub(mockGenericValue.getString("name")).toReturn(fieldName);
        stub(mockGenericValue.getString("customfieldtypekey")).toReturn("CustomFieldType");

        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("CustomFieldType");

        stub(customFieldTypeModuleDescriptor.getModule()).toReturn(customFieldType);

        stub(mockDelegator.findAll("CustomField")).toReturn(genericValues);

        stub(mockDelegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);
        stub(mockDelegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);

        stub(mockCustomField.getId()).toReturn(fieldSId);
        stub(mockCustomField.getName()).toReturn(fieldName);
        stub(mockCustomField.getIdAsLong()).toReturn(fieldId);
        stub(mockCustomField.remove()).toReturn(new HashSet<Long>(Lists.newArrayList(10001L)));

        DefaultCustomFieldManager customFieldManagerSpy = spy(customFieldManager);
        doReturn(mockCustomField).when(customFieldManagerSpy).getCustomFieldObject(fieldId);


        customFieldManagerSpy.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);
        assert(customFieldManager.getCustomFieldObjects().isEmpty());

    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithExistingCustomFieldFromdb() throws Exception
    {
        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;

        stub(mockDelegator.findById("CustomField", 999L)).toReturn(mockGenericValue);

        stub(mockDelegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);
        stub(mockDelegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId))).toReturn(1);

        final CustomFieldValuePersister customFieldValuePersister = mock(CustomFieldValuePersister.class);
        stub(customFieldValuePersister.removeAllValues(fieldSId)).toReturn(new HashSet<Long>(Lists.newArrayList(10001L)));

        customFieldManager.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);
        assert(customFieldManager.getCustomFieldObjects().isEmpty());
    }

    @Test
    public void testUpdateCustomFieldObject() throws Exception
    {
        final String oldName = "oldName";
        final String newName = "newName";
        doReturn(customFieldTypeModuleDescriptor).when(mockPluginAccessor).getEnabledPluginModule("key");
        stub(customFieldTypeModuleDescriptor.getModule()).toReturn(customFieldType);
        stub(customFieldType.getKey()).toReturn("key");
        stub(customFieldManager.getCustomFieldType("key")).toReturn(customFieldType);

        Map<String, Object> createFields = createCustomFieldMap(oldName, "key", "description");
        stub(mockDelegator.createValue(eq("CustomField"), anyMap())).toReturn(createGv("CustomField", createFields));
        CustomFieldTestImpl cf = new CustomFieldTestImpl(customFieldManager.createCustomField(oldName, "description", customFieldType, null, null, null));
        final Long fieldId = cf.getIdAsLong();
        CustomFieldTestImpl mockUpdatedField =  mock(CustomFieldTestImpl.class);
        stub(mockUpdatedField.getIdAsLong()).toReturn(fieldId);
        stub(mockUpdatedField.getName()).toReturn(newName);
        assertEquals(oldName, customFieldManager.getCustomFieldObject(fieldId).getName());
        GenericValue gv = new GenericValue(cf.copyGenericValue());
        gv.setString("name", newName);
        stub(mockUpdatedField.copyGenericValue()).toReturn(gv);
        stub(mockUpdatedField.getCustomFieldType()).toReturn(customFieldType);
        customFieldManager.updateCustomField(mockUpdatedField);
        assertEquals(newName, customFieldManager.getCustomFieldObject(fieldId).getName());

    }

    private Map<String, Object> createCustomFieldMap (String fieldName, String key, String description)
    {
        Map<String, Object> createFields = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(fieldName))
        {
            createFields.put("name", StringUtils.abbreviate(fieldName, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
        }
        if (StringUtils.isNotEmpty(key))
        {
            createFields.put("customfieldtypekey", key);
        }
        if (StringUtils.isNotEmpty(description))
            createFields.put("description", description);

        return createFields;
    }

    private GenericValue createGv(String fieldName, Map<String, Object> params)
    {
        if (!params.containsKey("id"))
        {
            params.put("id", new Long (1));
        }
        return CoreFactory.getGenericDelegator().makeValue("CustomField", params);
    }
}
