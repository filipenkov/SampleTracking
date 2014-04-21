package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import static com.atlassian.crowd.search.query.entity.restriction.MatchMode.EXACTLY_MATCHES;
import static com.atlassian.crowd.search.query.entity.restriction.MatchMode.STARTS_WITH;
import com.atlassian.crowd.search.query.entity.restriction.*;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.group.Group;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class QueryBuilderTest
{

    public void testQueryForActiveUser()
    {
        QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.ACTIVE).exactlyMatching(Boolean.TRUE)).startingAt(0).returningAtMost(1).getEntityDescriptor();

        QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(Restriction.on(UserTermKeys.DISPLAY_NAME).startingWith("bob")).startingAt(0).returningAtMost(1).getEntityDescriptor();
    }

    @Test
    public void testQueryForUserBasic()
    {
        UserQuery expected = new UserQuery(User.class, NullRestrictionImpl.INSTANCE, 0, 100);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(100);

        assertEquals(expected, result);
    }

    @Test
    public void testQueryForUserWithStartIndex()
    {
        UserQuery expected = new UserQuery(User.class, NullRestrictionImpl.INSTANCE, 1000, 100);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).startingAt(1000).returningAtMost(100);

        assertEquals(expected, result);
    }

    @Test
    public void testQueryForUserWithSingleRestriction()
    {
        UserQuery expected = new UserQuery(User.class, Restriction.on(UserTermKeys.USERNAME).startingWith("b"), 100, 10);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
                Restriction.on(UserTermKeys.USERNAME).startingWith("b")
        ).startingAt(100).returningAtMost(10);

        assertEquals(expected, result);
    }

    @Test
    public void testQueryForUserWithDisjuction()
    {
        BooleanRestriction disjunction = Combine.anyOf(
                Restriction.on(PropertyUtils.ofTypeString("color")).startingWith("red"),
                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("scarlet"),
                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("crimson")
        );
        UserQuery expected = new UserQuery(User.class, disjunction, 0, 50);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
                Combine.anyOf(
                        Restriction.on(PropertyUtils.ofTypeString("color")).startingWith("red"),
                        Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("scarlet"),
                        Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("crimson")
                )
        ).returningAtMost(50);

        assertEquals(expected, result);
    }

    @Test
    public void testQueryForUserWithConjunction()
    {
        BooleanRestriction disjunction = Combine.allOf(
                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("red"),
                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("blue")
        );
        UserQuery expected = new UserQuery(User.class, disjunction, 0, 50);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
                Combine.allOf(
                        Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("red"),
                        Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("blue")
                )
        ).returningAtMost(50);

        assertEquals(expected, result);
    }

    @Test
    public void testQueryForUserWithNesting()
    {
        PropertyRestriction<String> colorRed = new TermRestriction<String>(PropertyUtils.ofTypeString("color"), EXACTLY_MATCHES, "red");
        PropertyRestriction<String> colorBlue = new TermRestriction<String>(PropertyUtils.ofTypeString("color"), EXACTLY_MATCHES, "blue");
        PropertyRestriction<String> userNameR = new TermRestriction<String>(UserTermKeys.USERNAME, STARTS_WITH, "r");
        PropertyRestriction<String> userNameB = new TermRestriction<String>(UserTermKeys.USERNAME, STARTS_WITH, "b");
        BooleanRestriction conjuction1 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, colorRed, userNameR);
        BooleanRestriction conjuction2 = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND, colorBlue, userNameB);
        BooleanRestriction disjunction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.OR, conjuction1, conjuction2);
        UserQuery expected = new UserQuery(User.class, disjunction, 0, 10);

        EntityQuery result = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(
                Combine.anyOf(
                        Combine.allOf(
                                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("red"),
                                Restriction.on(UserTermKeys.USERNAME).startingWith("r")
                        ),
                        Combine.allOf(
                                Restriction.on(PropertyUtils.ofTypeString("color")).exactlyMatching("blue"),
                                Restriction.on(UserTermKeys.USERNAME).startingWith("b")
                        )
                )
        ).returningAtMost(10);

        assertEquals(expected, result);
    }

    @Test
    public void testInvalidGroupMembersOfUserQuery()
    {
        try
        {
            QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.user()).withName("blah").startingAt(10).returningAtMost(5);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {

        }
    }

    @Test
    public void testInvalidUserMembersOfUserQuery()
    {
        try
        {
            QueryBuilder.queryFor(Group.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.user()).withName("blah").startingAt(10).returningAtMost(5);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {

        }
    }

    @Test
    public void testInvalidUserMembershipsOfGroupQuery()
    {
        try
        {
            QueryBuilder.queryFor(Group.class, EntityDescriptor.user()).parentsOf(EntityDescriptor.group()).withName("blah").startingAt(10).returningAtMost(5);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {

        }
    }

    @Test
    public void testInvalidUserMembershipsOfUserQuery()
    {
        try
        {
            QueryBuilder.queryFor(Group.class, EntityDescriptor.user()).parentsOf(EntityDescriptor.user()).withName("blah").startingAt(10).returningAtMost(5);
            fail("IllegalArgumentException expected");
        }
        catch (IllegalArgumentException e)
        {

        }
    }
}
