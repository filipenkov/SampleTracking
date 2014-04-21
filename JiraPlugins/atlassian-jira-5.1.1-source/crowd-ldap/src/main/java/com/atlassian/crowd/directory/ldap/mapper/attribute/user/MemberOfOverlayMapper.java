package com.atlassian.crowd.directory.ldap.mapper.attribute.user;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import org.springframework.ldap.core.DirContextAdapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MemberOfOverlayMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "memberOf";

    private final String userMemberOfAttribute;
    private final boolean relaxedDnStandardisation;

    public MemberOfOverlayMapper(final String userMemberOfAttribute, final boolean relaxedDnStandardisation)
    {
        this.userMemberOfAttribute = userMemberOfAttribute;
        this.relaxedDnStandardisation = relaxedDnStandardisation;
    }

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(final DirContextAdapter ctx) throws Exception
    {
        String[] memberships = ctx.getStringAttributes(userMemberOfAttribute);

        if (memberships != null)
        {
            Set<String> standardDNs = new HashSet<String>(memberships.length);
            for (String memberDN : memberships)
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
