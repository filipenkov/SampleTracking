package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;

import javax.annotation.Nullable;

/**
 * Contains utility methods for getting an {@link ApplicationUser} from a directory {@link User}.
 *
 * @since v5.1.1
 */
public final class ApplicationUsers
{
    /**
     * Obtains an ApplicationUser for the given directory User.
     *
     * @param user the directory User
     * @return the Application User or null if the incoming user is null
     */
    public static ApplicationUser from(@Nullable final User user)
    {
        if (user == null)
        {
            return null;
        }

        if (user instanceof ApplicationUser)
        {
            return (ApplicationUser) user;
        }

        return ComponentAccessor.getUserManager().getUserByName(user.getName());
    }

    /**
     * Gets the user key for the given directory User.
     * <p>
     * This is a null-safe shorthand for
     * <pre>  ApplicationUsers.from(user).getKey()</pre>
     *
     * @param user the directory User
     * @return the application user Key for the given directory User or null if the incoming user is null
     */
    public static String getKeyFor(@Nullable final User user)
    {
        ApplicationUser applicationUser = from(user);
        if (applicationUser == null)
            return null;
        else
            return applicationUser.getKey();
    }
}