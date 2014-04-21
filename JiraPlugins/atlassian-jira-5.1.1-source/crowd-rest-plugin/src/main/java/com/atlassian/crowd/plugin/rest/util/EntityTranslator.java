package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.crowd.directory.MultiValuedAttributeValuesHolder;
import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.plugin.rest.entity.AbstractEventEntity;
import com.atlassian.crowd.plugin.rest.entity.EventEntityList;
import com.atlassian.crowd.plugin.rest.entity.GroupEntity;
import com.atlassian.crowd.plugin.rest.entity.GroupEntityList;
import com.atlassian.crowd.plugin.rest.entity.GroupEventEntity;
import com.atlassian.crowd.plugin.rest.entity.GroupMembershipEventEntity;
import com.atlassian.crowd.plugin.rest.entity.MultiValuedAttributeEntity;
import com.atlassian.crowd.plugin.rest.entity.MultiValuedAttributeEntityList;
import com.atlassian.crowd.plugin.rest.entity.PasswordEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntityList;
import com.atlassian.crowd.plugin.rest.entity.UserEventEntity;
import com.atlassian.crowd.plugin.rest.entity.UserMembershipEventEntity;
import com.atlassian.plugins.rest.common.Link;
import com.google.common.collect.Sets;
import org.apache.commons.lang.Validate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates between REST entities and <tt>com.atlassian.crowd.model</tt> classes.
 *
 * @since v2.1
 */
public class EntityTranslator
{
    private EntityTranslator()
    {
        // prevent instantiation
    }

    /**
     * Translates a User to a UserEntity.
     *
     * @param user User to convert
     * @param userLink Link to user resource
     * @return UserEntity if user is not null, otherwise null
     */
    public static UserEntity toUserEntity(final User user, final Link userLink)
    {
        if (user == null)
        {
            return null;
        }

        Validate.notNull(userLink);

        UserEntity userEntity = new UserEntity(user.getName(), user.getFirstName(), user.getLastName(), user.getDisplayName(), user.getEmailAddress(), null, user.isActive(), userLink);
        userEntity.setAttributes(getEmptyAttributes(userLink));
        userEntity.setPassword(getEmptyPassword(userLink));
        return userEntity;
    }

    /**
     * Translates a list of users to a list of UserEntities.
     *
     * @param users list of users to convert
     * @param baseUri base URI
     * @return list of UserEntities
     */
    public static UserEntityList toUserEntities(final List<User> users, final URI baseUri)
    {
        final List<UserEntity> userEntities = new ArrayList<UserEntity>(users.size());
        for (User user : users)
        {
            userEntities.add(toUserEntity(user, LinkUriHelper.buildUserLink(baseUri, user.getName())));
        }
        return new UserEntityList(userEntities);
    }

    /**
     * Translates a list of usernames to a UserEntityList.
     *
     * @param usernames usernames to translate
     * @param baseUri base URI
     * @return list of UserEntities
     */
    public static UserEntityList toMinimalUserEntities(List<String> usernames, URI baseUri)
    {
        final List<UserEntity> userEntities = new ArrayList<UserEntity>(usernames.size());
        for (String username : usernames)
        {
            userEntities.add(UserEntity.newMinimalUserEntity(username, null, LinkUriHelper.buildUserLink(baseUri, username)));
        }
        return new UserEntityList(userEntities);
    }

    /**
     * Translates a UserEntity to a UserWithAttributes.
     *
     * @param userEntity UserEntity to convert
     * @return UserWithAttributes if user is not null, otherwise null
     */
    public static UserWithAttributes fromUserEntity(final UserEntity userEntity)
    {
        if (userEntity == null)
        {
            return null;
        }

        UserTemplateWithAttributes user = new UserTemplateWithAttributes(userEntity.getName(), -1L);
        user.setFirstName(userEntity.getFirstName());
        user.setLastName(userEntity.getLastName());
        user.setDisplayName(userEntity.getDisplayName());
        user.setEmailAddress(userEntity.getEmail());
        user.setActive(userEntity.isActive() != null ? userEntity.isActive() : false);

        if (userEntity.getAttributes() != null)
        {
            for (MultiValuedAttributeEntity attributeEntity : userEntity.getAttributes())
            {
                user.setAttribute(attributeEntity.getName(), Sets.newHashSet(attributeEntity.getValues()));
            }
        }

        return user;
    }

    /**
     * Translates a User with Attributes to a UserEntity.
     *
     * @param user User
     * @param attributes attributes of the user.
     * @param userLink link to the user.
     * @return UserEntity if user is not null, otherwise null
     */
    public static UserEntity toUserEntity(final User user, final Attributes attributes, final Link userLink)
    {
        if (user == null)
        {
            return null;
        }

        Validate.notNull(attributes);
        Validate.notNull(userLink);

        UserEntity userEntity = toUserEntity(user, userLink);
        Link userAttributesLink = Link.self(LinkUriHelper.buildEntityAttributeListUri(userLink.getHref()));
        userEntity.setAttributes(toMultiValuedAttributeEntityList(attributes, userAttributesLink));
        return userEntity;
    }

