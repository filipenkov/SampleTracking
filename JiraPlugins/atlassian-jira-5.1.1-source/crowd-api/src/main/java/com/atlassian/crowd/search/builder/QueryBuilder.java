package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.AliasQuery;
import com.atlassian.crowd.search.query.entity.ApplicationQuery;
import com.atlassian.crowd.search.query.entity.DirectoryQuery;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.TokenQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.membership.GroupMembersOfGroupQuery;
import com.atlassian.crowd.search.query.membership.GroupMembershipQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.query.membership.UserMembersOfGroupQuery;
import com.atlassian.crowd.search.query.membership.UserMembershipQuery;
import org.apache.commons.lang.Validate;

/**
 * Recommended convenience class to build queries.
 * <p/>
 * Examples are presented below.
 * <ol>
 * <li> Find all users, return results indexed from 0 to 99
 * or less:
 * <p/>
 * <pre>
 * QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(100);
 * </pre>
 * This is the minimum required structure for a query.<p/>
 * </li>
 * <li> Find all users with the username matching b*, return results
 * indexed from 100 to 109 or less:
 * <pre>
 * QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
 *      Restriction.on(UserTermKeys.USERNAME).startingWith("b")
 *  ).startingAt(100).returningAtMost(10);
 * </pre>
 * </li>
 * <li> Find all users that have an attribute 'color' with a value
 * that matches red* OR scarlet OR crimson, returning results
 * indexed from 0 to 49 or less:
 * <pre>
 * QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
 * Combine.anyOf(
 *      Restriction.on(PropertyUtils.ofTypeString("color")).startingWith("red"),
 *      Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("scarlet"),
 *      Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("crimson")
 *  )
 * ).returningAtMost(50);
 * </pre>
 * </li>
 * <li> Find all users that like the color red AND blue, returning
 * results indexed from 0 to 49 (only return usernames):
 * <pre>
 * QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(
 * Combine.allOf(
 *      Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("red"),
 *      Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("blue")
 *  )
 * ).returningAtMost(50);
 * </pre>
 * </li>
 * <li> Find all users that like the color red and have a name
 * starting "r" or like blue and have a name starting with "b",
 * returning results indexed from 0 to 9 or less:
 * <pre>
 * QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
 *  Combine.anyOf(
 *      Combine.allOf(
 *          Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("red"),
 *          Restriction.on(UserTermKeys.USERNAME).startingWith("r")
 *      ),
 *      Combine.allOf(
 *          Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("blue"),
 *          Restriction.on(UserTermKeys.USERNAME).startingWith("b")
 *      )
 *  )
 * ).returningAtMost(10);
 * </pre>
 * <p/>
 * This is equivalent to verbosely constructing the same query as so:
 * <pre>
 * TermRestriction colorRed = new TermRestriction<String>(PropertyUtils.ofTypeString("color"), MatchMode.EXACTLY_MATCHES, "red");
 * TermRestriction colorBlue = new TermRestriction<String>(PropertyUtils.ofTypeString("color"), MatchMode.EXACTLY_MATCHES, "blue");
 * TermRestriction userNameR = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.STARTS_WITH, "r");
 * TermRestriction userNameB = new TermRestriction<String>(UserTermKeys.USERNAME, MatchMode.STARTS_WITH, "b");
 * BooleanRestriction conjuction1 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, colorRed, userNameR);
 * BooleanRestriction conjuction2 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, colorBlue, userNameB);
 * BooleanRestriction disjunction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR,  conjuction1, conjuction2);
 * UserQuery query = new UserQuery(User.class, disjunction, 0, 10);
 * </pre>
 * </li>
 * </ol>
 * Membership Queries
 * <ol>
 * <li> Find first 10 users of a group:
 * <pre>
 * QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName("group-name").returningAtMost(10);
 * </pre>
 * </li>
 * <li> Find first 10 users of a group (returning just the names):
 * <pre>
 * QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName("group-name").returningAtMost(10);
 * </pre>
 * </li>
 * <li> Find first 10 groups that are members (children) of a group:
 * <pre>
 * QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName("group-name").returningAtMost(10);
 * </pre>
 * </li>
 * <li> Find first 10 groups that a user belongs to:
 * <pre>
 * QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName("user-name").returningAtMost(10);
 * </pre>
 * </li>
 * <li> Find first 10 groups that a user belongs to (returning just the names):
 * <pre>
 * QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName("user-name").returningAtMost(10);
 * </pre>
 * </li>
 * </ol>
 */
