package com.atlassian.crowd.manager.login;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.*;
import com.atlassian.crowd.manager.login.exception.InvalidResetPasswordTokenException;
import com.atlassian.crowd.model.application.Application;

/**
 * Manages functionality related to retrieving forgotten usernames or resetting forgotten passwords.
 *
 * <p>To reset a user's password, clients of {@link ForgottenLoginManager} would do the following:
 * <ol>
 *   <li><tt>sendResetLink</tt> sends the user a unique link to reset their password</li>
 *   <li><tt>resetUserCredential</tt> verifies that the reset token given by the user is correct using
 *       <tt>isValidResetToken</tt>, then resets if the user credentials if the token is valid.</li>
 * </ol>
 *
 * @since v2.1.0
 */
public interface ForgottenLoginManager
{
    /**
     * Sends a reset link to the first user with the matching <tt>username</tt> from all the active directories assigned
     * to the application.
     *
     * @param application user is searched in <tt>application</tt>'s assigned directories
     * @param username username of the user to send the password reset link
     * @throws UserNotFoundException if no user with the supplied username exists
     * @throws InvalidEmailAddressException if the user does not have a valid email address to send the password reset email to
     * @throws ApplicationPermissionException if the application does not have permission to modify the user
     */
    void sendResetLink(Application application, String username)
            throws UserNotFoundException, InvalidEmailAddressException, ApplicationPermissionException;

    /**
     * Sends the usernames associated with the given email address. No email will be sent if there are no usernames
     * associated with a given <code>email</code>.
     *
     * @param application search application's assigned directories for usernames associated with the <code>email</code>
     * @param email email address of the user
     * @throws InvalidEmailAddressException if the <code>email</code> is not valid
     */
    void sendUsernames(Application application, String email) throws InvalidEmailAddressException;

    /**
     * Sends a reset link to the user with specified username and directory ID.
     *
     * <p>Similar to {@link ForgottenLoginManager#sendResetLink(Application, String)} except applying to a directory-specific
     * user.
     *
     * @param directoryId directory ID of the user to modify
     * @param username username of the user to send the password reset link
     * @throws DirectoryNotFoundException if the directory specified by <tt>directoryId</tt> could not be found
     * @throws UserNotFoundException if the user specified by <tt>username</tt> could not be found
     * @throws InvalidEmailAddressException if the user does not have a valid email address to send the password reset email to
     */
    void sendResetLink(long directoryId, String username)
            throws DirectoryNotFoundException, UserNotFoundException, InvalidEmailAddressException, OperationFailedException;

    /**
     * Returns <tt>true</tt> if the password reset token for the user with the specified username and directory ID are
     * valid and not expired.  The valid password reset token is created by {@link ForgottenLoginManager#sendResetLink}.
     *
     * @param directoryId directory ID of the user to validate
     * @param username username of the user to verify the <tt>token</tt>
     * @param token password reset token
     * @return <tt>true</tt> if the username and reset token are a valid combination and the reset token has not expired.
     */
    boolean isValidResetToken(long directoryId, String username, String token);

    /**
     * Resets the user credentials and invalidates the token.
     *
     * @param directoryId directory ID of the user
     * @param username user name of the user to perform a credential reset
     * @param credential new credentials
     * @param token password reset token
     * @throws DirectoryNotFoundException if the directory could not be found.
     * @throws UserNotFoundException if the user could not be found in the given directory.
     * @throws InvalidResetPasswordTokenException if the reset token is not valid.
     * @throws OperationFailedException if there was an error performing the operation or instantiating the backend directory.
     * @throws InvalidCredentialException if the user's credential does not meet the validation requirements for an associated directory.
     * @throws DirectoryPermissionException if the directory is not allowed to perform the operation
     */
    void resetUserCredential(long directoryId, String username, PasswordCredential credential, String token)
            throws DirectoryNotFoundException, UserNotFoundException, InvalidResetPasswordTokenException, OperationFailedException, InvalidCredentialException, DirectoryPermissionException;
}
