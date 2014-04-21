package com.atlassian.crowd.search.hibernate;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.model.alias.Alias;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalGroup;
import com.atlassian.crowd.model.membership.InternalMembership;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.token.Token;
import com.atlassian.crowd.model.user.InternalUser;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.AliasTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.DirectoryTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.TokenTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import java.util.Date;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.atlassian.crowd.search.Entity.ALIAS;
import static com.atlassian.crowd.search.Entity.APPLICATION;
import static com.atlassian.crowd.search.Entity.DIRECTORY;
import static com.atlassian.crowd.search.Entity.GROUP;
import static com.atlassian.crowd.search.Entity.TOKEN;
import static com.atlassian.crowd.search.Entity.USER;

/**
 * Translates implementation agnostic Queries into executable
 * Hibernate Query Language code.
 * <p/>
 * Before you think this is an epic fail due the the existence of
 * Hibernate criteria queries (CBQ), criteria queries can't do
 * the join we want with user and user attribute classes without
 * explicitly mapping the join in Hibernate (AFAIK). Experience has
 * shown mapping joins for unbounded collections results in a
 * performance nightmare when mutating the collection.
 */
public class HQLQueryTranslater
{
    protected static final String HQL_USER_NAME = "lowerName";
    protected static final String HQL_USER_EMAIL_ADDRESS = "lowerEmailAddress";
    protected static final String HQL_USER_FIRST_NAME = "lowerFirstName";
    protected static final String HQL_USER_LAST_NAME = "lowerLastName";
    protected static final String HQL_USER_DISPLAY_NAME = "lowerDisplayName";
    protected static final String HQL_USER_ACTIVE = "active";
    protected static final String HQL_CREATED_DATE = "createdDate";
    protected static final String HQL_UPDATED_DATE = "updatedDate";

    protected static final String HQL_GROUP_NAME = "lowerName";
    protected static final String HQL_GROUP_ACTIVE = "active";
    protected static final String HQL_GROUP_TYPE = "type";
    protected static final String HQL_GROUP_LOCAL = "local";

    protected static final String HQL_TOKEN_NAME = "name";
    protected static final String HQL_TOKEN_LAST_ACCESSED_DATE = "lastAccessedDate";
    protected static final String HQL_TOKEN_DIRECTORY_ID = "directoryId";
    protected static final String HQL_TOKEN_RANDOM_NUMBER = "randomNumber";

    protected static final String HQL_DIRECTORY_NAME = "lowerName";
    protected static final String HQL_DIRECTORY_ACTIVE = "active";
    protected static final String HQL_DIRECTORY_TYPE = "type";
    protected static final String HQL_DIRECTORY_IMPLEMENTATION_CLASS = "lowerImplementationClass";

    protected static final String HQL_APPLICATION_NAME = "lowerName";
    protected static final String HQL_APPLICATION_ACTIVE = "active";
    protected static final String HQL_APPLICATION_TYPE = "type";

    protected static final String HQL_ALIAS_NAME = "lowerAlias";
    protected static final String HQL_ALIAS_APPLICATION_ID = "application.id";
    protected static final String HQL_ALIAS_USERNAME = "lowerName";

    protected static final String HQL_ATTRIBUTE_NAME = "name";
    protected static final String HQL_ATTRIBUTE_VALUE = "lowerValue";
    protected static final String HQL_ATTRIBUTE_ALIAS = "attr";
    protected static final String HQL_DIRECTORY_ID = ".directory.id";

    protected static final String HQL_MEMBERSHIP_ALIAS = "mem";
    protected static final String HQL_MEMBERSHIP_TYPE = "membershipType";
    protected static final String HQL_MEMBERSHIP_GROUP_TYPE = "groupType";

    public HQLQuery asHQL(long directoryID, MembershipQuery query)
    {
        if (query.getReturnType() == String.class) // as name
        {
            return membershipNamesQueryAsHQL(directoryID, query);
        }
        else
        {
            return membershipEntityQueryAsHQL(directoryID, query);
        }
    }

