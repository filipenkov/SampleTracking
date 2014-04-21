package com.atlassian.jira.bc.issue.watcher;

import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import mock.MockComment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestAutoWatchService
{
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private WatcherService watcherService;
    @Mock
    private UserPreferencesManager userPreferencesManager;
    @Mock
    private Preferences preferences;

    @Before
    public void setUp() throws Exception
    {
        when(watcherService.isWatchingEnabled()).thenReturn(true);
    }

    @Test
    public void testIssueCreatedIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_CREATED_ID);
    }

    @Test
    public void testIssueCommentedIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_COMMENTED_ID);
    }

    @Test
    public void testIssueUpdatedIsNotAutowatched()
    {
        isAutowatchEvent(false, EventType.ISSUE_UPDATED_ID);
    }

    @Test
    public void testIssueUpdatedWithCommentIsAutowatched()
    {
        isAutowatchEvent(true, EventType.ISSUE_UPDATED_ID, new MockComment("", ""));
    }

    @Test
    public void testIsEnabledWhenFalseSetting()
    {
        when(preferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(false);
        isAutowatchEvent(true);
    }

    @Test
    public void testIsNotEnabled()
    {
        when(preferences.getBoolean(PreferenceKeys.USER_AUTOWATCH_DISABLED)).thenReturn(true);
        isAutowatchEvent(false);
    }

    @Test
    public void testNullUser()
    {
        isAutowatchEvent(false, null, null, null);
    }

    private void isAutowatchEvent(boolean expected)
    {
        isAutowatchEvent(expected, EventType.ISSUE_COMMENTED_ID);
    }

    private void isAutowatchEvent(boolean expected, Long eventType)
    {
        isAutowatchEvent(expected, eventType, null);
    }

    private void isAutowatchEvent(boolean expected, Long eventType, Comment comment)
    {
        isAutowatchEvent(expected, eventType, comment, new MockUser("user"));
    }

    private void isAutowatchEvent(boolean expected, Long eventType, Comment comment, User user)
    {
        final IssueEvent event = new IssueEvent(new MockIssue(), user, comment, null, null, null, eventType);
        when(userPreferencesManager.getPreferences(user)).thenReturn(preferences);
        new AutoWatchService(eventPublisher, watcherService, userPreferencesManager).onIssueEvent(event);
        verify(watcherService, times(expected ? 1 : 0)).addWatcher(event.getIssue(), event.getUser(), event.getUser());
    }
}
