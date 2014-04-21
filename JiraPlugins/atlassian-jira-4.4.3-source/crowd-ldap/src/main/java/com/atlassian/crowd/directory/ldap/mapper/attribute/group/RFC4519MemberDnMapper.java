package com.atlassian.crowd.directory.ldap.mapper.attribute.group;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.google.common.collect.Sets;
import org.springframework.ldap.core.DirContextAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A generic memberDn mapper that will look for the 'member' attribute in groups.
 */
public class RFC4519MemberDnMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "memberDNs";

    private final String groupMemberAttribute;
    private final boolean relaxedDnStandardisation;

    public RFC4519MemberDnMapper(final String groupMemberAttribute, final boolean relaxedDnStandardisation)
    {
        this.groupMemberAttribute = groupMemberAttribute;
        this.relaxedDnStandardisation = relaxedDnStandardisation;
    }

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(final DirContextAdapter ctx) throws Exception
    {
        String[] memberArray = ctx.getStringAttributes(groupMemberAttribute);

        if (memberArray != null)
        {
            Set<String> members = Sets.newHashSet(memberArray);
            Set<String> standardDNs = new HashSet<String>(members.size());
            for (String memberDN : members)
            {
                String dn = DNStandardiser.standardise(memberDN, !relaxedDnStandardisation);
                standardDNs.add(dn);
            }
            return standardDNs;
        }
        else
        {
            return Collections.emptySet();
        }
    }
}        