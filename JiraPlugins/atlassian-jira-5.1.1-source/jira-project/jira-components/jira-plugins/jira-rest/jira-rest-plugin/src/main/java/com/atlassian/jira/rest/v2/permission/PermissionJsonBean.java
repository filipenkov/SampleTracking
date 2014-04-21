package com.atlassian.jira.rest.v2.permission;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents one permission and whether the caller has it
 *
 * @since v5.0
 */
public class PermissionJsonBean
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

//    @JsonProperty
//    private String type;

    @JsonProperty
    boolean havePermission;

    public PermissionJsonBean(Permissions.Permission permission, boolean havePermission, JiraAuthenticationContext authenticationContext)
    {
       this.id =  Long.toString(permission.getId());
       this.key = permission.name();
       this.name = authenticationContext.getI18nHelper().getText(permission.getNameKey());
       this.description = authenticationContext.getI18nHelper().getText(permission.getDescriptionKey());
//       this.type = permission.getType().name();
       this.havePermission = havePermission;
    }

    // please dont use this one...
    public PermissionJsonBean(Permissions.Permission permission, String name, String description, boolean havePermission)
    {
        this.id = Long.toString(permission.getId());
        this.key = permission.name();
        this.name = name;
        this.description = description;
//        this.type = permission.getType().name();
        this.havePermission = havePermission;
    }
}
