/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.util.Predicate;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class IssueUtils
{
    public static final String AUTOMATIC_ASSIGNEE = "-1";
    public static final String SEPERATOR_ASSIGNEE = "-2";
    protected static final Logger log = Logger.getLogger(IssueUtils.class);

    /**
     * Filters a list of issues based on the type of IssueFilter passed in
     *
     * @param issues The list of issues. Any issues to be filtered will be removed from this list
     * @param filter a predicate which evaluates to true when the issue is to be removed from the list.
     */
    public static void filterIssues(Collection issues, Predicate<GenericValue> filter)
    {
        for (Iterator iterator = issues.iterator(); iterator.hasNext();)
        {
            GenericValue issue = (GenericValue) iterator.next();
            if (filter.evaluate(issue))
            {
                iterator.remove();
            }
        }
    }

    @Deprecated
    public static GenericValue setPriority(GenericValue issue, User remoteUser, String priority) throws GenericEntityException
    {
        final GenericValue originalIssue = ManagerFactory.getIssueManager().getIssue(issue.getLong("id"));
        issue.setString(IssueFieldConstants.PRIORITY, priority);
        issue.set("updated", UtilDateTime.nowTimestamp());
        GenericValue changeGroup = ChangeLogUtils.createChangeGroup(remoteUser, originalIssue, issue, Collections.EMPTY_LIST, true);
        CoreFactory.getGenericDelegator().storeAll(Arrays.asList(issue));
        return changeGroup;
    }

    /**
     * Does an issue have timetracking data, only makes sense if time tracking is turned on.
     *
     * @param issue the issue.
     * @return true if the issue has any time tracking information
     * @since v3.11
     */
    public static boolean hasTimeTracking(Issue issue)
    {
        if (issue == null)
        {
            return false;
        }
        Long orig = issue.getOriginalEstimate();
        Long est = issue.getEstimate();
        Long spent = issue.getTimeSpent();
        return isNonZeroNumber(orig) || isNonZeroNumber(est) || isNonZeroNumber(spent);
    }

    private static boolean isNonZeroNumber(Long aLong)
    {
        return aLong != null && aLong != 0;
    }

}
