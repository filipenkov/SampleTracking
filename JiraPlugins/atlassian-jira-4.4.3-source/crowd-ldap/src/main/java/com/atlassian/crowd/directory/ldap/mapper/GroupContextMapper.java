package com.atlassian.crowd.directory.ldap.mapper;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.AttributeMapper;
import com.atlassian.crowd.directory.ldap.mapper.entity.LDAPGroupAttributesMapper;
import com.atlassian.crowd.directory.ldap.util.DNStandardiser;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import org.apache.log4j.Logger;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.directory.Attributes;
import java.util.List;

/**
 * Translates information returned from an LDAP directory into a {@link com.atlassian.crowd.model.group.LDAPGroupWithAttributes}
 * implementation of {@link com.atlassian.crowd.model.group.Group}.
 */
public class GroupContextMapper implements ContextMapper
{
    private final Logger logger = Logger.getLogger(this.getClass());

    protected final long directoryId;
    protected final GroupType groupType;
    protected final LDAPPropertiesMapper ldapPropertiesMapper;
    protected final List<AttributeMapper> customAttributeMappers;

    public GroupContextMapper(long directoryId, GroupType groupType, LDAPPropertiesMapper ldapPropertiesMapper, List<AttributeMapper> customAttributeMappers)
    {
        this.directoryId = directoryId;
        this.groupType = groupType;
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
        LDAPGroupAttributesMapper mapper = getAttributesMapper();

        // build group from common attributes
        GroupTemplateWithAttributes groupTemplate = mapper.mapGroupFromAttributes(attributes);

        // map custom attributes
        for (AttributeMapper attributeMapper : customAttributeMappers)
        {
            try
            {
                groupTemplate.setAttribute(attributeMapper.getKey(), attributeMapper.getValues(context));
            }
            catch (Exception e)
            {
                logger.error("Failed to map attribute <" + attributeMapper.getKey() + "> from context with DN <" + context.getDn().toString() + ">", e);
            }
        }

        String dn = DNStandardiser.standardise((DistinguishedName) context.getDn(), !ldapPropertiesMapper.isRelaxedDnStandardisation());

        LDAPGroupWithAttributes group = new LDAPGroupWithAttributes(dn, groupTemplate);

        if (logger.isTraceEnabled())
        {
            logger.trace("Created group <" + group + "> from DN <" + context.getDn() + ">");
        }

        return group;
    }

    /**
     * Split out so it can be overridden.
     *
     * @return
     */
    protected LDAPGroupAttributesMapper getAttributesMapper()
    {
        return new LDAPGroupAttributesMapper(directoryId, groupType, ldapPropertiesMapper);
    }
}
