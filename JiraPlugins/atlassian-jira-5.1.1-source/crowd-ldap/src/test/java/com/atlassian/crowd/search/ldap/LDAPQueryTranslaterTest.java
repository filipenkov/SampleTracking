package com.atlassian.crowd.search.ldap;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import junit.framework.TestCase;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LDAPQueryTranslaterTest extends TestCase
{
    private LDAPQueryTranslater queryTranslater;
    private LDAPPropertiesMapper ldapPropertiesMapper;

    public void setUp()
    {
        queryTranslater = new LDAPQueryTranslaterImpl();
        ldapPropertiesMapper = mock(LDAPPropertiesMapper.class);
        when(ldapPropertiesMapper.getUserFilter()).thenReturn("(&(objectCategory=Person)(sAMAccountName=*))");
        when(ldapPropertiesMapper.getUserObjectClass()).thenReturn("user");
        when(ldapPropertiesMapper.getUserNameAttribute()).thenReturn("sAMAccountName");
        when(ldapPropertiesMapper.getUserNameRdnAttribute()).thenReturn("cn");
        when(ldapPropertiesMapper.getUserLastNameAttribute()).thenReturn("sn");
        when(ldapPropertiesMapper.getUserFirstNameAttribute()).thenReturn("givenName");
        when(ldapPropertiesMapper.getUserDisplayNameAttribute()).thenReturn("displayName");
        when(ldapPropertiesMapper.getUserEmailAttribute()).thenReturn("mail");
        when(ldapPropertiesMapper.getUserGroupMembershipsAttribute()).thenReturn("memberOf");
        when(ldapPropertiesMapper.getUserPasswordAttribute()).thenReturn("unicodePwd");

        when(ldapPropertiesMapper.getGroupFilter()).thenReturn("(objectCategory=Group)");
        when(ldapPropertiesMapper.getGroupObjectClass()).thenReturn("group");
        when(ldapPropertiesMapper.getGroupNameAttribute()).thenReturn("cn");
        when(ldapPropertiesMapper.getGroupDescriptionAttribute()).thenReturn("description");
        when(ldapPropertiesMapper.getGroupMemberAttribute()).thenReturn("member");

        when(ldapPropertiesMapper.getRoleFilter()).thenReturn("(objectCategory=Role)");
        when(ldapPropertiesMapper.getRoleObjectClass()).thenReturn("group");
        when(ldapPropertiesMapper.getRoleNameAttribute()).thenReturn("cn");
        when(ldapPropertiesMapper.getRoleDescriptionAttribute()).thenReturn("description");
        when(ldapPropertiesMapper.getRoleMemberAttribute()).thenReturn("member");
    }

    public void testSearchAllUsers() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(10);
        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(objectCategory=Person)(sAMAccountName=*))", filter);
    }

    public void testSearchUserByName() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching("bob")).returningAtMost(10);
        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(sAMAccountName=bob))", filter);
    }

    public void testSearchUserByEmailAndDisplayName() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.allOf(
                Restriction.on(UserTermKeys.EMAIL).startingWith("a"),
                Restriction.on(UserTermKeys.DISPLAY_NAME).containing("BoB")
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(&(mail=a*)(displayName=*BoB*)))", filter);
    }

    public void testSearchUserByFirstNameOrLastNameOrDisplayNameAndActive() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.allOf(
                Combine.anyOf(
                        Restriction.on(UserTermKeys.FIRST_NAME).startingWith("joe"),
                        Restriction.on(UserTermKeys.LAST_NAME).startingWith("joe"),
                        Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("joe")
                ),
                Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(true)
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(|(givenName=joe*)(sn=joe*)(displayName=joe*)))", filter);
    }

    public void testSearchInactiveUsers()
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(false)).returningAtMost(10);

        try
        {
            queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
            fail("NullResultException expected");
        }
        catch (NullResultException e)
        {
            // expected
        }
    }

    public void testSearchInactiveUsersViaNestingReturningNullResult()
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.anyOf(
                Combine.allOf(
                        Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(false),
                        Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("b")
                ),
                Combine.allOf(
                        Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(false),
                        Restriction.on(UserTermKeys.FIRST_NAME).startingWith("b")
                )
        )).returningAtMost(10);

        try
        {
            queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
            fail("NullResultException expected");
        }
        catch (NullResultException e)
        {
            // expected
        }
    }

    public void testSearchActiveUsersViaNestingReturningEverything() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.allOf(
                Combine.anyOf(
                        Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(true),
                        Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("b")
                ),
                Combine.anyOf(
                        Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(true),
                        Restriction.on(UserTermKeys.FIRST_NAME).startingWith("b")
                )
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(objectCategory=Person)(sAMAccountName=*))", filter);
    }

    public void testSearchInactiveUsersViaNestingReturnResult() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.anyOf(
                Combine.allOf(
                        Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(false),
                        Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("b")
                ),
                Combine.allOf(
                        Restriction.on(UserTermKeys.LAST_NAME).startingWith("b"),
                        Restriction.on(UserTermKeys.FIRST_NAME).startingWith("b")
                )
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(&(sn=b*)(givenName=b*)))", filter);
    }

    public void testSearchMassiveNestedQuery() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.anyOf(
                Combine.allOf(
                        Restriction.on(UserTermKeys.USERNAME).exactlyMatching("test"),
                        Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("hello")
                ),
                Combine.allOf(
                        Restriction.on(UserTermKeys.LAST_NAME).exactlyMatching("cool"),
                        Restriction.on(UserTermKeys.FIRST_NAME).startingWith("b")
                )
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(|(&(sAMAccountName=test)(displayName=hello*))(&(sn=cool)(givenName=b*))))", filter);
    }

    public void testSearchActiveUsers() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(true)).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(objectCategory=Person)(sAMAccountName=*))", filter);
    }

    public void testSearchActiveUsersMatchingUsername() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.allOf(
                Restriction.on(UserTermKeys.ACTIVE).startingWith(true),
                Restriction.on(UserTermKeys.USERNAME).containing("bob")
        )).returningAtMost(10);

        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(&(objectCategory=Person)(sAMAccountName=*))(sAMAccountName=*bob*))", filter);
    }

    public void testSearchAllGroups() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group(GroupType.GROUP)).returningAtMost(10);
        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(objectCategory=Group)", filter);
    }

    public void testSearchRoleByName() throws NullResultException
    {
        EntityQuery query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group(GroupType.LEGACY_ROLE)).with(Restriction.on(GroupTermKeys.NAME).exactlyMatching("admins")).returningAtMost(10);
        LDAPQuery ldapQuery = queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
        String filter = ldapQuery.encode();

        assertEquals("(&(objectCategory=Role)(cn=admins))", filter);
    }

    public void testSearchInactiveGroups()
    {
        EntityQuery query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group(GroupType.GROUP)).with(Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(false)).returningAtMost(10);

        try
        {
            queryTranslater.asLDAPFilter(query, ldapPropertiesMapper);
            fail("NullResultException expected");
        }
        catch (NullResultException e)
        {
            // expected
        }
    }


}
