package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;

import java.util.Date;

public class TokenTermKeys
{
    /**
     * Username or application name associated with token.
     */
    public static final Property<String> NAME = new PropertyImpl<String>("name", String.class);

    /**
     * Date the token was last accessed.
     */
    public static final Property<Date> LAST_ACCESSED_DATE = new PropertyImpl<Date>("lastAccessedDate", Date.class);

    /**
     * Directory ID associated with token.
     */
    public static final Property<Long> DIRECTORY_ID = new PropertyImpl<Long>("directoryId", Long.class);

    /**
     * Random number associated with token.
     */
    public static final Property<Long> RANDOM_NUMBER = new PropertyImpl<Long>("randomNumber", Long.class);
}
