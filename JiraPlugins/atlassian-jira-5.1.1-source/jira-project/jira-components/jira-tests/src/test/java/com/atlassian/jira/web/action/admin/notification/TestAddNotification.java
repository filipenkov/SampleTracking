/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.util.JiraTypeUtils;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;
import webwork.action.ActionContext;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

public class TestAddNotification extends AbstractWebworkTestCase
{
    private final Long TEST_EVENT_ID = new Long(1);

    private GenericValue scheme;

    public TestAddNotification(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        scheme = UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("id", new Long(1), "name", "name"));
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(NotificationTypeManager.class);

        super.tearDown();
    }

    public void testGetsSets() throws GenericEntityException
    {
        final AddNotification ad = new AddNotification();
        assertNull(ad.getType());
        ad.setType("This Type");
        assertEquals("This Type", ad.getType());
    }

    public void testGetTypes()
    {
        final AddNotification ad = new AddNotification();
        final Map types = ad.getTypes();
        assertNotNull(types);
        assertTrue(!types.isEmpty());
        assertEquals(JiraTypeUtils.loadTypes("notification-event-types.xml", NotificationTypeManager.class).size(), types.size()); // number of entries in notification-event-types.xml

        final NotificationType type = (NotificationType) types.get("Current_Assignee");
        assertEquals("Current Assignee", type.getDisplayName());
    }

    public void testDoValidation1() throws Exception
    {
        final AddNotification ad = new AddNotification();
        final String result = ad.execute();
        assertEquals(Action.INPUT, result);
        assertTrue(!ad.getErrorMessages().isEmpty());
        assertTrue(ad.getErrorMessages().contains(
            "You must select a scheme to add the notification to.  Click \"Notification Schemes\" link in the left hand navigation to pick one."));
        assertEquals(ad.getErrors().get("eventTypeIds"), ("You must select a notification to add."));
        assertTrue(ad.getErrorMessages().contains("You must select a type for this notification."));
    }

    // test that user enters a value for a type that requires them to fill in an extra text field
    // eg the Single Email Address type
    public void testDoValidation2() throws Exception
    {
        setupNotificationTypes();

        final AddNotification ad = new AddNotification();
        ad.setSchemeId(scheme.getLong("id"));
        ad.setEventTypeIds(new Long[] { TEST_EVENT_ID });
        ad.setType("TEST_TYPE_1");

        final String result = ad.execute();
        assertEquals(Action.INPUT, result);
        assertTrue(!ad.getErrorMessages().isEmpty());
        assertEquals(1, ad.getErrorMessages().size());
        assertTrue(ad.getErrorMessages().contains("Please fill out the box next to the radio button with valid data."));
    }

    private void setupNotificationTypes()
    {
        ManagerFactory.addService(NotificationTypeManager.class, new NotificationTypeManager("test-notification-event-types.xml"));
    }

    // test that adding a type to an event works
    // ALSO test that adding a duplicate type to a event is ignored (and not re-added)
    public void testDoExecute() throws Exception
    {
        setupNotificationTypes();

        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("EditNotifications!default.jspa?schemeId=" + scheme.getLong("id"));

        ActionContext.setSingleValueParameters(EasyMap.build("TEST_TYPE_1", "Anything"));

        final AddNotification ad = new AddNotification();
        ad.setSchemeId(scheme.getLong("id"));
        ad.setEventTypeIds(new Long[] { TEST_EVENT_ID });
        ad.setType("TEST_TYPE_1");

        final String result = ad.execute();
        assertEquals(Action.NONE, result);

        response.verify();

        final List notifications = ManagerFactory.getNotificationSchemeManager().getEntities(scheme);
        assertEquals(1, notifications.size());
    }

    public void testDoExecuteWithDuplicate() throws Exception
    {
        //generic setup for this class
        setupNotificationTypes();

        final Long schemeId = scheme.getLong("id");

        //setup scheme manager
        final Mock mockNotificationSchemeManager = new Mock(NotificationSchemeManager.class);
        ManagerFactory.addService(NotificationSchemeManager.class, (NotificationSchemeManager) mockNotificationSchemeManager.proxy());

        //set this up so that it returns the scheme
        mockNotificationSchemeManager.expectAndReturn("getScheme", P.args(P.eq(schemeId)), scheme);

        // set up eventExists() to return true by getting hasEntities to return a dummy non-empty list that will make the function return true
        mockNotificationSchemeManager.expectAndReturn("hasEntities", P.ANY_ARGS, Boolean.TRUE);

        // check that createSchemeEntity is called
        mockNotificationSchemeManager.expectNotCalled("createSchemeEntity");
    }
}
