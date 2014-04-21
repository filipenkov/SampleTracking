package com.atlassian.jira.issue.issuelink;

import com.atlassian.jira.issue.Issue;

import java.util.Collection;
import java.util.Set;

/**
 * This represents the set of issue links from and to a particular {@link Issue}, seen in terms of that {@link Issue}
 *
 * @since v4.4
 */
public interface IssueLinks
{
    /**
     * @return the root issue that this set of issue links belongs to.
     */
    Issue getIssue();

    /**
     * Returns a set of link types, {@link com.atlassian.jira.issue.link.IssueLinkType} objects.
     *
     * @return a set of {@link com.atlassian.jira.issue.link.IssueLinkType} objects
     */
    Set<IssueLinkType> getLinkTypes();

    /**
     * Looks up and returns a sorted list of all outward linked issues by given link name.
     *
     * @param linkName link name to lookup issues by
     * @return a sorted list of browsable outward linked issues
     */
    Collection<IssueLink> getOutwardIssues(String linkName);

    /**
     * Looks up and returns a sorted list of all inward linked issues by given link name.
     *
     * @param linkName link name to lookup issues by
     * @return a sorted list of browsable inward linked issues
     */
    Collection<IssueLink> getInwardIssues(String linkName);


    /**
     * Returns a collection of issues that contains both inward and outward linking issues. The returned collection is
     * sorted and does not contain duplicates.
     *
     * @return a collection of all linked issues
     */
    Collection<IssueLink> getAllIssues();
}
