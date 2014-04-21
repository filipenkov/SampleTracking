package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.RFC2307GidNumberMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC2307MemberUidMapper;
import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.filter.OrFilter;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Read-only, non-nesting implementation of RFC2307 user-group membership
 * interactions.
 * <p/>
 * A user is a member of a group if either:
 * - the gidNumber of the user matches the gidNumber of the group
 * - the username of user is present in the collection of member attribute values of the group
 *
 * @see com.atlassian.crowd.directory.ldap.mapper.attribute.RFC2307GidNumberMapper
 * @see com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC2307MemberUidMapper
 */
public abstract class RFC2307Directory extends SpringLDAPConnector
{
    private static final Logger logger = Logger.getLogger(RFC2307Directory.class);
    public RFC2307Directory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
    }

    @Override
    protected List<AttributeMapper> getCustomGroupAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomGroupAttributeMappers();
        mappers.add(new RFC2307MemberUidMapper(ldapPropertiesMapper.getGroupMemberAttribute()));
        mappers.add(new RFC2307GidNumberMapper());

        return mappers;
    }

    @Override
    protected List<AttributeMapper> getCustomUserAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomUserAttributeMappers();
        mappers.add(new RFC2307GidNumberMapper());

        return mappers;
    }

    private Set<String> getMemberNames(LDAPGroupWithAttributes group)
    {
        return group.getValues(RFC2307MemberUidMapper.ATTRIBUTE_KEY);
    }

    private String getGid(Attributes entity)
    {
        return entity.getValue(RFC2307GidNumberMapper.ATTRIBUTE_KEY);
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws OperationFailedException
    {
        Validate.notEmpty(username, "username argument cannot be null or empty");
        Validate.notEmpty(groupName, "groupName argument cannot be null or empty");

        boolean isMember = false;

        try
        {
            // find the group first
            LDAPGroupWithAttributes group = findGroupByName(groupName);

            // see if the user is on the "member" attribute
            Set<String> memberNames = getMemberNames(group);
            if (memberNames != null)
            {
                for (String member : memberNames)
                {
                    if (member.equalsIgnoreCase(username))
                    {
                        isMember = true;
                        break;
                    }
                }
            }

            // try to see if it's the primary group on the user (gid)
            if (!isMember)
            {
                LDAPUserWithAttributes user = findUserByName(username);

                String userGid = getGid(user);
                String groupGid = getGid(group);
                if (StringUtils.equals(userGid, groupGid))
                {
                    isMember = true;
                }
            }
        }
        catch (UserNotFoundException e)
        {
            // user not found, therefore membership cannot exist
        }
        catch (GroupNotFoundException e)
        {
            // group not found, therefore membership cannot exist
        }

        return isMember;
    }

    /**
     * @param childGroup  name of child group.
     * @param parentGroup name of parent group.
     * @return <code>false</code> as nested groups are not supported.
     */
    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
    {
        return false;
    }

    protected <T> List<T> searchGroupRelationshipsWithGroupTypeSpecified(final MembershipQuery<T> query)
            throws OperationFailedException
    {
        Validate.notNull(query, "query argument cannot be null");

        List<? extends DirectoryEntity> relations;

        if (query.isFindChildren())
        {
            if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
            {
                if (query.getEntityToReturn().getEntityType() == Entity.USER)
                {
                    // query is to find USER members of GROUP
                    relations = findUserMembersOfGroup(query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), query.getStartIndex(), query.getMaxResults());
                }
                else if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                {
                    // query is to find GROUP members of GROUP but nested groups is not supported
                    relations = Collections.emptyList();
                }
                else
                {
                    throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
                }
            }
            else
            {
                throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
            }
        }
        else
        {
            // find memberships
            if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
            {
                if (query.getEntityToMatch().getEntityType() == Entity.USER)
                {
                    // query is to find GROUP memberships of USER
                    relations = findGroupMembershipsOfUser(query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), query.getStartIndex(), query.getMaxResults());
                }
                else if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
                {
                    // query is to find GROUP memberships of GROUP but nested groups is not supported
                    relations = Collections.emptyList();
                }
                else
                {
                    throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
                }
            }
            else
            {
                throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
            }
        }

        if (query.getReturnType() == String.class) // as name
        {
            return (List<T>) SearchResultsUtil.convertEntitiesToNames(relations);
        }
        else
        {
            return (List<T>) relations;
        }
    }

    private List<LDAPGroupWithAttributes> findGroupMembershipsOfUser(final String username, GroupType groupType, final int startIndex, final int maxResults)
            throws OperationFailedException
    {
        try
        {
            ContextMapper contextMapper = getGroupContextMapper(groupType);
            Name baseDN;
            String memberAttribute;
            String containerFilter;

            if (groupType == GroupType.GROUP)
            {
                baseDN = searchDN.getGroup();
                memberAttribute = ldapPropertiesMapper.getGroupMemberAttribute();
                containerFilter = ldapPropertiesMapper.getGroupFilter();
            }
            else if (groupType == GroupType.LEGACY_ROLE)
            {
                baseDN = searchDN.getRole();
                memberAttribute = ldapPropertiesMapper.getRoleMemberAttribute();
                containerFilter = ldapPropertiesMapper.getRoleFilter();
            }
            else
            {
                throw new IllegalArgumentException("Cannot find membership of user that are of GroupType: " + groupType);
            }

            LDAPUserWithAttributes user = findUserByName(username);
            String gidNumber = getGid(user);

            // either gid number of user matches gid number of group OR username is present in groups' member attribute values
            OrFilter membershipFilter = new OrFilter();
            membershipFilter.or(new EqualsFilter(memberAttribute, user.getName()));
            if (gidNumber != null)
            {
                membershipFilter.or(new EqualsFilter(RFC2307GidNumberMapper.ATTRIBUTE_KEY, gidNumber));
            }

            AndFilter rootFilter = new AndFilter();
            rootFilter.and(new HardcodedFilter(containerFilter));
            rootFilter.and(membershipFilter);

            return searchEntities(baseDN, rootFilter.encode(), contextMapper, startIndex, maxResults);
        }
        catch (UserNotFoundException e)
        {
            return Collections.emptyList();
        }
    }

    private List<LDAPUserWithAttributes> findUserMembersOfGroup(final String groupName, GroupType groupType, final int startIndex, final int maxResults)
            throws OperationFailedException
    {
        try
        {
            LDAPGroupWithAttributes group = findGroupByNameAndType(groupName, groupType);

            Set<LDAPUserWithAttributes> members = new HashSet<LDAPUserWithAttributes>();

            // first try getting users matching the group's gid
            String gidNumber = getGid(group);
            if (gidNumber != null)
            {
                try
                {
                    AndFilter filter = new AndFilter();
                    filter.and(new HardcodedFilter(ldapPropertiesMapper.getUserFilter()));
                    filter.and(new EqualsFilter(RFC2307GidNumberMapper.ATTRIBUTE_KEY, gidNumber));

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Executing search at DN: <" + searchDN.getUser() + "> with filter: <" + filter.encode() + ">");
                    }

                    members.addAll(searchEntities(searchDN.getUser(), filter.encode(), getUserContextMapper(), startIndex, maxResults));
                }
                catch (OperationFailedException e)
                {
                    logger.debug("Unable to get gid members for group: " + group.getDn(), e);
                }
            }

            // second try getting the users that are on member attribute of the group
            Set<String> memberNames = getMemberNames(group);
            if (memberNames != null)
            {
                for (String memberName : memberNames)
                {
                    try
                    {
                        members.add(findUserByName(memberName));
                    }
                    catch (UserNotFoundException e)
                    {
                        // skip (maybe object filter/baseDN don't match)
                    }
                }
            }

            // return the combined set (not really efficient, we could have only looked up the members we needed)
            return SearchResultsUtil.constrainResults(new ArrayList<LDAPUserWithAttributes>(members), startIndex, maxResults);
        }
        catch (GroupNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Group with name <" + groupName + "> does not exist and therefore has no members");
            }
            return Collections.emptyList();
        }
    }

    /**
     * As best I can determine, the RFC2307 schema does not support nested groups.
     *
     * @return <code>false</code>.
     */
    @Override
    public boolean supportsNestedGroups()
    {
        return false;
    }
}
