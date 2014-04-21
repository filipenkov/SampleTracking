package com.atlassian.jira.bc;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Simple implementation of a validation result.
 *
 * Consider using {@link com.atlassian.jira.bc.ServiceOutcome}, which avoids to have to create a new class.
 *
 * @since v4.0
 */
public class ServiceResultImpl implements ServiceResult
{
    private final ErrorCollection errorCollection;

    public ServiceResultImpl(ErrorCollection errorCollection)
    {
        Assertions.notNull("errorCollection", errorCollection);
        this.errorCollection = errorCollection;
    }

    public boolean isValid()
    {
        return !errorCollection.hasAnyErrors();
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }
}