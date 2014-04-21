package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.google.common.collect.Maps;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

/**
 * Representation of an issue in the JIRA REST API.
 *
 * @since v4.3
 */
public class Issue
{
    public String self;
    public String key;
    public Fields fields;
    public Html html;
    public String expand;
    public String transitions;

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class Fields
    {
        public IssueField<List<Attachment>> attachment;
        public IssueField<List<Comment>> comment;
        public IssueField<String> description;
        public IssueField<String> environment;
        public IssueField<String> summary;
        public IssueField<Vote> votes;
        public IssueField<String> security;
        public IssueField<String> resolutiondate;
        public IssueField<String> updated;
        public IssueField<String> created;
        public IssueField<String> duedate;
        public IssueField<TimeTracking> timetracking;
        public IssueField<List<String>> labels;
        public IssueField<IssueType> issuetype;
        public IssueField<List<Version>> fixVersions;
        public IssueField<List<Version>> versions;
        public IssueField<List<Component>> components;
        public IssueField<Priority> priority;
        public IssueField<Project> project;
        public IssueField<Resolution> resolution;
        public IssueField<User> assignee;
        public IssueField<User> reporter;
        public IssueField<Status> status;
        public IssueField<List<Worklog>> worklog;
        public IssueField<List<IssueLink>> links;
        public IssueField<Watchers> watcher;
        @JsonProperty ("sub-tasks")
        public IssueField<List<IssueLink>> subtasks;
        public IssueField<IssueLink> parent;
        private Map<String, IssueField<Object>> customFields;

        @JsonAnySetter
        public void addCustomField(String key, IssueField<Object> value)
        {
            if (customFields == null)
            {
                customFields = Maps.newHashMap();
            }

            customFields.put(key, value);
        }

        /**
         * Returns the IssueField for the field with the given id, or null if it is not defined. Normally this will be
         * used for custom fields, since the system field values are available as fields of this class.
         *
         * @param fieldID the field id
         * @param <T> the field's value type
         * @return an IssueField
         * @throws IllegalArgumentException if calling #has with the field id would return false
         */
        @SuppressWarnings ("unchecked")
        public <T> IssueField<T> get(String fieldID) throws IllegalArgumentException
        {
            IssueField<T> customFieldValue = (IssueField<T>) customFields.get(fieldID);
            if (customFieldValue != null)
            {
                return customFieldValue;
            }

            return reflectiveGet(fieldID);
        }

        /**
         * Returns a boolean indicating whether this Fields has a field with the given id.
         *
         * @param fieldID a String containing the field id
         * @return a boolean indicating whether this Fields has a field with the given id.
         */
        public boolean has(String fieldID)
        {
            boolean hasCustomField = customFields.containsKey(fieldID);
            if (!hasCustomField)
            {
                return getPublicField(fieldID) != null;
            }

            return hasCustomField;
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

        /**
         * Returns the value of the field with the given id, if it is a field on this class. Otherwise returns null.
         *
         * @param fieldID a String containing the field id
         * @return an IssueField, or null
         */
        private IssueField reflectiveGet(String fieldID)
        {
            Field f = getPublicField(fieldID);
            if (f != null)
            {
                try
                {
                    f.get(this);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Couldn't get field value", e);
                }
            }

            throw new IllegalStateException("Field does not exist: " + fieldID);
        }

        private Field getPublicField(String fieldID)
        {
            try
            {
                Field f = Fields.class.getDeclaredField(fieldID);
                if (Modifier.isPublic(f.getModifiers()))
                {
                    return f;
                }
            }
            catch (Exception e)
            {
                // ignore
            }

            return null;
        }
    }

    public static class IssueField<T>
    {
        public String name;
        public String type;
        public T value;


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
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class Html
    {
        public String description;
        public String environment;
        public List<String> comment;
        public List<String> worklog;

        /**
         * Returns the number of non-null fields in this Html.
         *
         * @return an int containing the number of non-null fields
         */
        public int length()
        {
            int len = 0;
            for (Field field : Html.class.getFields())
            {
                try
                {
                    if (field.get(this) != null)
                    {
                        len++;
                    }
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException(e);
                }
            }

            return len;
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
    }

    public static enum Expand
    {
        html
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
}
