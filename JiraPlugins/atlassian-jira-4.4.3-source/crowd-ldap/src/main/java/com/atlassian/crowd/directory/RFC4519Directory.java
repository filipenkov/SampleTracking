package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.user.MemberOfOverlayMapper;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.LDAPDirectoryEntity;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.ldap.AttributeInUseException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.OperationNotSupportedException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;

import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Read-write, nesting-aware implementation of RFC4519 user-group membership
 * interactions.
 * <p/>
 * A user is a member of a group if either:
 * - the DN of user is present in the collection of member attribute values of the group
 * - the user has a memberOf attribute which contains the DN of the group (must be enabled via LDAPPropertiesMapper)
 *
 * @see com.atlassian.crowd.directory.ldap.mapper.attribute.RFC2307GidNumberMapper
 * @see com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC2307MemberUidMapper
 */
public abstract class RFC4519Directory extends SpringLDAPConnector
{
    private static final Logger logger = Logger.getLogger(RFC4519Directory.class);
    
    public RFC4519Directory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
    }

    @Override
    protected List<AttributeMapper> getCustomGroupAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomGroupAttributeMappers();
        mappers.addAll(getMemberDnMappers());

        return mappers;
    }

    protected List<AttributeMapper> getMemberDnMappers()
    {
        return Collections.<AttributeMapper>singletonList(new RFC4519MemberDnMapper(ldapPropertiesMapper.getGroupMemberAttribute(), ldapPropertiesMapper.isRelaxedDnStandardisation()));
    }

    @Override
    protected List<AttributeMapper> getCustomUserAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomUserAttributeMappers();
        if (ldapPropertiesMapper.isUsingUserMembershipAttributeForGroupMembership())
        {
            mappers.add(new MemberOfOverlayMapper(ldapPropertiesMapper.getUserGroupMembershipsAttribute(), ldapPropertiesMapper.isRelaxedDnStandardisation()));
        }

        return mappers;
    }

    private Set<String> getMemberDNs(LDAPGroupWithAttributes group)
    {
        return group.getValues(RFC4519MemberDnMapper.ATTRIBUTE_KEY);
    }

    private Set<String> getMemberOfs(LDAPUserWithAttributes user)
    {
        return user.getValues(MemberOfOverlayMapper.ATTRIBUTE_KEY);
    }

    private boolean isDnDirectGroupMember(String memberDN, LDAPGroupWithAttributes parentGroup)
    {
        boolean isMember = false;

        Set<String> memberDNs = getMemberDNs(parentGroup);
        if (memberDNs != null)
        {
            isMember = memberDNs.contains(memberDN);
        }

        return isMember;
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws OperationFailedException
    {
        Validate.notEmpty(username, "username argument cannot be null or empty");
        Validate.notEmpty(groupName, "groupName argument cannot be null or empty");

        try
        {
            LDAPGroupWithAttributes group = findGroupByName(groupName);
            LDAPUserWithAttributes user = findUserByName(username);

            return isDnDirectGroupMember(user.getDn(), group);
        }
        catch (UserNotFoundException e)
        {
            return false;
        }
        catch (GroupNotFoundException e)
        {
            return false;
        }
    }

    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
            throws OperationFailedException
    {
        Validate.notEmpty(childGroup, "childGroup argument cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup argument cannot be null or empty");

        try
        {
            LDAPGroupWithAttributes parent = findGroupByName(parentGroup);
            LDAPGroupWithAttributes child = findGroupByName(childGroup);

            return isDnDirectGroupMember(child.getDn(), parent);
        }
        catch (GroupNotFoundException e)
        {
            return false;
        }
    }

    private void addDnToGroup(final String dn, final LDAPGroupWithAttributes group) throws OperationFailedException
    {
        try
        {
            ModificationItem mods[] = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(ldapPropertiesMapper.getGroupMemberAttribute(), dn));

            ldapTemplate.modifyAttributes(asLdapGroupName(group.getDn(), group.getName()), mods);
        }
        catch (AttributeInUseException e)   // ApacheDS, OpenLDAP, etc
        {
            // already member, do nothing
        }
        catch (NameAlreadyBoundException e)   // Active Directory
        {
            // already member, do nothing
        }
        catch (GroupNotFoundException e)
        {
            logger.error("Could not modify members of group with DN: " + dn, e);
        }
        catch (org.springframework.ldap.NamingException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addUserToGroup(final String username, final String groupName)
            throws GroupNotFoundException, OperationFailedException, UserNotFoundException
    {
        Validate.notEmpty(username, "username argument cannot be null or empty");
        Validate.notEmpty(groupName, "groupName argument cannot be null or empty");

        LDAPGroupWithAttributes group = findGroupByName(groupName);
        LDAPUserWithAttributes user = findUserByName(username);

        addDnToGroup(user.getDn(), group);
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException
    {
        Validate.notEmpty(childGroup, "childGroup argument cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup argument cannot be null or empty");

        LDAPGroupWithAttributes parent = findGroupByName(parentGroup);
        LDAPGroupWithAttributes child = findGroupByName(childGroup);

        if (parent.getType() != child.getType())
        {
            throw new InvalidMembershipException("Cannot add group of type " + child.getType().name() + " to group of type " + parent.getType().name());
        }

        addDnToGroup(child.getDn(), parent);
    }

    private void removeDnFromGroup(final String dn, LDAPGroupWithAttributes group) throws OperationFailedException
    {
        try
        {
            ModificationItem mods[] = new ModificationItem[1];

            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, new BasicAttribute(ldapPropertiesMapper.getGroupMemberAttribute(), dn));

            ldapTemplate.modifyAttributes(asLdapGroupName(group.getDn(), group.getName()), mods);
        }
        catch (OperationNotSupportedException e)
        {
            // AD: Thrown when the user is not a member of the group.
            // silently fail
        }
        catch (GroupNotFoundException e)
        {
            logger.error("Could not modify memers of group with DN: " + dn, e);
        }
        catch (org.springframework.ldap.NamingException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws UserNotFoundException, GroupNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        Validate.notEmpty(username, "username argument cannot be null or empty");
        Validate.notEmpty(groupName, "groupName argument cannot be null or empty");

        LDAPGroupWithAttributes group = findGroupByName(groupName);
        LDAPUserWithAttributes user = findUserByName(username);

        if (!isDnDirectGroupMember(user.getDn(), group))
        {
            throw new MembershipNotFoundException(username, groupName);
        }

        removeDnFromGroup(user.getDn(), group);
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, MembershipNotFoundException, InvalidMembershipException, OperationFailedException
    {
        Validate.notEmpty(childGroup, "childGroup argument cannot be null or empty");
        Validate.notEmpty(parentGroup, "parentGroup argument cannot be null or empty");

        LDAPGroupWithAttributes parent = findGroupByName(parentGroup);
        LDAPGroupWithAttributes child = findGroupByName(childGroup);

        if (!isDnDirectGroupMember(child.getDn(), parent))
        {
            throw new MembershipNotFoundException(childGroup, parentGroup);
        }

        if (parent.getType() != child.getType())
        {
            throw new InvalidMembershipException("Cannot remove group of type " + child.getType().name() + " from group of type " + parent.getType().name());
        }

        removeDnFromGroup(child.getDn(), parent);
    }

    protected <T> List<T> searchGroupRelationshipsWithGroupTypeSpecified(final MembershipQuery<T> query)
            throws OperationFailedException
    {
        List<? extends DirectoryEntity> relations;

        if (query.isFindChildren())
        {
            if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
            {
                if (query.getEntityToReturn().getEntityType() == Entity.USER)
                {
                    // query is to find USER members of GROUP
                    if (ldapPropertiesMapper.isUsingUserMembershipAttribute())
                    {
                        relations = findUserMembersOfGroupViaMemberOf(query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }
                    else
                    {
                        relations = findUserMembersOfGroupViaMemberDN(query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }
                }
                else if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                {
                    // query is to find GROUP members of GROUP (only if nesting is enabled)
                    if (ldapPropertiesMapper.isNestedGroupsDisabled())
                    {
                        relations = Collections.emptyList();
                    }
                    else
                    {
                        relations = findGroupMembersOfGroupViaMemberDN(query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }
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
                    if (ldapPropertiesMapper.isUsingUserMembershipAttributeForGroupMembership())
                    {
                        relations = findGroupMembershipsOfUserViaMemberOf(query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }
                    else
                    {
                        relations = findGroupMembershipsOfUserViaMemberDN(query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }

                }
                else if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
                {
                    // query is to find GROUP memberships of GROUP (only if nesting is enabled)
                    if (ldapPropertiesMapper.isNestedGroupsDisabled())
                    {
                        relations = Collections.emptyList();
                    }
                    else
                    {
                        relations = findGroupMembershipsOfGroupViaMemberDN(query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), query.getStartIndex(), query.getMaxResults());
                    }
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
            return toGenericList(SearchResultsUtil.convertEntitiesToNames(relations));
        }
        else
        {
            return toGenericList(relations);
        }
    }

    private List<LDAPGroupWithAttributes> findGroupMembershipsOfUserViaMemberOf(final String username, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        List<LDAPGroupWithAttributes> results;

        try
        {
            LDAPUserWithAttributes user = findUserByName(username);

            Set<String> memberOfs = getMemberOfs(user);
            if (memberOfs != null)
            {
                int totalResultSize;
                if (maxResults == EntityQuery.ALL_RESULTS)
                {
                    results = new ArrayList<LDAPGroupWithAttributes>();
                    totalResultSize = EntityQuery.ALL_RESULTS;
                }
                else
                {
                    results = new ArrayList<LDAPGroupWithAttributes>(maxResults);
                    totalResultSize = startIndex + maxResults;
                }

                for (String groupDN : memberOfs)
                {
                    try
                    {
                        LDAPGroupWithAttributes entity = findEntityByDN(groupDN, LDAPGroupWithAttributes.class);

                        if (entity.getType() == groupType)
                        {
                            results.add(entity);
                        }
                    }
                    catch (GroupNotFoundException e)
                    {
                        // entity does not exist at specified DN (or does not match object filter/baseDN)
                    }

                    // if we have enough results then break out
                    if (totalResultSize != EntityQuery.ALL_RESULTS && results.size() >= totalResultSize)
                    {
                        break;
                    }
                }

                results = SearchResultsUtil.constrainResults(results, startIndex, maxResults);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("User with name <" + username + "> does not have any memberOf values and therefore has no memberships");
                }
                results = Collections.emptyList();
            }
        }
        catch (UserNotFoundException e)
        {
            // user not found
            if (logger.isDebugEnabled())
            {
                logger.debug("User with name <" + username + "> does not exist and therefore has no memberships");
            }
            results = Collections.emptyList();
        }

        return results;
    }

    private List<LDAPGroupWithAttributes> findGroupMembershipsOfUserViaMemberDN(final String username, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        try
        {
            LDAPUserWithAttributes user = findUserByName(username);
            return findGroupMembershipsOfEntityViaMemberDN(user.getDn(), groupType, startIndex, maxResults);
        }
        catch (UserNotFoundException e)
        {
            return Collections.emptyList();
        }
        catch (IllegalArgumentException e)
        {
            return Collections.emptyList();
        }
    }

    private List<LDAPGroupWithAttributes> findGroupMembershipsOfGroupViaMemberDN(final String groupName, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        try
        {
            LDAPGroupWithAttributes group = findGroupByNameAndType(groupName, groupType);
            return findGroupMembershipsOfEntityViaMemberDN(group.getDn(), groupType, startIndex, maxResults);
        }
        catch (GroupNotFoundException e)
        {
            return Collections.emptyList();
        }
    }

    private List<LDAPGroupWithAttributes> findGroupMembershipsOfEntityViaMemberDN(final String dn, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        AndFilter filter = new AndFilter();
        ContextMapper contextMapper;
        Name baseDN;

        if (groupType == GroupType.GROUP)
        {
            filter.and(new HardcodedFilter(ldapPropertiesMapper.getGroupFilter()));
            filter.and(new EqualsFilter(ldapPropertiesMapper.getGroupMemberAttribute(), dn));
            contextMapper = getGroupContextMapper(GroupType.GROUP);
            baseDN = searchDN.getGroup();
        }
        else if (groupType == GroupType.LEGACY_ROLE)
        {
            filter.and(new HardcodedFilter(ldapPropertiesMapper.getRoleFilter()));
            filter.and(new EqualsFilter(ldapPropertiesMapper.getRoleMemberAttribute(), dn));
            contextMapper = getGroupContextMapper(GroupType.LEGACY_ROLE);
            baseDN = searchDN.getRole();
        }
        else
        {
            throw new IllegalArgumentException("Cannot find group memberships of entity via member DN for GroupType: " + groupType);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Executing search at DN: <" + searchDN.getGroup() + "> with filter: <" + filter.encode() + ">");
        }

        return toGenericList(searchEntities(baseDN, filter.encode(), contextMapper, startIndex, maxResults));
    }

    private List<LDAPGroupWithAttributes> findGroupMembersOfGroupViaMemberDN(final String groupName, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        return findMembersOfGroupViaMemberDN(groupName, groupType, LDAPGroupWithAttributes.class, startIndex, maxResults);
    }

    private List<LDAPUserWithAttributes> findUserMembersOfGroupViaMemberDN(final String groupName, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        return findMembersOfGroupViaMemberDN(groupName, groupType, LDAPUserWithAttributes.class, startIndex, maxResults);
    }

    /*
     * Executes a user search to find users with memberOf=group dn.
     */
    private List<LDAPUserWithAttributes> findUserMembersOfGroupViaMemberOf(final String groupName, GroupType groupType, int startIndex, int maxResults)
            throws OperationFailedException
    {
        List<LDAPUserWithAttributes> results;

        try
        {
            LDAPGroupWithAttributes group = findGroupWithAttributesByName(groupName);
            if (group.getType() == groupType)
            {
                AndFilter filter = new AndFilter();
                filter.and(new HardcodedFilter(ldapPropertiesMapper.getUserFilter()));
                filter.and(new EqualsFilter(ldapPropertiesMapper.getUserGroupMembershipsAttribute(), group.getDn()));

                if (logger.isDebugEnabled())
                {
                    logger.debug("Executing search at DN: <" + searchDN.getUser() + "> with filter: <" + filter.encode() + ">");
                }

                results = toGenericList(searchEntities(searchDN.getUser(), filter.encode(), getUserContextMapper(), startIndex, maxResults));
            }
            else
            {
                // group exists but is not of desired type
                if (logger.isDebugEnabled())
                {
                    logger.debug("Group with name <" + groupName + "> does exist but is of GroupType <" + group.getType() + "> and not <" + groupType + ">");
                }
                results = Collections.emptyList();
            }
        }
        catch (GroupNotFoundException e)
        {
            // group does not exist
            if (logger.isDebugEnabled())
            {
                logger.debug("Group with name <" + groupName + "> does not exist and therefore has no members");
            }
            results = Collections.emptyList();
        }

        return results;
    }

    /*
     * Finds the group and goes through each memberDN to find the user/group member by executing
     * successive lookups that are required to match both the memberBaseDN (suffix match) and
     * the member filter (during the LDAP lookup).
     */

    private <T extends LDAPDirectoryEntity> List<T> findMembersOfGroupViaMemberDN(final String groupName, GroupType groupType, Class<T> memberClass, int startIndex, int maxResults)
            throws OperationFailedException
    {
        List<T> results;

        try
        {
            LDAPGroupWithAttributes group = findGroupByNameAndType(groupName, groupType);

            Set<String> memberDNs = getMemberDNs(group);
            if (memberDNs != null)
            {
                // check indexes for "all results" special case
                int totalResultSize;
                if (maxResults == EntityQuery.ALL_RESULTS)
                {
                    results = new ArrayList<T>();
                    totalResultSize = EntityQuery.ALL_RESULTS;
                }
                else
                {
                    results = new ArrayList<T>(maxResults);
                    totalResultSize = startIndex + maxResults;
                }

                for (String memberDN : memberDNs)
                {
                    try
                    {
                        T entity = findEntityByDN(memberDN, memberClass);

                        if (entity instanceof LDAPGroupWithAttributes)
                        {
                            // only add results of the requested group type
                            if (((LDAPGroupWithAttributes) entity).getType() == groupType)
                            {
                                results.add(entity);
                            }
                        }
                        else
                        {
                            // all users can get added
                            results.add(entity);
                        }

                    }
                    catch (UserNotFoundException e)
                    {
                        // entity does not exist at specified DN (or does not match object filter/baseDN)
                    }
                    catch (GroupNotFoundException e)
                    {
                        // entity does not exist at specified DN (or does not match object filter/baseDN)
                    }

                    // if we have enough results then break out
                    if (totalResultSize != EntityQuery.ALL_RESULTS && results.size() >= totalResultSize)
                    {
                        break;
                    }
                }

                results = SearchResultsUtil.constrainResults(results, startIndex, maxResults);
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Group with name <" + groupName + "> does not have any memberDNs and therefore has no members");
                }
                results = Collections.emptyList();
            }
        }
        catch (GroupNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Group with name <" + groupName + "> does not exist and therefore has no members");
            }
            results = Collections.emptyList();
        }

        return results;
    }

    /**
     * Converts a List to a generic List.
     *
     * @param list list to convert
     * @return List
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> toGenericList(List list) {
        return (List<T>) list;
    }

}