public class QueryBuilder
{
    private static final SearchRestriction DEFAULT_RESTRICTION = NullRestrictionImpl.INSTANCE;
    private static final int DEFAULT_START_INDEX = 0;

    public static <T> PartialEntityQuery<T> queryFor(final Class<T> returnType, EntityDescriptor entity)
    {
        Validate.notNull(returnType, "returnType");
        Validate.notNull(entity, "entity");
        return new PartialEntityQuery<T>(returnType, entity);
    }

    public static <T> EntityQuery<T> queryFor(final Class<T> returnType, final EntityDescriptor entity, final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        if (Entity.USER.equals(entity.getEntityType()))
        {
            return new UserQuery<T>(returnType, searchRestriction, startIndex, maxResults);
        }
        else if (Entity.GROUP.equals(entity.getEntityType()))
        {
            return new GroupQuery<T>(returnType, entity.getGroupType(), searchRestriction, startIndex, maxResults);
        }
        else if (Entity.ALIAS.equals(entity.getEntityType()))
        {
            return (EntityQuery<T>) new AliasQuery(searchRestriction, startIndex, maxResults);
        }
        else if (Entity.APPLICATION.equals(entity.getEntityType()))
        {
            return (EntityQuery<T>) new ApplicationQuery(searchRestriction, startIndex, maxResults);
        }
        else if (Entity.DIRECTORY.equals(entity.getEntityType()))
        {
            return (EntityQuery<T>) new DirectoryQuery(searchRestriction, startIndex, maxResults);
        }
        else if (Entity.TOKEN.equals(entity.getEntityType()))
        {
            return (EntityQuery<T>) new TokenQuery(searchRestriction, startIndex, maxResults);
        }
        else
        {
            throw new IllegalStateException("Unknown entity type <" + entity + "> is not supported by the builder");
        }
    }

    public static <T> MembershipQuery<T> createMembershipQuery(int maxResults, int startIndex, boolean findMembers, EntityDescriptor entityToReturn, Class<T> returnType, EntityDescriptor entityToMatch, String nameToMatch)
    {
        if (findMembers && (entityToReturn.equals(EntityDescriptor.group()) || entityToReturn.equals(EntityDescriptor.role())))
        {
            return new GroupMembersOfGroupQuery<T>(returnType, findMembers, entityToMatch, nameToMatch, entityToReturn, startIndex, maxResults);
        }
        else if (findMembers && entityToReturn.equals(EntityDescriptor.user()))
        {
            return new UserMembersOfGroupQuery<T>(returnType, findMembers, entityToMatch, nameToMatch, entityToReturn, startIndex, maxResults);
        }
        else if (!findMembers && (entityToReturn.equals(EntityDescriptor.group()) || entityToReturn.equals(EntityDescriptor.role())))
        {
            return new GroupMembershipQuery<T>(returnType, findMembers, entityToMatch, nameToMatch, entityToReturn, startIndex, maxResults);
        }
        else if (!findMembers && entityToReturn.equals(EntityDescriptor.user()))
        {
            return new UserMembershipQuery<T>(returnType, findMembers, entityToMatch, nameToMatch, entityToReturn, startIndex, maxResults);
        }
        else
        {
            throw new IllegalStateException("What the f**k happened!");
        }
    }

    public static class PartialEntityQuery<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entity;

        public PartialEntityQuery(final Class<T> returnType, final EntityDescriptor entity)
        {
            this.returnType = returnType;
            this.entity = entity;
        }

        /**
         * Example: Restriction.on(UserTermKeys.FIRST_NAME).exactlyMatching("bob"))
         * @param restriction
         * @return
         */
        public PartialEntityQueryWithRestriction<T> with(SearchRestriction restriction)
        {
            return new PartialEntityQueryWithRestriction<T>(returnType, entity, restriction);
        }

        public PartialEntityQueryWithStartIndex<T> startingAt(int index)
        {
            return new PartialEntityQueryWithStartIndex<T>(returnType, entity, DEFAULT_RESTRICTION, index);
        }

        public EntityQuery<T> returningAtMost(int maxResults)
        {
            return queryFor(returnType, entity, DEFAULT_RESTRICTION, DEFAULT_START_INDEX, maxResults);
        }

        public PartialMembershipQueryWithEntityToMatch<T> childrenOf(EntityDescriptor entityToMatch)
        {
            return new PartialMembershipQueryWithEntityToMatch<T>(returnType, entity, entityToMatch, true);
        }

