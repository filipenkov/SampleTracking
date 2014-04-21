package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;

/**
 * Returned email address appropriately masked/hidden for the current user.
 */
public interface EmailFormatter
{
    /**
     * Is Email visible.
     * @param user
     * @return true if emnail should be visible
     * @since v4.3
     */
    boolean emailVisible(User user);

    /**
     * Is Email visible.
     * @param user
     * @return  true if emnail should be visible
     * @deprecated use {@link #emailVisible(User)}. Since v4.3
     */
    boolean emailVisible(com.opensymphony.user.User user);

    /**
     * Formats how the <em>user</em>'s Email address is to be displayed to the <em>currentUser</em>
     * determined by the <em>user email visibility</em> value
     * @param user owner of the email address to format and display
     * @param currentUser the current logged in user viewing the email address of the above user
     * @return String - formatted email address, or null if currentUser is not allowed to view it
     * @since v4.3
     */
    String formatEmail(User user, User currentUser);

    /**
     * Formats how the <em>user</em>'s Email address is to be displayed to the <em>currentUser</em>
     * determined by the <em>user email visibility</em> value
     * @param user owner of the email address to format and display
     * @param currentUser the current logged in user viewing the email address of the above user
     * @return String - formatted email address, or null if currentUser is not allowed to view it
     * @deprecated use {@link #formatEmail(User, User)}. Since v4.3
     */
    String formatEmail(com.opensymphony.user.User user, com.opensymphony.user.User currentUser);

    /**
     * Potentially hide/mask an email address.
     *
     * @param email The email address to show/mask/hide.
     * @param isCurrentUserLoggedIn is the current user logged in or anonymous
     * @return String - formatted email address, or null if currentUser is not allowed to view it
     */
    String formatEmail(String email, boolean isCurrentUserLoggedIn);

    /**
     * Potentially hide/mask an email address.
     * 
     * @param email The email address to show/mask/hide.
     * @param currentUser The user viewing the email address.
     * @return The email address, possibly masked, or null if the user is not allowed to view it.
     * @since v4.3
     */
    String formatEmail(String email, User currentUser);

    /**
     * Potentially hide/mask an email address.
     * @param email The email address to show/mask/hide.
     * @param currentUser The user viewing the email address.
     * @return The email address, possibly masked, or null if the user is not allowed to view it.
     * @deprecated use {@link #formatEmail(String, User)}. Since v4.3
     */
    String formatEmail(String email, com.opensymphony.user.User currentUser);

    /**
     * Returns email address as HTML.
     * @return &lt;a href="foo@bar.com">foo@bar.com&lt;/a> (public),
     *  &lt;a href="foo at bar.com">foo at bar.com&lt;/a> (masked), or
     *  "" (hidden).
     * @since v4.3
     */
    String formatEmailAsLink(String email, User currentUser);

    /**
     * Returns email address as HTML.
     * @return &lt;a href="foo@bar.com">foo@bar.com&lt;/a> (public),
     *  &lt;a href="foo at bar.com">foo at bar.com&lt;/a> (masked), or
     *  "" (hidden).
     * @deprecated use {@link #formatEmailAsLink(String, User)}. Since v4.3
     */
    String formatEmailAsLink(String email, com.opensymphony.user.User currentUser);
}
