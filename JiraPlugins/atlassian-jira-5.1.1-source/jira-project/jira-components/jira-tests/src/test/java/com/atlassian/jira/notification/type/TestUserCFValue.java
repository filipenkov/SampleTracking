package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyList;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for user custom field notification type
 *
 * @see UserCFValue
 * @since v4.4
 */
public class TestUserCFValue extends ListeningTestCase
{
    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private FieldManager fieldManager;

    @Mock
    private UserPropertyManager userPropertyManager;

    @Mock
    private PropertySet propertySet;

    @Mock
    private Issue issue;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);
        expect(issue.getIssueTypeObject()).andReturn(new MockIssueType("test type", "test type")).anyTimes();
        expect(userPropertyManager.getPropertySet(any(User.class))).andReturn(propertySet).anyTimes();
        expect(propertySet.exists(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).andReturn(true).anyTimes();
        expect(propertySet.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).andReturn("html").anyTimes();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(UserPropertyManager.class, userPropertyManager));
    }

    @Test
    public void shouldGetSingleRecipientFromEventParams()
    {
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            "customfield_10000", new MockUser("a recipient"),
            "customfield_10010", 10L,
            "customfield_10020", new MockUser("not really a recipient")
        ));
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(1, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
    }

    @Test
    public void shouldGetMultipleRecipientsFromEventParams()
    {
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            "customfield_10000", ImmutableList.<User>of(new MockUser("a recipient"),
                new MockUser("a nother recipient"), new MockUser("even more recipients")),
            "customfield_10010", 10L,
            "customfield_10020", new MockUser("not really a recipient")
        ));
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(3, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
        assertEquals("a nother recipient", recipients.get(1).getUserRecipient().getName());
        assertEquals("even more recipients", recipients.get(2).getUserRecipient().getName());
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenValueInEventParamsIsNotRecognized()
    {
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            "customfield_10000", "what the hell is this?",
            "customfield_10010", 10L,
            "customfield_10020", new MockUser("not really a recipient")
        ));
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }

    @Test
    public void shouldGetSingleRecipientFromIssueGivenValueInEventParamsIsNull()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn(new MockUser("a recipient"));
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            // no customfield_10000
            "customfield_10010", 10L,
            "customfield_10020", new MockUser("not really a recipient")
        ));
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(1, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
    }

    @Test
    public void shouldGetMultipleRecipientsFromIssueGivenValueInEventParamsIsNull()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn(ImmutableList.<User>of(new MockUser("a recipient"),
                new MockUser("a nother recipient"), new MockUser("even more recipients")));
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        Map<String,Object> params = ImmutableMap.<String,Object>of(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, ImmutableMap.<String, Object>of(
            // no customfield_10000
            "customfield_10010", 10L,
            "customfield_10020", new MockUser("not really a recipient")
        ));
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, params, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(3, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
        assertEquals("a nother recipient", recipients.get(1).getUserRecipient().getName());
        assertEquals("even more recipients", recipients.get(2).getUserRecipient().getName());
    }

    @Test
    public void shouldGetSingleRecipientFromIssueGivenNoEventParam()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn(new MockUser("a recipient"));
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(1, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
    }


    @Test
    public void shouldGetMultipleRecipientsFromIssueGivenNoEventParam()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn(ImmutableList.<User>of(new MockUser("a recipient"),
                new MockUser("a nother recipient"), new MockUser("even more recipients")));
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertEquals(3, recipients.size());
        assertEquals("a recipient", recipients.get(0).getUserRecipient().getName());
        assertEquals("a nother recipient", recipients.get(1).getUserRecipient().getName());
        assertEquals("even more recipients", recipients.get(2).getUserRecipient().getName());
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndValueInIssueNotRecognized()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn("WOOOOT?");
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndValueInIssueIsNull()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(true);
        expect(userCustomField.getValue(issue)).andReturn(null);
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }

    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndFieldIsNotInScope()
    {
        CustomField userCustomField = createMock(CustomField.class);
        expect(userCustomField.isInScope(any(Project.class), anyList(String.class))).andReturn(false);
        expect(userCustomField.getValue(issue)).andReturn(new MockUser("a recipient"));
        replay(userCustomField);
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(userCustomField);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }


    @Test
    public void shouldReturnEmptyRecipientsListGivenNoEventParamAndNoCorrespondingField()
    {
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(null);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, Collections.emptyMap(), new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }

    @Test
    public void shouldHandleNullEventParams()
    {
        expect(fieldManager.getCustomField("customfield_10000")).andReturn(null);
        final UserCFValue tested = new UserCFValue(jiraAuthenticationContext, fieldManager);
        replayMocks();
        final IssueEvent issueEvent = new IssueEvent(issue, null, new MockUser("sender"), 1L, true);

        List<NotificationRecipient> recipients = tested.getRecipients(issueEvent, "customfield_10000");
        assertTrue(recipients.isEmpty());
    }

    private void replayMocks()
    {
        replay(jiraAuthenticationContext, fieldManager, userPropertyManager, issue, propertySet);
    }
}
