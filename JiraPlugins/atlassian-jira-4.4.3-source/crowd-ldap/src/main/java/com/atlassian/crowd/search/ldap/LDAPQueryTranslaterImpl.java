package com.atlassian.crowd.search.ldap;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.*;
import static com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction.BooleanLogic.AND;
import static com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction.BooleanLogic.OR;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import org.springframework.ldap.filter.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The LDAPQueryTranslater:
 * - Does not support searching based on GroupTermKeys.GROUP_TYPE: this cannot exist as a search restriction.
 * If it does, say hello to IllegalArgumentException.
 * - Assumes that all groups and users are 'active' in the underlying directory implementation. Thus if a
 * subsearch if made for an 'inactive' groups/users, that subsearch is returns nothing.
 */
public class LDAPQueryTranslaterImpl implements LDAPQueryTranslater
{
    public LDAPQuery asLDAPFilter(EntityQuery query, LDAPPropertiesMapper ldapPropertiesMapper) throws NullResultException
    {
        LDAPQuery ldapQuery = new LDAPQuery(getObjectFilter(query.getEntityDescriptor(), ldapPropertiesMapper));

        Filter ldapFilter = searchRestrictionAsFilter(query.getEntityDescriptor(), query.getSearchRestriction(), ldapPropertiesMapper);

        if (ldapFilter instanceof EverythingResult)
        {
            // don't need to add to the root filter
        }
        else if (ldapFilter instanceof NothingResult)
        {
            throw new NullResultException();
        }
        else
        {
            ldapQuery.addFilter(ldapFilter);
        }

        return ldapQuery;
    }

    private Filter searchRestrictionAsFilter(EntityDescriptor entityDescriptor, SearchRestriction restriction, LDAPPropertiesMapper ldapPropertiesMapper)
    {
        if (restriction instanceof NullRestriction)
        {
            return new EverythingResult();
        }
        else if (restriction instanceof PropertyRestriction)
        {
            PropertyRestriction propertyRestriction = (PropertyRestriction) restriction;

            if (String.class.equals(propertyRestriction.getProperty().getPropertyType()))
            {
                return stringTermRestrictionAsFilter(entityDescriptor, propertyRestriction, ldapPropertiesMapper);
            }
            else if (Boolean.class.equals(propertyRestriction.getProperty().getPropertyType()))
            {
                return booleanTermRestrictionAsFilter(entityDescriptor, propertyRestriction, ldapPropertiesMapper);
            }
            else
            {
                throw new IllegalArgumentException("Search restriction on property '" + propertyRestriction.getProperty().getPropertyName() + "' not supported");
            }

        }
        else if (restriction instanceof BooleanRestriction)
        {
            return multiTermRestrictionAsFilter(entityDescriptor, (BooleanRestriction) restriction, ldapPropertiesMapper);
        }
        else
        {
            throw new IllegalArgumentException("SearchRestriction not supported: " + restriction.getClass());
        }
    }

    private Filter multiTermRestrictionAsFilter(final EntityDescriptor entityDescriptor, final BooleanRestriction restriction, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        // recursively build filter (process Everything and NothingResults)
        List<Filter> filters = new ArrayList<Filter>();
        for (SearchRestriction subRestriction : restriction.getRestrictions())
        {
            Filter filter = searchRestrictionAsFilter(entityDescriptor, subRestriction, ldapPropertiesMapper);

            if (restriction.getBooleanLogic() == OR && filter instanceof EverythingResult)
            {
                return filter; // return everything
            }
            else if (restriction.getBooleanLogic() == AND && filter instanceof NothingResult)
            {
                return filter; // return nothing
            }
            else
            {
                filters.add(filter);
            }
        }

        // if the multi restriction isn't an Everything/Nothing result, then we can build the multifilter
        BinaryLogicalFilter multiFilter;
        switch (restriction.getBooleanLogic())
        {
            case AND:
                multiFilter = new AndFilter();
                break;
            case OR:
                multiFilter = new OrFilter();
                break;
            default:
                throw new IllegalArgumentException("BooleanLogic not supported: " + restriction.getBooleanLogic());
        }

        // build the multi term restriction to ignore Nothing/Everything results
        // cater for the case where all subfilters are Nothing or Everything
        boolean allNothingResult = true;
        boolean allEverythingResult = true;
        for (Filter filter : filters)
        {
            if (filter instanceof NothingResult)
            {
                allEverythingResult = false;
            }
            else if (filter instanceof EverythingResult)
            {
                allNothingResult = false;
            }
            else
            {
                allEverythingResult = false;
                allNothingResult = false;
                multiFilter.append(filter);
            }
        }

        if (allNothingResult)
        {
            return new NothingResult();
        }
        else if (allEverythingResult)
        {
            return new EverythingResult();
        }
        else
        {
            return multiFilter;
        }
    }


