package com.atlassian.crowd.directory.ldap.mapper.entity;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.util.DirectoryAttributeRetriever;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

public class LDAPGroupAttributesMapper
{
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final long directoryId;
    protected final GroupType groupType;
    protected final String objectClassAttribute;
    protected final String objectClassValue;
    protected final String nameAttribute;
    protected final String descriptionAttribute;

    public LDAPGroupAttributesMapper(long directoryId, final GroupType groupType, final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.directoryId = directoryId;
        this.groupType = groupType;

        this.objectClassAttribute = ldapPropertiesMapper.getObjectClassAttribute();

        switch (this.groupType)
        {
            case GROUP:

                this.objectClassValue = ldapPropertiesMapper.getGroupObjectClass();
                this.nameAttribute = ldapPropertiesMapper.getGroupNameAttribute();
                this.descriptionAttribute = ldapPropertiesMapper.getGroupDescriptionAttribute();
                break;
            case LEGACY_ROLE:
                this.objectClassValue = ldapPropertiesMapper.getRoleObjectClass();
                this.nameAttribute = ldapPropertiesMapper.getRoleNameAttribute();
                this.descriptionAttribute = ldapPropertiesMapper.getRoleDescriptionAttribute();
                break;
            default:
                throw new IllegalArgumentException("Cannot create LDAPGroupAttributesMapper for groupType: " + groupType);
        }
    }

    /**
     * Maps an LDAP object to the Crowd {@link com.atlassian.crowd.model.group.Group} object type. Implementation of AttributesMapper from Spring
     * LDAP framework.
     *
     * @param attributes The principal from the LDAP server to map into a {Group} object.
     * @return The populated {Group}.
     * @throws org.springframework.ldap.NamingException
     *          A mapping exception occured.
     * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes) throws NamingException
    {
        return mapGroupFromAttributes(attributes);
    }

    /**
     * Creates an LDAP {@link Attributes} object containing the information in the {@link com.atlassian.crowd.model.group.Group} object.
     *
     * @param group The object to take the values from
     * @return A populated directory-specific Attributes object.
     */
    public Attributes mapAttributesFromGroup(Group group) throws NamingException
    {
        if (group == null)
        {
            throw new UncategorizedLdapException("Cannot map attributes from a null Group");
        }

        Attributes directoryAttributes = new BasicAttributes(true);

        directoryAttributes.put(new BasicAttribute(objectClassAttribute, objectClassValue));

        // groupname, can never be blank.
        putValueInAttributes(group.getName(), nameAttribute, directoryAttributes);

        // description (optional)
        if (StringUtils.isNotBlank(group.getDescription()) && StringUtils.isNotBlank(descriptionAttribute))
        {
            putValueInAttributes(group.getDescription(), descriptionAttribute, directoryAttributes);
        }

        return directoryAttributes;
    }

    /**
     * Creates a {@link Group} object containing the information in the {@link Attributes} object.
     *
     * @param directoryAttributes The directory-specific {Attributes} object to take the values from
     * @return A populated {Group} object.
     */
    public GroupTemplateWithAttributes mapGroupFromAttributes(Attributes directoryAttributes) throws NamingException
    {
        if (directoryAttributes == null)
        {
            throw new UncategorizedLdapException("Cannot map from null attributes");
        }

        String groupname = getGroupNameFromAttributes(directoryAttributes);

        GroupTemplateWithAttributes group = new GroupTemplateWithAttributes(groupname, directoryId, groupType);

        // active
        group.setActive(getGroupActiveFromAttribute(directoryAttributes));

        // description
        group.setDescription(getGroupDescriptionFromAttribute(directoryAttributes));

        return group;
    }

    protected String getGroupDescriptionFromAttribute(final Attributes directoryAttributes)
    {
        return DirectoryAttributeRetriever.getValueFromAttributes(descriptionAttribute, directoryAttributes);
    }

    protected boolean getGroupActiveFromAttribute(final Attributes directoryAttributes)
    {
        // TODO: if you override this you need to make sure the LDAPQueryTranslater's search by "active" works
        return true;
    }

    private void putValueInAttributes(String groupAttributeValue, String directoryAttributeName, Attributes directoryAttributes)
    {
        if (groupAttributeValue != null)
        {
            directoryAttributes.put(new BasicAttribute(directoryAttributeName, groupAttributeValue));
        }
    }

    protected String getGroupNameFromAttributes(Attributes directoryAttributes) throws NamingException
    {
        String groupname = DirectoryAttributeRetriever.getValueFromAttributes(nameAttribute, directoryAttributes);

        if (groupname == null)
        {
            logger.fatal("The following record does not have a groupname: " + directoryAttributes.toString());
            throw new UncategorizedLdapException("Unable to find the groupname of the principal.");
        }

        return groupname;
    }
}
