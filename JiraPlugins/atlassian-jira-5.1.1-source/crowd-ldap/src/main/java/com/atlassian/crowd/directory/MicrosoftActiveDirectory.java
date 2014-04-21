/*
 * Copyright (c) 2006 Atlassian Software Systems. All Rights Reserved.
 */
package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapperImpl;
import com.atlassian.crowd.directory.ldap.control.DeletedResultsControl;
import com.atlassian.crowd.directory.ldap.mapper.TombstoneContextMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.ObjectGUIDMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.USNChangedMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnRangeOffsetMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnRangedMapper;
import com.atlassian.crowd.directory.ldap.name.ActiveDirectoryEncoder;
import com.atlassian.crowd.directory.ldap.name.Encoder;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.atlassian.crowd.directory.ldap.util.IncrementalAttributeMapper;
import com.atlassian.crowd.directory.ldap.util.ListAttributeValueProcessor;
import com.atlassian.crowd.directory.ldap.util.RangeOption;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.Tombstone;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.ldap.LDAPQueryTranslater;
import com.atlassian.crowd.util.InstanceFactory;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.GreaterThanOrEqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchControls;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Microsoft Active Directory connector.
 */
public class MicrosoftActiveDirectory extends RFC4519Directory
{
    private static final Logger logger = Logger.getLogger(MicrosoftActiveDirectory.class);
    // how to establish a secure connection with AD
    // http://forum.java.sun.com/thread.jspa?threadID=581425&tstart=50

    private static final String PRINCIPAL_NO_SSL_CONNECTION = "Secure SSL connections for this directory are not configured; unable to perform this operation.";

    // some useful constants from lmaccess.h
    private static final int UF_ACCOUNTDISABLE = 0x0002;
    private static final int UF_PASSWD_NOTREQD = 0x0020;
    private static final int UF_PASSWD_CANT_CHANGE = 0x0040;
    private static final int UF_NORMAL_ACCOUNT = 0x0200;
    private static final int UF_DONT_EXPIRE_PASSWD = 0x10000;
    private static final int UF_PASSWORD_EXPIRED = 0x800000;

    private static final String AD_USER_ACCOUNT_CONTROL = "userAccountControl";
    private static final String AD_SAM_ACCOUNT_NAME = "samAccountName";
    private static final String AD_PASSWORD_ENCODED = "UTF-16LE";
    private static final String AD_MEMBEROF = "memberOf";
    private static final String AD_HIGHEST_COMMITTED_USN = "highestCommittedUSN";
    private static final String AD_IS_DELETED = "isDeleted";
    private static final String AD_OBJECT_CLASS = "objectClass";

    private static final String DELETED_OBJECTS_DN_ADDITION = "CN=Deleted Objects";
    private static final String ROOT_DOMAIN_NAMING_CONTEXT = "rootDomainNamingContext";

    private static final String GROUP_TYPE_NAME = "groupType";
    private static final String GROUP_TYPE_VALUE = "2";

    public MicrosoftActiveDirectory(LDAPQueryTranslater ldapQueryTranslater, EventPublisher eventPublisher, InstanceFactory instanceFactory)
    {
        super(ldapQueryTranslater, eventPublisher, instanceFactory);
    }

    public static String getStaticDirectoryType()
    {
        return "Microsoft Active Directory";
    }

    public String getDescriptiveName()
    {
        return MicrosoftActiveDirectory.getStaticDirectoryType();
    }

    /**
     * Returns an Encoder that can correctly escape AD-specific special characters
     *
     * @return
     */
    @Override
    protected Encoder getEncoder()
    {
        return new ActiveDirectoryEncoder();
    }

    /**
     * AD does not need a default container member.
     *
     * @return <code>null</code>.
     */
    @Override
    protected String getInitialGroupMemberDN()
    {
        return null;
    }

    /**
     * Converts the clear-text password to the {<code>AD_PASSWORD_ENCODED</code> encoding - currently UTF-16LE
     *
     * @param unencodedPassword
     * @return byte array containing password in UTF-16LE encoding.
     * @throws com.atlassian.crowd.exception.InvalidCredentialException
     *          If the specified encoding is not available on this system.
     */
    protected byte[] encodePassword(String unencodedPassword) throws InvalidCredentialException
    {
        try
        {
            //Replace the "unicdodePwd" attribute with a new value
            //Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + unencodedPassword + "\"";
            return newQuotedPassword.getBytes(AD_PASSWORD_ENCODED);
        }
        catch (UnsupportedEncodingException e)  // if the Charset AD_PASSWORD_ENCODED isn't available on this system
        {
            throw new InvalidCredentialException(e.getMessage(), e);
        }
    }

