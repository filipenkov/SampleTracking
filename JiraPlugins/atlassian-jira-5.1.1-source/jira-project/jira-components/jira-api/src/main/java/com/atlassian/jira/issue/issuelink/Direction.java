package com.atlassian.jira.issue.issuelink;

import com.atlassian.annotations.PublicApi;

/**
 * Represents the direction of a link. Links may have a different name and description for each direction of the link.
 * @deprecated Use {@link com.atlassian.jira.issue.link.Direction} instead. Since v5.0.
 */
@PublicApi
public enum Direction
{
    /**
     * Going from this issue to the other issue.
     */
    OUT,

    /**
     * Going from the other issue to this issue.
     */
    IN
}