        public PartialMembershipQueryWithEntityToMatch<T> parentsOf(EntityDescriptor entityToMatch)
        {
            return new PartialMembershipQueryWithEntityToMatch<T>(returnType, entity, entityToMatch, false);
        }

        public Object ofType(GroupType groupType)
        {
            return null;// GroupQuery(groupType, ..
        }
    }

    public static class PartialMembershipQueryWithEntityToMatch<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entityToReturn;
        private final EntityDescriptor entityToMatch;
        private final boolean findMembers;

        public PartialMembershipQueryWithEntityToMatch(final Class<T> returnType, final EntityDescriptor entityToReturn, final EntityDescriptor entityToMatch, final boolean findMembers)
        {
            this.returnType = returnType;
            this.entityToReturn = entityToReturn;
            this.entityToMatch = entityToMatch;
            this.findMembers = findMembers;
        }

        public PartialMembershipQueryWithNameToMatch<T> withName(String name)
        {
            return new PartialMembershipQueryWithNameToMatch<T>(returnType, entityToReturn, entityToMatch, findMembers, name);
        }
    }

    public static class PartialMembershipQueryWithNameToMatch<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entityToReturn;
        private final EntityDescriptor entityToMatch;
        private final boolean findMembers;
        private final String nameToMatch;

        public PartialMembershipQueryWithNameToMatch(Class<T> returnType, final EntityDescriptor entityToReturn, final EntityDescriptor entityToMatch, final boolean findMembers, final String nameToMatch)
        {
            this.returnType = returnType;
            this.entityToReturn = entityToReturn;
            this.entityToMatch = entityToMatch;
            this.findMembers = findMembers;
            this.nameToMatch = nameToMatch;
        }

        public PartialMembershipQueryWithStartIndex<T> startingAt(int index)
        {
            return new PartialMembershipQueryWithStartIndex<T>(returnType, entityToReturn, entityToMatch, findMembers, nameToMatch, index);
        }

        public MembershipQuery<T> returningAtMost(int maxResults)
        {
            return createMembershipQuery(maxResults, DEFAULT_START_INDEX, findMembers, entityToReturn, returnType, entityToMatch, nameToMatch);
        }

    }

    public static class PartialMembershipQueryWithStartIndex<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entityToReturn;
        private final EntityDescriptor entityToMatch;
        private final boolean findMembers;
        private final String nameToMatch;
        private final int startIndex;

        public PartialMembershipQueryWithStartIndex(final Class<T> returnType, final EntityDescriptor entityToReturn, final EntityDescriptor entityToMatch, final boolean findMembers, final String nameToMatch, final int startIndex)
        {
            this.returnType = returnType;
            this.entityToReturn = entityToReturn;
            this.entityToMatch = entityToMatch;
            this.findMembers = findMembers;
            this.nameToMatch = nameToMatch;
            this.startIndex = startIndex;
        }

        public MembershipQuery<T> returningAtMost(int maxResults)
        {
            return createMembershipQuery(maxResults, startIndex, findMembers, entityToReturn, returnType, entityToMatch, nameToMatch);
        }
    }

    public static class PartialEntityQueryWithRestriction<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entity;
        private final SearchRestriction restriction;

        public PartialEntityQueryWithRestriction(Class<T> returnType, final EntityDescriptor entity, final SearchRestriction restriction)
        {
            this.returnType = returnType;
            this.entity = entity;
            this.restriction = restriction;
        }

        public PartialEntityQueryWithStartIndex<T> startingAt(int index)
        {
            return new PartialEntityQueryWithStartIndex<T>(returnType, entity, restriction, index);
        }

        public EntityQuery<T> returningAtMost(int maxResults)
        {
            return queryFor(returnType, entity, restriction, DEFAULT_START_INDEX, maxResults);
        }
    }

    public static class PartialEntityQueryWithStartIndex<T>
    {
        private final Class<T> returnType;
        private final EntityDescriptor entity;
        private final SearchRestriction restriction;
        private final int startIndex;

        public PartialEntityQueryWithStartIndex(Class<T> returnType, final EntityDescriptor entity, final SearchRestriction restriction, final int startIndex)
        {
            this.returnType = returnType;
            this.entity = entity;
            this.startIndex = startIndex;
            this.restriction = restriction;
        }

        public EntityQuery<T> returningAtMost(int maxResults)
        {
            return queryFor(returnType, entity, restriction, startIndex, maxResults);
        }
    }

}
