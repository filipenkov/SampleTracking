package com.atlassian.jira.web.action.setup;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.security.JiraPermission;
import com.atlassian.jira.security.Permissions;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * This helper is used to create users, groups and memberships in the pre-Embedded Crowd OSUser tables.
 * The setup entities need to be in theOSUser tables, because later setup tasks rely on them being in that format.
 *
 * @since v4.3
 */
public class SetupOldUserHelper
{
    private static final String OSUSER_ENTITY = "OSUser";
    private static final String OSUSER_ID = "id";
    private static final String OSUSER_NAME = "name";
    private static final String OSUSER_PASSWORD_HASH = "passwordHash";

    private static final String OSGROUP_ENTITY = "OSGroup";
    private static final String OSGROUP_ID = "id";
    private static final String OSGROUP_NAME = "name";

    private static final String OSMEMBERSHIP_ENTITY = "OSMembership";
    private static final String OSMEMBERSHIP_ID = "id";
    private static final String OSMEMBERSHIP_GROUPNAME = "groupName";
    private static final String OSMEMBERSHIP_USERNAME = "userName";

    public static GenericValue addUser(UserService.CreateUserValidationResult result) throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        List currentuser = genericDelegator.findByAnd(OSUSER_ENTITY, EasyMap.build(OSUSER_NAME, result.getUsername()));
        if (currentuser != null && currentuser.size() > 0)
        {
            return (GenericValue) currentuser.get(0);
        }

        PasswordEncoderFactory passwordEncoderFactory = ComponentManager.getComponentInstanceOfType(PasswordEncoderFactory.class);
        String hash = passwordEncoderFactory.getEncoder(PasswordEncoderFactory.ATLASSIAN_SECURITY_ENCODER).encodePassword(result.getPassword(), null);

        GenericValue user = genericDelegator.create(OSUSER_ENTITY, EasyMap.build(OSUSER_ID, genericDelegator.getNextSeqId(OSUSER_ENTITY), OSUSER_NAME, result.getUsername(),
                OSUSER_PASSWORD_HASH, hash));

        // Add the user name and email properties
        JiraPropertySetFactory jiraPropertySetFactory = ComponentManager.getComponentInstanceOfType(JiraPropertySetFactory.class);
        PropertySet ps = jiraPropertySetFactory.buildNoncachingPropertySet(OSUSER_ENTITY, user.getLong(OSUSER_ID));
        ps.setString("fullName", result.getFullname());
        ps.setString("email", result.getEmail());
       
        return user;
    }

    public static boolean groupExists(String groupName) throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        List currentuser = genericDelegator.findByAnd(OSGROUP_ENTITY, EasyMap.build(OSGROUP_NAME, groupName));
        if (currentuser != null && currentuser.size() > 0)
        {
            return true;
        }
        return false;
    }

    public static GenericValue addGroup(String groupName) throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        List currentuser = genericDelegator.findByAnd(OSGROUP_ENTITY, EasyMap.build(OSGROUP_NAME, groupName));
        if (currentuser != null && currentuser.size() > 0)
        {
            return (GenericValue) currentuser.get(0);
        }
        return genericDelegator.create(OSGROUP_ENTITY, EasyMap.build(OSGROUP_ID, genericDelegator.getNextSeqId(OSGROUP_ENTITY), OSGROUP_NAME, groupName));
    }


    public static GenericValue addToGroup(final String groupName, final String userName) throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        List currentuser = genericDelegator.findByAnd(OSMEMBERSHIP_ENTITY, EasyMap.build(
                OSMEMBERSHIP_GROUPNAME, groupName, OSMEMBERSHIP_USERNAME, userName));
        if (currentuser != null && currentuser.size() > 0)
        {
            return (GenericValue) currentuser.get(0);
        }
        return genericDelegator.create(OSMEMBERSHIP_ENTITY, EasyMap.build(OSMEMBERSHIP_ID, genericDelegator.getNextSeqId(OSMEMBERSHIP_ENTITY),
                OSMEMBERSHIP_GROUPNAME, groupName, OSMEMBERSHIP_USERNAME, userName));
    }

    public static GenericValue getExistingAdmins() throws GenericEntityException
    {
        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        final Collection<JiraPermission> jiraPermissions = ManagerFactory.getGlobalPermissionManager().getPermissions(Permissions.ADMINISTER);
        for (JiraPermission jiraPermission : jiraPermissions)
        {
            List currentuser = genericDelegator.findByAnd(OSMEMBERSHIP_ENTITY, EasyMap.build(OSMEMBERSHIP_GROUPNAME, jiraPermission.getGroup()));
            if (currentuser != null && currentuser.size() > 0)
            {
                return (GenericValue) currentuser.get(0);
            }
        }

        return null;
    }


}
