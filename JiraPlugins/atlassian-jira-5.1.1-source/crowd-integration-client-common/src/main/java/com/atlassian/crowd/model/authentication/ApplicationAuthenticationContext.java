/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.model.authentication;

import com.atlassian.crowd.embedded.api.PasswordCredential;

/**
 * The <code>ApplicationAuthenticationContext</code> is used by authenticating
 * {@link com.atlassian.crowd.model.application.Application applications}.
 *
 * @author Justen Stepka <jstepka@atlassian.com>
 * @version 1.0
 */
public class ApplicationAuthenticationContext extends AuthenticationContext 
{
    public ApplicationAuthenticationContext()
    {
    }

    public ApplicationAuthenticationContext(String name, PasswordCredential credential, ValidationFactor[] validationFactors)
    {
        super(name, credential, validationFactors);
    }
}