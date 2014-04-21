package com.atlassian.crowd.directory.ldap.validator;

import com.atlassian.crowd.embedded.api.Directory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aggregates errors for the list of validators
 */
public class ConnectorValidator
{
    private final List<Validator> validators;

    public ConnectorValidator(List<Validator> validators)
    {
        this.validators = validators;
    }

    /**
     * Gets the errors detected by the list of validators for the specified directory
     * @param directory a directory object.
     * @return a list of aggregated errors or an empty list if there are no errors. Never returns null
     */
    public Set<String> getErrors(Directory directory)
    {
        Set<String> errors = new HashSet<String>();
        for (Validator validator : validators)
        {
            String error = validator.getError(directory);
            if (error != null)
            {
                errors.add(error);
            }
        }
        return errors;
    }        
}
