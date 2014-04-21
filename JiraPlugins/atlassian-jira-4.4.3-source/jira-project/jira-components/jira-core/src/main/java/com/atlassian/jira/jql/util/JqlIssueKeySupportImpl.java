package com.atlassian.jira.jql.util;

import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.jira.util.JiraKeyUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of the {@link JqlIssueKeySupport} interface.
 *
 * @since v4.0
 */
@InjectableComponent
public class JqlIssueKeySupportImpl implements JqlIssueKeySupport
{
    public boolean isValidIssueKey(final String issueKey)
    {
        return JiraKeyUtils.validIssueKey(issueKey);
    }

    public long parseKeyNum(final String issueKey)
    {
        if (StringUtils.isBlank(issueKey))
        {
            return -1;
        }
        else
        {
            return JiraKeyUtils.getFastCountFromKey(issueKey);
        }
    }

    public String parseProjectKey(final String issueKey)
    {
        if (StringUtils.isBlank(issueKey))
        {
            return null;
        }
        else
        {
            return JiraKeyUtils.getProjectKeyFromIssueKey(issueKey);
        }
    }
}
