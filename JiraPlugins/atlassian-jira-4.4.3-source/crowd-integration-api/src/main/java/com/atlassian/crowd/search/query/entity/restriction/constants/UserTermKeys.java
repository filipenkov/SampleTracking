package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.google.common.collect.ImmutableSet;

import java.util.Date;

/**
 * Attributes of a user.
 */
public class UserTermKeys
{
    public static final Property<String> USERNAME = new PropertyImpl<String>("name", String.class);
    public static final Property<String> EMAIL = new PropertyImpl<String>("email", String.class);
    public static final Property<String> FIRST_NAME = new PropertyImpl<String>("firstName", String.class);
    public static final Property<String> LAST_NAME = new PropertyImpl<String>("lastName", String.class);
    public static final Property<String> DISPLAY_NAME = new PropertyImpl<String>("displayName", String.class);
    public static final Property<Boolean> ACTIVE = new PropertyImpl<Boolean>("active", Boolean.class);

    public static final Property<Date> CREATED_DATE = new PropertyImpl<Date>("createdDate", Date.class);
    public static final Property<Date> UPDATED_DATE = new PropertyImpl<Date>("updatedDate", Date.class);

    public static final Iterable<Property<?>> ALL_USER_PROPERTIES;

    static
    {
        ALL_USER_PROPERTIES = ImmutableSet.<Property<?>>of(
                USERNAME, EMAIL, FIRST_NAME, LAST_NAME, DISPLAY_NAME, ACTIVE, CREATED_DATE, UPDATED_DATE);
    }

    private UserTermKeys() {}
}
