package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;

public class ApplicationTermKeys
{
    /**
     * Name of application. Case-insensitive search.
     */
    public static final Property<String> NAME = new PropertyImpl<String>("name", String.class);

    /**
     * Active flag of application. Exact match search.
     */
    public static final Property<Boolean> ACTIVE = new PropertyImpl<Boolean>("active", Boolean.class);

    /**
     * Type of application (<code>ApplicationType</code>). Exact match search.
     */
    public static final Property<Enum> TYPE = new PropertyImpl<Enum>("type", Enum.class);
}
