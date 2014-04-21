package com.atlassian.crowd.search.query.entity.restriction;

import org.apache.commons.lang.builder.ToStringBuilder;

public class TermRestriction<T> implements PropertyRestriction<T>
{
    private final Property<T> property;
    private final MatchMode matchMode;
    private final T value;

    /**
     * Constructs a restriction based on an entity property value and match mode.
     * The property should normally be one of the constants defined on
     * {@link com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys} and
     * {@link com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys}.
     *
     * @param property the property to match
     * @param matchMode the method of matching (exact match, prefix, etc.)
     * @param value the value to match
     */
    public TermRestriction(final Property<T> property, final MatchMode matchMode, final T value)
    {
        this.property = property;
        this.matchMode = matchMode;
        this.value = value;
    }

    public TermRestriction(final Property<T> property, final T value)
    {
        this(property, MatchMode.EXACTLY_MATCHES, value);
    }

    public final T getValue()
    {
        return value;
    }

    public final Property<T> getProperty()
    {
        return property;
    }

    public final MatchMode getMatchMode()
    {
        return matchMode;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PropertyRestriction)) return false;

        PropertyRestriction that = (PropertyRestriction) o;

        if (matchMode != that.getMatchMode()) return false;
        if (property != null ? !property.equals(that.getProperty()) : that.getProperty() != null) return false;
        if (value != null ? !value.equals(that.getValue()) : that.getValue() != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = property != null ? property.hashCode() : 0;
        result = 31 * result + (matchMode != null ? matchMode.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("property", property).
                append("matchMode", matchMode).
                append("value", value).
                toString();
    }
}
