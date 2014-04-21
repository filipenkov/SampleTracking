package com.atlassian.crowd.manager.authentication;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationAccessDeniedException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;

public interface TokenAuthenticationManager
{
    /**
     * Authenticates an application and generates an authentication token.
     *
     * @param authenticationContext application authentication credentials.
     * @return generated authentication token.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException
     *          authentication was not successful because either the application does not exist, the password is incorrect, the application is inactive or there was a problem generating the authentication token.
     */
    Token authenticateApplication(ApplicationAuthenticationContext authenticationContext) throws InvalidAuthenticationException;

    /**
     * Authenticates a user and and generates an authentication token. The password of the user is validated before generating a token.
     * <p/>
     * The {@link com.atlassian.crowd.directory.RemoteDirectory#authenticate(String, com.atlassian.crowd.embedded.api.PasswordCredential)} method is
     * iteratively called for each assigned directory. If the user does not exist in one directory, the directory is skipped and the next one is examined. If the user does
     * not exist in any of the assigned directories then an {@link com.atlassian.crowd.exception.InvalidAuthenticationException} is thrown.
     *
     * @param authenticateContext The authentication details for the user.
     * @return The authenticated token for the user.
     * @throws InvalidAuthenticationException The authentication was not successful.
     * @throws OperationFailedException         error thrown by directory implementation when attempting to find or authenticate the user.
     * @throws InactiveAccountException         user account is inactive.
     * @throws ApplicationAccessDeniedException user does not have access to authenticate with application.
     * @throws ExpiredCredentialException       the user's credentials have expired. The user must change their credentials in order to successfully authenticate.
     * @throws ApplicationNotFoundException     if the application could not be found
     */
    Token authenticateUser(UserAuthenticationContext authenticateContext)
            throws InvalidAuthenticationException, OperationFailedException, InactiveAccountException, ApplicationAccessDeniedException, ExpiredCredentialException, ApplicationNotFoundException;

    /**
     * Feigns the authentication process for a user and creates a token for the authentication <b>without</b> validating the password.
     * <p/>
     * This method only be used to generate a token for a user that has already authenticated credentials via
     * some other means (eg. SharePoint NTLM connector) as this method bypasses any password checks.
     * <p/>
     * If you want actual password authentication, use the {@link #authenticateUser(com.atlassian.crowd.model.authentication.UserAuthenticationContext)} method.
     *
     * @param authenticateContext The authentication details for the user.
     * @return The authenticated token for the user.
     * @throws InvalidAuthenticationException   if the authentication was not successful.
     * @throws OperationFailedException         if the error thrown by directory implementation when attempting to find or authenticate the user.
     * @throws InactiveAccountException         if the user account is inactive.
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate with application.
     * @throws ApplicationNotFoundException     if the application could not be found
     */
    Token authenticateUserWithoutValidatingPassword(UserAuthenticationContext authenticateContext)
            throws InvalidAuthenticationException, OperationFailedException, InactiveAccountException, ApplicationAccessDeniedException, ApplicationNotFoundException;

    /**
     * Validates an application token key given validation factors.
     *
     * @param tokenKey          returns a valid token corresponding to the tokenKey.
     * @param validationFactors validation factors for generating the token hash.
     * @return validated token.
     * @throws InvalidTokenException if the tokenKey or corresponding client validation factors do not represent a valid application token.
     */
    Token validateApplicationToken(String tokenKey, ValidationFactor[] validationFactors) throws InvalidTokenException;

    /**
     * Validates a user token key given validation factors and checks that the user is allowed to authenticate
     * with the specified application
     *
     * @param userTokenKey      returns a valid token corresponding to the tokenKey.
     * @param validationFactors validation factors for generating the token hash.
     * @param application       name of application to authenticate with.
     * @return validated authentication token.
     * @throws InvalidTokenException    if the userTokenKey or corresponding validationFactors do not represent a valid SSO token.
     * @throws com.atlassian.crowd.exception.OperationFailedException there was an error communicating with an underlying directory when determining if a user is allowed to authenticate with the application (eg. if a user has the appropriate group memberships).
     * @throws ApplicationAccessDeniedException
     *                                  the user is not allowed to authenticate with the application.
     */
    Token validateUserToken(String userTokenKey, ValidationFactor[] validationFactors, String application) throws InvalidTokenException, ApplicationAccessDeniedException, OperationFailedException;


    /**
     * Attempts to invalidate a Token based on the passed in Token key (random hash).
     * <p/>
     * If the token does not exist (ie. already invalidated) this method silently
     * returns. If an existing token is successfully invalidated, a
     * TokenInvalidatedEvent is fired.
     *
     * @param token the token key (random hash) to invalidate.
     */
    void invalidateToken(String token);

    /**
     * Returns a list of users matching the given query.
     *
     * @param query entity query for {@link com.atlassian.crowd.search.Entity#TOKEN}.
     * @return list of {@link com.atlassian.crowd.model.token.Token} matching the search criteria.
     */
    List<Token> searchTokens(EntityQuery<Token> query);

    /**
     * Removes all tokens that have exceeded their expiry time.
     * <p/>
     * NOTE: Do not call this method from the web layer, as this is wrapped in a Spring managed transaction.
     */
    void removeExpiredTokens();

    /**
     * Will find a user via the passed in token key.
     *
     * @param tokenKey the token key
     * @param applicationName name of the current application
     * @return the User associated to the given token key
     * @throws InvalidTokenException
     *          if the User or Directory cannot be found that relates to the given token,
     *          or the token is associated to an Application and not a User
     * @throws OperationFailedException if there was an issue accessing the user from the underlying directory
     * @throws ApplicationNotFoundException if the application could not be found
     * @throws ApplicationAccessDeniedException
     *                                  the user is not allowed to authenticate with the application.
     */
    User findUserByToken(String tokenKey, String applicationName)
            throws InvalidTokenException, OperationFailedException, ApplicationNotFoundException, ApplicationAccessDeniedException;

    /**
     * Returns a list of applications a user
     * is authorised to authenticate with.
     * <p/>
     * NOTE: this is a potentially expensive call, iterating
     * all applications and all group mappings for
     * each application and determining group membership,
     * ie. expense = number of applications * number of group mappings per application.
     *
     * @param user user to search for.
     * @param applicationName name of the current application
     * @return list of applications.
     * @throws OperationFailedException if there was an error querying directory.
     * @throws DirectoryNotFoundException if the directory could not be found.
     * @throws ApplicationNotFoundException if the application could not be found
     */
    List<Application> findAuthorisedApplications(User user, String applicationName)
            throws OperationFailedException, DirectoryNotFoundException, ApplicationNotFoundException;
}
