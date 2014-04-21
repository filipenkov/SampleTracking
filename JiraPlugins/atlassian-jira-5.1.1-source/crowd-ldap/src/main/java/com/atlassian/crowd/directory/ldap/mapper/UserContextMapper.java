package com.atlassian.crowd.directory.ldap.mapper;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.entity.LDAPUserAttributesMapper;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import org.apache.log4j.Logger;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.directory.Attributes;
import java.util.List;

/**
 * Translates information returned from an LDAP directory into a {@link com.atlassian.crowd.model.user.LDAPUserWithAttributes}
 * implementation of {@link com.atlassian.crowd.model.user.User}.
 */
public class UserContextMapper implements ContextMapper
{
    private final Logger logger = Logger.getLogger(this.getClass());

    protected final long directoryId;
    protected final LDAPPropertiesMapper ldapPropertiesMapper;
    protected final List<AttributeMapper> customAttributeMappers;

    public UserContextMapper(long directoryId, LDAPPropertiesMapper ldapPropertiesMapper, List<AttributeMapper> customAttributeMappers)
    {
        this.directoryId = directoryId;
        this.ldapPropertiesMapper = ldapPropertiesMapper;
        this.customAttributeMappers = customAttributeMappers;
    }

    /**
     * Called by Spring LDAP on every object fetched from the LDAP directory.
     *
     * @param ctx A {@link org.springframework.ldap.core.DirContextAdapter DirContextAdapter} containing information about the object
     * @return {@link com.atlassian.crowd.model.user.LDAPUserWithAttributes}.
     */
    public Object mapFromContext(Object ctx) throws NamingException
    {
        DirContextAdapter context = (DirContextAdapter) ctx;
        Attributes attributes = context.getAttributes();
        LDAPUserAttributesMapper mapper = getAttributesMapper();

        // build user from common attributes
        UserTemplateWithAttributes userTemplate = mapper.mapUserFromAttributes(attributes);

        // map custom attributes
        for (AttributeMapper attributeMapper : customAttributeMappers)
        {
            try
            {
                userTemplate.setAttribute(attributeMapper.getKey(), attributeMapper.getValues(context));
            }
            catch (Exception e)
            {
                logger.warn("Failed to map attribute <" + attributeMapper.getKey() + "> from context with DN <" + context.getDn().toString() + ">", e);
            }
        }

        String dn = DNStandardiser.standardise((DistinguishedName) context.getDn(), !ldapPropertiesMapper.isRelaxedDnStandardisation());

        LDAPUserWithAttributes user = new LDAPUserWithAttributes(dn, userTemplate);

        if (logger.isTraceEnabled())
        {
            logger.trace("Created user <" + user + "> from DN <" + context.getDn() + ">");
        }

        return user;
    }

    /**
     * Split out so it can be overriden.
     *
     * @return
     */
    protected LDAPUserAttributesMapper getAttributesMapper()
    {
        return new LDAPUserAttributesMapper(directoryId, ldapPropertiesMapper);
    }
}
