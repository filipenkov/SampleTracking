package com.atlassian.crowd.embedded.admin.crowd;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public final class CrowdDirectoryConfigurationValidator implements Validator
{
    public boolean supports(Class clazz)
    {
        return CrowdDirectoryConfiguration.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors)
    {
        CrowdDirectoryConfiguration configuration = (CrowdDirectoryConfiguration) target;
        if (configuration.getCrowdServerSynchroniseIntervalInMin() < 1)
        {
            errors.rejectValue("crowdServerSynchroniseIntervalInMin", "invalid");
        }
    }
}
