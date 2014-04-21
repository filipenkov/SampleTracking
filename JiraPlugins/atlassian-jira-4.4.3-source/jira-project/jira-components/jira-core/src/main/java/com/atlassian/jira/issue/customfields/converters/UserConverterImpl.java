package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.user.util.UserUtil;
import org.apache.commons.lang.StringUtils;

public class UserConverterImpl implements UserConverter
{
    private final UserUtil userUtil;

    public UserConverterImpl(UserUtil userUtil)
    {
        this.userUtil = userUtil;
    }

    public String getString(User user)
    {
        if (user == null)
        {
            return "";
        }
        return user.getName();
    }

    @Override
    public String getString(com.opensymphony.user.User user)
    {
        return getString((User) user);
    }

    public com.opensymphony.user.User getUser(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
        {
            return null;
        }
        com.opensymphony.user.User user = userUtil.getUser(stringValue);
        if (user == null)
        {
            // For backward compatibility
            throw new FieldValidationException("User '" + stringValue + "' was not found in the system.");
        }
        return user;
    }

    @Override
    public User getUserObject(String stringValue) throws FieldValidationException
    {
        return getUser(stringValue);
    }

}
