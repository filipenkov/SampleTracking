package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Representation of a field in the JIRA REST API.
 *
 * @since v5.0
 */
public class Field
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private boolean custom;

    @JsonProperty
    private boolean orderable;

    @JsonProperty
    private boolean navigable;

    @JsonProperty
    private boolean searchable;

    @JsonProperty
    public IssueCreateMeta.JsonType schema;


    public Field()
    {
    }

    public Field(String id, String name, boolean custom, IssueCreateMeta.JsonType schema, boolean orderable, boolean navigable, boolean searchable)
    {
        this.id = id;
        this.name = name;
        this.custom = custom;
        this.schema = schema;
        this.orderable = orderable;
        this.navigable = navigable;
        this.searchable = searchable;
    }

    public String id()
    {
        return id;
    }

    public Field id(String id)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public String name()
    {
        return name;
    }

    public Field name(String name)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public boolean orderable()
    {
        return this.orderable;
    }

    public Field orderable(boolean orderable)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public boolean navigable()
    {
        return this.navigable;
    }

    public Field navigable(boolean navigable)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public boolean searchable()
    {
        return this.searchable;
    }

    public Field searchable(boolean searchable)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public boolean custom()
    {
        return this.custom;
    }

    public Field custom(boolean custom)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
    }

    public IssueCreateMeta.JsonType schema()
    {
        return this.schema;
    }

    public Field description(IssueCreateMeta.JsonType schema)
    {
        return new Field(id, name, custom, schema, orderable, navigable, searchable);
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
}
