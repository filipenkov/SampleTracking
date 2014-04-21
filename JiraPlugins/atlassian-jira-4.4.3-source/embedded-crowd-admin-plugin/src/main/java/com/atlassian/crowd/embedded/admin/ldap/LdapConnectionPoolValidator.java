package com.atlassian.crowd.embedded.admin.ldap;

import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.impl.ConnectionPoolPropertyUtil;
import com.google.common.collect.ImmutableList;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

public class LdapConnectionPoolValidator implements Validator
{
    public boolean supports(Class clazz)
    {
        return ConnectionPoolProperties.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors)
    {
        ConnectionPoolProperties configuration = (ConnectionPoolProperties) target;
        if (!ConnectionPoolPropertyUtil.isValidProtocol(configuration.getSupportedProtocol()))
        {
            errors.rejectValue("supportedProtocol", "invalid");
        }

        if (!ConnectionPoolPropertyUtil.isValidAuthentication(configuration.getSupportedAuthentication()))
        {
            errors.rejectValue("supportedAuthentication", "invalid");
        }
    }

    private boolean isValidEntry(String userInput, Set<String> validValues)
    {
        // We expect the input values to be space separated
        return validValues.containsAll(ImmutableList.of(userInput.split(" ")));
    }
}