    /**
     * Active Directory needs a couple of additional attributes set - the sAMAccountName (which is the account name
     * you use to log on to Windows), and the account disabled flag.
     *
     * @param user
     * @param attributes
     */
    @Override
    protected void getNewUserDirectorySpecificAttributes(User user, Attributes attributes)
    {
        //These are the mandatory attributes for a user object
        //Note that Win2K3 will automagically create a random
        //samAccountName if it is not present. (Win2K does not)
        attributes.put(AD_SAM_ACCOUNT_NAME, user.getName());

        // Set the account status
        String accountStatus = null;
        if (user.isActive())
        {
            accountStatus = Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED);
        }
        else
        {
            accountStatus = Integer.toString(UF_NORMAL_ACCOUNT + UF_PASSWD_NOTREQD + UF_PASSWORD_EXPIRED + UF_ACCOUNTDISABLE);
        }

        attributes.put(new BasicAttribute(AD_USER_ACCOUNT_CONTROL, accountStatus));
    }

    /**
     * If we want to be able to nest groups, we need to create distribution groups rather than security groups.
     * To do this we need to set groupType to 2.
     *
     * @param group
     * @param attributes
     */
    @Override
    protected void getNewGroupDirectorySpecificAttributes(final Group group, final Attributes attributes)
    {
        attributes.put(GROUP_TYPE_NAME, GROUP_TYPE_VALUE);
    }

    @Override
    protected List<AttributeMapper> getCustomUserAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomUserAttributeMappers();
        mappers.add(new ObjectGUIDMapper());
        mappers.add(new USNChangedMapper());

        return mappers;
    }

    @Override
    protected List<AttributeMapper> getCustomGroupAttributeMappers()
    {
        List<AttributeMapper> mappers = super.getCustomGroupAttributeMappers();
        mappers.add(new ObjectGUIDMapper());
        mappers.add(new USNChangedMapper());

        return mappers;
    }

    @Override
    protected List<AttributeMapper> getMemberDnMappers()
    {
        return Arrays.asList(
                new RFC4519MemberDnRangedMapper(ldapPropertiesMapper.getGroupMemberAttribute(), ldapPropertiesMapper.isRelaxedDnStandardisation()),
                new RFC4519MemberDnRangeOffsetMapper(ldapPropertiesMapper.getGroupMemberAttribute()));
    }

    @Override
    protected List<LDAPGroupWithAttributes> postprocessGroups(final List<LDAPGroupWithAttributes> groups)
    {
        List<LDAPGroupWithAttributes> result = Lists.newArrayList();
        for (LDAPGroupWithAttributes group : groups)
        {
            if (group.getValue(RFC4519MemberDnRangeOffsetMapper.ATTRIBUTE_KEY) != null)
            {
                ListAttributeValueProcessor valueAggregator = new ListAttributeValueProcessor();
                String rangeStart = group.getValue(RFC4519MemberDnRangeOffsetMapper.ATTRIBUTE_KEY);
                RangeOption range = new RangeOption(Integer.valueOf(rangeStart));
                IncrementalAttributeMapper incrementalAttributeMapper = new IncrementalAttributeMapper(ldapPropertiesMapper.getGroupMemberAttribute(), valueAggregator, range);

                while (incrementalAttributeMapper.hasMore())
                {
                    ldapTemplate.lookup(group.getDn(), incrementalAttributeMapper.getAttributesArray(), incrementalAttributeMapper);
                }

                // standardise the memberDNs (remember to include the existing members!)
                Set<String> initialMembers = group.getValues(RFC4519MemberDnMapper.ATTRIBUTE_KEY);
                Set<String> standardDNs = new HashSet<String>(initialMembers.size() + valueAggregator.getValues().size());
                standardDNs.addAll(initialMembers);
                for (String memberDN : valueAggregator.getValues())
                {
                    String dn = standardiseDN(memberDN);
                    standardDNs.add(dn);
                }

                // Create the new group template - add existing attributes and set the memberDNs
                GroupTemplateWithAttributes groupTemplate = new GroupTemplateWithAttributes(group);

                groupTemplate.setAttribute(RFC4519MemberDnMapper.ATTRIBUTE_KEY, standardDNs);
                groupTemplate.removeAttribute(RFC4519MemberDnRangeOffsetMapper.ATTRIBUTE_KEY); // remove, we don't need this anymore
                result.add(new LDAPGroupWithAttributes(group.getDn(), groupTemplate));
            }
            else
            {
                result.add(group);
            }
        }
        return result;
    }

    @Override
    protected Map<String, String> getBaseEnvironmentProperties()
    {
        Map<String, String> env = super.getBaseEnvironmentProperties();

        // ensure objectGUID is read as binary (byte array and not string)
        env.put(LDAPPropertiesMapperImpl.CONNECTION_BINARY_ATTRIBUTES, ObjectGUIDMapper.ATTRIBUTE_KEY);

        return env;
    }

    public long fetchHighestCommittedUSN() throws OperationFailedException
    {
        try
        {
            Attribute highestCommittedUSN = ((DirContextAdapter) ldapTemplate.lookup("")).getAttributes().get(AD_HIGHEST_COMMITTED_USN);
            long usn = Long.parseLong((String) highestCommittedUSN.get(0));
            if (logger.isDebugEnabled())
            {
                logger.debug("Fetched highest committed uSN of " + usn);
            }
            return usn;
        }
        catch (NamingException e)
        {
            logger.error("Error retrieving highestCommittedUSN from AD root", e);
            throw new OperationFailedException("Error retrieving highestCommittedUSN from AD root", e);
        }
        catch (org.springframework.ldap.NamingException e)
        {
            logger.error("Error looking up attributes for highestCommittedUSN", e);
            throw new OperationFailedException("Error looking up attributes for highestCommittedUSN", e);
        }
    }

    public List<LDAPUserWithAttributes> findAddedOrUpdatedUsersSince(long usnChange) throws OperationFailedException
    {
        return findAddedOrUpdatedObjectsSince(usnChange, searchDN.getUser(), ldapPropertiesMapper.getUserFilter(), getUserContextMapper());
    }

    public List<LDAPGroupWithAttributes> findAddedOrUpdatedGroupsSince(long usnChanged) throws OperationFailedException
    {
        return findAddedOrUpdatedObjectsSince(usnChanged, searchDN.getGroup(), ldapPropertiesMapper.getGroupFilter(), getGroupContextMapper(GroupType.GROUP));
    }

    public List<Tombstone> findUserTombstonesSince(long usnChange) throws OperationFailedException
    {
        return findTombstonesSince(usnChange, searchDN.getUser(), ldapPropertiesMapper.getUserObjectClass());
    }

    public List<Tombstone> findGroupTombstonesSince(long usnChange) throws OperationFailedException
    {
        return findTombstonesSince(usnChange, searchDN.getGroup(), ldapPropertiesMapper.getGroupObjectClass());
    }

    protected List findAddedOrUpdatedObjectsSince(long usnChange, Name objectBaseDN, String objectFilter, ContextMapper contextMapper) throws OperationFailedException
    {
        AndFilter filter = new AndFilter();

        // restrict the object type to the role object type
        filter.and(new HardcodedFilter(objectFilter));
        filter.and(new GreaterThanOrEqualsFilter(USNChangedMapper.ATTRIBUTE_KEY, Long.toString(usnChange + 1)));

        logger.debug("Performing polling search: baseDN = " + objectBaseDN + " - filter = " + filter.encode());

        return searchEntities(objectBaseDN, filter.encode(), contextMapper, 0, -1);
    }

    private Name getDeletedObjectsDN()
    {
        try
        {
            // CWD-1339 - Correctly find the root of this domain.
            DirContextAdapter root = (DirContextAdapter) ldapTemplate.lookup("");
            String rootDN = root.getStringAttribute(ROOT_DOMAIN_NAMING_CONTEXT);

            // we could refactor this out and allow it to be customisable
            String dn = new StringBuffer(DELETED_OBJECTS_DN_ADDITION).append(",").append(rootDN).toString();

            return new CompositeName(dn);
        }
        catch (NamingException e)
        {
            // if we can't build the DN, then just search from the base DN
            return searchDN.getNamingContext();
        }
    }

    protected List<Tombstone> findTombstonesSince(long usnChange, Name objectBaseDN, String objectClass) throws OperationFailedException
    {
        SearchControls searchControls = getSubTreeSearchControl();
        searchControls.setReturningAttributes(new String[]{ObjectGUIDMapper.ATTRIBUTE_KEY, USNChangedMapper.ATTRIBUTE_KEY});

        // create a filter for finding deleted objects of the appropriate class
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter(AD_IS_DELETED, "TRUE"));
        filter.and(new EqualsFilter(AD_OBJECT_CLASS, objectClass));
        filter.and(new GreaterThanOrEqualsFilter(USNChangedMapper.ATTRIBUTE_KEY, Long.toString(usnChange + 1)));

        // get the deleted objects container DN or the root DN (deleted objects are moved to this container)
        Name deletedObjectsDN = getDeletedObjectsDN();

        logger.debug("Performing tombstones search: baseDN = " + deletedObjectsDN + " - filter = " + filter.encode());

        ContextMapper contextMapper = new TombstoneContextMapper();

        return searchEntitiesWithRequestControls(deletedObjectsDN, filter.encode(), contextMapper, searchControls, new DeletedResultsControl(), 0, -1);
    }
}
