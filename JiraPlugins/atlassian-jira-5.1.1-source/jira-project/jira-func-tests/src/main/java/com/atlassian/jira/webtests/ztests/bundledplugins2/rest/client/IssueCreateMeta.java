package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Representation of the create meta data for issues in the JIRA REST API.
 *
 * @since v5.0
 */
public class IssueCreateMeta
{
    public String expand;
    public List<Project> projects;

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class Project
    {
        public String expand;
        public String self;
        public String id;
        public String key;
        public String name;
        public Map<String, String> avatarUrls;
        public List<IssueType> issuetypes;

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
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class IssueType
    {
        public String expand;
        public String self;
        public String id;
        public String name;
        public String iconUrl;
        public Map<String, FieldMetaData> fields;

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
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class JsonType
    {
        @JsonProperty
        public String type;
        @JsonProperty
        public String items;
        @JsonProperty
        public String system;
        @JsonProperty
        public String custom;
        @JsonProperty
        public Long customId;

        public JsonType()
        {
        }

        public JsonType(String type, String items, String system, String custom, Long customId)
        {
            this.type = type;
            this.items = items;
            this.system = system;
            this.custom = custom;
            this.customId = customId;
        }

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

        public static JsonType system(String type, String system)
        {
            return new JsonType(type, null, system, null, null);
        }
        public static JsonType systemArray(String items, String system)
        {
            return new JsonType("array", items, system, null, null);
        }
        public static JsonType custom(String type, String custom, Long customId)
        {
            return new JsonType(type, null, null, custom, customId);
        }
        public static JsonType customArray(String items, String custom, Long customId)
        {
            return new JsonType("array", items, null, custom, customId);
        }
    }

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

    public static enum Expand
    {
        fields("projects.issuetypes.fields");

        private final String actualString;

        Expand(final String actualString)
        {
            this.actualString = actualString;
        }

        @Override
        public String toString()
        {
            return actualString;
        }

    }
}
