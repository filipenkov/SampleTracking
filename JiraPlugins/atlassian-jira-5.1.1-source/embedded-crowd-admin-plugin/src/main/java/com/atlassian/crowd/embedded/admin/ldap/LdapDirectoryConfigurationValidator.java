package com.atlassian.crowd.embedded.admin.ldap;

import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public final class LdapDirectoryConfigurationValidator implements Validator
{
    public boolean supports(Class clazz)
    {
        return LdapDirectoryConfiguration.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors)
    {
        LdapDirectoryConfiguration configuration = (LdapDirectoryConfiguration) target;
        if (NumberUtils.toLong(configuration.getLdapCacheSynchroniseIntervalInMin()) < 1)
        {
            errors.rejectValue("ldapCacheSynchroniseIntervalInMin", "invalid");
        }
        if (configuration.getLdapAutoAddGroups().indexOf(DirectoryImpl.AUTO_ADD_GROUPS_SEPARATOR) != -1)
        {
            errors.rejectValue("ldapAutoAddGroups", "invalid");
        }
//        if (configuration.isExistingConfiguration())
//            ValidationUtils.rejectIfEmpty(errors, "type", "required");
    }
}
