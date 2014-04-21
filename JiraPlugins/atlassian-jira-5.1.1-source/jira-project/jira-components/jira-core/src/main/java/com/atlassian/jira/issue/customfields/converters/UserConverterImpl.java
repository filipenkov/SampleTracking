package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.user.util.UserManager;
import org.apache.commons.lang.StringUtils;

public class UserConverterImpl implements UserConverter
{
    private final UserManager userManager;

    public UserConverterImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public String getString(User user)
    {
        if (user == null)
        {
            return "";
        }
        return user.getName();
    }

    public User getUser(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        User user = userManager.getUser(stringValue);
        if (user == null)
        {
            // For backward compatibility
            throw new FieldValidationException("User '" + stringValue + "' was not found in the system.");
        }
        return user;
    }

    @Override
    public User getUserEvenWhenUnknown(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        return userManager.getUserEvenWhenUnknown(stringValue);
    }

    @Override
    public User getUserObject(String stringValue) throws FieldValidationException
    {
        return getUser(stringValue);
    }

}
