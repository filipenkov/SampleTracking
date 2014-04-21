package com.atlassian.crowd.directory.ldap.mapper.attribute.group;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.google.common.collect.Sets;
import org.springframework.ldap.core.DirContextAdapter;

import java.util.Collections;
import java.util.Set;

public class RFC2307MemberUidMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "memberUIDs";

    private final String groupMemberAttribute;

    public RFC2307MemberUidMapper(final String groupMemberAttribute)
    {
        this.groupMemberAttribute = groupMemberAttribute;
    }

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(final DirContextAdapter ctx) throws Exception
    {
        String[] members = ctx.getStringAttributes(groupMemberAttribute);
        return members == null ? Collections.<String>emptySet() : Sets.newHashSet(members);
    }
}