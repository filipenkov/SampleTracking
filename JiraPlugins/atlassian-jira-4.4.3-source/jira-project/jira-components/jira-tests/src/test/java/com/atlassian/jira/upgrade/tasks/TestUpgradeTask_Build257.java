package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;
import org.easymock.EasyMock;

public class TestUpgradeTask_Build257 extends LegacyJiraMockTestCase
{
    public void testDoUpgradeKeysExist() throws Exception
    {
        final I18nHelper i18n = EasyMock.createMock(I18nHelper.class);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME + "test");
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC + "test");
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME + "test");
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC + "test");
        EasyMock.replay(i18n);
        
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        // Add the notification type and make sure it is in the db and available through the manager
        final OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        UpgradeTask_Build257 upgradeTask_build257 = new UpgradeTask_Build257(ofBizDelegator, eventTypeManager)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        // Do the upgrade task
        upgradeTask_build257.doUpgrade(false);
        // Check the db for the value
        final GenericValue issueWorklogUpdatedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_UPDATED_ID));
        assertNotNull(issueWorklogUpdatedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME + "test", issueWorklogUpdatedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC + "test", issueWorklogUpdatedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType updatedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_UPDATED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME + "test", updatedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC + "test", updatedEventType.getDescription());
        assertTrue(updatedEventType.isSystemEventType());

        // Check the db for the value
        final GenericValue issueWorklogDeletedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_DELETED_ID));
        assertNotNull(issueWorklogDeletedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME + "test", issueWorklogDeletedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC + "test", issueWorklogDeletedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogDeletedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType deletedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_DELETED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME + "test", deletedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC + "test", deletedEventType.getDescription());
        assertTrue(deletedEventType.isSystemEventType());

        EasyMock.verify(i18n);
    }

    public void testDoUpgradeKeysDontExist() throws Exception
    {
        final I18nHelper i18n = EasyMock.createMock(I18nHelper.class);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME_KEY);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC_KEY);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME_KEY);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC_KEY);
        EasyMock.replay(i18n);

        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        // Add the notification type and make sure it is in the db and available through the manager
        final OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        UpgradeTask_Build257 upgradeTask_build257 = new UpgradeTask_Build257(ofBizDelegator, eventTypeManager)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        // Do the upgrade task
        upgradeTask_build257.doUpgrade(false);
        // Check the db for the value
        final GenericValue issueWorklogUpdatedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_UPDATED_ID));
        assertNotNull(issueWorklogUpdatedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType updatedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_UPDATED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME, updatedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC, updatedEventType.getDescription());
        assertTrue(updatedEventType.isSystemEventType());

        // Check the db for the value
        final GenericValue issueWorklogDeletedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_DELETED_ID));
        assertNotNull(issueWorklogDeletedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME, issueWorklogDeletedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC, issueWorklogDeletedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogDeletedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType deletedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_DELETED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME, deletedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC, deletedEventType.getDescription());
        assertTrue(deletedEventType.isSystemEventType());

        EasyMock.verify(i18n);
    }
    
    public void testDoUpgrade2ndRun() throws Exception
    {
        final I18nHelper i18n = EasyMock.createMock(I18nHelper.class);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME);
        EasyMock.expect(i18n.getText(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC_KEY)).andReturn(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC);
        EasyMock.replay(i18n);

        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        // Add the notification type and make sure it is in the db and available through the manager
        final OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        UpgradeTask_Build257 upgradeTask_build257 = new UpgradeTask_Build257(ofBizDelegator, eventTypeManager)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        // Do the upgrade task
        upgradeTask_build257.doUpgrade(false);
        upgradeTask_build257.doUpgrade(false);

        // Check the db for the value
        final GenericValue issueWorklogUpdatedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_UPDATED_ID));
        assertNotNull(issueWorklogUpdatedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogUpdatedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType updatedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_UPDATED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_NAME, updatedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_UPDATED_DESC, updatedEventType.getDescription());
        assertTrue(updatedEventType.isSystemEventType());

        // Check the db for the value
        final GenericValue issueWorklogDeletedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_WORKLOG_DELETED_ID));
        assertNotNull(issueWorklogDeletedGV);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME, issueWorklogDeletedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC, issueWorklogDeletedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueWorklogDeletedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType deletedEventType = eventTypeManager.getEventType(EventType.ISSUE_WORKLOG_DELETED_ID);
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_NAME, deletedEventType.getName());
        assertEquals(UpgradeTask_Build257.ISSUE_WORKLOG_DELETED_DESC, deletedEventType.getDescription());
        assertTrue(deletedEventType.isSystemEventType());

    }
}