    protected HQLQuery membershipNamesQueryAsHQL(final long directoryID, final MembershipQuery query)
    {
        final HQLQuery hql = new HQLQuery();

        hql.appendSelect(HQL_MEMBERSHIP_ALIAS);
        hql.appendOrderBy(HQL_MEMBERSHIP_ALIAS);
        if (query.isFindChildren())
        {
            hql.appendSelect(".childName, ").append(HQL_MEMBERSHIP_ALIAS).append(".lowerChildName");
            hql.appendOrderBy(".lowerChildName");
        }
        else
        {
            hql.appendSelect(".parentName, ").append(HQL_MEMBERSHIP_ALIAS).append(".lowerParentName");
            hql.appendOrderBy(".lowerParentName");
        }

        hql.appendFrom(InternalMembership.class.getSimpleName()).append(" ").append(HQL_MEMBERSHIP_ALIAS);


        if (query.isFindChildren())
        {
            hql.appendWhere(HQL_MEMBERSHIP_ALIAS).append(".lowerParentName = ?");
        }
        else
        {
            hql.appendWhere(HQL_MEMBERSHIP_ALIAS).append(".lowerChildName = ?");
        }

        hql.addParameterValue(toLowerCase(query.getEntityNameToMatch()));

        appendMembershipTypeAndDirectoryIDAndGroupType(directoryID, query, hql);

        return hql;
    }

    private void appendMembershipTypeAndDirectoryIDAndGroupType(final long directoryID, final MembershipQuery query, final HQLQuery hql)
    {
        hql.appendWhere(" AND ").append(HQL_MEMBERSHIP_ALIAS).append(".").append(HQL_MEMBERSHIP_TYPE).append(" = ?");
        if (query.getEntityToMatch().getEntityType() == GROUP && query.getEntityToReturn().getEntityType() == GROUP)
        {
            hql.addParameterValue(MembershipType.GROUP_GROUP);
        }
        else
        {
            hql.addParameterValue(MembershipType.GROUP_USER);
        }

        hql.appendWhere(" AND ").append(HQL_MEMBERSHIP_ALIAS).append(".directory.id = ?");
        hql.addParameterValue(directoryID);

        // add group type restriction if present
        GroupType groupType = null;
        if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
        {
            groupType = query.getEntityToMatch().getGroupType();
        }
        if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
        {
            if (groupType != null && groupType != query.getEntityToReturn().getGroupType())
            {
                throw new IllegalArgumentException("Cannot search memberships of conflicting group types");
            }
            groupType = query.getEntityToReturn().getGroupType();
        }

        if (groupType != null)
        {
            hql.appendWhere(" AND ").append(HQL_MEMBERSHIP_ALIAS).append(".").append(HQL_MEMBERSHIP_GROUP_TYPE).append(" = ?");
            hql.addParameterValue(groupType);
        }
    }

    protected HQLQuery membershipEntityQueryAsHQL(final long directoryID, final MembershipQuery query)
    {
        final HQLQuery hql = new HQLQuery();

        String persistedClass = transformEntityToPersistedClass(query.getEntityToReturn().getEntityType());
        String alias = transformEntityToAlias(query.getEntityToReturn().getEntityType());

        hql.appendSelect(alias);
        hql.appendFrom(persistedClass).append(" ").append(alias).append(", ").append(InternalMembership.class.getSimpleName()).append(" ").append(HQL_MEMBERSHIP_ALIAS);

        hql.appendWhere(alias).append(".id = ").append(HQL_MEMBERSHIP_ALIAS);

        hql.appendOrderBy(HQL_MEMBERSHIP_ALIAS);
        if (query.isFindChildren())
        {
            hql.appendWhere(".childId AND ").append(HQL_MEMBERSHIP_ALIAS).append(".lowerParentName = ?");
            hql.appendOrderBy(".lowerChildName");
        }
        else
        {
            hql.appendWhere(".parentId AND ").append(HQL_MEMBERSHIP_ALIAS).append(".lowerChildName = ?");
            hql.appendOrderBy(".lowerChildName");
        }

        hql.addParameterValue(toLowerCase(query.getEntityNameToMatch()));

        appendMembershipTypeAndDirectoryIDAndGroupType(directoryID, query, hql);

        return hql;
    }

