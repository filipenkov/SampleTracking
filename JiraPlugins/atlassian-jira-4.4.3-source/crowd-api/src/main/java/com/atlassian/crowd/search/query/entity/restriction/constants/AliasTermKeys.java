package com.atlassian.crowd.search.query.entity.restriction.constants;

import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;

public class AliasTermKeys
{
    /**
     * Application ID. Exact match restriction.
     */
    public static final Property<Long> APPLICATION_ID = new PropertyImpl<Long>("applicationId", Long.class);

    /**
     * Alias for a user. Case-insensitive search.
     */
    public static final Property<String> ALIAS = new PropertyImpl<String>("alias", String.class);
}
