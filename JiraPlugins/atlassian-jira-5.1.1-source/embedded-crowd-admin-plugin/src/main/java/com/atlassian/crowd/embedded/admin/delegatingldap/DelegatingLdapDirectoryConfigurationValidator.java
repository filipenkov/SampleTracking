package com.atlassian.crowd.embedded.admin.delegatingldap;

import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public final class DelegatingLdapDirectoryConfigurationValidator implements Validator
{
    public boolean supports(Class clazz)
    {
        return DelegatingLdapDirectoryConfiguration.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors)
    {
        final DelegatingLdapDirectoryConfiguration configuration = (DelegatingLdapDirectoryConfiguration) target;
        if (configuration.getLdapAutoAddGroups().indexOf(DirectoryImpl.AUTO_ADD_GROUPS_SEPARATOR) != -1)
        {
            errors.rejectValue("ldapAutoAddGroups", "invalid");
        }
        if (configuration.isCreateUserOnAuth())
        {
            validateCreateUserOnAuthFields(configuration, errors);

            if (configuration.isSynchroniseGroupMemberships())
            {
                validateSynchroniseGroupMembershipsFields(configuration, errors);
            }
        }
    }

    private void validateSynchroniseGroupMembershipsFields(DelegatingLdapDirectoryConfiguration configuration, Errors errors)
    {
        if (configuration.getLdapGroupObjectclass().isEmpty())
        {
            errors.rejectValue("ldapGroupObjectclass", "required");
        }
        if (configuration.getLdapGroupFilter().isEmpty())
        {
            errors.rejectValue("ldapGroupFilter", "required");
        }
        if (configuration.getLdapGroupName().isEmpty())
        {
            errors.rejectValue("ldapGroupName", "required");
        }
        if (configuration.getLdapGroupDescription().isEmpty())
        {
            errors.rejectValue("ldapGroupDescription", "required");
        }
        if (configuration.getLdapGroupUsernames().isEmpty())
        {
            errors.rejectValue("ldapGroupUsernames", "required");
        }
        if (configuration.getLdapUserGroup().isEmpty())
        {
            errors.rejectValue("ldapUserGroup", "required");
        }
    }

    private void validateCreateUserOnAuthFields(DelegatingLdapDirectoryConfiguration configuration, Errors errors)
    {
        if (configuration.getLdapUserObjectclass().isEmpty())
        {
            errors.rejectValue("ldapUserObjectclass", "required");
        }
        if (configuration.getLdapUserFilter().isEmpty())
        {
            errors.rejectValue("ldapUserFilter", "required");
        }
        if (configuration.getLdapUserUsername().isEmpty())
        {
            errors.rejectValue("ldapUserUsername", "required");
        }
        if (configuration.getLdapUserUsernameRdn().isEmpty())
        {
            errors.rejectValue("ldapUserUsernameRdn", "required");
        }
        if (configuration.getLdapUserFirstname().isEmpty())
        {
            errors.rejectValue("ldapUserFirstname", "required");
        }
        if (configuration.getLdapUserLastname().isEmpty())
        {
            errors.rejectValue("ldapUserLastname", "required");
        }
        if (configuration.getLdapUserDisplayname().isEmpty())
        {
            errors.rejectValue("ldapUserDisplayname", "required");
        }
        if (configuration.getLdapUserEmail().isEmpty())
        {
            errors.rejectValue("ldapUserEmail", "required");
        }
    }
}