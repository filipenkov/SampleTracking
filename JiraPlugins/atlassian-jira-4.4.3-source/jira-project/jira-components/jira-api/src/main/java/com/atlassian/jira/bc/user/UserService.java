package com.atlassian.jira.bc.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.user.ImmutableException;

/**
 * UserService provides User manipulation methods exposed for remote API and actions.
 *
 * @since v4.0
 */
public interface UserService
{
    /**
     * Validates creating a user during setup of JIRA or during public signup.  This method checks that external user
     * management is disabled.  It also validates that all parameters (username, email, fullname, password) have been
     * provided.  Email is also checked to ensure that it is a valid email address.  The username is required to be
     * lowercase characters only and unique. The confirmPassword has to match the password provided.
     * <p/>
     * This validation differs from the 'ForAdminPasswordRequired' and 'ForAdmin' validations as follows: <ul> <li>Does
     * not require global admin rights</li> <li>The password is required</li> <ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.0
     */
    CreateUserValidationResult validateCreateUserForSignupOrSetup(User user, String username, String password,
            String confirmPassword, String email, String fullname);

    /**
     * Validates creating a user during setup of JIRA or during public signup.  This method checks that external user
     * management is disabled.  It also validates that all parameters (username, email, fullname, password) have been
     * provided.  Email is also checked to ensure that it is a valid email address.  The username is required to be
     * lowercase characters only and unique. The confirmPassword has to match the password provided.
     * <p/>
     * This validation differs from the 'ForAdminPasswordRequired' and 'ForAdmin' validations as follows: <ul> <li>Does
     * not require global admin rights</li> <li>The password is required</li> <ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.0
     */
    CreateUserValidationResult validateCreateUserForSignupOrSetup(com.opensymphony.user.User user, String username, String password,
            String confirmPassword, String email, String fullname);

    /**
     * Validates creating a user for RPC calls.  This method checks that external user management is disabled and that
     * the user performing the operation has global admin rights.  It also validates that all parameters (username,
     * email, fullname, password) have been provided.  Email is also checked to ensure that it is a valid email address.
     * The username is required to be lowercase characters only and unique. The confirmPassword has to match the
     * password provided.
     * <p/>
     * This validation differs from the 'ForSetup' and 'ForAdmin' validations as follows: <ul> <li>Does require global
     * admin rights</li> <li>The password is required</li> <ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.0
     */
    CreateUserValidationResult validateCreateUserForAdminPasswordRequired(User user, String username, String password, String confirmPassword,
            String email, String fullname);

    /**
     * Validates creating a user for RPC calls.  This method checks that external user management is disabled and that
     * the user performing the operation has global admin rights.  It also validates that all parameters (username,
     * email, fullname, password) have been provided.  Email is also checked to ensure that it is a valid email address.
     * The username is required to be lowercase characters only and unique. The confirmPassword has to match the
     * password provided.
     * <p/>
     * This validation differs from the 'ForSetup' and 'ForAdmin' validations as follows: <ul> <li>Does require global
     * admin rights</li> <li>The password is required</li> <ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.0
     */
    CreateUserValidationResult validateCreateUserForAdminPasswordRequired(com.opensymphony.user.User user, String username, String password, String confirmPassword,
            String email, String fullname);

    /**
     * Validates creating a user for the admin section.  This method checks that external user management is disabled
     * and that the user performing the operation has global admin rights.  It also validates that all parameters
     * (username, email, fullname) except for the password have been provided.  Email is also checked to ensure that it
     * is a valid email address.  The username is required to be lowercase characters only and unique. The
     * confirmPassword has to match the password provided.
     * <p/>
     * This validation differs from the 'ForSetup' and 'ForAdminPasswordRequired' validations as follows: <ul> <li>Does
     * require global admin rights</li> <li>The password is NOT required</li> </ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.3
     */
    CreateUserValidationResult validateCreateUserForAdmin(User user, String username, String password, String confirmPassword,
            String email, String fullname);

