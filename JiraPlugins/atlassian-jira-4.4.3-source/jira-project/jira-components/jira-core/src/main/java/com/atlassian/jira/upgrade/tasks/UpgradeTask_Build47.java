/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.ObjectUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class UpgradeTask_Build47 extends AbstractUpgradeTask
{
    private final PermissionSchemeManager psm;
    private final SchemePermissions schemePermissions;

    public UpgradeTask_Build47(PermissionSchemeManager psm, SchemePermissions schemePermissions)
    {
        this.psm = psm;
        this.schemePermissions = schemePermissions;
    }

    //Set permSet;
    public String getBuildNumber()
    {
        return "47";
    }

    public String getShortDescription()
    {
        return "Create permission schemes and migrate data";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        //Get a list of permissions
        Set permSet = schemePermissions.getSchemePermissions().entrySet();

        //Create the default permission scheme if no schemes exist
        GenericValue defaultScheme = psm.createDefaultScheme();

        //Loop through the permission events and add all global permissions to the default scheme
        for (Iterator iterator = permSet.iterator(); iterator.hasNext();)
        {
            Entry mapEntry = (Entry) iterator.next();
            Integer type = (Integer) mapEntry.getKey();

            //Add the global permission to the scheme
            addOldGlobalPermissionsToScheme(psm, type.intValue(), defaultScheme);
        }

        //add the ADMIN and USER permissions as global permissions
        //Pass a null scheme which mean that they are still global in the new configuration
        addOldGlobalPermissionsToScheme(psm, Permissions.ADMINISTER, null);
        addOldGlobalPermissionsToScheme(psm, Permissions.USE, null);

        //add this as global as well
        addOldGlobalPermissionsToScheme(psm, Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, null);

        //create permission schemes for the projects
        createProjectSchemes(psm, defaultScheme);

        mergeDuplicateSchemes(psm);

        // Removing this - what is there to cache here?
        //ManagerFactory.removeService(PermissionManager.class);

        ManagerFactory.getPermissionSchemeManager().flushSchemeEntities();

        return;
    }

    /**
     * Given a scheme, add the old global permissions to it.  This is needed as the old global permissions were part
     * of project permissions as well.
     */
    public void addOldGlobalPermissionsToScheme(SchemeManager psm, int permType, GenericValue scheme) throws GenericEntityException, CreateException
    {
        //get all global permissions for this permissiontype
        List globalPermissions = CoreFactory.getGenericDelegator().findByAnd("Permission", EasyMap.build("type", new Long(permType), "project", null));

        //Add the global permissions for this type for all groups that already have them
        for (Iterator iterator = globalPermissions.iterator(); iterator.hasNext();)
        {
            GenericValue perm = (GenericValue) iterator.next();

            if (scheme == null)
                ManagerFactory.getGlobalPermissionManager().addPermission(permType, perm.getString("group"));
            else
                psm.createSchemeEntity(scheme, new SchemeEntity(GroupDropdown.DESC, perm.getString("group"), new Long(permType)));
        }
    }

    /**
     * Adds all project permissions to the scheme.  Given a project, get the project specific permissions, and add them
     * to this scheme.
     */
    public void addOldProjectPermissionsToScheme(SchemeManager psm, int permType, GenericValue scheme, Long project) throws GenericEntityException
    {
        //get all project permissions for this permissiontype
        List globalPermissions = CoreFactory.getGenericDelegator().findByAnd("Permission", EasyMap.build("type", new Long(permType), "project", project));

        //Add the project permissions for this type for all groups that already have them
        for (Iterator iterator = globalPermissions.iterator(); iterator.hasNext();)
        {
            GenericValue perm = (GenericValue) iterator.next();

            List perms = psm.getEntities(scheme, new Long(permType), perm.getString("group"));

            if (perms == null || perms.size() == 0)
                psm.createSchemeEntity(scheme, new SchemeEntity(GroupDropdown.DESC, perm.getString("group"), new Long(permType)));
        }
    }

    /**
     * Create schemes based on project specific permissions. Get all projects and check if they have project
     * specific permissions set up. If there are project specific permissions, then create a new scheme for it.  If
     * there are no specific permissions then associate the project with the default scheme.
     */
    public void createProjectSchemes(SchemeManager psm, GenericValue defaultScheme) throws GenericEntityException, CreateException
    {
        List projects = CoreFactory.getGenericDelegator().findAll("Project");

        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();

            //if the project has no permissions then associate with the default scheme
            List permissions = CoreFactory.getGenericDelegator().findByAnd("Permission", EasyMap.build("project", project.getLong("id")));

            //if there are permissions then add a new scheme for this project
            if (permissions != null && permissions.size() > 0)
            {
                GenericValue projectScheme = psm.createScheme(project.getString("name"), "This permission scheme has been set up for Project " + project.getString("name"));

                //Add the scheme to the project
                psm.addSchemeToProject(project, projectScheme);

                Set permSet = schemePermissions.getSchemePermissions().entrySet();

                //Loop through the permission events
                for (Iterator permissionIterator = permSet.iterator(); permissionIterator.hasNext();)
                {
                    Entry mapEntry = (Entry) permissionIterator.next();
                    Integer type = (Integer) mapEntry.getKey();

                    //Add the global permission to the scheme
                    addOldGlobalPermissionsToScheme(psm, type.intValue(), projectScheme);

                    //add the project permissions to the scheme
                    addOldProjectPermissionsToScheme(psm, type.intValue(), projectScheme, project.getLong("id"));
                }
            }
            else
            {
                psm.addSchemeToProject(project, defaultScheme);
            }
        }
    }

    /**
     * Merges duplicate schemes. The second scheme of a duplicate set will be
     * deleted and all projects associated to that scheme will be moved to the first scheme
     * @throws Exception
     */
    public void mergeDuplicateSchemes(SchemeManager psm) throws Exception
    {
        Iterator schemes = psm.getSchemes().iterator();

        //loop each scheme through the rest of the schemes
        while (schemes.hasNext())
        {
            GenericValue scheme = (GenericValue) schemes.next();

            //if something was modified, then reset the list of schemes
            if (mergeSchemeIter(psm, scheme))
                schemes = psm.getSchemes().iterator();
        }
    }

    /**
     * Given a scheme, find any duplicates and merge them.
     */
    public boolean mergeSchemeIter(SchemeManager psm, GenericValue scheme) throws Exception
    {
        boolean modified = false;

        Iterator schemes = psm.getSchemes().iterator();

        while (schemes.hasNext())
        {
            GenericValue schemeB = (GenericValue) schemes.next();

            //cannot merge a scheme with itself, or with the default scheme
            if (!scheme.equals(schemeB) && !(scheme.equals(psm.getDefaultScheme()) || schemeB.equals(psm.getDefaultScheme())))
            {
                //if the 2 schemes are duplicates then delete the second scheme and move all projects to the first scheme
                if (isDuplicate(psm, scheme, schemeB))
                {
                    deleteSchemeAndMoveProjects(psm, scheme, schemeB);

                    //remove the scheme from the list also
                    schemes.remove();
                    modified = true;
                }
            }
        }

        return modified;
    }

    /**
     * Deletes one scheme and moves projects to the other scheme
     * @param schemeA The scheme that the projects will be moved to
     * @param schemeB The scheme that will be deleted
     */
    public void deleteSchemeAndMoveProjects(SchemeManager psm, GenericValue schemeA, GenericValue schemeB) throws Exception
    {
        List projects = psm.getProjects(schemeB);

        psm.deleteScheme(schemeB.getLong("id"));

        for (Iterator iterator = projects.iterator(); iterator.hasNext();)
        {
            GenericValue project = (GenericValue) iterator.next();
            psm.addSchemeToProject(project, schemeA);

            schemeA.set("name", schemeA.getString("name") + ", " + project.getString("name"));
            schemeA.set("description", schemeA.getString("description") + ", " + project.getString("name"));
        }

        // The problem is that the scheme name cannot be longer than about 250 chars (due to firebird). So we need to bound it here
        // See JRA-6295
        String schemeName = schemeA.getString("name");
        if (schemeName != null && schemeName.length() > 250)
        {
            schemeA.set("name", schemeName.substring(0, 245) + "...");
        }

        schemeA.store();
    }

    /**
     * Checks to see if two schemes are duplicates based on their permissions, parameters and types
     * @param schemeA First scheme to compare
     * @param schemeB Second scheme to compare
     * @return true if the schemes are duplicates otherwise false
     */
    public boolean isDuplicate(SchemeManager psm, GenericValue schemeA, GenericValue schemeB) throws GenericEntityException
    {
        List permissionsA = psm.getEntities(schemeA);
        List permissionsB = psm.getEntities(schemeB);

        //only continue if there are the same amount of permissions
        if (permissionsA.size() == permissionsB.size())
        {
            //permissions have to be in the same order if they are to be compared in order
            Collections.sort(permissionsA);
            Collections.sort(permissionsB);

            for (int i = 0; i < permissionsA.size(); i++)
            {
                GenericValue permA = (GenericValue) permissionsA.get(i);
                GenericValue permB = (GenericValue) permissionsB.get(i);

                //permission can never be null.
                if (!permA.getLong("permission").equals(permB.getLong("permission")))
                    return false;

                //compare ignoring nulls
                if (ObjectUtils.isDifferent(permA.getString("type"), permB.getString("type")))
                    return false;

                //compare ignoring nulls
                if (ObjectUtils.isDifferent(permA.getString("parameter"), permB.getString("parameter")))
                    return false;
            }

            return true;
        }

        return false;
    }
}
