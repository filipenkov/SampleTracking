package com.atlassian.crowd.embedded.core.util;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.PropertyUtils;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConversionUtilsTest
{
    @Test
    public void testToEmbeddedGroup() throws Exception
    {
        // basic group
        GroupTemplate modelGroup = new GroupTemplate("name", 0, GroupType.GROUP);
        Group group = ConversionUtils.toEmbeddedGroup(modelGroup);
        assertEquals(modelGroup.getName(), group.getName());

        // null group
        assertNull(ConversionUtils.toEmbeddedGroup(null));
    }

    private void assertAttributesEqual(Attributes expected, Attributes actual)
    {
        assertEquals(expected.getKeys(), actual.getKeys());
        for (String key : expected.getKeys())
        {
            assertEquals(expected.getValues(key), actual.getValues(key));
        }
    }

    @Test
    public void testToEmbeddedGroupWithAttributes() throws Exception
    {
        // basic group without attributes
        GroupTemplateWithAttributes modelGroup = new GroupTemplateWithAttributes("name", 0, GroupType.GROUP);
        GroupWithAttributes group = ConversionUtils.toEmbeddedGroupWithAttributes(modelGroup);
        assertEquals(modelGroup.getName(), group.getName());
        assertAttributesEqual(modelGroup, group);

        // basic group with attributes
        modelGroup.setAttribute("key", "value");
        modelGroup.setAttribute("key", ImmutableSet.of("value1", "value2"));
        group = ConversionUtils.toEmbeddedGroupWithAttributes(modelGroup);
        assertEquals(modelGroup.getName(), group.getName());
        assertAttributesEqual(modelGroup, group);

        // null
        assertNull(ConversionUtils.toEmbeddedGroupWithAttributes(null));
    }

    @Test
    public void testToEmbeddedGroups() throws Exception
    {
        List<com.atlassian.crowd.model.group.Group> modelGroups = Arrays.<com.atlassian.crowd.model.group.Group>asList(new GroupTemplate("a"), new GroupTemplate("b"));

        // multiple
        List<Group> groups = ConversionUtils.toEmbeddedGroups(modelGroups);
        assertEquals(modelGroups.size(), groups.size());
        for (int i = 0; i < groups.size(); i++)
        {
            assertEquals(modelGroups.get(i).getName(), groups.get(i).getName());
        }

        // empty
        groups = ConversionUtils.toEmbeddedGroups(Collections.<com.atlassian.crowd.model.group.Group>emptyList());
        assertTrue(groups.isEmpty());

        // null
        assertNull(ConversionUtils.toEmbeddedGroups(null));
    }

    @Test
    public void testToModelUserQuery() throws Exception
    {
        UserQuery<User> query = new UserQuery<User>(User.class, Restriction.on(PropertyUtils.ofTypeString("key")).exactlyMatching("val"), 1, 2);

        UserQuery<com.atlassian.crowd.model.user.User> modelQuery = ConversionUtils.toModelUserQuery(query);

        assertEquals(com.atlassian.crowd.model.user.User.class, modelQuery.getReturnType());
        assertEquals(query.getSearchRestriction(), modelQuery.getSearchRestriction());
        assertEquals(query.getEntityDescriptor(), modelQuery.getEntityDescriptor());
        assertEquals(query.getMaxResults(), modelQuery.getMaxResults());
        assertEquals(query.getStartIndex(), modelQuery.getStartIndex());
    }

    @Test
    public void testToModelGroupQuery() throws Exception
    {
        GroupQuery<Group> query = new GroupQuery<Group>(Group.class, GroupType.GROUP, Restriction.on(PropertyUtils.ofTypeString("key")).exactlyMatching("val"), 1, 2);

        GroupQuery<com.atlassian.crowd.model.group.Group> modelQuery = ConversionUtils.toModelGroupQuery(query);

        assertEquals(com.atlassian.crowd.model.group.Group.class, modelQuery.getReturnType());
        assertEquals(query.getSearchRestriction(), modelQuery.getSearchRestriction());
        assertEquals(query.getEntityDescriptor(), modelQuery.getEntityDescriptor());
        assertEquals(query.getMaxResults(), modelQuery.getMaxResults());
        assertEquals(query.getStartIndex(), modelQuery.getStartIndex());
    }

    @Test
    public void testToModelUserMembershipQuery() throws Exception
    {
        MembershipQuery<User> query = new MembershipQuery<User>(User.class, true, EntityDescriptor.group(), "name", EntityDescriptor.user(), 1, 2);

        MembershipQuery<com.atlassian.crowd.model.user.User> modelQuery = ConversionUtils.toModelUserMembershipQuery(query);

        assertEquals(com.atlassian.crowd.model.user.User.class, modelQuery.getReturnType());
        assertEquals(query.isFindChildren(), modelQuery.isFindChildren());
        assertEquals(query.getEntityToReturn(), modelQuery.getEntityToReturn());
        assertEquals(query.getEntityNameToMatch(), modelQuery.getEntityNameToMatch());
        assertEquals(query.getEntityToMatch(), modelQuery.getEntityToMatch());
        assertEquals(query.getMaxResults(), modelQuery.getMaxResults());
        assertEquals(query.getStartIndex(), modelQuery.getStartIndex());
        assertEquals(query.getSearchRestriction(), modelQuery.getSearchRestriction());
    }

    @Test
    public void testToModelGroupMembershipQuery() throws Exception
    {
        MembershipQuery<Group> query = new MembershipQuery<Group>(Group.class, true, EntityDescriptor.group(), "name", EntityDescriptor.group(), 1, 2);

        MembershipQuery<com.atlassian.crowd.model.group.Group> modelQuery = ConversionUtils.toModelGroupMembershipQuery(query);

        assertEquals(com.atlassian.crowd.model.group.Group.class, modelQuery.getReturnType());
        assertEquals(query.isFindChildren(), modelQuery.isFindChildren());
        assertEquals(query.getEntityToReturn(), modelQuery.getEntityToReturn());
        assertEquals(query.getEntityNameToMatch(), modelQuery.getEntityNameToMatch());
        assertEquals(query.getEntityToMatch(), modelQuery.getEntityToMatch());
        assertEquals(query.getMaxResults(), modelQuery.getMaxResults());
        assertEquals(query.getStartIndex(), modelQuery.getStartIndex());
        assertEquals(query.getSearchRestriction(), modelQuery.getSearchRestriction());
    }
}
