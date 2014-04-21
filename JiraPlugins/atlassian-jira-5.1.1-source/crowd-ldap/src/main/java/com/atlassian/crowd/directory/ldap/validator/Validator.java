package com.atlassian.crowd.directory.ldap.validator;

import com.atlassian.crowd.embedded.api.Directory;

public interface Validator
{
    /**
     * Validates a specific directory configuration.
     * @param directory directory object.
     * @return error message or <code>null</code> if there is no error in the configuration.
     */
    String getError(Directory directory);
}
