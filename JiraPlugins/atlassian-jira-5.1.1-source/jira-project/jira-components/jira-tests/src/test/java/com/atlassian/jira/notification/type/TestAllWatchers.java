/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class TestAllWatchers extends ListeningTestCase
{
    @Mock
    private JiraAuthenticationContext authenticationContext;

    @Mock
    private IssueManager mockIssueManager;

    @Mock
    private Issue mockIssue;

    @Mock
    private UserPropertyManager userPropertyManager;

    @Mock
    private PropertySet propertySet;

    @Before
    public void initMocks() throws Exception
    {
        EasyMockAnnotations.initMocks(this);

        expect(userPropertyManager.getPropertySet(any(User.class))).andReturn(propertySet).anyTimes();
        expect(propertySet.exists(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).andReturn(true).anyTimes();
        expect(propertySet.getString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE)).andReturn("html").anyTimes();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(UserPropertyManager.class, userPropertyManager));
    }

    @Test
    public void testGetDisplayName()
    {
        I18nHelper mockHelper = createNiceMock(I18nHelper.class);
        expect(mockHelper.getText("admin.notification.types.all.watchers")).andReturn("All Watchers");
        replay(mockHelper);
        expect(authenticationContext.getI18nHelper()).andReturn(mockHelper);
        replayMocks();

        assertEquals("All Watchers", createTested().getDisplayName());
        verify(mockHelper);
    }

    @Test
    public void shouldGetRecipientsEventParams()
    {
        expect(mockIssueManager.getWatchers(mockIssue)).andReturn(ImmutableList.<User>of(
                new MockUser("one"),
                new MockUser("two")
        ));
        replayMocks();

        IssueEvent event = new IssueEvent(mockIssue, null, new MockUser("sender"), 1L);
        List<NotificationRecipient> recipients = createTested().getRecipients(event, null);

        assertEquals(2, recipients.size());
        assertEquals("one", recipients.get(0).getUserRecipient().getName());
        assertEquals("two", recipients.get(1).getUserRecipient().getName());
    }

    @Test
    public void shouldGetRecipientsFromIssueGivenNoEventParams()
    {
        replayMocks();

        IssueEvent event = new IssueEvent(mockIssue, defaultParams(), new MockUser("sender"), 1L);
        List<NotificationRecipient> recipients = createTested().getRecipients(event, null);

        assertEquals(2, recipients.size());
        assertEquals("one", recipients.get(0).getUserRecipient().getName());
        assertEquals("two", recipients.get(1).getUserRecipient().getName());
    }



    private AllWatchers createTested()
    {
        return new AllWatchers(authenticationContext, mockIssueManager);
    }

    private ImmutableMap<String, Object> defaultParams()
    {
        return ImmutableMap.<String,Object>of(IssueEvent.WATCHERS_PARAM_NAME,
                ImmutableList.<User>of(new MockUser("one"), new MockUser("two")));
    }

    private void replayMocks()
    {
        replay(mockIssue, mockIssueManager, authenticationContext, userPropertyManager, propertySet);
    }
}
