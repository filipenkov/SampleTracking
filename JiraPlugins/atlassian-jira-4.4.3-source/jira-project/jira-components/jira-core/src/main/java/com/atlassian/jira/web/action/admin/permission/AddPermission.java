/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.permission;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.scheme.SchemeTypeManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.util.Map;

@WebSudoRequired
public class AddPermission extends SchemeAwarePermissionAction
{
    private String type;
    private SchemeTypeManager schemeTypeManager = ManagerFactory.getPermissionTypeManager();
    private Long[] permissions;
    private final SchemePermissions schemePermissions;

    public AddPermission(SchemePermissions schemePermissions)
    {
        this.schemePermissions = schemePermissions;
    }

    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected void doValidation()
    {
        try
        {
            String permType = getType();
            SchemeType schemeType = schemeTypeManager.getSchemeType(permType);
            if (getSchemeId() == null || getScheme() == null)
            {
                addErrorMessage(getText("admin.permissions.errors.mustselectscheme"));
            }
            if (getPermissions() == null || getPermissions().length == 0)
            {
                addError("permissions", getText("admin.permissions.errors.mustselectpermission"));
            }
            if (!TextUtils.stringSet(permType))
            {
                addErrorMessage(getText("admin.permissions.errors.mustselecttype"));
            }
            else
            {
                if (schemeType != null)
                {
                    // Let the scheme type do any specific validation.
                    // It will add Error Messages to the JiraServiceContext
                    schemeType.doValidation(permType, getParameters(), getJiraServiceContext());
                }
            }
            // Check if these Permissions are valid for this SchemeType
            if (getPermissions() != null)
            {
                for (int i = 0; i < getPermissions().length; i++)
                {
                    Long permission = getPermissions()[i];
                    if (schemeType != null && permission != null && !schemeType.isValidForPermission(permission.intValue()))
                    {
                        String permName = getPermissionName(permission.intValue());
                        addErrorMessage(getText("admin.permissions.errors.invalid.combination", permName, schemeType.getDisplayName()));
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.permissions.error.occured.adding", "\n") + e.getMessage());
        }
    }

    private String getPermissionName(int permissionId)
    {
        return ((Permission) getAllPermissions().get(new Integer(permissionId))).getName();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        String permissionType = getType(); //eg 'group' or 'single user'
        String parameter = getParameter(permissionType); // the group or username
        for (int i = 0; i < permissions.length; i++)
        {
            Long permission = permissions[i];

            //if the permission already exists then dont add it again
            if (!permissionExists(permission, permissionType, parameter))
            {
                SchemeEntity schemeEntity = new SchemeEntity(permissionType, parameter, permission);
                ManagerFactory.getPermissionSchemeManager().createSchemeEntity(getScheme(), schemeEntity);
            }
        }
        return getRedirect(getRedirectURL() + getSchemeId());
    }

    private boolean permissionExists(Long permission, String type, String parameter) throws GenericEntityException
    {
        return !(ManagerFactory.getPermissionSchemeManager().getEntities(getScheme(), permission, type, parameter).isEmpty());
    }

    public Map getTypes()
    {
        return schemeTypeManager.getTypes();
    }

    /**
     * The type of the permission (eg group / single user / null for all others)
     *
     * @return the Type of the permission.
     */
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getParameters()
    {
        return ActionContext.getSingleValueParameters();
    }

    /**
     * Because we the value is set dynamically, we need to pull the correct value from the parameter chosen
     *
     * @param key 'group' / 'single user' / null
     * @return the value passed in (eg group name / user name)
     * @see #getType()
     */
    public String getParameter(String key)
    {
        String param = (String) getParameters().get(key);
        return (TextUtils.stringSet(param)) ? param : null;
    }

    public String getRedirectURL()
    {
        return "EditPermissions!default.jspa?schemeId=";
    }

    public Map getAllPermissions()
    {
        return schemePermissions.getSchemePermissions();
    }

    public Long[] getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Long[] permissions)
    {
        this.permissions = permissions;
    }
}
