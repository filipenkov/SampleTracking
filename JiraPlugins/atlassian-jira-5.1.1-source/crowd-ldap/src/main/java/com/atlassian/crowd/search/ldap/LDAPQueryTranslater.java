package com.atlassian.crowd.search.ldap;

import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;

/**
 * The LDAPQueryTranslater: - Does not support searching based on GroupTermKeys.GROUP_TYPE: this cannot exist as a
 * search restriction. If it does, say hello to IllegalArgumentException. - Assumes that all groups and users are
 * 'active' in the underlying directory implementation. Thus if a subsearch if made for an 'inactive' groups/users, that
 * subsearch is returns nothing.
 */
public interface LDAPQueryTranslater
{
    LDAPQuery asLDAPFilter(EntityQuery query, LDAPPropertiesMapper ldapPropertiesMapper) throws NullResultException;
}
