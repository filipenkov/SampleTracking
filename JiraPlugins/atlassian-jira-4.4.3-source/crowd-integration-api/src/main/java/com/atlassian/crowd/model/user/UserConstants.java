package com.atlassian.crowd.model.user;

/**
 * A simple class to hold the Constants that are used on a {@link com.atlassian.crowd.model.user.User}
 */
public class UserConstants
{
    /**
     * Key for the username value.
     */
    public static final String USERNAME = "username";

    /**
     * Key for the first name attribute.
     */
    public static final String FIRSTNAME = "givenName";

    /**
     * Key for the last name attribute.
     */
    public static final String LASTNAME = "sn";

    /**
     * Key for if the principal has a display name attribute.
     */
    public static final String DISPLAYNAME = "displayName";

    /**
     * Key for the email attribute.
     */
    public static final String EMAIL = "mail";

    /**
     * Key for the password last changed attribute.
     */
    public static final String PASSWORD_LASTCHANGED = "passwordLastChanged";

    /**
     * Key for the last authentication.
     */
    public static final String LAST_AUTHENTICATED = "lastAuthenticated";

    /**
     * Key for the total invalid password attempts.
     */
    public static final String INVALID_PASSWORD_ATTEMPTS = "invalidPasswordAttempts";

    /**
     * Key for if the principal needs to change their password.
     */
    public static final String REQUIRES_PASSWORD_CHANGE = "requiresPasswordChange";

    /**
     * Key that represents if a user is active or inactive
     */
    public static final String ACTIVE = "active";
}
