package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.model.InternalEntityTemplate;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.Operation;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.AliasTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TranslatingApplicationServiceTest
{
    private static final String GROUP_NAME = "Group";

    private static final Group GROUP = new GroupTemplate(GROUP_NAME);

    private static final GroupTemplateWithAttributes GROUP_WITH_ATTRIBUTES = GroupTemplateWithAttributes.ofGroupWithNoAttributes(GROUP);
    static {
        GROUP_WITH_ATTRIBUTES.setAttribute("key", "value");
    }

    private static final ImmutableList<String> GROUP_NAMES = ImmutableList.of(GROUP_NAME);

    private static final String USER1_USERNAME = "User1";
    private static final String USER2_USERNAME = "User2";
    private static final String USER3_USERNAME = "User3";
    private static final String USER4A_USERNAME = "User4";
    private static final String USER4B_USERNAME = "USER4";
    private static final String USER5_USERNAME = "user5";
    private static final String USER5A_USERNAME = "User5";
    private static final String USER5B_USERNAME = "USER5";

    private static final String USER1_ALIAS = "Alias1";
    private static final String USER2_ALIAS = "Alias2";
    private static final String USER5_ALIAS = "Alias5";

    private static final ImmutableList<String> REAL_USERNAMES = ImmutableList.of(USER1_USERNAME, USER2_USERNAME, USER3_USERNAME);
    private static final ImmutableList<String> ALIASED_USERNAMES = ImmutableList.of(USER1_ALIAS, USER2_ALIAS, USER3_USERNAME); // User 3 has no alias

    private static final User USER1 = new UserTemplate(USER1_USERNAME, 1);
    private static final User USER2 = new UserTemplate(USER2_USERNAME, 2);
    private static final User USER3 = new UserTemplate(USER3_USERNAME, 1);
    private static final User USER4A = new UserTemplate(USER4A_USERNAME, 1);
    private static final User USER4B = new UserTemplate(USER4B_USERNAME, 2);
    private static final User USER5A = new UserTemplate(USER5A_USERNAME, 1);
    private static final User USER5B = new UserTemplate(USER5B_USERNAME, 2);

    private static final UserTemplateWithAttributes USER1_WITH_ATTRIBUTES = UserTemplateWithAttributes.ofUserWithNoAttributes(USER1);
    static {
        USER1_WITH_ATTRIBUTES.setAttribute("key", "value");
    }
    private static final UserTemplateWithAttributes USER3_WITH_ATTRIBUTES = UserTemplateWithAttributes.ofUserWithNoAttributes(USER3);
    static {
        USER3_WITH_ATTRIBUTES.setAttribute("key", "value");
    }

    private static final ImmutableList<User> REAL_USERS = ImmutableList.of(USER1, USER2, USER3);

    private static final User ALIASED_USER1 = new UserTemplate(USER1_ALIAS, 1);
    private static final User ALIASED_USER2 = new UserTemplate(USER2_ALIAS, 2);
    private static final User ALIASED_USER5A = new UserTemplate(USER5_ALIAS, 1);
    private static final User ALIASED_USER5B = new UserTemplate(USER5_ALIAS, 2);

    private static final UserTemplateWithAttributes ALIASED_USER1_WITH_ATTRIBUTES = UserTemplateWithAttributes.ofUserWithNoAttributes(ALIASED_USER1);
    static {
        ALIASED_USER1_WITH_ATTRIBUTES.setAttribute("key", "value");
    }

    private static final ImmutableList<User> ALIASED_USERS = ImmutableList.of(ALIASED_USER1, ALIASED_USER2, USER3); // User 3 has no alias

    private static final Directory DIRECTORY1 = new DirectoryImpl(new InternalEntityTemplate(1L, "1", true, null, null));
    private static final Directory DIRECTORY2 = new DirectoryImpl(new InternalEntityTemplate(2L, "2", true, null, null));

    @Mock private Application application;
    @Mock private ApplicationService applicationService;
    @Mock private AliasManager aliasManager;

    private TranslatingApplicationService translatingApplicationService;

    @Before
    public void setup() throws Exception
    {

        when(application.getId()).thenReturn(1L);
        when(application.isAliasingEnabled()).thenReturn(true);
        when(application.getDirectoryMappings()).thenReturn(ImmutableList.of(
                new DirectoryMapping(application, DIRECTORY1, false),
                new DirectoryMapping(application, DIRECTORY2, false)));

        when(applicationService.findUserByName(eq(application), matches("(?i)" + USER1_USERNAME))).thenReturn(USER1);
        when(applicationService.findUserByName(eq(application), matches("(?i)" + USER2_USERNAME))).thenReturn(USER2);
        when(applicationService.findUserByName(eq(application), matches("(?i)" + USER3_USERNAME))).thenReturn(USER3);
        when(applicationService.findUserByName(eq(application), matches("(?i)" + USER4A_USERNAME))).thenReturn(USER4A);
        when(applicationService.findUserByName(eq(application), matches("(?i)" + USER5_USERNAME))).thenReturn(USER5A);

        when(aliasManager.findAliasByUsername(eq(application), matches("(?i)" + USER1_USERNAME))).thenReturn(USER1_ALIAS);
        when(aliasManager.findAliasByUsername(eq(application), matches("(?i)" + USER2_USERNAME))).thenReturn(USER2_ALIAS);
        when(aliasManager.findAliasByUsername(eq(application), matches("(?i)" + USER3_USERNAME))).thenReturn(USER3_USERNAME); // User 3 has no alias
        when(aliasManager.findAliasByUsername(eq(application), matches("(?i)" + USER4A_USERNAME))).thenReturn(USER4A_USERNAME); // User 4 has no alias
        when(aliasManager.findAliasByUsername(eq(application), matches("(?i)" + USER5_USERNAME))).thenReturn(USER5_ALIAS);

        when(aliasManager.findUsernameByAlias(eq(application), matches("(?i)" + USER1_ALIAS))).thenReturn(USER1_USERNAME);
        when(aliasManager.findUsernameByAlias(eq(application), matches("(?i)" + USER2_ALIAS))).thenReturn(USER2_USERNAME);
        when(aliasManager.findUsernameByAlias(eq(application), matches("(?i)" + USER3_USERNAME))).thenReturn(USER3_USERNAME); // User 3 has no alias
        when(aliasManager.findUsernameByAlias(eq(application), matches("(?i)" + USER4A_USERNAME))).thenReturn(USER4A_USERNAME); // User 4 has no alias
        when(aliasManager.findUsernameByAlias(eq(application), matches("(?i)" + USER5_ALIAS))).thenReturn(USER5_USERNAME);

        translatingApplicationService = new TranslatingApplicationService(applicationService, aliasManager);
    }

    @Test
    public void testFindUserByName() throws Exception
    {
        when(applicationService.findUserByName(application, USER1_USERNAME)).thenReturn(USER1);

        assertEquals(ALIASED_USER1, translatingApplicationService.findUserByName(application, USER1_ALIAS));
    }

    @Test
    public void testFindUserByName_NoAlias() throws Exception
    {
        when(applicationService.findUserByName(application, USER3_USERNAME)).thenReturn(USER3);

        assertEquals(USER3, translatingApplicationService.findUserByName(application, USER3_USERNAME));
    }

    @Test
    public void testFindUserByName_LowerCaseOutput() throws Exception
    {
        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.findUserByName(application, USER3_USERNAME)).thenReturn(USER3);

        final User user = translatingApplicationService.findUserByName(application, USER3_USERNAME);
        assertEquals(toLowerCase(USER3_USERNAME), user.getName());
    }

    @Test
    public void testFindUserByName_LowerCaseInputAlias() throws Exception
    {
        when(applicationService.findUserByName(application, USER1_USERNAME)).thenReturn(USER1);

        final User user = translatingApplicationService.findUserByName(application, toLowerCase(USER1_ALIAS));
        assertEquals(USER1_ALIAS, user.getName());
    }

    @Test
    public void testFindUserByName_LowerCaseInputNoAlias() throws Exception
    {
        when(applicationService.findUserByName(application, toLowerCase(USER3_USERNAME))).thenReturn(USER3);

        final User user = translatingApplicationService.findUserByName(application, toLowerCase(USER3_USERNAME));
        assertEquals(USER3_USERNAME, user.getName());
    }

    @Test
    public void testFindUserWithAttributesByName() throws Exception
    {
        when(applicationService.findUserWithAttributesByName(application, USER1_USERNAME)).thenReturn(USER1_WITH_ATTRIBUTES);


        final UserWithAttributes user = translatingApplicationService.findUserWithAttributesByName(application, USER1_ALIAS);
        assertEquals(ALIASED_USER1_WITH_ATTRIBUTES, user);
        assertAttributesEquals(ALIASED_USER1_WITH_ATTRIBUTES, user);
    }

    @Test
    public void testFindUserWithAttributesByName_NoAlias() throws Exception
    {
        when(applicationService.findUserWithAttributesByName(application, USER3_USERNAME)).thenReturn(USER3_WITH_ATTRIBUTES);

        final UserWithAttributes user = translatingApplicationService.findUserWithAttributesByName(application, USER3_USERNAME);
        assertEquals(USER3_WITH_ATTRIBUTES, user);
        assertAttributesEquals(USER3_WITH_ATTRIBUTES, user);
    }

    @Test
    public void testFindGroupByName_LowerCaseOutput() throws Exception
    {
        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.findGroupByName(application, GROUP_NAME)).thenReturn(GROUP);

        final Group group = translatingApplicationService.findGroupByName(application, GROUP_NAME);
        assertEquals(toLowerCase(GROUP_NAME), group.getName());
    }

    @Test
    public void testFindGroupWithAttributesByName_LowerCaseOutput() throws Exception
    {
        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.findGroupWithAttributesByName(application, GROUP_NAME)).thenReturn(GROUP_WITH_ATTRIBUTES);

        final GroupWithAttributes group = translatingApplicationService.findGroupWithAttributesByName(application, GROUP_NAME);
        assertEquals(GROUP_WITH_ATTRIBUTES, group);
        assertAttributesEquals(GROUP_WITH_ATTRIBUTES, group);
    }

    @Test
    public void testSearchUsers_ExactUsername() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_ALIAS)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> replacedQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchUsers(application, replacedQuery)).thenReturn(ImmutableList.of(USER1));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS), result);
    }

    @Test
    public void testSearchUsers_Paging() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).startingAt(1).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> replacedQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchUsers(application, replacedQuery)).thenReturn(ImmutableList.of(USER1, USER2));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER2_ALIAS), result);
    }

    @Test
    public void testSearchUsers_AliasesDisabledPaging() throws Exception
    {
        EntityQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).startingAt(1).returningAtMost(EntityQuery.ALL_RESULTS);

        when(application.isAliasingEnabled()).thenReturn(false);
        // Alias manager returns the same username when aliasing is disabled
        when(aliasManager.findAliasByUsername(application, USER1_USERNAME)).thenReturn(USER1_USERNAME);
        when(aliasManager.findAliasByUsername(application, USER2_USERNAME)).thenReturn(USER2_USERNAME);
        when(applicationService.searchUsers(application, query)).thenReturn(ImmutableList.of(USER1_USERNAME, USER2_USERNAME));

        final List<String> result = translatingApplicationService.searchUsers(application, query);

        assertEquals(ImmutableList.of(USER1_USERNAME, USER2_USERNAME), result);
    }

    @Test
    public void testSearchUsers_NonExactUsernames() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS, USER3_USERNAME), result);
    }

    @Test
    public void testSearchUsers_UsernameMatchIgnored() throws Exception
    {
        // User with alias should be ignored when username matched but the alias did not
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(Collections.<String>emptyList());
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER1));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(Collections.<String>emptyList(), result);
    }

    @Test
    public void testSearchUsers_UsernameMatchedTwice() throws Exception
    {
        // Only one result should be returned when both the username and the alias matched
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER1));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS), result);
    }

    @Test
    public void testSearchUsers_AliasOnlyMatch() throws Exception
    {
        // It is enough for the alias to match
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(Collections.<User>emptyList());

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS), result);
    }

    @Test
    public void testSearchUsers_Users() throws Exception
    {
        EntityQuery<User> originalQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> aliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = originalQuery;

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER3));

        final List<User> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(ALIASED_USER1, USER3), result);
    }

    @Test
    public void testSearchUsers_AndQuery() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.allOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull())).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> otherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER3));
        when(applicationService.searchUsers(application, otherQuery)).thenReturn(ImmutableList.of(USER2, USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER3_USERNAME), result);
    }

    @Test
    public void testSearchUsers_OrQuery() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.anyOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull())).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> otherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER3));
        when(applicationService.searchUsers(application, otherQuery)).thenReturn(ImmutableList.of(USER2, USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS, USER2_ALIAS, USER3_USERNAME), result);
    }

    @Test
    public void testSearchUsers_OrQueryDuplicateUsers() throws Exception
    {
        EntityQuery<User> originalQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Combine.anyOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull())).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> otherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER4A));
        when(applicationService.searchUsers(application, otherQuery)).thenReturn(ImmutableList.of(USER4B));

        final List<User> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER4A), result);
    }

    @Test
    public void testSearchUsers_OrQueryDuplicateUsernames() throws Exception
    {
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.anyOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull())).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> otherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchUsers(application, userQuery)).thenReturn(ImmutableList.of(USER4A));
        when(applicationService.searchUsers(application, otherQuery)).thenReturn(ImmutableList.of(USER4B));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER4A_USERNAME), result);
    }

    @Test
    public void testSearchUsers_NestedQuery() throws Exception
    {
        // (username starts with 'b' OR email is null) AND username contains 'c'
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.allOf(Combine.anyOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull()), Restriction.on(UserTermKeys.USERNAME).containing("c"))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).containing("c"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).containing("c")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> nestedAliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUsersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUserQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedOtherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER2_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER2));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(Collections.<User>emptyList());
        when(aliasManager.search(nestedAliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, nestedUsersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, nestedUserQuery)).thenReturn(ImmutableList.of(USER3));
        when(applicationService.searchUsers(application, nestedOtherQuery)).thenReturn(ImmutableList.of(USER2, USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER2_ALIAS), result);
    }

    @Test
    public void testSearchUsers_NestedOrQuery() throws Exception
    {
        // (username starts with 'b' OR email is null) OR username contains 'c'
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.anyOf(Combine.anyOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull()), Restriction.on(UserTermKeys.USERNAME).containing("c"))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).containing("c"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).containing("c")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> nestedAliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUsersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUserQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedOtherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER2_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER2));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(Collections.<User>emptyList());
        when(aliasManager.search(nestedAliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME));
        when(applicationService.searchUsers(application, nestedUsersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, nestedUserQuery)).thenReturn(ImmutableList.of(USER3));
        when(applicationService.searchUsers(application, nestedOtherQuery)).thenReturn(ImmutableList.of(USER2, USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER1_ALIAS, USER2_ALIAS, USER3_USERNAME), result);
    }

    @Test
    public void testSearchUsers_NestedAndQuery() throws Exception
    {
        // (username starts with 'b' AND email is null) AND username contains 'c'
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.allOf(Combine.allOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull()), Restriction.on(UserTermKeys.USERNAME).containing("c"))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).containing("c"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).containing("c")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> nestedAliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUsersWithAliasQuery1 = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUsersWithAliasQuery2 = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedUserQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> nestedOtherQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER2_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, userQuery)).thenReturn(Collections.<User>emptyList());
        when(aliasManager.search(nestedAliasQuery)).thenReturn(ImmutableList.of(USER1_USERNAME, USER2_USERNAME));
        when(applicationService.searchUsers(application, nestedUsersWithAliasQuery1)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, nestedUsersWithAliasQuery2)).thenReturn(ImmutableList.of(USER2));
        when(applicationService.searchUsers(application, nestedUserQuery)).thenReturn(ImmutableList.of(USER3));
        when(applicationService.searchUsers(application, nestedOtherQuery)).thenReturn(ImmutableList.of(USER2, USER3));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER2_ALIAS), result);
    }

    @Test
    public void testSearchUsers_ManyTermsQuery() throws Exception
    {
        // username starts with 'b' AND email is null AND username contains 'c' and lastName is 'Jones'
        EntityQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(Combine.allOf(Restriction.on(UserTermKeys.USERNAME).startingWith("b"), Restriction.on(UserTermKeys.EMAIL).isNull(), Restriction.on(UserTermKeys.USERNAME).containing("c"), Restriction.on(UserTermKeys.LAST_NAME).exactlyMatching("Jones"))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQueryB = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQueryB = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQueryB = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> emailQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.EMAIL).isNull()).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQueryC = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).containing("c"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQueryC1 = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER1_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQueryC2 = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER2_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> userQueryC = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).containing("c")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> lastNameQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.LAST_NAME).exactlyMatching("Jones")).returningAtMost(EntityQuery.ALL_RESULTS);

        when(aliasManager.search(aliasQueryB)).thenReturn(ImmutableList.of(USER2_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQueryB)).thenReturn(ImmutableList.of(USER2));
        when(applicationService.searchUsers(application, userQueryB)).thenReturn(Collections.<User>emptyList());
        when(applicationService.searchUsers(application, emailQuery)).thenReturn(ImmutableList.of(USER2, USER3));
        when(aliasManager.search(aliasQueryC)).thenReturn(ImmutableList.of(USER1_USERNAME, USER2_USERNAME));
        when(applicationService.searchUsers(application, usersWithAliasQueryC1)).thenReturn(ImmutableList.of(USER1));
        when(applicationService.searchUsers(application, usersWithAliasQueryC2)).thenReturn(ImmutableList.of(USER2));
        when(applicationService.searchUsers(application, userQueryC)).thenReturn(ImmutableList.of(USER1, USER2));
        when(applicationService.searchUsers(application, lastNameQuery)).thenReturn(ImmutableList.of(USER1, USER2));

        final List<String> result = translatingApplicationService.searchUsers(application, originalQuery);

        assertEquals(ImmutableList.of(USER2_ALIAS), result);
    }

    @Test
    public void testSearchUsersAllowingDuplicateNames_DuplicateUsersNoAlias() throws Exception
    {
        EntityQuery<User> originalQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usernameQuery = originalQuery;

        when(applicationService.searchUsersAllowingDuplicateNames(application, usernameQuery)).thenReturn(ImmutableList.of(USER4B, USER4A));

        final List<User> result = translatingApplicationService.searchUsersAllowingDuplicateNames(application, originalQuery);

        assertEquals(ImmutableList.of(USER4A, USER4B), result);
    }

    @Test
    public void testSearchUsersAllowingDuplicateNames_DuplicateUsersAlias() throws Exception
    {
        EntityQuery<User> originalQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).startingWith("b")).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<String> aliasQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.alias()).with(Combine.allOf(Restriction.on(AliasTermKeys.ALIAS).startingWith("b"), Restriction.on(AliasTermKeys.APPLICATION_ID).exactlyMatching(application.getId()))).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usersWithAliasQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.USERNAME).exactlyMatching(USER5_USERNAME)).returningAtMost(EntityQuery.ALL_RESULTS);
        EntityQuery<User> usernameQuery = originalQuery;

        when(aliasManager.search(aliasQuery)).thenReturn(ImmutableList.of(USER5_USERNAME));
        when(applicationService.searchUsersAllowingDuplicateNames(application, usersWithAliasQuery)).thenReturn(ImmutableList.of(USER5B, USER5A));
        when(applicationService.searchUsersAllowingDuplicateNames(application, usernameQuery)).thenReturn(ImmutableList.of(USER5B, USER4A));

        final List<User> result = translatingApplicationService.searchUsersAllowingDuplicateNames(application, originalQuery);

        assertEquals(ImmutableList.of(ALIASED_USER5A, ALIASED_USER5B, USER4A), result);
    }

    @Test
    public void testSearchDirectGroupRelationships_Usernames()
    {
        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.LEGACY_ROLE)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(REAL_USERNAMES);

        assertEquals(ALIASED_USERNAMES, translatingApplicationService.searchDirectGroupRelationships(application, query));
    }

    @Test
    public void testSearchDirectGroupRelationships_LowerCaseOutputUsername()
    {
        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.LEGACY_ROLE)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(ImmutableList.of(USER1_USERNAME));

        assertEquals(ImmutableList.of(toLowerCase(USER1_ALIAS)), translatingApplicationService.searchDirectGroupRelationships(application, query));
    }

    @Test
    public void testSearchDirectGroupRelationships_Users()
    {
        final MembershipQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.LEGACY_ROLE)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(REAL_USERS);

        assertEquals(ALIASED_USERS, translatingApplicationService.searchDirectGroupRelationships(application, query));
    }

    @Test
    public void testSearchDirectGroupRelationships_LowerOutputCaseUsers()
    {
        final MembershipQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.LEGACY_ROLE)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(ImmutableList.of(USER1));

        final List<User> users = translatingApplicationService.searchDirectGroupRelationships(application, query);
        assertEquals(toLowerCase(USER1_ALIAS), Iterables.getOnlyElement(users).getName());
    }

    @Test
    public void testSearchNestedGroupRelationships_UsernamesForGroup()
    {
        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.GROUP)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchNestedGroupRelationships(application, query)).thenReturn(REAL_USERNAMES);

        assertEquals(ALIASED_USERNAMES, translatingApplicationService.searchNestedGroupRelationships(application, query));
    }

    @Test
    public void testSearchNestedGroupRelationships_UsersForGroup()
    {
        final MembershipQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(GroupType.GROUP)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchNestedGroupRelationships(application, query)).thenReturn(REAL_USERS);

        assertEquals(ALIASED_USERS, translatingApplicationService.searchNestedGroupRelationships(application, query));
    }

    @Test
    public void testSearchNestedGroupRelationships_GroupNames()
    {
        final MembershipQuery<String> originalQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP)).parentsOf(EntityDescriptor.user()).withName(USER1_ALIAS).returningAtMost(EntityQuery.ALL_RESULTS);
        final MembershipQuery<String> replacedQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP)).parentsOf(EntityDescriptor.user()).withName(USER1_USERNAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(applicationService.searchNestedGroupRelationships(application, replacedQuery)).thenReturn(GROUP_NAMES);

        assertEquals(GROUP_NAMES, translatingApplicationService.searchNestedGroupRelationships(application, originalQuery));
    }

    @Test
    public void testSearchDirectGroupRelationships_LowerCaseOutputGroupName()
    {
        final MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group(GroupType.GROUP)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(ImmutableList.of(GROUP_NAME));

        assertEquals(ImmutableList.of(toLowerCase(GROUP_NAME)), translatingApplicationService.searchDirectGroupRelationships(application, query));
    }

    @Test
    public void testSearchDirectGroupRelationships_LowerCaseOutputGroup()
    {
        final MembershipQuery<Group> query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group(GroupType.GROUP)).withName(GROUP_NAME).returningAtMost(EntityQuery.ALL_RESULTS);

        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.searchDirectGroupRelationships(application, query)).thenReturn(ImmutableList.of(GROUP));

        final List<Group> groups = translatingApplicationService.searchDirectGroupRelationships(application, query);
        assertEquals(toLowerCase(GROUP_NAME), Iterables.getOnlyElement(groups).getName());
    }

    @Test
    public void testGetNewEvents() throws Exception
    {
        final UserEvent userEvent = new UserEvent(Operation.CREATED, DIRECTORY1, USER1, null, null);
        final GroupEvent groupEvent = new GroupEvent(Operation.CREATED, DIRECTORY1, GROUP, null, null);
        final UserMembershipEvent userMembershipEvent = new UserMembershipEvent(Operation.UPDATED, DIRECTORY1, USER1_USERNAME, ImmutableSet.copyOf(GROUP_NAMES));
        final GroupMembershipEvent groupMembershipEvent = new GroupMembershipEvent(Operation.UPDATED, DIRECTORY1, GROUP_NAME, ImmutableSet.copyOf(GROUP_NAMES), ImmutableSet.copyOf(GROUP_NAMES));

        final Events events = new Events(ImmutableList.<OperationEvent>of(userEvent, groupEvent, userMembershipEvent, groupMembershipEvent), "eventToken");

        when(applicationService.getNewEvents(application, "eventToken")).thenReturn(events);

        final Events applicationEvents = translatingApplicationService.getNewEvents(application, "eventToken");
        final List<OperationEvent> applicationEventList = ImmutableList.copyOf(applicationEvents.getEvents());

        final UserEvent applicationUserEvent = (UserEvent) applicationEventList.get(0);
        final GroupEvent applicationGroupEvent = (GroupEvent) applicationEventList.get(1);
        final UserMembershipEvent applicationUserMembershipEvent = (UserMembershipEvent) applicationEventList.get(2);
        final GroupMembershipEvent applicationGroupMembershipEvent = (GroupMembershipEvent) applicationEventList.get(3);

        assertEquals(USER1_ALIAS, applicationUserEvent.getUser().getName());
        assertEquals(GROUP_NAME, applicationGroupEvent.getGroup().getName());
        assertEquals(USER1_ALIAS, applicationUserMembershipEvent.getChildUsername());
        assertEquals(GROUP_NAME, applicationUserMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getGroupName());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getChildGroupNames().iterator().next());
    }

    @Test
    public void testGetNewEvents_LowerCaseOutput() throws Exception
    {
        final UserEvent userEvent = new UserEvent(Operation.CREATED, DIRECTORY1, USER1, null, null);
        final GroupEvent groupEvent = new GroupEvent(Operation.CREATED, DIRECTORY1, GROUP, null, null);
        final UserMembershipEvent userMembershipEvent = new UserMembershipEvent(Operation.UPDATED, DIRECTORY1, USER1_USERNAME, ImmutableSet.copyOf(GROUP_NAMES));
        final GroupMembershipEvent groupMembershipEvent = new GroupMembershipEvent(Operation.UPDATED, DIRECTORY1, GROUP_NAME, ImmutableSet.copyOf(GROUP_NAMES), ImmutableSet.copyOf(GROUP_NAMES));

        final Events events = new Events(ImmutableList.<OperationEvent>of(userEvent, groupEvent, userMembershipEvent, groupMembershipEvent), "eventToken");

        when(application.isLowerCaseOutput()).thenReturn(true);
        when(applicationService.getNewEvents(application, "eventToken")).thenReturn(events);

        final Events applicationEvents = translatingApplicationService.getNewEvents(application, "eventToken");
        final List<OperationEvent> applicationEventList = ImmutableList.copyOf(applicationEvents.getEvents());

        final UserEvent applicationUserEvent = (UserEvent) applicationEventList.get(0);
        final GroupEvent applicationGroupEvent = (GroupEvent) applicationEventList.get(1);
        final UserMembershipEvent applicationUserMembershipEvent = (UserMembershipEvent) applicationEventList.get(2);
        final GroupMembershipEvent applicationGroupMembershipEvent = (GroupMembershipEvent) applicationEventList.get(3);

        assertEquals(toLowerCase(USER1_ALIAS), applicationUserEvent.getUser().getName());
        assertEquals(toLowerCase(GROUP_NAME), applicationGroupEvent.getGroup().getName());
        assertEquals(toLowerCase(USER1_ALIAS), applicationUserMembershipEvent.getChildUsername());
        assertEquals(toLowerCase(GROUP_NAME), applicationUserMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(toLowerCase(GROUP_NAME), applicationGroupMembershipEvent.getGroupName());
        assertEquals(toLowerCase(GROUP_NAME), applicationGroupMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(toLowerCase(GROUP_NAME), applicationGroupMembershipEvent.getChildGroupNames().iterator().next());
    }

    @Test
    public void testGetNewEvents_NoAlias() throws Exception
    {


        final UserEvent userEvent = new UserEvent(Operation.CREATED, DIRECTORY1, USER1, null, null);
        final GroupEvent groupEvent = new GroupEvent(Operation.CREATED, DIRECTORY1, GROUP, null, null);
        final UserMembershipEvent userMembershipEvent = new UserMembershipEvent(Operation.UPDATED, DIRECTORY1, USER1_USERNAME, ImmutableSet.copyOf(GROUP_NAMES));
        final GroupMembershipEvent groupMembershipEvent = new GroupMembershipEvent(Operation.UPDATED, DIRECTORY1, GROUP_NAME, ImmutableSet.copyOf(GROUP_NAMES), ImmutableSet.copyOf(GROUP_NAMES));

        final Events events = new Events(ImmutableList.<OperationEvent>of(userEvent, groupEvent, userMembershipEvent, groupMembershipEvent), "eventToken");

        when(application.isAliasingEnabled()).thenReturn(false);
        when(applicationService.getNewEvents(application, "eventToken")).thenReturn(events);

        final Events applicationEvents = translatingApplicationService.getNewEvents(application, "eventToken");
        final List<OperationEvent> applicationEventList = ImmutableList.copyOf(applicationEvents.getEvents());

        final UserEvent applicationUserEvent = (UserEvent) applicationEventList.get(0);
        final GroupEvent applicationGroupEvent = (GroupEvent) applicationEventList.get(1);
        final UserMembershipEvent applicationUserMembershipEvent = (UserMembershipEvent) applicationEventList.get(2);
        final GroupMembershipEvent applicationGroupMembershipEvent = (GroupMembershipEvent) applicationEventList.get(3);

        assertEquals(USER1_USERNAME, applicationUserEvent.getUser().getName());
        assertEquals(GROUP_NAME, applicationGroupEvent.getGroup().getName());
        assertEquals(USER1_USERNAME, applicationUserMembershipEvent.getChildUsername());
        assertEquals(GROUP_NAME, applicationUserMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getGroupName());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getParentGroupNames().iterator().next());
        assertEquals(GROUP_NAME, applicationGroupMembershipEvent.getChildGroupNames().iterator().next());
    }

    private static void assertAttributesEquals(Attributes a1, Attributes a2)
    {
        assertEquals(a1.getKeys(), a2.getKeys());

        for (String key : a1.getKeys())
        {
            assertEquals(a1.getValues(key), a2.getValues(key));
        }
    }
}
