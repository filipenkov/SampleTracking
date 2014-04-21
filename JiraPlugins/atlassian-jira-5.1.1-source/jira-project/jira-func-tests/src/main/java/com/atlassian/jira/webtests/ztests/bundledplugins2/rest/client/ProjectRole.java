package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * @since v4.4
 */
public class ProjectRole
{
    public String self;
    public String name;
    public Long id;
    public String description;
    public List<Actor> actors;

    public static class Actor
    {
        public Long id;
        public String displayName;
        public String type;
        public String name;
        public String avatarUrl;
    }
}
