package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.notification.NotificationRecipient;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Converts {@link com.atlassian.crowd.embedded.api.User} to {@link com.atlassian.jira.notification.NotificationRecipient}.
 *
 * @since v4.4
 */
public class UserToRecipient implements Function<User,NotificationRecipient>
{
    public static final UserToRecipient INSTANCE = new UserToRecipient();

    @Override
    public NotificationRecipient apply(@Nullable User user)
    {
        return new NotificationRecipient(user);
    }
}
