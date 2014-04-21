package com.atlassian.crowd.model;

import com.atlassian.crowd.util.InternalEntityUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

public class InternalEntityAttribute implements Serializable
{
    private Long id;
    private String name;
    private String value;
    private String lowerValue;

    protected InternalEntityAttribute()
    {
    }

    public InternalEntityAttribute(String name, String value)
    {
        Validate.notNull(name, "name cannot be null");
        InternalEntityUtils.validateLength(name);
        Validate.notNull(value, "value cannot be null");

        this.name = name;
        this.value = InternalEntityUtils.truncateValue(value);
        this.lowerValue = toLowerCase(this.value);
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    private String getLowerValue()
    {
        return lowerValue;
    }

    protected void setId(final Long id)
    {
        this.id = id;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setValue(final String value)
    {
        if (value != null)
        {
            this.value = value;
            this.lowerValue = toLowerCase(value);
        }
    }

    private void setLowerValue(final String lowerValue)
    {
        this.lowerValue = lowerValue;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof InternalEntityAttribute))
        {
            return false;
        }

        InternalEntityAttribute that = (InternalEntityAttribute) o;

        if (getLowerValue() != null ? !getLowerValue().equals(that.getLowerValue()) : that.getLowerValue() != null)
        {
            return false;
        }
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getLowerValue() != null ? getLowerValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("id", id).
                append("name", name).
                append("value", value).
                append("lowerValue", lowerValue).
                toString();
    }
}