    /**
     * Validates creating a user for the admin section.  This method checks that external user management is disabled
     * and that the user performing the operation has global admin rights.  It also validates that all parameters
     * (username, email, fullname) except for the password have been provided.  Email is also checked to ensure that it
     * is a valid email address.  The username is required to be lowercase characters only and unique. The
     * confirmPassword has to match the password provided.
     * <p/>
     * This validation differs from the 'ForSetup' and 'ForAdminPasswordRequired' validations as follows: <ul> <li>Does
     * require global admin rights</li> <li>The password is NOT required</li> </ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.0
     */
    CreateUserValidationResult validateCreateUserForAdmin(com.opensymphony.user.User user, String username, String password, String confirmPassword,
            String email, String fullname);

    /**
     * Validates creating a user for the admin section.  This method checks that external user management is disabled
     * and that the user performing the operation has global admin rights.  It also validates that all parameters
     * (username, email, fullname) except for the password have been provided.  Email is also checked to ensure that it
     * is a valid email address.  The username is required to be lowercase characters only and unique. The
     * confirmPassword has to match the password provided.
     * <p/>
     * This method allows the caller to name a directory to create the user in and the directoryId must be valid and
     * represent a Directory with "create user" permission.
     * <p/>
     * This validation differs from the 'ForSetup' and 'ForAdminPasswordRequired' validations as follows: <ul> <li>Does
     * require global admin rights</li> <li>The password is NOT required</li> </ul>
     *
     * @param user The remote user trying to add a new user
     * @param username The username of the new user. Needs to be lowercase and unique.
     * @param password The password for the new user.
     * @param confirmPassword The password confirmation.  Needs to match password.
     * @param email The email for the new user.  Needs to be a valid email address.
     * @param fullname The full name for the new user
     * @return a validation result containing appropriate errors or the new user's details
     * @since 4.3.2
     */
    CreateUserValidationResult validateCreateUserForAdmin(User user, String username, String password, String confirmPassword,
            String email, String fullname, Long directoryId);

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * Email notification will be send to created user - via UserEventType.USER_SIGNUP event.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.0
     *
     * @deprecated Please use {@link #createUserFromSignup(com.atlassian.jira.bc.user.UserService.CreateUserValidationResult)} instead. Since v4.3
     */
    com.opensymphony.user.User createUserForSignup(CreateUserValidationResult result)
            throws ImmutableException;

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * Email notification will be send to created user - via UserEventType.USER_SIGNUP event.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.3
     */
    public User createUserFromSignup(final CreateUserValidationResult result)
            throws PermissionException, CreateException;

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * Email notification will be send to created user - via UserEventType.USER_CREATED event.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.0
     *
     * @deprecated Please use {@link #createUserWithNotification(com.atlassian.jira.bc.user.UserService.CreateUserValidationResult)} instead. Since v4.3
     */
    com.opensymphony.user.User createUser(CreateUserValidationResult result)
            throws ImmutableException;

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * Email notification will be send to created user - via UserEventType.USER_CREATED event.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.3
     */
    User createUserWithNotification(CreateUserValidationResult result)
            throws PermissionException, CreateException;

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * No email notification will be send to created user.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.0
     *
     * @deprecated Please use {@link #createUserNoNotification(com.atlassian.jira.bc.user.UserService.CreateUserValidationResult)} instead. Since v4.3
     */
    com.opensymphony.user.User createUserNoEvent(CreateUserValidationResult result)
            throws ImmutableException;

    /**
     * Given a valid validation result, this will create the user using the details provided in the validation result.
     * No email notification will be send to created user.
     *
     * @param result The validation result
     * @return The new user object that was created
     * @since 4.3
     */
    User createUserNoNotification(CreateUserValidationResult result)
            throws PermissionException, CreateException;

    /**
     * Validates removing a user for the admin section.  This method checks that external user management is disabled
     * and that the user performing the operation has global admin rights.  It also validates that username have been
     * provided. Removing the user is not allowed if: <ul> <li>User is trying to remove himself</li> <li>User has any
     * issue assigned</li> <li>User reported any issue</li> <li>User is any project lead</li> <li>User performing
     * operation does not have SYSTEM_ADMIN rights and is trying to remove user having them </ul>
     * <p/>
     *
     * @param loggedInUser The remote user trying to remove an user
     * @param username The username of the user to remove. Needs to be valid
     * @return a validation result containing appropriate errors or the user object for delete
     * @since 4.0
     */
    DeleteUserValidationResult validateDeleteUser(final User loggedInUser, final String username);

    /**
     * Validates removing a user for the admin section.  This method checks that external user management is disabled
     * and that the user performing the operation has global admin rights.  It also validates that username have been
     * provided. Removing the user is not allowed if: <ul> <li>User is trying to remove himself</li> <li>User has any
     * issue assigned</li> <li>User reported any issue</li> <li>User is any project lead</li> <li>User performing
     * operation does not have SYSTEM_ADMIN rights and is trying to remove user having them </ul>
     * <p/>
     *
     * @param loggedInUser The remote user trying to remove an user
     * @param username The username of the user to remove. Needs to be valid
     * @return a validation result containing appropriate errors or the user object for delete
     * @since 4.0
     */
    DeleteUserValidationResult validateDeleteUser(final com.opensymphony.user.User loggedInUser, final String username);

    /**
     * Given a valid validation result, this will remove the user and removes the user from all the groups. All
     * components lead by user will have lead cleared.
     *
     * @param user the user to delete
     * @param result The validation result
     */
    void removeUser(User user, final DeleteUserValidationResult result);

    /**
     * Given a valid validation result, this will remove the user and removes the user from all the groups. All
     * components lead by user will have lead cleared.
     *
     * @param user the user to delete
     * @param result The validation result
     */
    void removeUser(com.opensymphony.user.User user, final DeleteUserValidationResult result);

    static final class CreateUserValidationResult extends ServiceResultImpl
    {
        private final String username;
        private final String password;
        private final String email;
        private final String fullname;
        private final Long directoryId;

        CreateUserValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            username = null;
            password = null;
            email = null;
            fullname = null;
            this.directoryId = null;
        }

        CreateUserValidationResult(final String username,
                final String password, final String email, final String fullname)
        {
            super(new SimpleErrorCollection());
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullname = fullname;
            this.directoryId = null;
        }

        CreateUserValidationResult(final String username,
                final String password, final String email, final String fullname, Long directoryId)
        {
            super(new SimpleErrorCollection());
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullname = fullname;
            this.directoryId = directoryId;
        }

        public String getUsername()
        {
            return username;
        }

        public String getPassword()
        {
            return password;
        }

        public String getEmail()
        {
            return email;
        }

        public String getFullname()
        {
            return fullname;
        }

        public Long getDirectoryId()
        {
            return directoryId;
        }
    }

    static final class DeleteUserValidationResult extends ServiceResultImpl
    {
        private final User user;

        DeleteUserValidationResult(ErrorCollection errorCollection)
        {
            super(errorCollection);
            user = null;
        }

        DeleteUserValidationResult(final User user)
        {
            super(new SimpleErrorCollection());
            this.user = user;

        }

        public User getUser()
        {
            return user;
        }
    }

    static final class FieldName
    {
        private FieldName()
        {
        }

        /**
         * The default name of HTML fields containing a User's email. Validation methods on this service will return an
         * {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed to this field name.
         */
        static String EMAIL = "email";
        /**
         * The default name of HTML fields containing a User's username. Validation methods on this service will return
         * an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed to this field name.
         */
        static String NAME = "username";
        /**
         * The default name of HTML fields containing a User's full name. Validation methods on this service will return
         * an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed to this field name.
         */
        static String FULLNAME = "fullname";
        /**
         * The default name of HTML fields containing a User's password. Validation methods on this service will return
         * an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed to this field name.
         */
        static String PASSWORD = "password";
        /**
         * The default name of HTML fields containing a User's password confirmation. Validation methods on this service
         * will return an {@link com.atlassian.jira.util.ErrorCollection} with error messages keyed to this field name.
         */
        static String CONFIRM_PASSWORD = "confirm";
    }    
}