    public HQLQuery asHQL(EntityQuery entityQuery)
    {
        final HQLQuery hql = new HQLQuery();

        appendQueryAsHQL(entityQuery, hql);

        return hql;
    }

    public HQLQuery asHQL(long directoryID, EntityQuery entityQuery)
    {
        final HQLQuery hql = new HQLQuery();

        String entityAlias = transformEntityToAlias(entityQuery.getEntityDescriptor().getEntityType());

        hql.appendWhere(entityAlias).append(HQL_DIRECTORY_ID).append(" = ?");
        hql.addParameterValue(directoryID);

        appendQueryAsHQL(entityQuery, hql);

        return hql;
    }

    protected void appendQueryAsHQL(EntityQuery query, HQLQuery hql)
    {
        String persistedClass = transformEntityToPersistedClass(query.getEntityDescriptor().getEntityType());
        String alias = transformEntityToAlias(query.getEntityDescriptor().getEntityType());

        hql.appendSelect(alias);

        if (query.getReturnType() == String.class) // as name
        {
            appendSelectProjectionAsNames(hql, query.getEntityDescriptor().getEntityType());
        }

        hql.appendFrom(persistedClass).append(" ").append(alias);

        // special case for GroupType restriction
        if (query.getEntityDescriptor().getEntityType() == Entity.GROUP && query.getEntityDescriptor().getGroupType() != null)
        {
            if (hql.whereRequired)
            {
                hql.appendWhere(" AND ");
            }
            appendGroupTypeRestrictionAsHQL(hql, query.getEntityDescriptor().getGroupType());
        }

        // add the actual query restrictions
        if (!(query.getSearchRestriction() instanceof NullRestriction))
        {
            if (hql.whereRequired)
            {
                // if where was used previously we need to join to the where with AND, otherwise we can just append to the where clause
                hql.appendWhere(" AND ");
            }

            appendPropertyRestrictionAsHQL(hql, query.getEntityDescriptor().getEntityType(), query.getSearchRestriction());
        }

        appendOrderByClause(hql, query.getEntityDescriptor().getEntityType());
    }

    @SuppressWarnings("unchecked")
    protected void appendPropertyRestrictionAsHQL(HQLQuery hql, Entity entityType, SearchRestriction restriction)
    {
        if (restriction instanceof NullRestriction)
        {
            // do nothing
        }
        else if (restriction instanceof PropertyRestriction)
        {
            final PropertyRestriction propertyRestriction = (PropertyRestriction) restriction;

            if (MatchMode.NULL == propertyRestriction.getMatchMode())
            {
                appendIsNullTermRestrictionAsHSQL(hql, entityType, propertyRestriction);
            }
            else if (String.class.equals(propertyRestriction.getProperty().getPropertyType()))
            {
                appendStringTermRestrictionAsHQL(hql, entityType, propertyRestriction);
            }
            else if (Boolean.class.equals(propertyRestriction.getProperty().getPropertyType()))
            {
                appendBooleanTermRestrictionAsHQL(hql, entityType, propertyRestriction);
            }
            else if (Enum.class.equals(propertyRestriction.getProperty().getPropertyType()))
            {
                appendEnumTermRestrictionAsHQL(hql, entityType, propertyRestriction);
            }
            else if (Date.class.isAssignableFrom(propertyRestriction.getProperty().getPropertyType()))
            {
                appendDateTermRestriction(hql, entityType, propertyRestriction);
            }
            else if (Number.class.isAssignableFrom(propertyRestriction.getProperty().getPropertyType()))
            {
                appendNumberTermRestriction(hql, entityType, propertyRestriction);
            }
            else
            {
                throw new IllegalArgumentException("ProperyRestriction unsupported: " + restriction.getClass());
            }
        }
        else if (restriction instanceof BooleanRestriction)
        {
            appendMultiTermRestrictionAsHQL(hql, entityType, (BooleanRestriction) restriction);
        }
        else
        {
            throw new IllegalArgumentException("ProperyRestriction unsupported: " + restriction.getClass());
        }
    }