    /**
     * Translates a Group to a GroupEntity.
     *
     * @param group Group to convert
     * @param baseURI base URI
     * @return GroupEntity
     */
    public static GroupEntity toGroupEntity(final Group group, final URI baseURI)
    {
        return toGroupEntity(group, LinkUriHelper.buildGroupLink(baseURI, group.getName()));
    }

    /**
     * Translates a list of Groups to a list of GroupEntities.
     *
     * @param groups List of groups to convert
     * @param baseURI base URI
     * @return list of GroupEntities
     */
    public static GroupEntityList toGroupEntities(final List<Group> groups, final URI baseURI)
    {
        final List<GroupEntity> groupEntities = new ArrayList<GroupEntity>(groups.size());
        for (Group group : groups)
        {
            groupEntities.add(toGroupEntity(group, baseURI));
        }
        return new GroupEntityList(groupEntities);
    }

    /**
     * Translates a list of group names to a GroupEntityList.
     *
     * @param groupNames group names to translate
     * @param baseUri base URI
     * @return group names as a GroupEntityList
     */
    public static GroupEntityList toMinimalGroupEntities(Collection<String> groupNames, URI baseUri)
    {
        final List<GroupEntity> groupEntities = new ArrayList<GroupEntity>(groupNames.size());
        for (String groupName : groupNames)
        {
            groupEntities.add(GroupEntity.newMinimalGroupEntity(groupName, null, baseUri));
        }
        return new GroupEntityList(groupEntities);
    }

    /**
     * Translates a Group to a GroupEntity.
     *
     * @param group Group to convert
     * @param groupLink Link to group resource
     * @return GroupEntity
     */
    public static GroupEntity toGroupEntity(final Group group, final Link groupLink)
    {
        GroupEntity groupEntity = new GroupEntity(group.getName(), group.getDescription(), group.getType(), group.isActive(), groupLink);
        groupEntity.setAttributes(getEmptyAttributes(groupLink));
        return groupEntity;
    }

    /**
     * Translates a Group with Attributes to a GroupEntity.
     *
     * @param group Group
     * @param attributes attributes of the group.
     * @param groupLink link to the group.
     * @return GroupEntity
     */
    public static GroupEntity toGroupEntity(final Group group, final Attributes attributes, final Link groupLink)
    {
        GroupEntity groupEntity = toGroupEntity(group, groupLink);
        Link groupAttributesLink = Link.self(LinkUriHelper.buildEntityAttributeListUri(groupLink.getHref()));
        groupEntity.setAttributes(toMultiValuedAttributeEntityList(attributes, groupAttributesLink));
        return groupEntity;
    }

    public static GroupTemplate toGroup(final GroupEntity groupEntity)
    {
        final GroupTemplate group = new GroupTemplate(groupEntity.getName());
        group.setDescription(groupEntity.getDescription());
        group.setType(groupEntity.getType());
        group.setActive(groupEntity.isActive());

        return group;
    }

    /**
     * Translates Attributes to MultiValuedAttributeEntityList.
     *
     * @param attributes attributes of an entity
     * @param link link to the attributes
     * @return MultiValuedAttributeEntityList if attributes is not null, otherwise null
     */
    public static MultiValuedAttributeEntityList toMultiValuedAttributeEntityList(final Map<String, Set<String>> attributes, final Link link)
    {
        if (attributes == null)
        {
            return null;
        }

        return toMultiValuedAttributeEntityList(new MultiValuedAttributeValuesHolder(attributes), link);
    }

    /**
     * Translates Attributes to MultiValuedAttributeEntityList.
     * 
     * @param attributes attributes of an entity
     * @param link link to the attributes
     * @return MultiValuedAttributeEntityList if attributes is not null, otherwise null
     */
    public static MultiValuedAttributeEntityList toMultiValuedAttributeEntityList(final Attributes attributes, final Link link)
    {
        if (attributes == null)
        {
            return null;
        }

        Validate.notNull(link);

        Collection<String> keys = attributes.getKeys();
        List<MultiValuedAttributeEntity> attributeList = new ArrayList<MultiValuedAttributeEntity>(keys.size());
        for (String key : keys)
        {
            final Link attributeLink = Link.self(LinkUriHelper.buildEntityAttributeUri(link.getHref(), key));
            attributeList.add(new MultiValuedAttributeEntity(key, attributes.getValues(key), attributeLink));
        }
        return new MultiValuedAttributeEntityList(attributeList, link);
    }

    public static MultiValuedAttributeEntityList toDeletedAttributeEntityList(final Set<String> attributes)
    {
        if (attributes == null)
        {
            return null;
        }

        final List<MultiValuedAttributeEntity> attributeList = new ArrayList<MultiValuedAttributeEntity>(attributes.size());
        for (String attribute : attributes)
        {
            attributeList.add(new MultiValuedAttributeEntity(attribute, null, null));
        }
        return new MultiValuedAttributeEntityList(attributeList, null);
    }

