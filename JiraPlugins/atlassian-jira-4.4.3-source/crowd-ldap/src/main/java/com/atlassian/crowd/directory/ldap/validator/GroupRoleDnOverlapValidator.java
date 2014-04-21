package com.atlassian.crowd.directory.ldap.validator;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.exception.*;
import org.apache.commons.lang.StringUtils;

/**
 * Checks if the Role DN Addition and Group DN Addition overlap each other
 */
public class GroupRoleDnOverlapValidator implements Validator
{
    public String getError(Directory directory)
    {
        boolean rolesDisabled = Boolean.parseBoolean(directory.getValue(LDAPPropertiesMapper.ROLES_DISABLED));
        DirectoryType directoryType = directory.getType();
        // For Internal and Delegating Roles cannot be disabled or overlapping
        if (!rolesDisabled && DirectoryType.CONNECTOR.equals(directoryType))
        {
            String roleDN = directory.getValue(LDAPPropertiesMapper.ROLE_DN_ADDITION);
            String groupDN = directory.getValue(LDAPPropertiesMapper.GROUP_DN_ADDITION);
            // Check if role dn overlaps with group dn
            if (isDNConfigOverlapping(roleDN, groupDN))
            {
                return "The supplied Role DN is invalid. It overlaps with the Group DN value.";
            }
        }

        return null;
    }

    /**
     * @param roleDN  DN addition for roles.
     * @param groupDN DN addition for groups.
     * @return true if either roleDN or groupDN are blank; or if roleDN starts with groupDN or vice versa.
     */
    public boolean isDNConfigOverlapping(String roleDN, String groupDN)
    {
        // Check if role dn overlaps with group dn
        if (StringUtils.isBlank(roleDN) || StringUtils.isBlank(groupDN))
        {
            return true;
        }

        roleDN = roleDN + ",";
        groupDN = groupDN + ",";
        if (StringUtils.startsWith(roleDN, groupDN) || StringUtils.startsWith(groupDN, roleDN))
        {
            return true;
        }

        return false;
    }
}
