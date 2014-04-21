package com.atlassian.crowd.manager.application;

/**
 * Thrown if user does not have access to a particular
 * application and attempts to authenticate against it.
 */
public class ApplicationAccessDeniedException extends Exception
{
    public ApplicationAccessDeniedException(final String application)
    {
        super("User does not have access to application <" + application + ">");
    }
}
