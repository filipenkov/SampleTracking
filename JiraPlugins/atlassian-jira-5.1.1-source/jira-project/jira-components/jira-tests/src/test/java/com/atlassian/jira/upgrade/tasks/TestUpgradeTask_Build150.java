package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.I18nHelper;
import electric.xml.Document;
import electric.xml.Element;

/**
 * @since v4.0
 */
public class TestUpgradeTask_Build150 extends MockControllerTestCase
{
    @Test
    public void testParseEventKeysExist() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", 1L));
        mockController.setReturnValue(null);
        ofBizDelegator.createValue(EventType.EVENT_TYPE, EasyMap.build("description", "i18n Desc", "type", EventType.JIRA_SYSTEM_EVENT_TYPE, "name", "i18n Name", "id", 1L));
        mockController.setReturnValue(null);
        
        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        i18n.getText("event.type.testevent.name");
        mockController.setReturnValue("i18n Name");
        i18n.getText("event.type.testevent.desc");
        mockController.setReturnValue("i18n Desc");

        mockController.replay();

        UpgradeTask_Build150 task = new UpgradeTask_Build150(ofBizDelegator)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        Document doc = new Document("<eventtype id=\"1\">\n"
                                + "        <i18n-name-key>event.type.testevent.name</i18n-name-key>\n"
                                + "        <i18n-description-key>event.type.testevent.desc</i18n-description-key>\n"
                                + "        <name>Test Event</name>\n"
                                + "        <description>This is the 'Test Event' event type.</description>\n"
                                + "        <notificationName>TEST_NOTIFICATION</notificationName>\n"
                                + "        <eventName>tested</eventName>\n"
                                + "    </eventtype>");

        Element action = doc.getElement("eventtype");

        task.parseAction(action);

        mockController.verify();
    }

    @Test
    public void testParseEventKeysDontExist() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", 1L));
        mockController.setReturnValue(null);
        ofBizDelegator.createValue(EventType.EVENT_TYPE, EasyMap.build("description", "This is the 'Test Event' event type.", "type", EventType.JIRA_SYSTEM_EVENT_TYPE, "name", "Test Event", "id", 1L));
        mockController.setReturnValue(null);

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        i18n.getText("event.type.testevent.name");
        mockController.setReturnValue("event.type.testevent.name");
        i18n.getText("event.type.testevent.desc");
        mockController.setReturnValue("event.type.testevent.desc");

        mockController.replay();

        UpgradeTask_Build150 task = new UpgradeTask_Build150(ofBizDelegator)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        Document doc = new Document("<eventtype id=\"1\">\n"
                                + "        <i18n-name-key>event.type.testevent.name</i18n-name-key>\n"
                                + "        <i18n-description-key>event.type.testevent.desc</i18n-description-key>\n"
                                + "        <name>Test Event</name>\n"
                                + "        <description>This is the 'Test Event' event type.</description>\n"
                                + "        <notificationName>TEST_NOTIFICATION</notificationName>\n"
                                + "        <eventName>tested</eventName>\n"
                                + "    </eventtype>");

        Element action = doc.getElement("eventtype");

        task.parseAction(action);

        mockController.verify();
    }

    @Test
    public void testParseEventKeysNotPresent() throws Exception
    {
        final OfBizDelegator ofBizDelegator = mockController.getMock(OfBizDelegator.class);
        ofBizDelegator.findByPrimaryKey(EventType.EVENT_TYPE, EasyMap.build("id", 1L));
        mockController.setReturnValue(null);
        ofBizDelegator.createValue(EventType.EVENT_TYPE, EasyMap.build("description", "This is the 'Test Event' event type.", "type", EventType.JIRA_SYSTEM_EVENT_TYPE, "name", "Test Event", "id", 1L));
        mockController.setReturnValue(null);

        final I18nHelper i18n = mockController.getMock(I18nHelper.class);
        mockController.replay();

        UpgradeTask_Build150 task = new UpgradeTask_Build150(ofBizDelegator)
        {
            @Override
            I18nHelper getApplicationI18n()
            {
                return i18n;
            }
        };

        Document doc = new Document("<eventtype id=\"1\">\n"
                                + "        <name>Test Event</name>\n"
                                + "        <description>This is the 'Test Event' event type.</description>\n"
                                + "        <notificationName>TEST_NOTIFICATION</notificationName>\n"
                                + "        <eventName>tested</eventName>\n"
                                + "    </eventtype>");

        Element action = doc.getElement("eventtype");

        task.parseAction(action);

        mockController.verify();
    }
}
