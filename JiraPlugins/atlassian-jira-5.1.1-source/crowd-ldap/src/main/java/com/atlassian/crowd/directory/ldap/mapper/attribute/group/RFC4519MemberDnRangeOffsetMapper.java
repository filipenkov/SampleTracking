package com.atlassian.crowd.directory.ldap.mapper.attribute.group;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.util.RangeOption;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingEnumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * If the group has ranged memberships (Active Directory specific) this mapper will obtain
 * the offset - ie. where to start the search for the second page of users.
 *
 * Tested by: _testBrowseLargeGroup() in DnRangeTest
 */
public class RFC4519MemberDnRangeOffsetMapper implements AttributeMapper
{
    public static final String ATTRIBUTE_KEY = "memberRangeStart";

    private final String groupMemberAttribute;

    public RFC4519MemberDnRangeOffsetMapper(String groupMemberAttribute)
    {
        this.groupMemberAttribute = groupMemberAttribute;
    }

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(DirContextAdapter ctx) throws Exception
    {
        Set<String> attributes = new HashSet<String>();

        NamingEnumeration<String> attrEnum = ctx.getAttributes().getIDs();
        try
        {
            while (attrEnum.hasMore())
            {
                String attrId = attrEnum.next();
                if (attrId.startsWith(groupMemberAttribute + ";"))
                {
                    RangeOption range = RangeOption.parse(attrId.split(";")[1]);
                    int newStart = range.getTerminal() + 1;
                    attributes.add(String.valueOf(newStart));
                    break;
                }
            }
        }
        finally
        {
            attrEnum.close(); // Finished with enumeration, close to free up resources
        }
        return attributes;
    }
}
