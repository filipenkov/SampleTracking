package com.atlassian.crowd.directory.ldap.mapper.entity;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.util.DirectoryAttributeRetriever;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.util.UserUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.UncategorizedLdapException;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Maps an LDAP {@link Attributes} object to the Crowd {User} object type, and vice versa.
 * Populates missing name information as required.
 *
 * @see UserUtils#populateNames(com.atlassian.crowd.model.user.User)
 */
public class LDAPUserAttributesMapper
{
    protected final Logger logger = Logger.getLogger(this.getClass());
    protected final LDAPPropertiesMapper ldapPropertiesMapper;      // understands directory-specific mappings
    protected final long directoryId;

    public LDAPUserAttributesMapper(long directoryId, LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.directoryId = directoryId;
        this.ldapPropertiesMapper = ldapPropertiesMapper;
    }

    /**
     * Maps an LDAP object to the Crowd {@link com.atlassian.crowd.model.user.User} object type. Implementation of AttributesMapper from Spring
     * LDAP framework.
     *
     * @param attributes The principal from the LDAP server to map into a {User} object.
     * @return The populated {User}.
     * @throws NamingException A mapping exception occured.
     * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
     */
    public Object mapFromAttributes(Attributes attributes) throws NamingException
    {
        return mapUserFromAttributes(attributes);
    }

    /**
     * Creates an LDAP {@link Attributes} object containing the information in the {@link User} object.
     *
     * @param user The object to take the values from
     * @return A populated directory-specific Attributes object.
     */
    public Attributes mapAttributesFromUser(User user) throws NamingException
    {
        if (user == null)
        {
            throw new UncategorizedLdapException("Cannot map attributes from a null User");
        }

        // pre-populate user names (first name, last name, display name may need to be constructed)
        User populatedUser = UserUtils.populateNames(user);

        Attributes directoryAttributes = new BasicAttributes(true);

        directoryAttributes.put(new BasicAttribute(ldapPropertiesMapper.getObjectClassAttribute(), ldapPropertiesMapper.getUserObjectClass()));

        // username
        putValueInAttributes(populatedUser.getName(), ldapPropertiesMapper.getUserNameAttribute(), directoryAttributes);

        // email address
        putValueInAttributes(populatedUser.getEmailAddress(), ldapPropertiesMapper.getUserEmailAttribute(), directoryAttributes);

        // first (given) name
        putValueInAttributes(populatedUser.getFirstName(), ldapPropertiesMapper.getUserFirstNameAttribute(), directoryAttributes);

        // surname
        putValueInAttributes(populatedUser.getLastName(), ldapPropertiesMapper.getUserLastNameAttribute(), directoryAttributes);

        // full name
        putValueInAttributes(populatedUser.getDisplayName(), ldapPropertiesMapper.getUserDisplayNameAttribute(), directoryAttributes);

        // TODO: currently we don't support arbitrary attributes / iconLocation / active

        return directoryAttributes;
    }

    /**
     * Creates a {@link User} object containing the information in the {@link Attributes} object.
     *
     * @param directoryAttributes The directory-specific {Attributes} object to take the values from
     * @return A populated {User} object.
     */
    public UserTemplateWithAttributes mapUserFromAttributes(Attributes directoryAttributes) throws NamingException
    {
        if (directoryAttributes == null)
        {
            throw new UncategorizedLdapException("Cannot map from null attributes");
        }

        String username = getUsernameFromAttributes(directoryAttributes);

        UserTemplate user = new UserTemplate(username, directoryId);

        // active
        user.setActive(getUserActiveFromAttribute(directoryAttributes));

        // email address
        user.setEmailAddress(StringUtils.defaultString(getUserEmailFromAttribute(directoryAttributes)));

        // first (given) name
        user.setFirstName(getUserFirstNameFromAttribute(directoryAttributes));

        // surname
        user.setLastName(getUserLastNameFromAttribute(directoryAttributes));

        // display name
        user.setDisplayName(getUserDisplayNameFromAttribute(directoryAttributes));

        // pre-populate user names (first name, last name, display name may need to be constructed)
        User prepopulatedUser = UserUtils.populateNames(user);

        return UserTemplateWithAttributes.ofUserWithNoAttributes(prepopulatedUser);
    }

    protected String getUserDisplayNameFromAttribute(final Attributes directoryAttributes)
    {
        return DirectoryAttributeRetriever.getValueFromAttributes(ldapPropertiesMapper.getUserDisplayNameAttribute(), directoryAttributes);
    }

    protected String getUserLastNameFromAttribute(final Attributes directoryAttributes)
    {
        return DirectoryAttributeRetriever.getValueFromAttributes(ldapPropertiesMapper.getUserLastNameAttribute(), directoryAttributes);
    }

    protected String getUserFirstNameFromAttribute(final Attributes directoryAttributes)
    {
        return DirectoryAttributeRetriever.getValueFromAttributes(ldapPropertiesMapper.getUserFirstNameAttribute(), directoryAttributes);
    }

    protected String getUserEmailFromAttribute(final Attributes directoryAttributes)
    {
        return DirectoryAttributeRetriever.getValueFromAttributes(ldapPropertiesMapper.getUserEmailAttribute(), directoryAttributes);
    }

    protected boolean getUserActiveFromAttribute(final Attributes directoryAttributes)
    {
        return true;
    }

    private void putValueInAttributes(String userAttributeValue, String directoryAttributeName, Attributes directoryAttributes)
    {
        // Allow space " ", but not empty ""
        if (StringUtils.isNotEmpty(userAttributeValue))
        {
            directoryAttributes.put(new BasicAttribute(directoryAttributeName, userAttributeValue));
        }
    }

    protected String getUsernameFromAttributes(Attributes directoryAttributes) throws NamingException
    {
        String username = DirectoryAttributeRetriever.getValueFromAttributes(ldapPropertiesMapper.getUserNameAttribute(), directoryAttributes);

        if (username == null)
        {
            logger.fatal("The following record does not have a username: " + directoryAttributes.toString());
            throw new UncategorizedLdapException("Unable to find the username of the principal.");
        }

        return username;
    }
}
