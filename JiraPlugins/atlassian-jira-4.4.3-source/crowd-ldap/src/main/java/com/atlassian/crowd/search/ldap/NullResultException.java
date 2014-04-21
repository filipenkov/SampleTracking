package com.atlassian.crowd.search.ldap;

/**
 * Denotes that the LDAPQuery could not be formed because
 * the query would result in a null result (empty collection).
 */
public class NullResultException extends Exception
{
}
