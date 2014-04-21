package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.plugin.rest.entity.PasswordEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.plugins.rest.common.Link;

import java.net.URI;

/**
 * User authentication controller.
 */
public class AuthenticationController extends AbstractResourceController
{
    public AuthenticationController(final ApplicationService applicationService, final ApplicationManager applicationManager)
    {
        super(applicationService, applicationManager);
    }

    /**
     * Authenticates the user with the given <code>username</code> and <code>password</code>. Does not generate a token.
     *
     * @param applicationName name of the application
     * @param username username to authenticate the user
     * @param password password to authenticate the user
     * @param baseUri Base URI of the REST service
     * @return UserEntity if the authentication succeeded
     * @throws ExpiredCredentialException if the user's credentials have expired.
     * @throws InactiveAccountException if the user's account is inactive.
     * @throws InvalidAuthenticationException if the authentication details provided are not valid, or if the user does not exist.
     * @throws OperationFailedException if the underlying directory implementation failed to execute the operation.
     */
    public UserEntity authenticateUser(final String applicationName, final String username, final PasswordEntity password, final URI baseUri)
            throws UserNotFoundException, ExpiredCredentialException, InactiveAccountException, InvalidAuthenticationException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        User user = applicationService.authenticateUser(application, username, PasswordCredential.unencrypted(password.getValue()));
        if (!applicationService.isUserAuthorised(application, username))
        {
            throw new InvalidAuthenticationException("User is not allowed to authenticate with the application");
        }
        Link userLink = LinkUriHelper.buildUserLink(baseUri, user.getName());
        UserEntity userEntity = EntityTranslator.toUserEntity(user, userLink);
        return userEntity;
    }
}
