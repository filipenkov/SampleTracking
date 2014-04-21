package com.atlassian.voorhees;

/**
 */
public class NotSoSimpleResponseObject
{
    private SimpleResponseObject internal;
    private String value;

    public NotSoSimpleResponseObject()
    {
    }

    public NotSoSimpleResponseObject(SimpleResponseObject internal, String name)
    {
        this.internal = internal;
        this.value = name;
    }

    public SimpleResponseObject getInternal()
    {
        return internal;
    }

    public void setInternal(SimpleResponseObject internal)
    {
        this.internal = internal;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotSoSimpleResponseObject that = (NotSoSimpleResponseObject) o;

        if (internal != null ? !internal.equals(that.internal) : that.internal != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = internal != null ? internal.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
