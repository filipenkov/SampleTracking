package com.atlassian.crowd.directory.ldap.mapper.attribute;

import org.springframework.ldap.core.DirContextAdapter;

import java.util.Collections;
import java.util.Set;

public class RFC2307GidNumberMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "gidNumber";

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(final DirContextAdapter ctx) throws Exception
    {
        return Collections.singleton((String) ctx.getAttributes().get(getKey()).get());
    }
}
