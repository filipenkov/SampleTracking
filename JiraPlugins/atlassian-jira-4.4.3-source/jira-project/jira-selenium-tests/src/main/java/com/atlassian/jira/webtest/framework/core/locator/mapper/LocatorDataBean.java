package com.atlassian.jira.webtest.framework.core.locator.mapper;

import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple bean holding main locator data: type and value. It defines {@link #equals(Object)} and
 * {@link #hashCode()} and thus may be safely used as a collection/map element.
 *
 * @since v4.3
 */
public final class LocatorDataBean implements LocatorData
{
    private final LocatorType type;
    private final String value;

    public LocatorDataBean(LocatorType type, String value)
    {
        this.type = notNull("type", type);
        this.value = notNull("value", value);
    }

    public LocatorDataBean(LocatorData toCopy)
    {
        this(toCopy.type(), toCopy.value());
    }

    public LocatorType type()
    {
       return type;
    }

    public String value()
    {
        return value;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final LocatorDataBean that = (LocatorDataBean) o;
        return this.type.equals(that.type()) && this.value().equals(that.value());
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "LocatorData[type=" + type +",value=" + value + "]";
    }
}
