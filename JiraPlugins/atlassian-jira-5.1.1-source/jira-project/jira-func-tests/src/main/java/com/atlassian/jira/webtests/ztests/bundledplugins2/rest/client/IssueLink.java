package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;

/**
 * Representation of an issue link in the JIRA REST API.
 *
 * @since v4.3
 */
public class IssueLink
{
    @JsonProperty
    private String id;

    @JsonProperty
    private URI self;

    @JsonProperty
    private Type type;

    @JsonProperty
    private IssueLinkRef inwardIssue;

    @JsonProperty
    private IssueLinkRef outwardIssue;

    public IssueLink()
    {
    }

    public IssueLink(Type type, IssueLinkRef inwardIssue, IssueLinkRef outwardIssue)
    {
        this.type = type;
        this.inwardIssue = inwardIssue;
        this.outwardIssue = outwardIssue;
    }

    public Type type()
    {
        return this.type;
    }

    public IssueLink type(Type type)
    {
        return new IssueLink(type, inwardIssue, outwardIssue);
    }

    public IssueLinkRef inwardIssue()
    {
        return inwardIssue;
    }

    public IssueLink inwardIssue(IssueLinkRef inwardIssue)
    {
        return new IssueLink(type, inwardIssue, outwardIssue);
    }

    public IssueLinkRef outwardIssue()
    {
        return outwardIssue;
    }

    public String id()
    {
        return id;
    }

    public URI self()
    {
        return self;
    }

    public IssueLink outwardIssue(IssueLinkRef outwardIssue)
    {
        return new IssueLink(type, inwardIssue, outwardIssue);
    }

    @Override
    public int hashCode() { return reflectionHashCode(this); }

    @Override
    public boolean equals(Object obj) { return reflectionEquals(this, obj); }

    @Override
    public String toString() { return reflectionToString(this); }

    public static class Type
    {
        @JsonProperty
        private String id;

        @JsonProperty
        private String name;

        @JsonProperty
        private String inward;

        @JsonProperty
        private String outward;

        @JsonProperty
        private String self;

        public Type()
        {
        }

        public Type(String id, String name, String inward, String outward, String self)
        {
            this.id = id;
            this.name = name;
            this.inward = inward;
            this.outward = outward;
            this.self = self;
        }

        public String id()
        {
            return this.id;
        }

        public Type id(String id)
        {
            return new Type(id, name, inward, outward, self);
        }

        public String name()
        {
            return this.name;
        }

        public Type name(String name)
        {
            return new Type(id, name, inward, outward, self);
        }

        public String inward()
        {
            return this.inward;
        }

        public Type inward(String inward)
        {
            return new Type(id, name, inward, outward, self);
        }

        public String outward()
        {
            return this.outward;
        }

        public Type outward(String outward)
        {
            return new Type(id, name, inward, outward, self);
        }

        public String self()
        {
            return this.self;
        }

        public Type self(String self)
        {
            return new Type(id, name, inward, outward, self);
        }

        @Override
        public int hashCode()
        {
            return reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class IssueLinkRef
    {
        @JsonProperty
        private String id;

        @JsonProperty
        private String key;

        @JsonProperty
        private String self;

        @JsonProperty
        private Fields fields;

        public IssueLinkRef()
        {
        }

        public IssueLinkRef(String id, String key, String self, Fields fields)
        {
            this.id = id;
            this.key = key;
            this.self = self;
            this.fields = fields;
        }

        public String id()
        {
            return this.id;
        }

        public IssueLinkRef id(String id)
        {
            return new IssueLinkRef(id, key, self, fields);
        }

        public String key()
        {
            return this.key;
        }

        public IssueLinkRef key(String key)
        {
            return new IssueLinkRef(id, key, self, fields);
        }

        public String self()
        {
            return this.self;
        }

        public IssueLinkRef self(String self)
        {
            return new IssueLinkRef(id, key, self, fields);
        }

        public Fields fields()
        {
            return fields;
        }

        public IssueLinkRef fields(Fields fields)
        {
            return new IssueLinkRef(id, key, self, fields);
        }

        @Override
        public int hashCode()
        {
            return reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj)
        {
            return reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public static class Fields
        {
            @JsonProperty
            private String summary;

            @JsonProperty
            private Status status;

            @JsonProperty("issuetype")
            private IssueType issueType;

            @JsonProperty
            private Priority priority;

            public Fields()
            {
            }

            public Fields(String summary, Status status, IssueType issueType, Priority priority)
            {
                this.summary = summary;
                this.status = status;
                this.issueType = issueType;
                this.priority = priority;
            }

            public String summary()
            {
                return this.summary;
            }

            public Fields summary(String summary)
            {
                return new Fields(summary, status, issueType, priority);
            }

            public Status status()
            {
                return this.status;
            }

            public Fields status(Status status)
            {
                return new Fields(summary, status, issueType, priority);
            }

            public IssueType issueType()
            {
                return this.issueType;
            }

            public Fields issueType(IssueType issueType)
            {
                return new Fields(summary, status, issueType, priority);
            }

            public Priority priority()
            {
                return this.priority;
            }

            public Fields priority(Priority priority)
            {
                return new Fields(summary, status, issueType, priority);
            }
            
            @Override
            public boolean equals(Object obj) { return reflectionEquals(this, obj); }

            @Override
            public int hashCode() { return reflectionHashCode(this); }

            @Override
            public String toString() { return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); }
        }
    }
}