    protected void appendIsNullTermRestrictionAsHSQL(final HQLQuery hql, final Entity entityType, final PropertyRestriction<?> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        hql.appendWhere("IS NULL");
    }

    private void appendNumberTermRestriction(final HQLQuery hql, final Entity entityType, final PropertyRestriction<? extends Number> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        appendComparableValueAsHQL(hql, restriction);
    }

    protected void appendDateTermRestriction(final HQLQuery hql, final Entity entityType, final PropertyRestriction<? extends Date> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        appendComparableValueAsHQL(hql, restriction);
    }

    protected void appendBooleanTermRestrictionAsHQL(final HQLQuery hql, final Entity entityType, final PropertyRestriction<Boolean> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        hql.appendWhere("= ?");
        hql.addParameterValue(restriction.getValue());
    }

    protected void appendEnumTermRestrictionAsHQL(final HQLQuery hql, final Entity entityType, final PropertyRestriction<Enum> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        hql.appendWhere("= ?");
        hql.addParameterValue(restriction.getValue());
    }

    protected void appendMultiTermRestrictionAsHQL(HQLQuery hql, Entity entityType, BooleanRestriction booleanRestriction)
    {
        hql.appendWhere(" (");

        boolean first = true;

        for (SearchRestriction restriction : booleanRestriction.getRestrictions())
        {
            // add boolean logic
            if (!first)
            {
                if (booleanRestriction.getBooleanLogic() == BooleanRestriction.BooleanLogic.AND)
                {
                    hql.appendWhere(" AND ");
                }
                else if (booleanRestriction.getBooleanLogic() == BooleanRestriction.BooleanLogic.OR)
                {
                    hql.appendWhere(" OR ");
                }
                else
                {
                    throw new IllegalArgumentException("BooleanLogic unsupported: " + booleanRestriction.getBooleanLogic());
                }
            }
            else
            {
                first = false;
            }

            // add property restriction
            appendPropertyRestrictionAsHQL(hql, entityType, restriction);
        }

        hql.appendWhere(") ");
    }

    protected void appendStringTermRestrictionAsHQL(HQLQuery hql, Entity entityType, PropertyRestriction<String> restriction)
    {
        appendEntityPropertyAsHQL(hql, entityType, restriction);
        appendStringValueAsHQL(hql, restriction);
    }

    protected void appendEntityPropertyAsHQL(final HQLQuery hql, Entity entityType, final PropertyRestriction restriction)
    {
        switch (entityType)
        {
            case USER:
                appendUserPropertyAsHQL(hql, restriction);
                break;
            case GROUP:
                appendGroupPropertyAsHQL(hql, restriction);
                break;
            case TOKEN:
                appendTokenPropertyAsHQL(hql, restriction);
                break;
            case DIRECTORY:
                appendDirectoryPropertyAsHQL(hql, restriction);
                break;
            case APPLICATION:
                appendApplicationPropertyAsHQL(hql, restriction);
                break;
            case ALIAS:
                appendAliasPropertyAsHQL(hql, restriction);
                break;
            default:
                throw new IllegalArgumentException("Cannot form property restriction for entity of type <" + entityType + ">");
        }
    }

    private void appendAliasPropertyAsHQL(final HQLQuery hql, final PropertyRestriction restriction)
    {
        String alias = transformEntityToAlias(ALIAS);

        if (restriction.getProperty().equals(AliasTermKeys.ALIAS))
        {
            hql.appendWhere(alias).append(".").append(HQL_ALIAS_NAME);
        }
        else if (restriction.getProperty().equals(AliasTermKeys.APPLICATION_ID))
        {
            hql.appendWhere(alias).append(".").append(HQL_ALIAS_APPLICATION_ID);
        }
        else
        {
            throw new IllegalArgumentException("Alias does not support searching by property: " + restriction.getProperty().getPropertyName());
        }

        hql.appendWhere(" ");

    }