    private Filter booleanTermRestrictionAsFilter(final EntityDescriptor entityDescriptor, final PropertyRestriction<Boolean> termRestriction, LDAPPropertiesMapper ldapPropertiesMapper)
    {
        // if boolean term restrictions are for anything other than the group/user active flag, then throw exception
        if (termRestriction.getProperty() != GroupTermKeys.ACTIVE && termRestriction.getProperty() != UserTermKeys.ACTIVE)
        {
            throw new IllegalArgumentException("Boolean restrictions for property " + termRestriction.getProperty().getPropertyName() + " are not supported");
        }
        else
        {
            if (termRestriction.getValue())
            {
                // everything is active = true, so no need to add a restriction
                return new EverythingResult();
            }
            else
            {
                // nothing is active = false, so need to
                return new NothingResult();
            }
        }
    }

    private Filter stringTermRestrictionAsFilter(final EntityDescriptor entityDescriptor, final PropertyRestriction<String> termRestriction, LDAPPropertiesMapper ldapPropertiesMapper)
    {
        String propertyName = getLDAPAttributeName(entityDescriptor, termRestriction.getProperty(), ldapPropertiesMapper);

        Filter stringFilter;

        switch (termRestriction.getMatchMode())
        {
            case STARTS_WITH:
                stringFilter = new LikeFilter(propertyName, termRestriction.getValue() + "*");
                break;
            case CONTAINS:
                if (termRestriction.getValue().length() > 0)
                {
                    stringFilter = new LikeFilter(propertyName, "*" + termRestriction.getValue() + "*");
                }
                else
                {
                    stringFilter = new LikeFilter(propertyName, "*");
                }
                break;
            default:
                stringFilter = new EqualsFilter(propertyName, termRestriction.getValue());
        }

        return stringFilter;
    }

    private String getLDAPAttributeName(final EntityDescriptor entityDescriptor, final Property property, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        switch (entityDescriptor.getEntityType())
        {
            case USER:
                return getUserLDAPAttributeName(property, ldapPropertiesMapper);
            case GROUP:
                switch (entityDescriptor.getGroupType())
                {
                    case GROUP:
                        return getGroupLDAPAttributeName(property, ldapPropertiesMapper);
                    case LEGACY_ROLE:
                        return getRoleLDAPAttributeName(property, ldapPropertiesMapper);
                    default:
                        throw new IllegalArgumentException("Cannot transform group type <" + entityDescriptor.getGroupType() + ">");
                }
            default:
                throw new IllegalArgumentException("Cannot transform entity of type <" + entityDescriptor.getEntityType() + ">");
        }
    }

    private String getUserLDAPAttributeName(final Property property, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        if (UserTermKeys.USERNAME.equals(property))
        {
            return ldapPropertiesMapper.getUserNameAttribute();
        }
        else if (UserTermKeys.FIRST_NAME.equals(property))
        {
            return ldapPropertiesMapper.getUserFirstNameAttribute();
        }
        else if (UserTermKeys.LAST_NAME.equals(property))
        {
            return ldapPropertiesMapper.getUserLastNameAttribute();
        }
        else if (UserTermKeys.DISPLAY_NAME.equals(property))
        {
            return ldapPropertiesMapper.getUserDisplayNameAttribute();
        }
        else if (UserTermKeys.EMAIL.equals(property))
        {
            return ldapPropertiesMapper.getUserEmailAttribute();
        }
        else if (UserTermKeys.ACTIVE.equals(property))
        {
            // this will skip processing of active as active is not mapped
            return null;
        }
        else
        {
            // assume custom attribute
            return property.getPropertyName();
        }
    }

    private String getGroupLDAPAttributeName(final Property property, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        if (GroupTermKeys.NAME.equals(property))
        {
            return ldapPropertiesMapper.getGroupNameAttribute();
        }
        else
        {
            // assume custom attribute
            return property.getPropertyName();
        }
    }

    private String getRoleLDAPAttributeName(final Property property, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        if (GroupTermKeys.NAME.equals(property))
        {
            return ldapPropertiesMapper.getRoleNameAttribute();
        }
        else
        {
            // assume custom attribute
            return property.getPropertyName();
        }
    }

    private String getObjectFilter(EntityDescriptor entityDescriptor, LDAPPropertiesMapper ldapPropertiesMapper)
    {
        switch (entityDescriptor.getEntityType())
        {
            case USER:
                return ldapPropertiesMapper.getUserFilter();
            case GROUP:
                if (entityDescriptor.getGroupType() == null)
                {
                    throw new IllegalArgumentException("Cannot search for groups where the GroupType has not been specified");
                }
                switch (entityDescriptor.getGroupType())
                {
                    case GROUP:
                        return ldapPropertiesMapper.getGroupFilter();
                    case LEGACY_ROLE:
                        return ldapPropertiesMapper.getRoleFilter();
                    default:
                        throw new IllegalArgumentException("Cannot transform group type <" + entityDescriptor.getGroupType() + ">");
                }
            default:
                throw new IllegalArgumentException("Cannot transform entity of type <" + entityDescriptor.getEntityType() + ">");
        }
    }
}
