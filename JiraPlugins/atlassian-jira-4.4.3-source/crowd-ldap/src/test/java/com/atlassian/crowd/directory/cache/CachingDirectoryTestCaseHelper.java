package com.atlassian.crowd.directory.cache;

import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnMapper;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import junit.framework.TestCase;

import java.util.Set;

public abstract class CachingDirectoryTestCaseHelper extends TestCase
{
    protected static final long DIRECTORY_ID = 1;

    protected LDAPUserWithAttributes makeUser(String name)
    {
        UserTemplateWithAttributes user = new UserTemplateWithAttributes(name, DIRECTORY_ID);
        user.setEmailAddress(name + "@example.com");
        return new LDAPUserWithAttributes("cn=" + name + ", ou=users, dc=com, dc=au", user);
    }

    protected LDAPGroupWithAttributes makeGroup(String name, Set<String> memberDNs)
    {
        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes(name, DIRECTORY_ID, GroupType.GROUP);
        group.setDescription("desc " + name);
        group.setAttribute(RFC4519MemberDnMapper.ATTRIBUTE_KEY, memberDNs);
        return new LDAPGroupWithAttributes("cn=" + name + ", ou=groups, dc=com, dc=au", group);
    }

    protected void assertUserEquals(User one, User two)
    {
        assertEquals(one.getName(), two.getName());
        assertEquals(one.getEmailAddress(), two.getEmailAddress());

        if (one instanceof LDAPUserWithAttributes && two instanceof LDAPUserWithAttributes)
        {
            assertEquals(((LDAPUserWithAttributes) one).getDn(), ((LDAPUserWithAttributes) two).getDn());
        }
    }

    protected void assertGroupEquals(Group one, Group two)
    {
        assertEquals(one.getName(), two.getName());
        assertEquals(one.getDescription(), two.getDescription());

        if (one instanceof LDAPGroupWithAttributes && two instanceof LDAPGroupWithAttributes)
        {
            assertEquals(((LDAPGroupWithAttributes) one).getDn(), ((LDAPGroupWithAttributes) two).getDn());
            assertEquals(((LDAPGroupWithAttributes) one).getValues(RFC4519MemberDnMapper.ATTRIBUTE_KEY), ((LDAPGroupWithAttributes) two).getValues(RFC4519MemberDnMapper.ATTRIBUTE_KEY));
        }
    }
}
