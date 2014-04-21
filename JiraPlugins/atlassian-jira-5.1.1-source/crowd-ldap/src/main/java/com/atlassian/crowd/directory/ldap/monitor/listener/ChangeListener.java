package com.atlassian.crowd.directory.ldap.monitor.listener;

import com.atlassian.crowd.directory.LDAPDirectory;
import com.atlassian.crowd.event.remote.RemoteDirectoryMonitorErrorEvent;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.Name;
import javax.naming.directory.SearchResult;
import javax.naming.event.NamespaceChangeListener;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;

public abstract class ChangeListener implements ObjectChangeListener, NamespaceChangeListener
{
    protected final Logger logger = Logger.getLogger(this.getClass());

    protected final LDAPDirectory remoteDirectory;
    protected final Name baseDN;
    protected final String objectFilter;
    protected final ContextMapper mapper;
    protected final EventPublisher eventPublisher;
    protected final boolean lookupUpdates;

    public ChangeListener(LDAPDirectory remoteDirectory, Name baseDN, String objectFilter, ContextMapper mapper, EventPublisher eventPublisher, boolean lookupUpdates)
    {
        this.remoteDirectory = remoteDirectory;
        this.baseDN = baseDN;
        this.objectFilter = objectFilter;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.lookupUpdates = lookupUpdates;
    }

    public long getDirectoryID()
    {
        return remoteDirectory.getDirectoryId();
    }

    public Name getBaseDN()
    {
        return baseDN;
    }

    public String getObjectFilter()
    {
        return objectFilter;
    }

    protected DirContextAdapter buildContextAdapter(SearchResult result)
    {
        return new DirContextAdapter(result.getAttributes(), new DistinguishedName(result.getName()));
    }

    /**
     * Fire an entity created event.
     * @param entity entity object.
     */
    protected abstract void publishEntityCreatedEvent(Object entity);

    /**
     * Fire an entity updated event.
     * @param entity entity object.
     */
    protected abstract void publishEntityUpdatedEvent(Object entity);

    /**
     * Fire an entity deleted event.
     * @param entity entity object.
     */
    protected abstract void publishEntityDeletedEvent(Object entity);

    /**
     * Find an entity based on it's DN.
     *
     * Return null if the entity cannot be found.
     * @param dn distinguished name.
     */
    protected abstract Object lookupEntity(String dn);

    public void objectAdded(NamingEvent event)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("objectAdded event " + event.getNewBinding().getName());
        }

        // convert attributes to remote principal object
        if (event.getNewBinding() != null && event.getNewBinding() instanceof SearchResult)
        {
            DirContextAdapter ctx = buildContextAdapter((SearchResult) event.getNewBinding());
            Object entity = mapper.mapFromContext(ctx);
            publishEntityCreatedEvent(entity);
        }
        else
        {
            logger.error("Received objectAdded event but new binding not present from directory with ID: " + getDirectoryID());
        }
    }

    public void objectChanged(NamingEvent event)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("objectChanged event " + event.getNewBinding().getName());
        }

        // convert attributes to remote principal object
        if (event.getNewBinding() != null && event.getNewBinding() instanceof SearchResult)
        {
            Object entity = null;

            if (lookupUpdates)
            {
                // we need to actually lookup the updated data from the directory
                // (eg. ApacheDS needs this because it returns the OLD event details
                // in the new binding, ie. a bug in the directory server)
                entity = lookupEntity(event.getNewBinding().getName());
            }
            else
            {
                // we can just use the data from the mutation event
                DirContextAdapter ctx = buildContextAdapter((SearchResult) event.getNewBinding());
                entity = mapper.mapFromContext(ctx);
            }

            if (entity != null)
            {
                publishEntityUpdatedEvent(entity);
            }
        }
        else
        {
            logger.error("Received objectChanged event but new binding not present from directory with ID: " + getDirectoryID());
        }
    }

    public void objectRemoved(NamingEvent event)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("objectRemoved event " + event.getOldBinding().getName());
        }

        // convert attributes to remote principal object
        if (event.getOldBinding() != null && event.getOldBinding() instanceof SearchResult)
        {
            DirContextAdapter ctx = buildContextAdapter((SearchResult) event.getOldBinding());
            Object entity = mapper.mapFromContext(ctx);
            publishEntityDeletedEvent(entity);
        }
        else
        {
            logger.error("Received objectRemoved event but old binding not present from directory with ID: " + getDirectoryID());
        }
    }

    public void objectRenamed(NamingEvent event)
    {
        logger.error("Recieved objectRenamed event " + event.getNewBinding().getName()
                + ". Crowd's cache does not support object renaming.");

        eventPublisher.publish(new RemoteDirectoryMonitorErrorEvent(this, getDirectoryID(), new UnsupportedOperationException("Renaming objects is not supported")));
    }

    public void namingExceptionThrown(NamingExceptionEvent event)
    {
        logger.error("namingExceptionThrown event " + event.getException().getMessage());

        eventPublisher.publish(new RemoteDirectoryMonitorErrorEvent(this, getDirectoryID(), event.getException()));
    }
}
