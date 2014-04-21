package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Locale;

public class UserPickerStatisticsMapper extends AbstractCustomFieldStatisticsMapper
{
    private final UserManager userManager;
    private final JiraAuthenticationContext authenticationContext;

    public UserPickerStatisticsMapper(CustomField customField, UserManager userManager, JiraAuthenticationContext authenticationContext)
    {
        super(customField);
        this.userManager = userManager;
        this.authenticationContext = authenticationContext;
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        if (StringUtils.isBlank(documentValue))
        {
            return null;
        }
        else
        {
            return userManager.getUser(documentValue);
        }
    }

    public Comparator getComparator()
    {
        return new UserBestNameComparator(getLocale());
    }

    protected String getSearchValue(Object value)
    {
        User user = (User) value;
        return user.getName();
    }

    Locale getLocale()
    {
        return authenticationContext.getLocale();
    }
}
