package com.atlassian.crowd.event;

import com.atlassian.crowd.event.application.ApplicationDirectoryAddedEvent;
import com.atlassian.crowd.event.application.ApplicationDirectoryOrderUpdatedEvent;
import com.atlassian.crowd.event.application.ApplicationDirectoryRemovedEvent;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.directory.DirectoryUpdatedEvent;
import com.atlassian.crowd.event.group.GroupAttributeDeletedEvent;
import com.atlassian.crowd.event.group.GroupAttributeStoredEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.group.GroupUpdatedEvent;
import com.atlassian.crowd.event.migration.XMLRestoreFinishedEvent;
import com.atlassian.crowd.event.user.UserAttributeDeletedEvent;
import com.atlassian.crowd.event.user.UserAttributeStoredEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.Operation;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class listens for events related to user and group changes and saves
 * them to {@link EventStore}.
 */
public class StoringEventListener
{
    private final EventStore eventStore;
    private final boolean ignoreAttributes;

    public StoringEventListener(final EventStore eventStore, final EventPublisher eventPublisher, final boolean ignoreAttributes)
    {
        this.eventStore = eventStore;
        eventPublisher.register(this);
        this.ignoreAttributes = ignoreAttributes;
    }

    @EventListener
    public void handleEvent(UserCreatedEvent event)
    {
        eventStore.storeEvent(new UserEvent(Operation.CREATED, event.getDirectory(), event.getUser(), null, null));
    }

    @EventListener
    public void handleEvent(UserUpdatedEvent event)
    {
        Map<String, Set<String>> storedAttributes = null;
        Set<String> deletedAttributes = null;

        if (event instanceof UserAttributeStoredEvent)
        {
            if (ignoreAttributes)
            {
                return;
            }
            final UserAttributeStoredEvent userAttributeStoredEvent = (UserAttributeStoredEvent) event;
            storedAttributes = new HashMap<String, Set<String>>();
            for (String storedAttribute : (Set<String>) userAttributeStoredEvent.getAttributeNames())
            {
                storedAttributes.put(storedAttribute, userAttributeStoredEvent.getAttributeValues(storedAttribute));
            }
        } else if (event instanceof UserAttributeDeletedEvent)
        {
            if (ignoreAttributes)
            {
                return;
            }
            deletedAttributes = Collections.singleton(((UserAttributeDeletedEvent) event).getAttributeName());
        }

        eventStore.storeEvent(new UserEvent(Operation.UPDATED, event.getDirectory(), event.getUser(), storedAttributes, deletedAttributes));
    }

    @EventListener
    public void handleEvent(UserDeletedEvent event)
    {
        final User user = new UserTemplate(event.getUsername(), event.getDirectory().getId());
        eventStore.storeEvent(new UserEvent(Operation.DELETED, event.getDirectory(), user, null, null));
    }

    @EventListener
    public void handleEvent(GroupCreatedEvent event)
    {
        eventStore.storeEvent(new GroupEvent(Operation.CREATED, event.getDirectory(), event.getGroup(), null, null));
    }

    @EventListener
    public void handleEvent(GroupUpdatedEvent event)
    {
        Map<String, Set<String>> storedAttributes = null;
        Set<String> deletedAttributes = null;

        if (event instanceof GroupAttributeStoredEvent)
        {
            if (ignoreAttributes)
            {
                return;
            }
            final GroupAttributeStoredEvent groupAttributeStoredEvent = (GroupAttributeStoredEvent) event;
            storedAttributes = new HashMap<String, Set<String>>();
            for (String storedAttribute : (Set<String>) groupAttributeStoredEvent.getAttributeNames())
            {
                storedAttributes.put(storedAttribute, groupAttributeStoredEvent.getAttributeValues(storedAttribute));
            }
        } else if (event instanceof GroupAttributeDeletedEvent)
        {
            if (ignoreAttributes)
            {
                return;
            }
            deletedAttributes = Collections.singleton(((GroupAttributeDeletedEvent) event).getAttributeName());
        }

        eventStore.storeEvent(new GroupEvent(Operation.UPDATED, event.getDirectory(), event.getGroup(), storedAttributes, deletedAttributes));
    }

    @EventListener
    public void handleEvent(GroupDeletedEvent event)
    {
        final Group group = new GroupTemplate(event.getGroupName(), event.getDirectory().getId());
        eventStore.storeEvent(new GroupEvent(Operation.DELETED, event.getDirectory(), group, null, null));
    }

    @EventListener
    public void handleEvent(GroupMembershipCreatedEvent event)
    {
        if (event.getMembershipType() == MembershipType.GROUP_USER)
        {
            eventStore.storeEvent(new UserMembershipEvent(Operation.CREATED, event.getDirectory(), event.getEntityName(), event.getGroupName()));
        } else if (event.getMembershipType() == MembershipType.GROUP_GROUP)
        {
            eventStore.storeEvent(new GroupMembershipEvent(Operation.CREATED, event.getDirectory(), event.getEntityName(), event.getGroupName()));
        } else
        {
            throw new IllegalArgumentException("MembershipType " + event.getMembershipType() + " is not supported");
        }
    }

    @EventListener
    public void handleEvent(GroupMembershipDeletedEvent event)
    {
        if (event.getMembershipType() == MembershipType.GROUP_USER)
        {
            eventStore.storeEvent(new UserMembershipEvent(Operation.DELETED, event.getDirectory(), event.getEntityName(), event.getGroupName()));
        } else if (event.getMembershipType() == MembershipType.GROUP_GROUP)
        {
            eventStore.storeEvent(new GroupMembershipEvent(Operation.DELETED, event.getDirectory(), event.getEntityName(), event.getGroupName()));
        } else
        {
            throw new IllegalArgumentException("MembershipType " + event.getMembershipType() + " is not supported");
        }
    }

    // Any event that causes major or unknown changes to the user and group
    // data seen by the application should force application to do a full sync.

    @EventListener
    public void handleEvent(DirectoryUpdatedEvent event)
    {
        eventStore.invalidateEvents();
    }

    @EventListener
    public void handleEvent(DirectoryDeletedEvent event)
    {
        eventStore.invalidateEvents();
    }

    @EventListener
    public void handleEvent(XMLRestoreFinishedEvent event)
    {
        eventStore.invalidateEvents();
    }

    @EventListener
    public void handleEvent(ApplicationDirectoryAddedEvent event)
    {
        eventStore.invalidateEvents();
    }

    @EventListener
    public void handleEvent(ApplicationDirectoryRemovedEvent event)
    {
        eventStore.invalidateEvents();
    }

    @EventListener
    public void handleEvent(ApplicationDirectoryOrderUpdatedEvent event)
    {
        eventStore.invalidateEvents();
    }
}