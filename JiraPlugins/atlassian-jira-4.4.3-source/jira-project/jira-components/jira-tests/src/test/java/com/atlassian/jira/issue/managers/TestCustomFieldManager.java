package com.atlassian.jira.issue.managers;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.plugin.event.PluginEventManager;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.persistence.FieldConfigContextPersister;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.EasyList;
import com.atlassian.plugin.PluginAccessor;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCustomFieldManager extends MockControllerTestCase
{

    private DefaultCustomFieldManager customFieldManager;

    // Level 1 Dependencies
    private PluginAccessor mockPluginAccessor;
    private MockControl controlPlugin;
    private OfBizDelegator mockDelegator;
    private EventPublisher mockEventPublisher;
    private MockControl controlDelgator;

    // Level 2 Dependencies
    private CustomFieldTypeModuleDescriptor customFieldTypeModuleDescriptor;
    private MockControl controlModuleDescriptor;
    private CustomFieldType cfType;
    private CustomFieldSearcher cfSearch;
    private FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;


    @Before
    public void setUp() throws Exception
    {
        controlPlugin = MockControl.createControl(PluginAccessor.class);
        mockPluginAccessor = (PluginAccessor) controlPlugin.getMock();
        mockEventPublisher = mockController.createStrictMock(EventPublisher.class);
        mockEventPublisher.register(anyObject());
        expectLastCall();

        controlDelgator = MockControl.createControl(OfBizDelegator.class);
        mockDelegator = (OfBizDelegator) controlDelgator.getMock();
        mockDelegator.findAll(CustomFieldImpl.ENTITY_TABLE_NAME);
        controlDelgator.expectAndReturn("findAll", Collections.EMPTY_LIST);

        // Level 2 Dependencies
        controlModuleDescriptor = MockClassControl.createControl(CustomFieldTypeModuleDescriptor.class);
        customFieldTypeModuleDescriptor = (CustomFieldTypeModuleDescriptor) controlModuleDescriptor.getMock();

        final CustomFieldType mockCustomFieldType = (CustomFieldType) MockControl.createControl(CustomFieldType.class).getMock();

        fieldConfigSchemeClauseContextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);
    }

    @Test
    public void testGetCustomFieldTypesInvalidKey() throws GenericEntityException
    {
        mockController.replay();
        mockPluginAccessor.getEnabledPluginModule("key");
        controlPlugin.setReturnValue(null);

        startTestPhase();

        assertNull(customFieldManager.getCustomFieldType("key"));
        //customFieldManager.createCustomField("newFieldName", "description", cfType, cfSearch, new Long(0), "Bug");
    }

    @Test
    public void testGetCustomFieldTypes() throws GenericEntityException
    {
        mockController.replay();
        final CustomFieldType customFieldType = (CustomFieldType)getMockObject(CustomFieldType.class);
        mockPluginAccessor.getEnabledPluginModule("key");
        controlPlugin.setReturnValue(customFieldTypeModuleDescriptor);

        customFieldTypeModuleDescriptor.getModule();
        controlModuleDescriptor.setReturnValue(customFieldType);

        startTestPhase();

        assertNotNull(customFieldManager.getCustomFieldType("key"));
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithNullId() throws Exception
    {
        DefaultCustomFieldManager manager = mockController.instantiateAndReplayNice(MockDefaultCustomFieldManager.class,
                MockDefaultCustomFieldManager.constructorWithNullCF);
        try
        {
            manager.removeCustomFieldPossiblyLeavingOrphanedData(null);
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
        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByPrimaryKey("CustomField", EasyMap.build("id", 999L));
        mockController.setReturnValue(null);

        DefaultCustomFieldManager manager = mockController.instantiateAndReplayNice(MockDefaultCustomFieldManager.class,
                MockDefaultCustomFieldManager.constructorWithNullCF);
        try
        {
            manager.removeCustomFieldPossiblyLeavingOrphanedData(999L);
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
        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;
        CustomField mockCustomField = mockController.getMock(CustomField.class);

        final FieldScreenManager screenManager = mockController.getMock(FieldScreenManager.class);
        screenManager.removeFieldScreenItems(fieldSId);

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId));
        mockController.setReturnValue(1);
        delegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId));
        mockController.setReturnValue(1);

        final FieldConfigSchemeManager configSchemeManager = mockController.getMock(FieldConfigSchemeManager.class);
        configSchemeManager.removeInvalidFieldConfigSchemesForCustomField(fieldSId);

        final NotificationSchemeManager notificationSchemeManager = mockController.getMock(NotificationSchemeManager.class);
        notificationSchemeManager.removeSchemeEntitiesForField(fieldSId);

        mockCustomField.getId();
        mockController.setReturnValue(fieldSId);
        mockCustomField.remove();
        mockController.setReturnValue(new HashSet<Long>(EasyList.build(10001L)));

        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.refresh();

        MockDefaultCustomFieldManager manager = mockController.instantiateAndReplayNice(MockDefaultCustomFieldManager.class,
                MockDefaultCustomFieldManager.constructorWithCF);

        manager.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);

        assertTrue(manager.refreshCalled.get());
    }

    @Test
    public void testRemoveCustomFieldPossiblyLeavingOrphanedDataWithExistingCustomFieldFromdb() throws Exception
    {
        final long fieldId = 999L;
        final String fieldSId = "customfield_" + fieldId;

        MockControl mockGenericValueControl = MockClassControl.createControl(GenericValue.class);
        GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();
        mockGenericValue.remove();
        mockGenericValueControl.replay();

        final OfBizDelegator delegator = mockController.getMock(OfBizDelegator.class);
        delegator.findByPrimaryKey("CustomField", EasyMap.build("id", 999L));


        mockController.setReturnValue(mockGenericValue);

        delegator.removeByAnd("ColumnLayoutItem", EasyMap.build("fieldidentifier", fieldSId));
        mockController.setReturnValue(1);
        delegator.removeByAnd("FieldLayoutItem", EasyMap.build("fieldidentifier", fieldSId));
        mockController.setReturnValue(1);

        final FieldScreenManager screenManager = mockController.getMock(FieldScreenManager.class);
        screenManager.removeFieldScreenItems(fieldSId);

        final FieldConfigSchemeManager configSchemeManager = mockController.getMock(FieldConfigSchemeManager.class);
        configSchemeManager.removeInvalidFieldConfigSchemesForCustomField(fieldSId);

        final NotificationSchemeManager notificationSchemeManager = mockController.getMock(NotificationSchemeManager.class);
        notificationSchemeManager.removeSchemeEntitiesForField(fieldSId);


        final CustomFieldValuePersister customFieldValuePersister = mockController.getMock(CustomFieldValuePersister.class);
        customFieldValuePersister.removeAllValues(fieldSId);
        mockController.setReturnValue(new HashSet<Long>(EasyList.build(10001L)));

        final FieldManager fieldManager = mockController.getMock(FieldManager.class);
        fieldManager.refresh();

        MockDefaultCustomFieldManager manager = mockController.instantiateAndReplayNice(MockDefaultCustomFieldManager.class,
                MockDefaultCustomFieldManager.constructorWithNullCF);

        manager.removeCustomFieldPossiblyLeavingOrphanedData(fieldId);


        assertTrue(manager.refreshCalled.get());
        mockGenericValueControl.verify();
    }

    private static class MockDefaultCustomFieldManager extends DefaultCustomFieldManager
    {
        public static final Constructor<MockDefaultCustomFieldManager> constructorWithNullCF;
        public static final Constructor<MockDefaultCustomFieldManager> constructorWithCF;
        static
        {
            try
            {
                constructorWithNullCF = MockDefaultCustomFieldManager.class.getConstructor(PluginAccessor.class, OfBizDelegator.class,
                        FieldConfigSchemeManager.class, JiraAuthenticationContext.class, ConstantsManager.class,
                        ProjectManager.class, PermissionManager.class, FieldConfigContextPersister.class,
                        FieldScreenManager.class, RendererManager.class, CustomFieldValuePersister.class, NotificationSchemeManager.class, FieldManager.class,
                        FieldConfigSchemeClauseContextUtil.class, EventPublisher.class);

                constructorWithCF = MockDefaultCustomFieldManager.class.getConstructor(PluginAccessor.class, OfBizDelegator.class,
                        FieldConfigSchemeManager.class, JiraAuthenticationContext.class, ConstantsManager.class,
                        ProjectManager.class, PermissionManager.class, FieldConfigContextPersister.class,
                        FieldScreenManager.class, RendererManager.class, CustomFieldValuePersister.class,
                        NotificationSchemeManager.class, FieldManager.class, CustomField.class, FieldConfigSchemeClauseContextUtil.class,
                        EventPublisher.class);
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }

        private final CustomField mockCustomField;
        public final AtomicBoolean refreshCalled = new AtomicBoolean(false);

        public MockDefaultCustomFieldManager(final PluginAccessor pluginAccessor, final OfBizDelegator delegator,
                final FieldConfigSchemeManager fieldConfigSchemeManager, final JiraAuthenticationContext authenticationContext,
                final ConstantsManager constantsManager, final ProjectManager projectManager,
                final PermissionManager permissionManager, final FieldConfigContextPersister contextPersister,
                final FieldScreenManager fieldScreenManager, final RendererManager rendererManager,
                final CustomFieldValuePersister customFieldValuePersister, final NotificationSchemeManager notificationSchemeManager,
                final FieldManager fieldManager, final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil,
                final EventPublisher eventPublisher)
        {
            super(pluginAccessor, delegator, fieldConfigSchemeManager, authenticationContext, constantsManager,
                    projectManager, permissionManager, contextPersister, fieldScreenManager, rendererManager,
                    customFieldValuePersister, notificationSchemeManager, fieldManager, fieldConfigSchemeClauseContextUtil,
                    eventPublisher);
            this.mockCustomField = null;
        }

        public MockDefaultCustomFieldManager(PluginAccessor pluginAccessor, OfBizDelegator delegator,
                FieldConfigSchemeManager fieldConfigSchemeManager, JiraAuthenticationContext authenticationContext,
                ConstantsManager constantsManager, ProjectManager projectManager, PermissionManager permissionManager,
                FieldConfigContextPersister contextPersister, FieldScreenManager fieldScreenManager,
                RendererManager rendererManager, CustomFieldValuePersister customFieldValuePersister,
                final NotificationSchemeManager notificationSchemeManager, final FieldManager fieldManager,
                CustomField mockCustomField, FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil,
                 final EventPublisher eventPublisher)
        {
            super(pluginAccessor, delegator, fieldConfigSchemeManager, authenticationContext, constantsManager,
                    projectManager, permissionManager, contextPersister, fieldScreenManager, rendererManager,
                    customFieldValuePersister, notificationSchemeManager, fieldManager, fieldConfigSchemeClauseContextUtil,
                    eventPublisher);
            this.mockCustomField = mockCustomField;
        }

        @Override
        public CustomField getCustomFieldObject(final Long id)
        {
            return mockCustomField;
        }

        @Override
        void populateCache()
        {
            //nuthing
        }

        @Override
        public void refresh()
        {
            refreshCalled.set(true);
        }
    }


    private void startTestPhase()
    {
        controlPlugin.replay();
        controlDelgator.replay();
        controlModuleDescriptor.replay();

        customFieldManager = new DefaultCustomFieldManager(mockPluginAccessor, mockDelegator, null, null, null, null, null, null, null, null, null, null, null, null, mockEventPublisher);
    }

    private static Object getMockObject(Class clazz)
    {
        MockControl mc = MockClassControl.createNiceControl(clazz);
        mc.replay();
        return mc.getMock();
    }
}
