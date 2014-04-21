package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Abstract condition for testing conditions on issues
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public abstract class AbstractIssueCondition extends AbstractJiraCondition
{
    private static final Logger log = Logger.getLogger(CanAttachFileToIssueCondition.class);

    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        final Map<String, Object> params = jiraHelper.getContextParams();

        final Issue issue = (Issue) params.get("issue");

        if (issue == null)
        {
            log.warn("Trying to run condition on an issue, but no issue exists");
            return false;
        }

        return shouldDisplay(user, issue, jiraHelper);
    }

    /**
     * Should we display this item for this issue?
     *
     * @param user       The current user
     * @param issue      The issue we are displaying against
     * @param jiraHelper The JiraHelper
     * @return true if we should display this item, false otherwise
     */
    public abstract boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper);

}