    private void appendApplicationPropertyAsHQL(final HQLQuery hql, final PropertyRestriction restriction)
    {
        String alias = transformEntityToAlias(APPLICATION);

        if (restriction.getProperty().equals(DirectoryTermKeys.NAME))
        {
            hql.appendWhere(alias).append(".").append(HQL_APPLICATION_NAME);
        }
        else if (restriction.getProperty().equals(DirectoryTermKeys.ACTIVE))
        {
            hql.appendWhere(alias).append(".").append(HQL_APPLICATION_ACTIVE);
        }
        else if (restriction.getProperty().equals(DirectoryTermKeys.TYPE))
        {
            hql.appendWhere(alias).append(".").append(HQL_APPLICATION_TYPE);
        }
        else
        {
            throw new IllegalArgumentException("Application does not support searching by property: " + restriction.getProperty().getPropertyName());
        }

        hql.appendWhere(" ");
    }

    protected void appendDirectoryPropertyAsHQL(final HQLQuery hql, final PropertyRestriction restriction)
    {
        String alias = transformEntityToAlias(DIRECTORY);

        if (restriction.getProperty().equals(DirectoryTermKeys.NAME))
        {
            hql.appendWhere(alias).append(".").append(HQL_DIRECTORY_NAME);
        }
        else if (restriction.getProperty().equals(DirectoryTermKeys.ACTIVE))
        {
            hql.appendWhere(alias).append(".").append(HQL_DIRECTORY_ACTIVE);
        }
        else if (restriction.getProperty().equals(DirectoryTermKeys.IMPLEMENTATION_CLASS))
        {
            hql.appendWhere(alias).append(".").append(HQL_DIRECTORY_IMPLEMENTATION_CLASS);
        }
        else if (restriction.getProperty().equals(DirectoryTermKeys.TYPE))
        {
            hql.appendWhere(alias).append(".").append(HQL_DIRECTORY_TYPE);
        }
        else
        {
            throw new IllegalArgumentException("Directory does not support searching by property: " + restriction.getProperty().getPropertyName());
        }

        hql.appendWhere(" ");
    }

    protected void appendTokenPropertyAsHQL(final HQLQuery hql, final PropertyRestriction restriction)
    {
        String tokenAlias = transformEntityToAlias(TOKEN);

        if (restriction.getProperty().equals(TokenTermKeys.NAME))
        {
            hql.appendWhere(tokenAlias).append(".").append(HQL_TOKEN_NAME);
        }
        else if (restriction.getProperty().equals(TokenTermKeys.LAST_ACCESSED_DATE))
        {
            hql.appendWhere(tokenAlias).append(".").append(HQL_TOKEN_LAST_ACCESSED_DATE);
        }
        else if (restriction.getProperty().equals(TokenTermKeys.DIRECTORY_ID))
        {
            hql.appendWhere(tokenAlias).append(".").append(HQL_TOKEN_DIRECTORY_ID);
        }
        else if (restriction.getProperty().equals(TokenTermKeys.RANDOM_NUMBER))
        {
            hql.appendWhere(tokenAlias).append(".").append(HQL_TOKEN_RANDOM_NUMBER);
        }
        else
        {
            throw new IllegalArgumentException("Token does not support searching by property: " + restriction.getProperty().getPropertyName());
        }

        hql.appendWhere(" ");
    }

    protected void appendGroupTypeRestrictionAsHQL(final HQLQuery hql, final GroupType groupType)
    {
        if (groupType != null)
        {
            String groupAlias = transformEntityToAlias(GROUP);
            hql.appendWhere(groupAlias).append(".").append(HQL_GROUP_TYPE);
            hql.appendWhere(" = ?");
            hql.addParameterValue(groupType);
        }
    }

