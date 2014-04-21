package com.atlassian.crowd.directory.ldap.monitor.listener;

import com.atlassian.crowd.directory.LDAPDirectory;
import com.atlassian.crowd.event.remote.group.RemoteGroupCreatedEvent;
import com.atlassian.crowd.event.remote.group.RemoteGroupDeletedEvent;
import com.atlassian.crowd.event.remote.group.RemoteGroupUpdatedEvent;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.event.api.EventPublisher;
import org.springframework.ldap.core.ContextMapper;

import javax.naming.Name;

public class GroupChangeListener extends ChangeListener
{
    public GroupChangeListener(LDAPDirectory remoteDirectory, Name baseDN, String objectFilter, ContextMapper mapper, EventPublisher eventPublisher, boolean lookupUpdates)
    {
        super(remoteDirectory, baseDN, objectFilter, mapper, eventPublisher, lookupUpdates);
    }

    protected void publishEntityCreatedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteGroupCreatedEvent(this, getDirectoryID(), (Group) entity));
    }

    protected void publishEntityUpdatedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteGroupUpdatedEvent(this, getDirectoryID(), (Group) entity));
    }

    protected void publishEntityDeletedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteGroupDeletedEvent(this, getDirectoryID(), ((Group) entity).getName()));
    }

    protected Object lookupEntity(String dn)
    {
        try
        {
            return remoteDirectory.findEntityByDN(dn, LDAPGroupWithAttributes.class);
        }
        catch (GroupNotFoundException e)
        {
            return null;
        }
        catch (OperationFailedException e)
        {
            // Certainly nothing changed, so just return null
            return null;
        }
        catch (UserNotFoundException e)
        {
            throw new AssertionError("Should not throw a UserNotFoundException");
        }
    }
}