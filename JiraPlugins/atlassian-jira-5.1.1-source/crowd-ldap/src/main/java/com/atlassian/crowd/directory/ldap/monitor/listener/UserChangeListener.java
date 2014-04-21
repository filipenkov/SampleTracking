package com.atlassian.crowd.directory.ldap.monitor.listener;

import com.atlassian.crowd.directory.LDAPDirectory;
import com.atlassian.crowd.event.remote.principal.RemoteUserCreatedEvent;
import com.atlassian.crowd.event.remote.principal.RemoteUserDeletedEvent;
import com.atlassian.crowd.event.remote.principal.RemoteUserUpdatedEvent;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.event.api.EventPublisher;
import org.springframework.ldap.core.ContextMapper;

import javax.naming.Name;

public class UserChangeListener extends ChangeListener
{
    public UserChangeListener(LDAPDirectory remoteDirectory, Name baseDN, String objectFilter, ContextMapper mapper, EventPublisher eventPublisher, boolean lookupUpdates)
    {
        super(remoteDirectory, baseDN, objectFilter, mapper, eventPublisher, lookupUpdates);
    }

    protected void publishEntityCreatedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteUserCreatedEvent(this, getDirectoryID(), (User) entity));
    }

    protected void publishEntityUpdatedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteUserUpdatedEvent(this, getDirectoryID(), (User) entity));
    }

    protected void publishEntityDeletedEvent(Object entity)
    {
        eventPublisher.publish(new RemoteUserDeletedEvent(this, getDirectoryID(), ((User) entity).getName()));
    }

    protected Object lookupEntity(String dn)
    {
        try
        {
            return remoteDirectory.findEntityByDN(dn, LDAPUserWithAttributes.class);
        }
        catch (UserNotFoundException e)
        {
            return null;
        }
        catch (OperationFailedException e)
        {
            // Certainly nothing changed, so just return null
            return null;
        }
        catch (GroupNotFoundException e)
        {
            throw new AssertionError("Should not throw GroupNotFoundException");
        }
    }
}
