package com.atlassian.crowd.integration.rest.util;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.integration.rest.entity.AbstractEventEntity;
import com.atlassian.crowd.integration.rest.entity.EventEntityList;
import com.atlassian.crowd.integration.rest.entity.GroupEntity;
import com.atlassian.crowd.integration.rest.entity.GroupEntityList;
import com.atlassian.crowd.integration.rest.entity.GroupEventEntity;
import com.atlassian.crowd.integration.rest.entity.GroupMembershipEventEntity;
import com.atlassian.crowd.integration.rest.entity.MultiValuedAttributeEntity;
import com.atlassian.crowd.integration.rest.entity.MultiValuedAttributeEntityList;
import com.atlassian.crowd.integration.rest.entity.PasswordEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntityList;
import com.atlassian.crowd.integration.rest.entity.UserEventEntity;
import com.atlassian.crowd.integration.rest.entity.UserMembershipEventEntity;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import org.apache.commons.lang.Validate;

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
     * @return UserEntity if user is not null, otherwise null
     */
    public static UserEntity toUserEntity(final User user)
    {
        return toUserEntity(user, (PasswordCredential) null);
    }

    /**
     * Translates a User to a UserEntity.
     *
     * @param user User to convert
     * @param passwordCredential user password
     * @return UserEntity if user is not null, otherwise null
     */
    public static UserEntity toUserEntity(final User user, final PasswordCredential passwordCredential)
    {
        if (user == null)
        {
            return null;
        }

        PasswordEntity password = null;
        if (passwordCredential != null)
        {
            password = new PasswordEntity(passwordCredential.getCredential());
        }

        return new UserEntity(user.getName(), user.getFirstName(), user.getLastName(), user.getDisplayName(), user.getEmailAddress(), password, user.isActive());
    }

    /**
     * Translates a User with Attributes to a UserEntity.
     *
     * @param user User
     * @param attributes attributes of the user.
     * @return UserEntity if user is not null, otherwise null
     */
    public static UserEntity toUserEntity(final User user, final Attributes attributes)
    {
        if (user == null)
        {
            return null;
        }

        Validate.notNull(attributes);

        UserEntity userEntity = toUserEntity(user);
        userEntity.setAttributes(toMultiValuedAttributeEntityList(attributes));
        return userEntity;
    }

    /**
     * Translates a Group to a GroupEntity.
     *
     * @param group Group to convert
     * @return GroupEntity
     */
    public static GroupEntity toGroupEntity(final Group group)
    {
        return new GroupEntity(group.getName(), group.getDescription(), group.getType(), group.isActive());
    }

    /**
     * Translates a Group with Attributes to a GroupEntity.
     *
     * @param group Group
     * @param attributes attributes of the group.
     * @return GroupEntity
     */
    public static GroupEntity toGroupEntity(final Group group, final Attributes attributes)
    {
        GroupEntity groupEntity = toGroupEntity(group);
        groupEntity.setAttributes(toMultiValuedAttributeEntityList(attributes));
        return groupEntity;
    }

    /**
     * Translates Attributes to MultiValuedAttributeEntityList.
     * 
     * @param attributes Attributes of an entity
     * @return MultiValuedAttributeEntityList if attributes is not null, otherwise null
     */
    public static MultiValuedAttributeEntityList toMultiValuedAttributeEntityList(final Attributes attributes)
    {
        if (attributes == null)
        {
            return null;
        }

        Collection<String> keys = attributes.getKeys();
        List<MultiValuedAttributeEntity> attributeList = new ArrayList<MultiValuedAttributeEntity>(keys.size());
        for (String key : keys)
        {
            attributeList.add(new MultiValuedAttributeEntity(key, attributes.getValues(key)));
        }
        return new MultiValuedAttributeEntityList(attributeList);
    }

    /**
     * Translates Attributes to MultiValuedAttributeEntityList.
     *
     * @param attributes Attributes of an entity
     * @return MultiValuedAttributeEntityList if attributes is not <tt>null</tt>, otherwise <tt>null</tt>
     */
    public static MultiValuedAttributeEntityList toMultiValuedAttributeEntityList(Map<String, Set<String>> attributes)
    {
        if (attributes == null)
        {
            return null;
        }

        final List<MultiValuedAttributeEntity> attributeEntities = new ArrayList<MultiValuedAttributeEntity>(attributes.size());
        for (Map.Entry<String, Set<String>> attribute : attributes.entrySet())
        {
            attributeEntities.add(new MultiValuedAttributeEntity(attribute.getKey(), attribute.getValue()));
        }
        return new MultiValuedAttributeEntityList(attributeEntities);
    }

    /**
     * Transforms GroupEntityList to a list of groups.
     *
     * @param groupEntityList GroupEntityList to transform
     * @return list of groups
     */
    public static List<Group> toGroupList(GroupEntityList groupEntityList)
    {
        final List<Group> groups = new ArrayList<Group>(groupEntityList.size());
        for (GroupEntity groupEntity : groupEntityList)
        {
            groups.add(groupEntity);
        }
        return groups;
    }

    /**
     * Transforms GroupEntityList to a list of group names.
     *
     * @param groupEntityList GroupEntityList to transform
     * @return list of group names
     */
    public static List<String> toNameList(GroupEntityList groupEntityList)
    {
        final List<String> names = new ArrayList<String>(groupEntityList.size());
        for (GroupEntity groupEntity : groupEntityList)
        {
            names.add(groupEntity.getName());
        }
        return names;
    }

    /**
     * Transforms UserEntityList to a list of users.
     *
     * @param userEntityList UserEntityList to transform
     * @return list of users
     */
    public static List<User> toUserList(UserEntityList userEntityList)
    {
        final List<User> users = new ArrayList<User>(userEntityList.size());
        for (UserEntity userEntity : userEntityList)
        {
            users.add(userEntity);
        }
        return users;
    }

    /**
     * Transforms UserEntityList to a list of usernames.
     *
     * @param userEntityList UserEntityList to transform
     * @return list of usernames
     */
    public static List<String> toNameList(UserEntityList userEntityList)
    {
        final List<String> names = new ArrayList<String>(userEntityList.size());
        for (UserEntity userEntity : userEntityList)
        {
            names.add(userEntity.getName());
        }
        return names;
    }
    
    public static Events toEvents(EventEntityList eventEntityList)
    {
        final List<AbstractEventEntity> eventEntities = eventEntityList.getEvents() != null ? eventEntityList.getEvents() : Collections.<AbstractEventEntity>emptyList();
        final List<OperationEvent> events = new ArrayList<OperationEvent>(eventEntities.size());
        for (AbstractEventEntity eventEntity : eventEntities)
        {
            events.add(toEvent(eventEntity));
        }
        return new Events(events, eventEntityList.getNewEventToken());
    }

    private static OperationEvent toEvent(AbstractEventEntity eventEntity)
    {
        if (eventEntity instanceof UserEventEntity)
        {
            final UserEventEntity userEventEntity = (UserEventEntity) eventEntity;
            return new UserEvent(eventEntity.getOperation(), null, userEventEntity.getUser(), toAttributes(userEventEntity.getStoredAttributes()), toAttributes(userEventEntity.getDeletedAttributes()).keySet());
        }
        else if (eventEntity instanceof GroupEventEntity)
        {
            final GroupEventEntity groupEventEntity = (GroupEventEntity) eventEntity;
            return new GroupEvent(eventEntity.getOperation(), null, groupEventEntity.getGroup(), toAttributes(groupEventEntity.getStoredAttributes()), toAttributes(groupEventEntity.getDeletedAttributes()).keySet());
        }
        else if (eventEntity instanceof UserMembershipEventEntity)
        {
            final UserMembershipEventEntity membershipEventEntity = (UserMembershipEventEntity) eventEntity;
            final Set<String> parentGroupNames = new HashSet<String>(toNameList(membershipEventEntity.getParentGroups()));
            return new UserMembershipEvent(eventEntity.getOperation(), null, membershipEventEntity.getChildUser().getName(), parentGroupNames);
        }
        else if (eventEntity instanceof GroupMembershipEventEntity)
        {
            final GroupMembershipEventEntity membershipEventEntity = (GroupMembershipEventEntity) eventEntity;
            final Set<String> parentGroupNames = new HashSet<String>(toNameList(membershipEventEntity.getParentGroups()));
            final Set<String> childGroupNames = new HashSet<String>(toNameList(membershipEventEntity.getChildGroups()));
            return new GroupMembershipEvent(eventEntity.getOperation(), null, membershipEventEntity.getGroup().getName(), parentGroupNames, childGroupNames);
        }
        else
        {
            throw new IllegalArgumentException(eventEntity.getClass() + " is not supported");
        }
    }

    /**
     * Translates AttributeEntityList to Attributes.
     *
     * @param attributeEntityList attributes of an entity
     * @return attributes
     */
    private static Map<String, Set<String>> toAttributes(final MultiValuedAttributeEntityList attributeEntityList)
    {
        if (attributeEntityList == null)
        {
            return Collections.emptyMap();
        }

        Map<String, Set<String>> attributes = new HashMap<String, Set<String>>(attributeEntityList.size());

        for (MultiValuedAttributeEntity attributeEntity : attributeEntityList)
        {
            final Set<String> values = attributeEntity.getValues() != null ? new HashSet<String>(attributeEntity.getValues()) : null;
            attributes.put(attributeEntity.getName(), values);
        }

        return attributes;
    }
}
