package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.sun.jersey.api.client.GenericType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Representation of a version in the JIRA REST API.
 *
 * @since v4.3
 */
@JsonSerialize (include = JsonSerialize.Inclusion.NON_NULL)
public class Version
{
    public static final GenericType<List<Version>> VERSIONS_TYPE = new GenericType<List<Version>>(){};
    private static final String DATE_FORMAT = "dd/MMM/yy";

    public String self;
    public String expand;
    public String name;
    public String description;
    public Long id;
    public Boolean released;
    public Boolean archived;
    public Boolean overdue;
    public String userReleaseDate;
    public String project;
    public List<SimpleLink> operations;

    @XmlJavaTypeAdapter (LocalDateAdapter.class)
    public LocalDate releaseDate;

    public Version self(URI self)
    {
        this.self = self.toString();
        return this;
    }

    public Version self(String self)
    {
        this.self = self;
        return this;
    }

    public Version name(String name)
    {
        this.name = name;
        return this;
    }

    public Version description(String description)
    {
        this.description = description;
        return this;
    }

    public Version released(Boolean released)
    {
        this.released = released;
        return this;
    }

    public Version archived(Boolean archived)
    {
        this.archived = archived;
        return this;
    }

    public Version overdue(Boolean overdue)
    {
        this.overdue = overdue;
        return this;
    }

    public Version id(Long id)
    {
        this.id = id;
        return this;
    }

    public Version releaseDate(LocalDate releaseDate)
    {
        this.releaseDate = releaseDate;
        return this;
    }

    public Version releaseDate(String releaseDate)
    {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        try
        {
            return releaseDate(releaseDate, LocalDate.fromDateFields(format.parse(releaseDate)));
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Version releaseDate(String releaseDate, LocalDate releaseDateObject)
    {
        this.releaseDate = releaseDateObject;
        this.userReleaseDate = releaseDate;
        return this;
    }

    public Version project(String project)
    {
        this.project = project;
        return this;
    }

    public Version userReleaseDate(String userReleaseDate)
    {
        this.userReleaseDate = userReleaseDate;
        return this;
    }

    public Version operations(List<SimpleLink> operations)
    {
        this.operations = operations;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        return EqualsBuilder.reflectionEquals(this, obj);
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
