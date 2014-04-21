package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * Representation of a remote issue link in the JIRA REST API.
 *
 * @since v5.0
 */
public class RemoteIssueLink
{
    public static final GenericType<List<RemoteIssueLink>> REMOTE_ISSUE_LINKS_TYPE = new GenericType<List<RemoteIssueLink>>(){};

    public String self;
    public Long id;
    public String globalId;
    public Application application;
    public String relationship;
    public RemoteObject object;

    @Override
    public boolean equals(Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Application
    {
        public String type;
        public String name;
    }

    public static class RemoteObject
    {
        public String url;
        public String title;
        public String summary;
        public Icon icon;
        public Status status;

        public static class Icon
        {
            public String url16x16;
            public String title;
            public String link;
        }

        public static class Status
        {
            public Boolean resolved;
            public Icon icon;
        }
    }
}
