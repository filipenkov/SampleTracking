package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * Representation for watchers in JIRA REST API.
 *
 * @since v4.3
 */
public class Watchers
{
    public String self;
    public boolean isWatching;
    public long watchCount;
    public List<User> watchers;
}
