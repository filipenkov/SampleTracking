package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;

public class DirectoryTermKeys
{
    /**
     * Name of directory.
     */
    public static final Property<String> NAME = new PropertyImpl<String>("name", String.class);

    /**
     * Active flag for directory. Exact match search.
     */
    public static final Property<Boolean> ACTIVE = new PropertyImpl<Boolean>("active", Boolean.class);

    /**
     * Type of directory. Exact match search.
     */
    public static final Property<Enum> TYPE = new PropertyImpl<Enum>("type", Enum.class);

    /**
     * Implementation class for directory. Case-insensitive search.
     */
    public static final Property<String> IMPLEMENTATION_CLASS = new PropertyImpl<String>("implementationClass", String.class);
}
