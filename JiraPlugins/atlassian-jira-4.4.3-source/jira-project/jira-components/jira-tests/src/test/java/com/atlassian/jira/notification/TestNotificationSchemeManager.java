/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.user.UserUtils;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.core.util.map.EasyMap;
import com.opensymphony.user.DuplicateEntityException;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestNotificationSchemeManager extends LegacyJiraMockTestCase
{
    private final Long TEST_EVENT_ID = new Long(1);
    private final Long TEST_TEMPLATE_ID = new Long(1);

    public TestNotificationSchemeManager(String s)
    {
        super(s);
    }

    public void testGetSchemeId() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.getScheme(new Long(1));
        assertNull(scheme);

        GenericValue createdScheme = UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "This Name"));
        scheme = nsm.getScheme(createdScheme.getLong("id"));
        assertNotNull(scheme);
    }

    public void testGetSchemeName() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.getScheme("This Name");
        assertNull(scheme);
        UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "This Name"));
        scheme = nsm.getScheme("This Name");
        assertNotNull(scheme);
    }

    public void testSchemeExists() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        assertTrue(!nsm.schemeExists("This Name"));
        UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "This Name"));
        assertTrue(nsm.schemeExists("This Name"));
    }

    public void testCreateScheme() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.createScheme("This Name", "Description");
        assertNotNull(scheme);
        scheme = nsm.getScheme("This Name");
        assertNotNull(scheme);

        boolean exceptionThrown = false;
        try
        {
            nsm.createScheme("This Name", "");
        }
        catch (GenericEntityException ex)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    public void testUpdateScheme() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.createScheme("This Name", "");
        assertNotNull(scheme);
        scheme.setString("name", "That Name");
        nsm.updateScheme(scheme);
        scheme = nsm.getScheme("This Name");
        assertNull(scheme);
        scheme = nsm.getScheme("That Name");
        assertNotNull(scheme);
    }

    public void testDeleteScheme() throws GenericEntityException
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.createScheme("This Name", "");
        assertNotNull(scheme);
        nsm.deleteScheme(scheme.getLong("id"));
        scheme = nsm.getScheme("This Name");
        assertNull(scheme);
    }

    public void testRemoveEntities() throws RemoveException, GenericEntityException
    {
        NotificationSchemeManager notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        GenericValue gvAllWatchers = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10000),
                "eventTypeId", new Long(1),
                "type", "All_Watchers",
                "parameter", "customfield_10030",
                "scheme", new Long(10000)));
        GenericValue gvCustomField_10030 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10001),
                "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value",
                "parameter", "customfield_10030",
                "scheme", new Long(10000)));
        GenericValue gvCustomField_10031 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10002),
                "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value",
                "parameter", "customfield_10031",
                "scheme", new Long(10000)));

        notificationSchemeManager.removeEntities("User_Custom_Field_Value", "customfield_10030");
        List notifications = CoreFactory.getGenericDelegator().findAll("Notification");
        assertEquals(2, notifications.size());
        assertTrue(notifications.contains(gvAllWatchers));
        assertTrue(notifications.contains(gvCustomField_10031));
        assertFalse(notifications.contains(gvCustomField_10030));
    }

    public void testRemoveSchemeEntitiesForField() throws RemoveException, GenericEntityException
    {
        NotificationSchemeManager notificationSchemeManager = ManagerFactory.getNotificationSchemeManager();
        // Create some rows in Notification table.
        GenericValue gvAllWatchers = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10000),
                "eventTypeId", new Long(1),
                "type", "All_Watchers",
                "parameter", "customfield_10030",
                "scheme", new Long(10000)));
        GenericValue gvUserCustomField_10030 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10001),
                "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value",
                "parameter", "customfield_10030",
                "scheme", new Long(10000)));
        GenericValue gvUserCustomField_10031 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10002),
                "eventTypeId", new Long(1),
                "type", "User_Custom_Field_Value",
                "parameter", "customfield_10031",
                "scheme", new Long(10000)));
        GenericValue gvGroupCustomField_10030 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10003),
                "eventTypeId", new Long(1),
                "type", "Group_Custom_Field_Value",
                "parameter", "customfield_10030",
                "scheme", new Long(10000)));
        GenericValue gvGroupCustomField_10031 = CoreFactory.getGenericDelegator().create("Notification", EasyMap.build(
                "id", new Long(10004),
                "eventTypeId", new Long(1),
                "type", "Group_Custom_Field_Value",
                "parameter", "customfield_10031",
                "scheme", new Long(10000)));

        notificationSchemeManager.removeSchemeEntitiesForField("customfield_10030");
        List notifications = CoreFactory.getGenericDelegator().findAll("Notification");
        assertEquals(3, notifications.size());
        assertTrue(notifications.contains(gvAllWatchers));
        assertTrue(notifications.contains(gvGroupCustomField_10031));
        assertTrue(notifications.contains(gvUserCustomField_10031));
        assertFalse(notifications.contains(gvGroupCustomField_10030));
        assertFalse(notifications.contains(gvUserCustomField_10030));
    }

    /**
     * Test to check that getRecipients gets the right list of recipients
     */
    public void testGetRecipients() throws GenericEntityException, AtlassianCoreException, ImmutableException, DuplicateEntityException
    {
        // Setup user
        User bill = UserUtils.createUser("bill", "bill@atlassian.com");
        Preferences userPref = new JiraUserPreferences(bill);
        //@TODO Also need to test if userPref is to be NOT notified by own changes.
        userPref.setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, true);

        // setup the Scheme, and project
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.createScheme("Test Scheme", null);
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10), "lead", bill.getName()));
        nsm.addSchemeToProject(project, scheme);
        nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", null, EventType.ISSUE_CREATED_ID));
        nsm.createSchemeEntity(scheme, new SchemeEntity("Project_Lead", null, EventType.ISSUE_CREATED_ID));

        // Add an event type to the system
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description", "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        GenericValue eventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(eventTypeGV);
        eventTypeManager.addEventType(eventType);

        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", new Long(10), "assignee", "bill"));

        // Get the issue object
        IssueFactory issueFactory = (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        Issue issueObject = issueFactory.getIssue(issue);

        IssueEvent event = new IssueEvent(issueObject, bill, null, null, null, null, eventType.getId());
        SchemeEntity notificationSchemeEntity = new SchemeEntity(new Long(10000), "Current_Assignee", null, EventType.ISSUE_CREATED_ID, null, scheme.getLong("id"));
//        Map notificationParamsMap = EasyMap.build("id", new Long(10000), "eventTypeId", EventType.ISSUE_CREATED_ID, "type", "Current_Assignee", "scheme", scheme.getLong("id"));
//        Set recipients = nsm.getRecipients(event, UtilsForTests.getTestEntity("Notification", notificationParamsMap));
        Set recipients = nsm.getRecipients(event, notificationSchemeEntity);

        //        Set recipients = nsm.getRecipients(project, issue, bill, MailNotifications..ISSUE_CREATED, null);
        //@TODO Fix up code so that there is a user with the same email as an existing email in the list, then reduce duplication!
        assertEquals(1, recipients.size());
    }

    public void testGetRecipientsWithCurrentAssigneeNotificationTypes() throws Exception
    {
        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("notification-event-types.xml"));
        User dave = UserUtils.createUser("dave", "dave@atlassian.com");

        // Add an event type to the system
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description", "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = nsm.createScheme("Test Scheme", null);
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10), "lead", "jeff"));
        nsm.addSchemeToProject(project, scheme);
        nsm.createSchemeEntity(scheme, new SchemeEntity("Current_Assignee", "dave@atlassian.com", eventType.getId()));

        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "project", new Long(10), "assignee", "dave"));

        Preferences userPref = new JiraUserPreferences(dave);
        userPref.setBoolean(PreferenceKeys.USER_NOTIFY_OWN_CHANGES, true);

        // Create the issue object
        IssueFactory issueFactory = (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        Issue issueObject = issueFactory.getIssue(issue);

        IssueEvent event = new IssueEvent(issueObject, dave, null, null, null, null, eventType.getId());
        SchemeEntity notificationSchemeEntity = new SchemeEntity(new Long(10000), "Current_Assignee", null, EventType.ISSUE_CREATED_ID, null, scheme.getLong("id"));
        Set recipients = nsm.getRecipients(event, notificationSchemeEntity);
        assertEquals(1, recipients.size());

    }

    public void testHasEntities() throws Exception
    {
        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        GenericValue scheme = UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("id", new Long(1), "name", "name"));
        SchemeEntity schemeEntity = new SchemeEntity("TEST_TYPE", "TEST_PARAM", TEST_EVENT_ID, TEST_TEMPLATE_ID);
        nsm.createSchemeEntity(scheme, schemeEntity);
        assertTrue(nsm.hasEntities(scheme, TEST_EVENT_ID, "TEST_TYPE", "TEST_PARAM", TEST_TEMPLATE_ID));
    }
}
