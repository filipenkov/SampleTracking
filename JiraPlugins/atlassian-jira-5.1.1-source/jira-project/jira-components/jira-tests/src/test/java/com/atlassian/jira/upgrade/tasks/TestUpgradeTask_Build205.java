package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;

public class TestUpgradeTask_Build205 extends LegacyJiraMockTestCase
{
    public void testDoUpgradeKeysExist() throws Exception
    {
        final I18nHelper i18n = EasyMock.createMock(I18nHelper.class);
        EasyMock.expect(i18n.getText(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME_KEY)).andReturn(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME + "test");
        EasyMock.expect(i18n.getText(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC_KEY)).andReturn(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC + "test");
        EasyMock.replay(i18n);

        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        // Add the notification type and make sure it is in the db and available through the manager
        final OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        UpgradeTask_Build205 upgradeTask_build205 = new UpgradeTask_Build205(ofBizDelegator, eventTypeManager)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        // Do the upgrade task
        upgradeTask_build205.doUpgrade(false);

        // Check the db for the value
        final GenericValue issueCommentEditedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_COMMENT_EDITED_ID));
        assertNotNull(issueCommentEditedGV);
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME + "test", issueCommentEditedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC + "test", issueCommentEditedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueCommentEditedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType eventType = eventTypeManager.getEventType(EventType.ISSUE_COMMENT_EDITED_ID);
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME + "test", eventType.getName());
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC + "test", eventType.getDescription());
        assertTrue(eventType.isSystemEventType());

        EasyMock.verify(i18n);
    }

    public void testDoUpgradeKeysDontExist() throws Exception
    {
        final I18nHelper i18n = EasyMock.createMock(I18nHelper.class);
        EasyMock.expect(i18n.getText(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME_KEY)).andReturn(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME_KEY);
        EasyMock.expect(i18n.getText(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC_KEY)).andReturn(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC_KEY);
        EasyMock.replay(i18n);

        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        // Add the notification type and make sure it is in the db and available through the manager
        final OfBizDelegator ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        UpgradeTask_Build205 upgradeTask_build205 = new UpgradeTask_Build205(ofBizDelegator, eventTypeManager)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        // Do the upgrade task
        upgradeTask_build205.doUpgrade(false);

        // Check the db for the value
        final GenericValue issueCommentEditedGV = ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", EventType.ISSUE_COMMENT_EDITED_ID));
        assertNotNull(issueCommentEditedGV);
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME, issueCommentEditedGV.getString(UpgradeTask_Build150.NAME_STRING));
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC, issueCommentEditedGV.getString(UpgradeTask_Build150.DESC_STRING));
        assertEquals(EventType.JIRA_SYSTEM_EVENT_TYPE, issueCommentEditedGV.getString(UpgradeTask_Build150.TYPE_STRING));

        // Now make sure we can get the object form from the manager
        final EventType eventType = eventTypeManager.getEventType(EventType.ISSUE_COMMENT_EDITED_ID);
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_NAME, eventType.getName());
        assertEquals(UpgradeTask_Build205.ISSUE_COMMENT_EDITED_DESC, eventType.getDescription());
        assertTrue(eventType.isSystemEventType());

        EasyMock.verify(i18n);
    }
}
