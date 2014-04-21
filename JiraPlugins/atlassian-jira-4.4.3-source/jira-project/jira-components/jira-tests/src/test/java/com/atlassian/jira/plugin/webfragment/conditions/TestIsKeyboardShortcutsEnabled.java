package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.core.user.preferences.Preferences;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.opensymphony.user.User;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestIsKeyboardShortcutsEnabled extends ListeningTestCase
{
    private MockProviderAccessor mpa = new MockProviderAccessor();
    private User user = new User("admin", mpa, new MockCrowdService());

    @Test
    public void testShouldDisplayAnonymous()
    {
        IsKeyboardShortcutsEnabledCondition condition = new IsKeyboardShortcutsEnabledCondition(null);
        final boolean result = condition.shouldDisplay(null, null);
        assertTrue(result);
    }

    @Test
    public void testShouldDisplayEnabled()
    {
        final Preferences mockPreferences = createMock(Preferences.class);
        expect(mockPreferences.getBoolean("user.keyboard.shortcuts.disabled")).andReturn(false);
        final UserPreferencesManager mockUserPreferencesManager = createMock(UserPreferencesManager.class);
        expect(mockUserPreferencesManager.getPreferences(user)).andReturn(mockPreferences);

        replay(mockUserPreferencesManager, mockPreferences);
        IsKeyboardShortcutsEnabledCondition condition = new IsKeyboardShortcutsEnabledCondition(mockUserPreferencesManager);
        final boolean result = condition.shouldDisplay(user, null);
        assertTrue(result);

        verify(mockUserPreferencesManager, mockPreferences);
    }

    @Test
    public void testShouldDisplayDisabled()
    {
        final Preferences mockPreferences = createMock(Preferences.class);
        expect(mockPreferences.getBoolean("user.keyboard.shortcuts.disabled")).andReturn(true);
        final UserPreferencesManager mockUserPreferencesManager = createMock(UserPreferencesManager.class);
        expect(mockUserPreferencesManager.getPreferences(user)).andReturn(mockPreferences);

        replay(mockUserPreferencesManager, mockPreferences);
        IsKeyboardShortcutsEnabledCondition condition = new IsKeyboardShortcutsEnabledCondition(mockUserPreferencesManager);
        final boolean result = condition.shouldDisplay(user, null);
        assertFalse(result);

        verify(mockUserPreferencesManager, mockPreferences);
    }
}
