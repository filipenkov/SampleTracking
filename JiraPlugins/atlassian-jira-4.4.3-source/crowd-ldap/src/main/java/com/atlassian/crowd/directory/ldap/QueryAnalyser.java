package com.atlassian.crowd.directory.ldap;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Used to inspect search queries to determine if they
 * are executable on LDAP implementations.
 */
public class QueryAnalyser
{
    /**
     * Even though "active" is not really an LDAP field, it has been implemented
     * as such in the past and thus we treat it as such.
     * <p/>
     * If the "active" flag is later migrated from LDAP to the Internal directory,
     * then we will need to remove it from the USER_LDAP_PROPERTIES and GROUP_LDAP_PROPERTIES.
     */

    public static final Set<Property> USER_LDAP_PROPERTIES = ImmutableSet.<Property>of(
            UserTermKeys.USERNAME,
            UserTermKeys.DISPLAY_NAME,
            UserTermKeys.EMAIL,
            UserTermKeys.FIRST_NAME,
            UserTermKeys.LAST_NAME,
            UserTermKeys.ACTIVE
    );

    public static final Set<Property> GROUP_LDAP_PROPERTIES = ImmutableSet.<Property>of(
            GroupTermKeys.NAME,
            GroupTermKeys.ACTIVE
    );

    private enum QueryPath
    {
        LDAP, INTERNAL
    }

    public static <T> boolean isQueryOnLdapFieldsOnly(EntityQuery<T> query)
    {
        return doesRestrictionFollowPath(query.getEntityDescriptor().getEntityType(), query.getSearchRestriction(), QueryPath.LDAP);
    }

    public static <T> boolean isQueryOnInternalFieldsOnly(EntityQuery<T> query)
    {
        return doesRestrictionFollowPath(query.getEntityDescriptor().getEntityType(), query.getSearchRestriction(), QueryPath.INTERNAL);
    }

    private static boolean doesRestrictionFollowPath(Entity entity, SearchRestriction restriction, QueryPath queryPath)
    {
        if (restriction instanceof NullRestriction)
        {
            // can follow either path
            return true;
        }
        else if (restriction instanceof PropertyRestriction)
        {
            PropertyRestriction propertyRestriction = (PropertyRestriction) restriction;

            boolean ldapQuery;

            switch (entity)
            {
                case USER:
                    ldapQuery = USER_LDAP_PROPERTIES.contains(propertyRestriction.getProperty());
                    break;
                case GROUP:
                    ldapQuery = GROUP_LDAP_PROPERTIES.contains(propertyRestriction.getProperty());
                    break;
                default:
                    return false; // entity is unknown, exit early
            }

            return (ldapQuery && queryPath == QueryPath.LDAP) || (!ldapQuery && queryPath == QueryPath.INTERNAL);
        }
        else if (restriction instanceof BooleanRestriction)
        {
            BooleanRestriction booleanRestriction = (BooleanRestriction) restriction;

            boolean followsPath = true;

            for (SearchRestriction subRestriction : booleanRestriction.getRestrictions())
            {
                followsPath = doesRestrictionFollowPath(entity, subRestriction, queryPath);

                // effective "and" of restriction path-checking
                if (!followsPath)
                {
                    return false;
                }
            }

            return followsPath;
        }
        else
        {
            // unknown restriction
            return false;
        }
    }
}
