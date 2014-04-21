package com.atlassian.upm.notification.cache;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.notification.DismissedState;
import com.atlassian.upm.notification.Notification;
import com.atlassian.upm.notification.NotificationFactory;
import com.atlassian.upm.notification.NotificationFactoryImpl;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.test.MapBackedPluginSettings;

import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class PluginSettingsPluginNotificationCacheTest
{
    private static final String LICENSE1_KEY = "license1.key";
    private static final String LICENSE2_KEY = "license2.key";
    private static final String USERNAME1 = "user1";
    private static final String USERNAME2 = "user2";

    private NotificationFactory factory;
    private NotificationCache cache;
    private Map<String, Object> settingsMap;
    private PluginSettingsFactory pluginSettingsFactory;
    private PluginLicense license1;
    private PluginLicense license2;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;
    private UserManager userManager;

    @DataPoints public static NotificationType[] notificationTypes = NotificationType.values();
   
    @Before
    public void setUp()
    {
        pluginSettingsFactory = mock(PluginSettingsFactory.class);
        license1 = mock(PluginLicense.class);
        license2 = mock(PluginLicense.class);
        notification1 = mock(Notification.class);
        notification2 = mock(Notification.class);
        notification3 = mock(Notification.class);
        userManager = mock(UserManager.class);

        when(license1.getPluginKey()).thenReturn(LICENSE1_KEY);
        when(license2.getPluginKey()).thenReturn(LICENSE2_KEY);
        when(license1.getExpiryDate()).thenReturn(Option.some(new DateTime().minusSeconds(1)));
        when(license2.getExpiryDate()).thenReturn(Option.some(new DateTime().minusSeconds(1)));
        when(notification1.getPluginKey()).thenReturn(LICENSE1_KEY);
        when(notification2.getPluginKey()).thenReturn(LICENSE2_KEY);
        when(notification3.getDismissedState()).thenReturn(new DismissedState(USERNAME1, true));

        when(userManager.getRemoteUsername()).thenReturn(USERNAME1);

        settingsMap = new HashMap<String, Object>();
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(new MapBackedPluginSettings(settingsMap));
        factory = new NotificationFactoryImpl(userManager);
        cache = new PluginSettingsNotificationCache(pluginSettingsFactory, factory);
    }

    @Theory
    public void verifyThatNotificationsAreStoredAndFetchedProperly(NotificationType notificationType)
    {
        cache.setNotifications(notificationType, ImmutableList.<String>of(LICENSE1_KEY, LICENSE2_KEY));
        assertThat(cache.getNotifications(notificationType, USERNAME1, false), contains(notification1, notification2));
    }

    @Theory
    public void verifyThatNotificationsAreDisplayedByDefault(NotificationType notificationType)
    {
        assertFalse(cache.isNotificationTypeDismissed(notificationType, USERNAME1));
    }

    @Theory
    public void verifyThatNotificationsCanBeIgnored(NotificationType notificationType)
    {
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, true);
        assertTrue(cache.isNotificationTypeDismissed(notificationType, USERNAME1));
    }

    @Theory
    public void verifyThatUserIgnoringNotificationsDoesNotAffectOtherUser(NotificationType notificationType)
    {
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, true);
        assertFalse(cache.isNotificationTypeDismissed(notificationType, USERNAME2));
    }

    @Theory
    public void verifyThatIgnoringNotificationsCanBeSetAsFalse(NotificationType notificationType)
    {
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, true);
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, false);
        assertFalse(cache.isNotificationTypeDismissed(notificationType, USERNAME1));
    }

    @Theory
    public void verifyThatNotificationIgnoringCanBeReset(NotificationType notificationType)
    {
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, true);
        cache.resetNotificationTypeDismissal(notificationType);
        assertFalse(cache.isNotificationTypeDismissed(notificationType, USERNAME1));
    }
    
    @Theory
    public void verifyThatDismissedIndividualNotificationIsIncludedWhenNotDismissed(NotificationType notificationType)
    {
        cache.setNotifications(notificationType, ImmutableList.of(LICENSE1_KEY, LICENSE2_KEY));
        cache.setNotificationDismissal(notificationType, USERNAME1, LICENSE1_KEY, false);
        assertThat(cache.getNotifications(notificationType, USERNAME1, true), contains(notification1, notification2));
    }

    @Theory
    public void verifyThatDismissedIndividualNotificationIsExcludedWhenDismissed(NotificationType notificationType)
    {
        try
        {
            cache.setNotifications(notificationType, ImmutableList.of(LICENSE1_KEY, LICENSE2_KEY));
            cache.setNotificationDismissal(notificationType, USERNAME1, LICENSE1_KEY, true);
            assertThat(cache.getNotifications(notificationType, USERNAME1, true), contains(notification2));
        }
        finally
        {
            cache.setNotificationDismissal(notificationType, USERNAME1, LICENSE1_KEY, false);
        }
    }

    @Theory
    public void verifyThatDismissedIndividualNotificationHasDismissedAttributeEqualsTrue(NotificationType notificationType)
    {
        try
        {
            cache.setNotifications(notificationType, ImmutableList.of(LICENSE1_KEY));
            cache.setNotificationDismissal(notificationType, USERNAME1, LICENSE1_KEY, true);
            assertTrue(getOnlyElement(cache.getNotifications(notificationType, USERNAME1, false)).getDismissedState().isDismissed());
        }
        finally
        {
            cache.setNotificationDismissal(notificationType, USERNAME1, LICENSE1_KEY, false);
        }
    }

    @Theory
    public void verifyThatNonDismissedIndividualNotificationHasDismissedAttributeEqualsFalse(NotificationType notificationType)
    {
        cache.setNotifications(notificationType, ImmutableList.of(LICENSE1_KEY));
        assertFalse(getOnlyElement(cache.getNotifications(notificationType, USERNAME1, false)).getDismissedState().isDismissed());
    }

    @Theory
    public void verifyThatNonDismissedNotificationHasCascadedDismissalStateFromDismissedNotificationType(NotificationType notificationType)
    {
        //the individual notification hasn't been dismissed, but the parent notification type has.
        //therefore, the produced notification should have a dismissed state and can be filtered as such.
        cache.setNotifications(notificationType, ImmutableList.of(LICENSE1_KEY));
        cache.setNotificationTypeDismissal(notificationType, USERNAME1, true);
        assertTrue(getOnlyElement(cache.getNotifications(notificationType, USERNAME1, false)).getDismissedState().isDismissed());
    }
}
