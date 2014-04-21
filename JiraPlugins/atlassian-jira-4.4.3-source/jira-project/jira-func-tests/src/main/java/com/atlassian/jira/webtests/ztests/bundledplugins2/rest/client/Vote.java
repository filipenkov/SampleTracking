package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * Representation of a vote in the JIRA REST API.
 *
 * @since v4.3
 */
public class Vote
{
    public String self;
    public int votes;
    public boolean hasVoted;
    public List<User> voters;
}
