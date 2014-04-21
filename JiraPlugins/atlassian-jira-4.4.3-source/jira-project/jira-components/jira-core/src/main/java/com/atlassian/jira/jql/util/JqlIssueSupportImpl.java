package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.CaseFolding;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link JqlIssueSupport}.
 *
 * @since v4.0
 */
public class JqlIssueSupportImpl implements JqlIssueSupport
{
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;

    public JqlIssueSupportImpl(final IssueManager issueManager, final PermissionManager permissionManager)
    {
        this.issueManager = notNull("issueManager", issueManager);
        this.permissionManager = notNull("permissionManager", permissionManager);
    }

    public Issue getIssue(final long id, final User user)
    {
        return checkPermission(user, issueManager.getIssueObject(id));
    }

    @Override
    public Issue getIssue(long id, com.opensymphony.user.User user)
    {
        return getIssue(id, (User) user);
    }

    public Issue getIssue(final long id)
    {
        return issueManager.getIssueObject(id);
    }

    public List<Issue> getIssues(final String issueKey, final User user)
    {
        return getIssues(issueKey, user, false);
    }

    @Override
    public List<Issue> getIssues(String issueKey, com.opensymphony.user.User user)
    {
        return getIssues(issueKey, (User) user);
    }

    public List<Issue> getIssues(final String issueKey)
    {
        return getIssues(issueKey, null, true);
    }

    List<Issue> getIssues(final String issueKey, final User user, final boolean skipPermissionCheck)
    {
        if (StringUtils.isBlank(issueKey))
        {
            return Collections.emptyList();
        }

        //Here to make sure we return unique issues.
        final Map<Long, Issue> issueMap = new HashMap<Long, Issue>();

        for (String key : keySearchNames(issueKey))
        {
            final Issue issue = issueManager.getIssueObject(key);
            if (issue != null)
            {
                if (!issueMap.containsKey(issue.getId()) && (skipPermissionCheck || permissionManager.hasPermission(Permissions.BROWSE, issue, user)))
                {
                    issueMap.put(issue.getId(), issue);
                }
            }
        }

        return new ArrayList<Issue>(issueMap.values());
    }

    private Issue checkPermission(final User user, final Issue issue)
    {
        if (issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            return issue;
        }
        else
        {
            return null;
        }
    }

    private static Set<String> keySearchNames(final String key)
    {
        final String upper = key.toUpperCase(Locale.ENGLISH);
        final String lower = key.toLowerCase(Locale.ENGLISH);
        final String folded = CaseFolding.foldString(key, Locale.ENGLISH);

        final Set<String> names = new HashSet<String>();
        names.add(key);
        names.add(upper);
        names.add(lower);
        names.add(folded);

        return names;
    }
}