    protected void appendGroupPropertyAsHQL(final HQLQuery hql, final PropertyRestriction restriction)
    {
        String groupAlias = transformEntityToAlias(GROUP);

        if (restriction.getProperty().equals(GroupTermKeys.NAME))
        {
            hql.appendWhere(groupAlias).append(".").append(HQL_GROUP_NAME);
        }
        else if (restriction.getProperty().equals(GroupTermKeys.ACTIVE))
        {
            hql.appendWhere(groupAlias).append(".").append(HQL_GROUP_ACTIVE);
        }
        else if (restriction.getProperty().equals(GroupTermKeys.CREATED_DATE))
        {
            hql.appendWhere(groupAlias).append(".").append(HQL_CREATED_DATE);
        }
        else if (restriction.getProperty().equals(GroupTermKeys.UPDATED_DATE))
        {
            hql.appendWhere(groupAlias).append(".").append(HQL_UPDATED_DATE);
        }
        else if (restriction.getProperty().equals(GroupTermKeys.LOCAL))
        {
            hql.appendWhere(groupAlias).append(".").append(HQL_GROUP_LOCAL);
        }
        else
        {
            // custom attribute
            StringBuilder alias = hql.getNextAlias(HQL_ATTRIBUTE_ALIAS);

            if (restriction.getMatchMode() == MatchMode.NULL)
            {
                hql.appendWhere("? NOT IN (SELECT ").append(alias).append(".").append(HQL_ATTRIBUTE_NAME).append(" FROM InternalGroupAttribute ").append(alias).append(" WHERE ").append(groupAlias).append(".id = ").append(alias).append(".group.id").append(")");

                hql.addParameterValue(restriction.getProperty().getPropertyName());

                // needed because "IS NULL" will be appended later, and we need to ignore it.
                hql.appendWhere(" AND ?");
                hql.addParameterValue(null);
            }
            else
            {
                hql.appendFrom(", InternalGroupAttribute ").append(alias);

                hql.appendWhere(groupAlias).append(".id = ").append(alias).append(".group.id").append(" AND ").append(alias).append(".").append(HQL_ATTRIBUTE_NAME).append(" = ?").append(" AND ").append(alias).append(".").append(HQL_ATTRIBUTE_VALUE);

                hql.addParameterValue(restriction.getProperty().getPropertyName());
            }

            hql.requireDistinct(); // needed because a join across the tables could potentially produce duplicate results in result set (think: multiply correct disjuction)
        }
        hql.appendWhere(" ");
    }

