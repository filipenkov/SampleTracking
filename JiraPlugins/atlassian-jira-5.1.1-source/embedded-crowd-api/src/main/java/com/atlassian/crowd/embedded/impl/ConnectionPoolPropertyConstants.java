package com.atlassian.crowd.embedded.impl;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Class containing the properties related to a connection pool.
 */
public final class ConnectionPoolPropertyConstants
{
    public static final String POOL_INITIAL_SIZE = "com.sun.jndi.ldap.connect.pool.initsize";
    public static final String POOL_MAXIMUM_SIZE = "com.sun.jndi.ldap.connect.pool.maxsize";
    public static final String POOL_PREFERRED_SIZE = "com.sun.jndi.ldap.connect.pool.prefsize";
    public static final String POOL_PROTOCOL = "com.sun.jndi.ldap.connect.pool.protocol";
    public static final String POOL_TIMEOUT = "com.sun.jndi.ldap.connect.pool.timeout";
    public static final String POOL_AUTHENTICATION = "com.sun.jndi.ldap.connect.pool.authentication";
    public static final String DEFAULT_INITIAL_POOL_SIZE = "1";
    public static final String DEFAULT_MAXIMUM_POOL_SIZE = "0";
    public static final String DEFAULT_PREFERRED_POOL_SIZE = "10";
    public static final String DEFAULT_POOL_TIMEOUT_MS = "30000";
    public static final String DEFAULT_POOL_PROTOCOL = "plain ssl";
    public static final String DEFAULT_POOL_AUTHENTICATION = "simple";

    // Valid protocol and authentication options
    public static final Set<String> VALID_PROTOCOL_TYPES = ImmutableSet.of("plain", "ssl");
    public static final Set<String> VALID_AUTHENTICATION_TYPES = ImmutableSet.of("none", "simple", "DIGEST-MD5");

    private ConnectionPoolPropertyConstants() {} // prevent instantiation
}