    /**
     * Translates MultiValuedAttributeEntityList to Attributes.
     *
     * @param attributeEntityList attributes of an entity
     * @return attributes
     */
    public static Map<String, Set<String>> toAttributes(final MultiValuedAttributeEntityList attributeEntityList)
    {
        Map<String, Set<String>> attributes = new HashMap<String, Set<String>>(attributeEntityList.size());

        for (MultiValuedAttributeEntity attributeEntity : attributeEntityList)
        {
            attributes.put(attributeEntity.getName(), new HashSet<String>(attributeEntity.getValues()));
        }

        return attributes;
    }

    public static EventEntityList toEventEntities(final Events events, final URI baseUri)
    {
        final List<AbstractEventEntity> eventEntities = new ArrayList<AbstractEventEntity>();
        for (OperationEvent event : events.getEvents())
        {
            eventEntities.add(toEventEntity(event, baseUri));
        }

        return EventEntityList.create(events.getNewEventToken(), eventEntities);
    }

    public static AbstractEventEntity toEventEntity(OperationEvent event, final URI baseUri)
    {
        if (event instanceof UserEvent)
        {
            final UserEvent userEvent = (UserEvent) event;
            final Link userLink = LinkUriHelper.buildUserLink(baseUri, userEvent.getUser().getName());
            final UserEntity user = toUserEntity(userEvent.getUser(), userLink);
            final Link attributesLink = Link.self(LinkUriHelper.buildEntityAttributeListUri(userLink.getHref()));
            final MultiValuedAttributeEntityList storedAttributes = toMultiValuedAttributeEntityList(userEvent.getStoredAttributes(), attributesLink);
            final MultiValuedAttributeEntityList deletedAttributes = toDeletedAttributeEntityList(userEvent.getDeletedAttributes());
            return new UserEventEntity(userEvent.getOperation(), user, storedAttributes, deletedAttributes);
        }
        else if (event instanceof GroupEvent)
        {
            final GroupEvent groupEvent = (GroupEvent) event;
            final Link groupLink = LinkUriHelper.buildGroupLink(baseUri, groupEvent.getGroup().getName());
            final GroupEntity group = toGroupEntity(groupEvent.getGroup(), groupLink);
            final Link attributesLink = Link.self(LinkUriHelper.buildEntityAttributeListUri(groupLink.getHref()));
            final MultiValuedAttributeEntityList storedAttributes = toMultiValuedAttributeEntityList(groupEvent.getStoredAttributes(), attributesLink);
            final MultiValuedAttributeEntityList deletedAttributes = toDeletedAttributeEntityList(groupEvent.getDeletedAttributes());
            return new GroupEventEntity(groupEvent.getOperation(), group, storedAttributes, deletedAttributes);
        }
        else if (event instanceof UserMembershipEvent)
        {
            final UserMembershipEvent userMembershipEvent = (UserMembershipEvent) event;
            final Link userLink = LinkUriHelper.buildUserLink(baseUri, userMembershipEvent.getChildUsername());
            final UserEntity childUser = UserEntity.newMinimalUserEntity(userMembershipEvent.getChildUsername(), null, userLink);
            final GroupEntityList parentGroups = toMinimalGroupEntities(userMembershipEvent.getParentGroupNames(), baseUri);
            return new UserMembershipEventEntity(event.getOperation(), childUser, parentGroups);
        }
        else if (event instanceof GroupMembershipEvent)
        {
            final GroupMembershipEvent groupMembershipEvent = (GroupMembershipEvent) event;
            final GroupEntity group = GroupEntity.newMinimalGroupEntity(groupMembershipEvent.getGroupName(), null, baseUri);
            final GroupEntityList parentGroups = toMinimalGroupEntities(groupMembershipEvent.getParentGroupNames(), baseUri);
            final GroupEntityList childGroups = toMinimalGroupEntities(groupMembershipEvent.getChildGroupNames(), baseUri);
            return new GroupMembershipEventEntity(event.getOperation(), group, parentGroups, childGroups);
        }
        else
        {
            throw new IllegalArgumentException(event.getClass() + " is not supported");
        }
    }

    /**
     * Returns an empty MultiValuedAttributeEntityList.
     *
     * @param entityLink Link to the parent entity.
     * @return empty MultiValuedAttributeEntityList.
     */
    private static MultiValuedAttributeEntityList getEmptyAttributes(final Link entityLink)
    {
        Validate.notNull(entityLink);
        
        return new MultiValuedAttributeEntityList(Collections.<MultiValuedAttributeEntity>emptyList(), Link.self(LinkUriHelper.buildEntityAttributeListUri(entityLink.getHref())));
    }

    /**
     * Returns an empty password.
     *
     * @param userLink Link to the user entity.
     * @return empty PasswordEntity.
     */
    private static PasswordEntity getEmptyPassword(final Link userLink)
    {
        Validate.notNull(userLink);

        return new PasswordEntity(null, Link.edit(LinkUriHelper.buildUserPasswordUri(userLink.getHref())));
    }
}