    protected void appendUserPropertyAsHQL(HQLQuery hql, PropertyRestriction restriction)
    {
        String userAlias = transformEntityToAlias(USER);

        if (restriction.getProperty().equals(UserTermKeys.USERNAME))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_NAME);
        }
        else if (restriction.getProperty().equals(UserTermKeys.EMAIL))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_EMAIL_ADDRESS);
        }
        else if (restriction.getProperty().equals(UserTermKeys.FIRST_NAME))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_FIRST_NAME);
        }
        else if (restriction.getProperty().equals(UserTermKeys.LAST_NAME))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_LAST_NAME);
        }
        else if (restriction.getProperty().equals(UserTermKeys.DISPLAY_NAME))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_DISPLAY_NAME);
        }
        else if (restriction.getProperty().equals(UserTermKeys.ACTIVE))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_USER_ACTIVE);
        }
        else if (restriction.getProperty().equals(UserTermKeys.CREATED_DATE))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_CREATED_DATE);
        }
        else if (restriction.getProperty().equals(UserTermKeys.UPDATED_DATE))
        {
            hql.appendWhere(userAlias).append(".").append(HQL_UPDATED_DATE);
        }
        else
        {
            // custom attribute
            StringBuilder attrAlias = hql.getNextAlias(HQL_ATTRIBUTE_ALIAS);

            if (restriction.getMatchMode() == MatchMode.NULL)
            {
                hql.appendWhere("? NOT IN (SELECT ").append(attrAlias).append(".").append(HQL_ATTRIBUTE_NAME).append(" FROM InternalUserAttribute ").append(attrAlias).append(" WHERE ").append(userAlias).append(".id = ").append(attrAlias).append(".user.id").append(")");

                hql.addParameterValue(restriction.getProperty().getPropertyName());

                // needed because "IS NULL" will be appended later, and we need to ignore it.
                hql.appendWhere(" AND ?");
                hql.addParameterValue(null);
            }
            else
            {
                hql.appendFrom(", InternalUserAttribute ").append(attrAlias);

                hql.appendWhere(userAlias).append(".id = ").append(attrAlias).append(".user.id").append(" AND ").append(attrAlias).append(".").append(HQL_ATTRIBUTE_NAME).append(" = ?").append(" AND ").append(attrAlias).append(".").append(HQL_ATTRIBUTE_VALUE);

                hql.addParameterValue(restriction.getProperty().getPropertyName());
            }

            hql.requireDistinct(); // needed because a join across the tables could potentially produce duplicate results in result set (think: multiply correct disjuction)
        }
        hql.appendWhere(" ");
    }

    protected void appendStringValueAsHQL(HQLQuery hql, PropertyRestriction<String> restriction)
    {
        final String lowerValue = toLowerCase(restriction.getValue());

        switch (restriction.getMatchMode())
        {
            case STARTS_WITH:
                hql.appendWhere("LIKE ?");
                hql.addParameterValue(lowerValue + "%");
                break;
            case CONTAINS:
                hql.appendWhere("LIKE ?");
                hql.addParameterValue("%" + lowerValue + "%");
                break;
            default:
                hql.appendWhere("= ?");
                hql.addParameterValue(lowerValue);
        }
    }

    protected void appendComparableValueAsHQL(HQLQuery hql, PropertyRestriction restriction)
    {
        switch (restriction.getMatchMode())
        {
            case GREATER_THAN:
                hql.appendWhere("> ?");
                break;
            case LESS_THAN:
                hql.appendWhere("< ?");
                break;
            default:
                hql.appendWhere(" = ?");
        }

        hql.addParameterValue(restriction.getValue());
    }

    private String transformEntityToAlias(Entity entity)
    {
        switch (entity)
        {
            case USER:
                return "usr";
            case GROUP:
                return "grp";
            case TOKEN:
                return "token";
            case DIRECTORY:
                return "directory";
            case APPLICATION:
                return "application";
            case ALIAS:
                return "alias";
            default:
                throw new IllegalArgumentException("Cannot transform entity of type <" + entity + ">");
        }
    }

    private String transformEntityToPersistedClass(Entity entity)
    {
        switch (entity)
        {
            case USER:
                return InternalUser.class.getSimpleName();
            case GROUP:
                return InternalGroup.class.getSimpleName();
            case TOKEN:
                return Token.class.getSimpleName();
            case DIRECTORY:
                return DirectoryImpl.class.getSimpleName();
            case APPLICATION:
                return ApplicationImpl.class.getSimpleName();
            case ALIAS:
                return Alias.class.getSimpleName();
            default:
                throw new IllegalArgumentException("Cannot transform entity of type <" + entity + ">");
        }
    }

    private String transormEntityToNameField(Entity entity)
    {
        switch (entity)
        {
            case USER:
                return HQL_USER_NAME;
            case GROUP:
                return HQL_GROUP_NAME;
            case TOKEN:
                return HQL_TOKEN_NAME;
            case DIRECTORY:
                return HQL_DIRECTORY_NAME;
            case APPLICATION:
                return HQL_APPLICATION_NAME;
            case ALIAS:
                return HQL_ALIAS_USERNAME;
            default:
                throw new IllegalArgumentException("Cannot transform entity of type <" + entity + ">");
        }
    }

    private void appendSelectProjectionAsNames(HQLQuery hql, Entity entity)
    {
        hql.appendSelect(".name");

        if (entity == USER || entity == GROUP || entity == DIRECTORY || entity == APPLICATION || entity == ALIAS)
        {
            hql.appendSelect(", ").append(transformEntityToAlias(entity)).append(".lowerName");
        }
    }

    private void appendOrderByClause(HQLQuery hql, Entity entity)
    {
        hql.appendOrderBy(transformEntityToAlias(entity)).append(".").append(transormEntityToNameField(entity));
    }
}
