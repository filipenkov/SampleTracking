package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

/**
 * This upgrade task adds new scheme entities to all permission schemes such that the scheme will have delete all
 * attachement and delete all comment permissions that match what exists for the delete issues permission.
 */
public class UpgradeTask_Build231 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build231.class);

    private final PermissionSchemeManager permissionSchemeManager;

    public UpgradeTask_Build231(PermissionSchemeManager permissionSchemeManager)
    {
        this.permissionSchemeManager = permissionSchemeManager;
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "This task updates all permission schemes with delete all attachment and delete all comment permissions for whatever the existing DELETE_ISSUE permission contains.";
    }

    /**
     * Returns 231 as string
     *
     * @return 231 as string
     */
    public String getBuildNumber()
    {
        return "231";
    }

    public void doUpgrade(boolean setupMode) throws GenericEntityException
    {
        log.info("About to add delete all comment and delete all attachment permissions to all permission schemes...");
        try
        {
            final List /* <GenericValue> */ schemes = permissionSchemeManager.getSchemes();
            for (Iterator i = schemes.iterator(); i.hasNext();)
            {
                GenericValue schemeGV = (GenericValue) i.next();
                addPermissionsIfNoEntityAlreadyHasIt(schemeGV, Permissions.COMMENT_DELETE_ALL);
                addPermissionsIfNoEntityAlreadyHasIt(schemeGV, Permissions.ATTACHMENT_DELETE_ALL);
            }
            log.info("Done adding delete all comment and delete all attachment permissions to all permission schemes.");
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to retrieve all permission schemes.", e);
            throw e;
        }
    }

    private void addPermissionsIfNoEntityAlreadyHasIt(GenericValue schemeGV, int permissionType) throws GenericEntityException
    {
        if (permissionSchemeManager.getEntities(schemeGV, new Long(permissionType)).isEmpty())
        {
            // Copy all the scheme entities registered for the delete issue permission to delete all comment and
            // delete all attachments
            List entities = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.DELETE_ISSUE));
            for (Iterator iterator = entities.iterator(); iterator.hasNext();)
            {
                GenericValue schemeEntity = (GenericValue) iterator.next();
                // Add a scheme entity for the permission type that is
                // the same as the scheme entity that exists for the DELETE_ISSUE permission type.
                addSchemeEntityForPermission(schemeGV, schemeEntity, permissionType);
            }
        }
        else
        {
            log.info("There are already existing entities for permission of type '"
                    + Permissions.getShortName(permissionType) + "', in permission scheme '"
                    + schemeGV.getString("name") + "'");
        }
    }

    private void addSchemeEntityForPermission(GenericValue schemeGV, GenericValue origSchemeEntity, int permissionType) throws GenericEntityException
    {
        if (log.isDebugEnabled())
        {
            log.debug("About to add permission of type '" + Permissions.getShortName(permissionType) + "' for '" + origSchemeEntity.getString("type") + "' and parameter '" + origSchemeEntity.getString("parameter") + "' to permission scheme '"
                    + schemeGV.getString("name") + "'");
        }

        SchemeEntity schemeEntity = new SchemeEntity(origSchemeEntity.getString("type"), origSchemeEntity.getString("parameter"), new Long(permissionType));
        try
        {
            permissionSchemeManager.createSchemeEntity(schemeGV, schemeEntity);
        }
        catch (GenericEntityException e)
        {
            log.error("Failed to add permission of type '" + Permissions.getShortName(permissionType) + "' for '"
                    + schemeEntity + "' to permission scheme '" + schemeGV.getString("name") + "'!");
        }        
    }
}
