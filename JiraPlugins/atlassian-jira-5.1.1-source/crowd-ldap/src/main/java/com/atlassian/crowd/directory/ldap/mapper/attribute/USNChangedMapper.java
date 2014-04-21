package com.atlassian.crowd.directory.ldap.mapper.attribute;

import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import java.util.Collections;
import java.util.Set;

/**
 * Maps the uSNChanged on an entity.
 *
 * This concept only applies to Active Directory.
 */
public class USNChangedMapper implements AttributeMapper
{
    /**
     * USN Changed attribute name.
     */
    public static final String ATTRIBUTE_KEY = "uSNChanged";

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(DirContextAdapter ctx) throws NamingException
    {
        return Collections.singleton((String) ctx.getAttributes().get(getKey()).get());
    }
}
