package com.atlassian.crowd.embedded.core.util;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.DelegatingGroupWithAttributes;
import com.atlassian.crowd.embedded.impl.ImmutableAttributes;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting between model objects and embedded/application objects.
 */
public class ConversionUtils
{
    private ConversionUtils()
    {
        // util class not instantiable
    }

    /**
     * Converts a directory-specific model group into a directory-agnostic embedded/application group.
     *
     * @param modelGroup model group.
     * @return immutable clone meeting the minimum requirements for an embedded/application group.
     */
    public static Group toEmbeddedGroup(com.atlassian.crowd.model.group.Group modelGroup)
    {
        return modelGroup == null ? null : new ImmutableGroup(modelGroup.getName());
    }

    /**
     * Converts a directory-specific model group (with attributes) into a directory-agnostic
     * embedded/application group (with attributes).
     *
     * @param modelGroup model group with attributes.
     * @return immutable clone meeting the minimum requirements for an embedded/application group with attributes.
     */
    public static GroupWithAttributes toEmbeddedGroupWithAttributes(com.atlassian.crowd.model.group.GroupWithAttributes modelGroup)
    {
        return modelGroup == null ? null : new DelegatingGroupWithAttributes(toEmbeddedGroup(modelGroup), new ImmutableAttributes(modelGroup));
    }

    /**
     * Converts a directory-specific model groups into a directory-agnostic
     * embedded/application groups.
     *
     * @param modelGroups model groups.
     * @return immutable clones meeting the minimum requirements for an embedded/application group.
     */
    public static List<Group> toEmbeddedGroups(List<com.atlassian.crowd.model.group.Group> modelGroups)
    {
        if (modelGroups == null)
        {
            return null;
        }

        // using google collections' transform will produce lazy transformations, which sucks if you do repeated "contains" on the list
        List<Group> groups = new ArrayList<Group>(modelGroups.size());

        for (com.atlassian.crowd.model.group.Group modelGroup : modelGroups)
        {
            groups.add(toEmbeddedGroup(modelGroup));
        }

        return groups;
    }

    /**
     * Extracts a directory-specific model group from the given {@link com.atlassian.crowd.exception.InvalidGroupException} and converts it into a directory-agnostic embedded/application group.
     * <p>
     * This is normally used to construct an embedded {@link com.atlassian.crowd.exception.embedded.InvalidGroupException}
     *
     * @param ex the InvalidGroupException
     * @return embedded/application group.
     */
    public static Group getEmbeddedGroup(final com.atlassian.crowd.exception.InvalidGroupException ex)
    {
        return toEmbeddedGroup(ex.getGroup());
    }

    /**
     * @param embeddedQuery query expected to return an embedded user.
     * @return equivalent query with the return type set to model users.
     */
    public static UserQuery<com.atlassian.crowd.model.user.User> toModelUserQuery(UserQuery embeddedQuery)
    {
        return new UserQuery<com.atlassian.crowd.model.user.User>(com.atlassian.crowd.model.user.User.class, embeddedQuery.getSearchRestriction(), embeddedQuery.getStartIndex(), embeddedQuery.getMaxResults());
    }

    /**
     * @param embeddedQuery query expected to return an embedded group.
     * @return equivalent query with the return type set to model groups.
     */
    public static GroupQuery<com.atlassian.crowd.model.group.Group> toModelGroupQuery(GroupQuery embeddedQuery)
    {
        return new GroupQuery<com.atlassian.crowd.model.group.Group>(com.atlassian.crowd.model.group.Group.class, embeddedQuery.getEntityDescriptor().getGroupType(), embeddedQuery.getSearchRestriction(), embeddedQuery.getStartIndex(), embeddedQuery.getMaxResults());
    }

    /**
     * @param embeddedQuery query expected to return an embedded user.
     * @return equivalent query with the return type set to model users.
     */
    public static MembershipQuery<com.atlassian.crowd.model.user.User> toModelUserMembershipQuery(MembershipQuery embeddedQuery)
    {
        return new MembershipQuery<com.atlassian.crowd.model.user.User>(com.atlassian.crowd.model.user.User.class, embeddedQuery.isFindChildren(), embeddedQuery.getEntityToMatch(), embeddedQuery.getEntityNameToMatch(), embeddedQuery.getEntityToReturn(), embeddedQuery.getStartIndex(), embeddedQuery.getMaxResults());
    }

    /**
     * @param embeddedQuery query expected to return an embedded group.
     * @return equivalent query with the return type set to model groups.
     */
    public static MembershipQuery<com.atlassian.crowd.model.group.Group> toModelGroupMembershipQuery(MembershipQuery embeddedQuery)
    {
        return new MembershipQuery<com.atlassian.crowd.model.group.Group>(com.atlassian.crowd.model.group.Group.class, embeddedQuery.isFindChildren(), embeddedQuery.getEntityToMatch(), embeddedQuery.getEntityNameToMatch(), embeddedQuery.getEntityToReturn(), embeddedQuery.getStartIndex(), embeddedQuery.getMaxResults());
    }

    /**
     * Converts a list of model Users to a list of embedded Users.
     *
     * @param modelUsers a list of model Users.
     * @return equivalent list of embedded Users
     */
    public static List<User> toEmbeddedUsers(List<com.atlassian.crowd.model.user.User> modelUsers)
    {
        return Lists.<User>newArrayList(modelUsers);
    }
}
