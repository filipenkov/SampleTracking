package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.issue.JsonTypeBean;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.mutable.MutableInt;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Representation of an issue in the JIRA REST API.
 *
 * @since v4.3
 */
public class Issue
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public String self;
    public String id;
    public String key;
    public Fields fields;
    public RenderedFields renderedFields;
    public String expand;
    public List<IssueTransitionsMeta.Transition> transitions;
    public Opsbar operations;
    public Map<String, String> names;
    public Map<String, JsonTypeBean> schema;
    public Editmeta editmeta;
    public ChangeLog changelog;

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class Fields
    {
        public List<Attachment> attachment;
        public CommentsWithPaginationBean comment;
        public String description;
        public String environment;
        public String summary;
        public Vote votes;
        public IssueSecurityType security;
        public String resolutiondate;
        public String updated;
        public String created;
        public String duedate;
        public TimeTracking timetracking;
        public List<String> labels;
        public IssueType issuetype;
        public List<Version> fixVersions;
        public List<Version> versions;
        public List<Component> components;
        public Progress progress;
        public Progress aggregateprogress;
        public Priority priority;
        public Project project;
        public Resolution resolution;
        public User assignee;
        public User reporter;
        public Status status;
        public WorklogWithPaginationBean worklog;
        public Long workratio;
        public List<IssueLink> issuelinks;
        public Watches watches;
        public List<IssueLink.IssueLinkRef> subtasks;
        public IssueLink.IssueLinkRef parent;
        private Map<String, Object> customFields;

        @JsonAnySetter
        public void addCustomField(String key, Object value)
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
        public <T> T get(String fieldID) throws IllegalArgumentException
        {
            if (customFields != null && customFields.containsKey(fieldID))
            {
                return (T) customFields.get(fieldID);
            }

            return (T) reflectiveGet(fieldID);
        }

        /**
         * Returns the IssueField for the field with the given id, or null if it is not defined. Normally this will be
         * used for custom fields, since the system field values are available as fields of this class.
         *
         * @param fieldID the field id
         * @param cls the Class to deserialise into
         * @param <T> the field's value type
         * @return an IssueField
         * @throws IllegalArgumentException if calling #has with the field id would return false
         */
        @SuppressWarnings ("unchecked")
        public <T> T get(String fieldID, Class<T> cls) throws IllegalArgumentException
        {
            return new ObjectMapper().convertValue(get(fieldID), cls);
        }

        /**
         * Returns the IssueField for the field with the given id, or null if it is not defined. Normally this will be
         * used for custom fields, since the system field values are available as fields of this class.
         *
         * @param fieldID the field id
         * @param type the TypeReference of the type to deserialise into
         * @param <T> the field's value type
         * @return an IssueField
         * @throws IllegalArgumentException if calling #has with the field id would return false
         */
        @SuppressWarnings ("unchecked")
        public <T> T get(String fieldID, TypeReference<T> type) throws IllegalArgumentException
        {
            return MAPPER.<T>convertValue(get(fieldID), type);
        }

        /**
         * Returns a boolean indicating whether this Fields has a field with the given id.
         *
         * @param fieldID a String containing the field id
         * @return a boolean indicating whether this Fields has a field with the given id.
         */
        public boolean has(String fieldID)
        {
            boolean hasCustomField = (customFields != null) && customFields.containsKey(fieldID);
            if (!hasCustomField)
            {
                return getPublicField(fieldID) != null;
            }

            return hasCustomField;
        }

        public Set<String> idSet()
        {
            Set<String> fieldIds = Sets.newHashSet();
            ReflectionUtils.doWithFields(Issue.Fields.class, new AddFieldNamesTo(fieldIds), new JsonPropertyFilter());
            if (customFields != null) fieldIds.addAll(customFields.keySet());

            return fieldIds;
        }

        public int size()
        {
            MutableInt systemFieldCount = new MutableInt();
            ReflectionUtils.doWithFields(Issue.Fields.class, new CountFieldsCallback(systemFieldCount), new JsonPropertyFilter());

            return systemFieldCount.intValue() + (customFields != null ? customFields.size() : 0);
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
         * @return an Object
         */
        private <T> T reflectiveGet(String fieldID)
        {
            Field f = getPublicField(fieldID);
            if (f != null)
            {
                try
                {
                    //noinspection unchecked
                    return (T) f.get(this);
                }
                catch (IllegalAccessException e)
                {
                    throw new RuntimeException("Couldn't get field value", e);
                }
            }

            throw new IllegalStateException("Field does not exist: " + fieldID);
        }

        private Field getPublicField(final String fieldID)
        {
            Field field = null;
            try
            {
                field = Fields.class.getDeclaredField(fieldID);
                if (Modifier.isPublic(field.getModifiers()))
                {
                    return field;
                }
            }
            catch (Exception e)
            {
                // ignore
            }

            // no luck. now try each property
            final AtomicReference<Field> fRef = new AtomicReference<Field>();
            ReflectionUtils.doWithFields(Issue.Fields.class, new ExtractFieldById(fieldID, fRef), new JsonPropertyFilter());
            if (fRef.get() != null)
            {
                field = fRef.get();
                if (Modifier.isPublic(field.getModifiers()))
                {
                    return field;
                }
            }

            return null;
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        private static class ExtractFieldById implements ReflectionUtils.FieldCallback
        {
            private final String fieldID;
            private final AtomicReference<Field> f;

            public ExtractFieldById(String fieldID, AtomicReference<Field> f)
            {
                this.fieldID = fieldID;
                this.f = f;
            }

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
            {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null && fieldID.equals(jsonProperty.value()))
                {
                    f.set(field);
                }
            }
        }

        class JsonPropertyFilter implements ReflectionUtils.FieldFilter
        {
            @Override
            public boolean matches(Field field)
            {
                return (field.getModifiers() & Modifier.PUBLIC) != 0;
            }
        }
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    public static class RenderedFields
    {
        public String description;
        public String environment;
        public String updated;
        public String created;
        public String resolutiondate;
        public String duedate;
        public CommentsWithPaginationBean comment;
        public WorklogWithPaginationBean worklog;
        public TimeTracking timetracking;
        public Collection<AttachmentRendered> attachment;
        private Map<String, Object> customFields;

        /**
         * Returns the number of non-null fields in this Html.
         *
         * @return an int containing the number of non-null fields
         */
        public int length()
        {
            int len = 0;
            for (Field field : RenderedFields.class.getFields())
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


        @JsonAnySetter
        public void addCustomField(String key, Object value)
        {
            if (customFields == null)
            {
                customFields = Maps.newHashMap();
            }

            customFields.put(key, value);
        }

        @SuppressWarnings ("unchecked")
        public <T> T getCustomField(String fieldID) throws IllegalArgumentException
        {
            if (customFields != null && customFields.containsKey(fieldID))
            {
                return (T) customFields.get(fieldID);
            }

            return null;
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
        renderedFields,
        names,
        schema,
        editmeta,
        transitions,
        operations,
        changelog
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

    public List<Comment> getComments()
    {
        return fields.comment.getComments();
    }

    static class CountFieldsCallback implements ReflectionUtils.FieldCallback
    {
        private final MutableInt counter;

        public CountFieldsCallback(MutableInt counter) {this.counter = counter;}

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
        {
            counter.increment();
        }
    }

    static class AddFieldNamesTo implements ReflectionUtils.FieldCallback
    {
        private final Set<String> fieldIds;

        public AddFieldNamesTo(Set<String> fieldIds)
        {

            this.fieldIds = fieldIds;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException
        {
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            if (jsonProperty != null && !"".equals(jsonProperty.value()))
            {
                fieldIds.add(jsonProperty.value());
            }
            else
            {
                fieldIds.add(field.getName());
            }
        }
    }
}
