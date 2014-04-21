package com.atlassian.crowd.embedded.directory;

import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.impl.DefaultConnectionPoolProperties;
import com.atlassian.crowd.event.application.ApplicationReadyEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Listens to ApplicationReadyEvent that should be thrown by the application upon startup.
 * Will set the LDAP Connection Pool System Properties with values retrieved from the database.
 */
public class LdapConnectionPoolInitialisationListener
{
    private final Logger logger = Logger.getLogger(LdapConnectionPoolInitialisationListener.class);

    private final ApplicationFactory applicationFactory;

    public LdapConnectionPoolInitialisationListener(ApplicationFactory applicationFactory, EventPublisher eventPublisher)
    {
        this.applicationFactory = applicationFactory;
        eventPublisher.register(this);
    }

    @EventListener
    public void handleEvent(ApplicationReadyEvent event)
    {
        Map<String, String> attributes = applicationFactory.getApplication().getAttributes();
        ConnectionPoolProperties connectionPoolConfiguration = DefaultConnectionPoolProperties.fromPropertiesMap(attributes);
        Map<String, String> propertiesMap = connectionPoolConfiguration.toPropertiesMap();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet())
        {
            if (entry.getValue() != null) // ignore property if value is null; values set by JVM or on command line (-D...) will take precedence
            {
                logger.debug("Setting system-wide LDAP connection pool property: <" + entry.getKey() + "> with value: <" + entry.getValue() + ">");
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
}
