/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

/**
 * Unit tests for the CurrentAssignee notification type
 * <p/>
 * See JRA-6344 for more details on how things changed
 */
public class TestCurrentAssignee extends AbstractUsersTestCase
{
    private Issue issueObject;
    private Issue nullUserIssueObject;
    private User newAssignee;
    private MockApplicationProperties oldBehaviourMockApplicationProperties;
    private MockApplicationProperties newBehaviourMockApplicationProperties;
    private static final String NEW_ASSIGNEE_EMAIL = "owen@atlassian.com";
    private User previousAssignee;

    public TestCurrentAssignee(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        newAssignee = createMockUser("Watcher 1", "Watcher 1", NEW_ASSIGNEE_EMAIL);
        new JiraUserPreferences(newAssignee).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);

        IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);

        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "this", "assignee", "Watcher 1"));
        issueObject = issueFactory.getIssue(issue);
        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("WatchIssue", newAssignee, issue);

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "this"));
        nullUserIssueObject = issueFactory.getIssue(issue);

        oldBehaviourMockApplicationProperties = new MockApplicationProperties();
        oldBehaviourMockApplicationProperties.setString(APKeys.JIRA_ASSIGNEE_CHANGE_IS_SENT_TO_BOTH_PARTIES, "false");

        newBehaviourMockApplicationProperties = new MockApplicationProperties();
        newBehaviourMockApplicationProperties.setString(APKeys.JIRA_ASSIGNEE_CHANGE_IS_SENT_TO_BOTH_PARTIES, "true");

        previousAssignee = createMockUser("TestCurrentAssignee_previousAssignee", "TestCurrentAssignee_previousAssignee", "previous_assignee@example.com");

    }

    protected void tearDown() throws Exception
    {
        ComponentManager.getComponentInstanceOfType(UserAssociationStore.class).removeAssociation("WatchIssue", newAssignee, issueObject.getGenericValue());
        issueObject = null;
        nullUserIssueObject = null;
        newAssignee = null;
        previousAssignee = null;
        super.tearDown();
    }

    /**
     * Helper class for providing a previous assignee
     */
    private class PreviousAssigneeAwareCurrentAssignee extends CurrentAssignee
    {
        private final User thePreviousAssignee;

        private PreviousAssigneeAwareCurrentAssignee(final JiraAuthenticationContext jiraAuthenticationContext, final ApplicationProperties applicationProperties, final User thePreviousAssignee)
        {
            super(jiraAuthenticationContext, applicationProperties);
            this.thePreviousAssignee = thePreviousAssignee;
        }

        public PreviousAssigneeAwareCurrentAssignee(final JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties)
        {
            this(jiraAuthenticationContext, applicationProperties, null);
        }

        @Override
        protected User getPreviousAssignee(final IssueEvent event)
        {
            return thePreviousAssignee;
        }
    }

    public void testGetDisplayName()
    {
        final JiraAuthenticationContext context = ComponentAccessor.getJiraAuthenticationContext();
        CurrentAssignee al = new CurrentAssignee(context, oldBehaviourMockApplicationProperties);
        assertEquals("Current Assignee", al.getDisplayName());
    }

    public void testGetRecipients_OldBehaviour()
    {
        CurrentAssignee currentAssignee = new CurrentAssignee(null, oldBehaviourMockApplicationProperties);

        IssueEvent event = new IssueEvent(issueObject, null, null, null);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);

        NotificationRecipient nr = actualList.get(0);
        assertEquals(NEW_ASSIGNEE_EMAIL, nr.getEmail());
        assertTrue(nr.isHtml());
    }

    /**
     * This is very unlikely to happen really but lets test it in isolation anyways
     */
    public void testGetRecipients_NullIssue()
    {
        CurrentAssignee currentAssignee = new CurrentAssignee(null, oldBehaviourMockApplicationProperties);

        IssueEvent event = new IssueEvent(null, null, null, null);

        final List<NotificationRecipient> expectedList = Collections.emptyList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the old behaviour when the issue event IS an assigned event
     */
    public void testGetRecipientsIssueAssigned_OldBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, oldBehaviourMockApplicationProperties, previousAssignee);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(previousAssignee), new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    /**
     * Tests the old behaviour when the issue event is NOT an assigned event
     */
    public void testGetRecipientsIssueNotAssigned_OldBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, oldBehaviourMockApplicationProperties, previousAssignee);
        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    /**
     * Tests the old behaviour when the issue event IS an assigned event BUt there is a null previous assignee
     */
    public void testGetRecipientsIssueAssigned_NullPreviousAssignee_OldBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, oldBehaviourMockApplicationProperties);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the old behaviour when the issue event is NOT an assigned event BUt the new assignee was null
     */
    public void testGetRecipientsIssueNotAssigned_NullNewAssignee_OldBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, oldBehaviourMockApplicationProperties, previousAssignee);
        IssueEvent event = new IssueEvent(nullUserIssueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = Collections.emptyList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the old behaviour when the issue event is NOT an assigned event BUT the previous and new assignee are
     * null
     */
    public void testGetRecipientsIssueNotAssigned_NullParties_OldBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, oldBehaviourMockApplicationProperties);
        IssueEvent event = new IssueEvent(nullUserIssueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = Collections.emptyList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    /* ----- NEW BEHAVIOUR TESTS ----- */

    /**
     * Tests the new behaviour when the issue event IS an assigned event
     */
    public void testGetRecipientsIssueAssigned_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties, previousAssignee);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(previousAssignee), new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

   /**
     * Tests the new behaviour when the issue event IS an assigned event when the application properties flag is missing
     */
    public void testGetRecipientsIssueAssigned_NewBehaviour_WhenFlagIsMissing()
    {

        final MockApplicationProperties missingProperties = new MockApplicationProperties();
        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, missingProperties, previousAssignee);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(previousAssignee), new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the new behaviour when the issue event IS an assigned event BUT there is not previous assignee
     */
    public void testGetRecipientsIssueAssigned_NullPreviousAssignee_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties);

        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_ASSIGNED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    /**
     * Tests the new behaviour when the issue event is NOT an assigned event.  Should have both parties
     */
    public void testGetRecipientsIssueNotAssigned_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties, previousAssignee);
        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(previousAssignee), new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUt the previous assignee was null
     */
    public void testGetRecipientsIssueNotAssigned_NullPreviousAssignee_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties);
        IssueEvent event = new IssueEvent(issueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(newAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUt the new assignee was null
     */
    public void testGetRecipientsIssueNotAssigned_NullNewAssignee_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties, previousAssignee);
        IssueEvent event = new IssueEvent(nullUserIssueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = CollectionBuilder.newBuilder(new NotificationRecipient(previousAssignee)).asList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }

    /**
     * Tests the new behaviour when the issue event is NOT an assigned event BUT the previous and new assignee are null
     */
    public void testGetRecipientsIssueNotAssigned_NullParties_NewBehaviour()
    {

        CurrentAssignee currentAssignee = new PreviousAssigneeAwareCurrentAssignee(null, newBehaviourMockApplicationProperties, null);
        IssueEvent event = new IssueEvent(nullUserIssueObject, null, null, EventType.ISSUE_UPDATED_ID);

        final List<NotificationRecipient> expectedList = Collections.emptyList();
        final List<NotificationRecipient> actualList = currentAssignee.getRecipients(event, null);
        assertReceipents(expectedList, actualList);
    }


    private void assertReceipents(final List<NotificationRecipient> expectedList, final List<NotificationRecipient> actualList)
    {
        assertEquals(expectedList.size(), actualList.size());
        for (NotificationRecipient recipient : expectedList)
        {
            assertTrue(actualList.contains(recipient));
        }
    }
}
