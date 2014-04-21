package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Transition payload.
 *
 * @since v4.3
 */
public class TransitionPost
{
    public int transition;
    public Map<String, Object> fields = Maps.newHashMap();
    public Comment comment;
}
