package com.atlassian.crowd.directory.ldap.mapper;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import org.apache.log4j.Logger;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

/**
 * Maps either a RemotePrincipal, RemoteGroup or RemoteRole.
 *
 * If a context can be mapped to either a RemoteGroup or a
 * RemoteRole, the RemoteGroup is given preference.
 */
public class EntityContextMapper implements ContextMapper
{
    private final Logger logger = Logger.getLogger(this.getClass());

    private final ContextMapper principalContextMapper;
    private final ContextMapper groupContextMapper;
    private final ContextMapper roleContextMapper;
    private final DistinguishedName principalDN;
    private final DistinguishedName groupDN;
    private final DistinguishedName roleDN;
    private final LDAPPropertiesMapper ldapPropertiesMapper;

    public EntityContextMapper(LDAPPropertiesMapper ldapPropertiesMapper, ContextMapper principalContextMapper, ContextMapper groupContextMapper, ContextMapper roleContextMapper, DistinguishedName principalDN, DistinguishedName groupDN, DistinguishedName roleDN)
    {
        this.ldapPropertiesMapper = ldapPropertiesMapper;
        this.principalContextMapper = principalContextMapper;
        this.groupContextMapper = groupContextMapper;
        this.roleContextMapper = roleContextMapper;
        this.principalDN = principalDN;
        this.groupDN = groupDN;
        this.roleDN = roleDN;
    }

    public Object mapFromContext(Object ctx)
    {
        DirContextAdapter context = (DirContextAdapter) ctx;

        if (isPrincipal(context))
        {
            return principalContextMapper.mapFromContext(context);
        }
        else if (isGroup(context))
        {
            return groupContextMapper.mapFromContext(context);
        }
        else if (isRole(context))
        {
            return roleContextMapper.mapFromContext(context);
        }
        else
        {
            return null;    
        }
    }

    protected boolean isPrincipal(DirContextAdapter ctx)
    {
        return isOfObjectClass(ctx, ldapPropertiesMapper.getUserObjectClass())
                && isInSubtreeScope(ctx, principalDN);
    }

    protected boolean isGroup(DirContextAdapter ctx)
    {
        return isOfObjectClass(ctx, ldapPropertiesMapper.getGroupObjectClass())
                && isInSubtreeScope(ctx, groupDN);
    }

    protected boolean isRole(DirContextAdapter ctx)
    {
        return isOfObjectClass(ctx, ldapPropertiesMapper.getRoleObjectClass())
                && isInSubtreeScope(ctx, roleDN);
    }

    /**
     * Returns true if <code>member</code> is of the LDAP objectclass <code>objectClassToMatch</code>. Will match on
     * superclasses as well, so matching on <code>top</code> would/should match every object.
     *
     * @param ctx
     * @param objectClassToMatch
     * @return
     */
    protected boolean isOfObjectClass(DirContextAdapter ctx, String objectClassToMatch)
    {
        String objectClassName = ldapPropertiesMapper.getObjectClassAttribute();
        // LDAP objects often have multi-valued attributes for their objectclass, eg {top, person, organizationalperson, inetorgperson}
        try
        {
            Attributes attributes = ctx.getAttributes();
            if (attributes != null)
            {
                Attribute objectClassAttr = attributes.get(objectClassName);
                if (objectClassAttr != null)
                {
                    NamingEnumeration objectClasses = objectClassAttr.getAll();
                    while (objectClasses.hasMore())
                    {
                        Object objectClass = objectClasses.next();
                        if (objectClass != null && objectClassToMatch.equalsIgnoreCase((String) objectClass))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        catch (javax.naming.NamingException e)
        {
            logger.warn("Unable to determine class type of " + ctx.getDn() + ".", e);
        }
        return false;
    }

    protected boolean isInSubtreeScope(DirContextAdapter ctx, DistinguishedName baseDN)
    {
        // we use startsWith and not endsWith because DNs are
        // logically stored backwards (ie. less specific to more specific)
        return ctx.getDn().startsWith(baseDN);
    }
}
