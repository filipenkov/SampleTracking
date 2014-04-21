package com.atlassian.crowd.embedded.api;

import java.util.Map;

/**
 * The system-wide settings for LDAP connection pooling, as provided by JNDI.
 *
 * http://java.sun.com/products/jndi/tutorial/ldap/connect/pool.html
 */
public interface ConnectionPoolProperties
{
    /**
     * @return Number of connections to create when initially connecting to the pool.
     */
    String getInitialSize();

    /**
     * @return Maximum number of connections to the LDAP server. Value of 0 means no maximum.
     */
    String getMaximumSize();

    /**
     * @return The preferred number of connections to be maintained in the pool.
     */
    String getPreferredSize();

    /**
     * @return Idle time in seconds for a connection before it is removed from the pool. Value of 0 means there is no timeout.
     */
    String getTimeoutInSec();

    /**
     * @return The specified authentication types will be pooled. Valid types are: none, simple, DIGEST-MD5.
     */
    String getSupportedAuthentication();

    /**
     * @return The specified protocol types will be pooled. Valid types are: plain, ssl.
     */
    String getSupportedProtocol();

    /**
     * @return The LDAP connection pool properties as a map.
     */
    Map<String, String> toPropertiesMap();
}
