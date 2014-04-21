package com.atlassian.crowd.search.query.entity.restriction;

/**
 * Factory methods for creating Property objects.
 */
public class PropertyUtils
{
    public static Property<String> ofTypeString(String name)
    {
        return new PropertyImpl<String>(name, String.class);
    }

    public static Property<Enum> ofTypeEnum(String name)
    {
        return new PropertyImpl<Enum>(name, Enum.class);
    }

    public static Property<Boolean> ofTypeBoolean(String name)
    {
        return new PropertyImpl<Boolean>(name, Boolean.class);
    }
}
