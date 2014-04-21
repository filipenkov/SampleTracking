package com.atlassian.crowd.directory.ldap.mapper.attribute.group;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.google.common.collect.Sets;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A memberDN mapper that will handle both normal and ranged memberships attribute ("member" and "member;")
 * used in Microsoft Active Directory.
 * <p/>
 * Tested by: _testBrowseLargeGroup() in DnRangeTest
 */
public class RFC4519MemberDnRangedMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "memberDNs";

    private final String groupMemberAttribute;
    private final boolean relaxedDnStandardisation;

    public RFC4519MemberDnRangedMapper(final String groupMemberAttribute, final boolean relaxedDnStandardisation)
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
            if (members.isEmpty())
            {
                members = getInitialRangedMembers(ctx);
            }

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

    /**
     * Checks if the group has the "member;" attribute in order to handle groups that have a
     * larger number of members (ie. where ranged members exist)
     *
     * @param ctx
     * @return Set of members retreived from the "member;" attribute
     * @throws NamingException
     */
    private Set<String> getInitialRangedMembers(DirContextAdapter ctx) throws NamingException
    {
        Set<String> rangedMembers = new HashSet<String>();

        NamingEnumeration<String> attrEnum = ctx.getAttributes().getIDs();
        try
        {
            while (attrEnum.hasMore())
            {
                String attrId = attrEnum.next();
                if (attrId.startsWith(groupMemberAttribute + ";"))
                {
                    rangedMembers = Sets.newHashSet(ctx.getStringAttributes(attrId));
                    break;
                }
            }
        }
        finally
        {
            attrEnum.close(); // Finished with enumeration, close to free up resources
        }

        return rangedMembers;
    }
}
