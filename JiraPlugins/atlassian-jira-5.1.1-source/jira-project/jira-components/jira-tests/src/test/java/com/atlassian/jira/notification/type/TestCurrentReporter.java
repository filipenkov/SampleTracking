/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestCurrentReporter extends LegacyJiraMockTestCase
{
    public TestCurrentReporter()
    {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    @Test
    public void testGetDisplayName()
    {
        CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        assertEquals("Reporter", cr.getDisplayName());
    }

    @Test
    public void testGetRecipients()
            throws AtlassianCoreException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException, InvalidGroupException
    {
        UtilsForTestSetup.deleteAllEntities();

        User u1 = createMockUser("Watcher 1", "Watcher 1", "owen@atlassian.com");

        Group g1 = createMockGroup("test Group");
        addUserToGroup(u1, g1);
        new JiraUserPreferences(u1).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);

        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "this", "reporter", "Watcher 1"));
        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("WatchIssue", u1, issue);

        CurrentReporter cr = new CurrentReporter(new MockSimpleAuthenticationContext(null, Locale.ENGLISH));
        Map params = new HashMap();
        params.put("level", "test Group");

        MockIssue issueObject = new MockIssue();
        issueObject.setReporter(u1);

        IssueEvent event = new IssueEvent(issueObject, params, null, null);
        List recipients = cr.getRecipients(event, null);

        //        List recipients = cr.getRecipients(issue, null, null, "test Group");
        assertEquals(1, recipients.size());

        NotificationRecipient nr = (NotificationRecipient) recipients.get(0);
        assertEquals("owen@atlassian.com", nr.getEmail());
        assertTrue(nr.isHtml());
        UtilsForTestSetup.deleteAllEntities();
    }

}