package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationAccessDeniedException;
import com.atlassian.crowd.manager.authentication.TokenAuthenticationManager;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.plugin.rest.entity.SessionEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.entity.ValidationFactorEntity;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.Link;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Performs token authentication for the user.
 */
public class TokenController
{
    private final TokenAuthenticationManager tokenAuthenticationManager;

    public TokenController(TokenAuthenticationManager tokenAuthenticationManager)
    {
        this.tokenAuthenticationManager = tokenAuthenticationManager;
    }

    /**
     * Authenticates a user for the given application.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @param password password of the user
     * @param validationFactorEntities validation factors
     * @param baseUri base URI of the REST service
     * @return Crowd SSO Token if the user successfully authenticated.
     * @throws InvalidAuthenticationException if the authentication was not successful
     * @throws InactiveAccountException if the user account is marked as inactive
     * @throws ExpiredCredentialException if the user credential has expired and the user needs to set a new password
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate with the application
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public SessionEntity authenticateUser(String applicationName, String username, String password, Collection<ValidationFactorEntity> validationFactorEntities, URI baseUri)
            throws InvalidAuthenticationException, InactiveAccountException, ExpiredCredentialException, ApplicationAccessDeniedException, OperationFailedException
    {
        ValidationFactor[] validationFactors = convertToValidationFactors(validationFactorEntities);
        UserAuthenticationContext authenticationContext = new UserAuthenticationContext(username, PasswordCredential.unencrypted(password), validationFactors, applicationName);

        final Token token;
        try
        {
            token = tokenAuthenticationManager.authenticateUser(authenticationContext);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        return createSession(token, applicationName, baseUri);
    }

    /**
     * Feigns the authentication process for a user and creates a token for the authentication <b>without</b> validating the password.
     * <p/>
     * This method only be used to generate a token for a user that has already authenticated credentials via
     * some other means (eg. SharePoint NTLM connector) as this method bypasses any password checks.
     * <p/>
     * If you want actual password authentication, use the {@link #authenticateUser(String, String, String, java.util.Collection, java.net.URI)}  method.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @param validationFactorEntities validation factors
     * @param baseUri base URI of the REST service
     * @return Crowd SSO Token if the user successfully authenticated.
     * @throws InvalidAuthenticationException if the authentication was not successful
     * @throws InactiveAccountException if the user account is marked as inactive
     * @throws ExpiredCredentialException if the user credential has expired and the user needs to set a new password
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate with the application
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public SessionEntity authenticateUserWithoutValidatingPassword(String applicationName, String username, Collection<ValidationFactorEntity> validationFactorEntities, URI baseUri)
            throws InvalidAuthenticationException, InactiveAccountException, ExpiredCredentialException, ApplicationAccessDeniedException, OperationFailedException
    {
        ValidationFactor[] validationFactors = convertToValidationFactors(validationFactorEntities);
        UserAuthenticationContext authenticationContext = new UserAuthenticationContext(username, null, validationFactors, applicationName);

        final Token token;
        try
        {
            token = tokenAuthenticationManager.authenticateUserWithoutValidatingPassword(authenticationContext);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        return createSession(token, applicationName, baseUri);
    }

    /**
     * Invalidates a token.  If the token does not exist, the method will silently return. 
     *
     * @param token Token to invalidate
     */
    public void invalidateToken(String token)
    {
        tokenAuthenticationManager.invalidateToken(token);
    }

    /**
     * Validates a Crowd SSO token and creates a new token with an updated last accessed date (for the internal token representation).
     *
     * @param applicationName Name of the application
     * @param token Crowd SSO token
     * @param validationFactorEntities validation factors
     * @param baseUri base URI of the REST service
     * @return new token.
     * @throws InvalidTokenException if the token or validation factors are not valid.
     * @throws ApplicationAccessDeniedException if the user is not allowed to authenticate with the application.
     * @throws OperationFailedException if the application failed for any other reason.
     */
    public SessionEntity validateToken(String applicationName, String token, Collection<ValidationFactorEntity> validationFactorEntities, URI baseUri)
            throws InvalidTokenException, ApplicationAccessDeniedException, OperationFailedException
    {
        ValidationFactor[] validationFactors = convertToValidationFactors(validationFactorEntities);
        final Token validatedToken = tokenAuthenticationManager.validateUserToken(token, validationFactors, applicationName);

        return createSession(validatedToken, applicationName, baseUri);
    }

    /**
     * Returns the user associated with the Crowd SSO token.
     *
     * @param token Crowd SSO token
     * @param applicationName name of the current application
     * @param baseUri base URI of the REST service
     * @return User associated with the Crowd SSO token.
     * @throws InvalidTokenException if the token could not be found
     * @throws OperationFailedException if the operation failed for any other reason.
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate with the application
     */
    public UserEntity getUserFromToken(String token, String applicationName, URI baseUri) throws InvalidTokenException, OperationFailedException, ApplicationAccessDeniedException
    {
        final User user;
        try
        {
            user = tokenAuthenticationManager.findUserByToken(token, applicationName);
        }
        catch (ApplicationNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
        return EntityTranslator.toUserEntity(user, LinkUriHelper.buildUserLink(baseUri , user.getName()));
    }

    /**
     * Creates a new SessionEntity
     *
     * @param token token for the session
     * @param applicationName current application name
     * @param baseUri base URI of the REST service
     * @return new SessionEntity
     */
    private SessionEntity createSession(Token token, String applicationName, URI baseUri)
    {
        final String username = token.getName();
        final String randomHash  = token.getRandomHash();

        final Link userLink = LinkUriHelper.buildUserLink(baseUri, username);
        final UserEntity user = UserEntity.newMinimalUserEntity(username, applicationName, userLink);
        final Link link = LinkUriHelper.buildSessionLink(baseUri, randomHash);
        return new SessionEntity(randomHash, user, link);
    }

    private static ValidationFactor[] convertToValidationFactors(Collection<ValidationFactorEntity> validationFactorEntities)
    {
        List<ValidationFactor> validationFactors = new ArrayList<ValidationFactor>(validationFactorEntities.size());
        for (ValidationFactorEntity factor : validationFactorEntities)
        {
            validationFactors.add(new ValidationFactor(factor.getName(), factor.getValue()));
        }
        return validationFactors.toArray(new ValidationFactor[validationFactors.size()]);
    }
}
