package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.projectconfig.beans.NamedDefault;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
* @since v4.4
*/
class SimpleNamedDefault implements NamedDefault
{
    private final String name;
    private final boolean isDefault;

    SimpleNamedDefault(String name, boolean aDefault)
    {
        this.name = name;
        isDefault = aDefault;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isDefault()
    {
        return isDefault;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleNamedDefault that = (SimpleNamedDefault) o;

        if (isDefault != that.isDefault) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }
}
