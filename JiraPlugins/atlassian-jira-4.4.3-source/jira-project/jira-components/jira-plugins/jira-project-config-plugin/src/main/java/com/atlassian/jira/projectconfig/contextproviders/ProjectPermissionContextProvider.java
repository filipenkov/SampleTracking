package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.ProjectPermissionSchemeHelper;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * ContextProvider for the Permissions Tab in project Config
 *
 * @since v4.4
 */
public class ProjectPermissionContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_SCHEME_SHARED_PROJECTS_KEY = "sharedProjects";
    private static final String CONTEXT_SCHEME_NAME = "schemeName";
    private static final String CONTEXT_SCHEME_ID = "schemeId";
    private static final String CONTEXT_SCHEME_DESCRIPTION = "schemeDescription";

    private static final String CONTEXT_PERMISSION_GROUPS = "permissionGroups";
    private static final String ENTITY_TYPE = "type";

    private static final String ENTITY_PARAMETER = "parameter";
    private static final String SCHEME_NAME = "name";
    private static final String SCHEME_ID = "id";

    private static final String SCHEME_DESCRIPTION = "description";

    private final List<String> NON_PARAM_PERMISSION_TYPES = CollectionBuilder.list("reportercreate", "reporter", "lead", "assignee");
    private final SchemePermissions schemePermissions;
    private final PermissionSchemeManager permissionSchemeManager;
    private final SecurityTypeManager securityTypeManager;
    private final ContextProviderUtils contextProviderUtils;
    private final ProjectPermissionSchemeHelper helper;


    public ProjectPermissionContextProvider(PermissionSchemeManager permissionSchemeManager, SecurityTypeManager securityTypeManager,
            final ContextProviderUtils contextProviderUtils, final ProjectPermissionSchemeHelper helper)
    {
        this.securityTypeManager = securityTypeManager;
        this.contextProviderUtils = contextProviderUtils;
        this.helper = helper;
        this.schemePermissions = new SchemePermissions();
        this.permissionSchemeManager = permissionSchemeManager;
    }


    public void init(Map<String, String> params) throws PluginParseException
    {

    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {

        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final I18nHelper i18nHelper = (I18nHelper) defaultContext.get(ContextProviderUtils.CONTEXT_I18N_KEY);

        final GenericValue scheme = getProjectPermissionsScheme(project);

        final Scheme permissionScheme = permissionSchemeManager.getSchemeObject(scheme.getLong("id"));
        final List<Project> sharedProjects = helper.getSharedProjects(permissionScheme);
        contextMap.add(CONTEXT_SCHEME_SHARED_PROJECTS_KEY, sharedProjects);

        contextMap.add(CONTEXT_SCHEME_NAME, scheme.getString(SCHEME_NAME));
        contextMap.add(CONTEXT_SCHEME_ID, scheme.getLong(SCHEME_ID));
        final String description = scheme.getString(SCHEME_DESCRIPTION);
        if (StringUtils.isNotBlank(description))
        {
            contextMap.add(CONTEXT_SCHEME_DESCRIPTION, description);
        }

        final List<SimplePermissionGroup> permissionGroups = new ArrayList<SimplePermissionGroup>();

        permissionGroups.add(new SimplePermissionGroup("project", i18nHelper.getText("admin.permission.group.project.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getProjectPermissions().values())));
        permissionGroups.add(new SimplePermissionGroup("issue", i18nHelper.getText("admin.permission.group.issue.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getIssuePermissions().values())));
        permissionGroups.add(new SimplePermissionGroup("voterAndWatchers", i18nHelper.getText("admin.permission.group.voters.and.watchers.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getVotersAndWatchersPermissions().values())));
        permissionGroups.add(new SimplePermissionGroup("comments", i18nHelper.getText("admin.permission.group.comments.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getCommentsPermissions().values())));
        permissionGroups.add(new SimplePermissionGroup("attachments", i18nHelper.getText("admin.permission.group.attachments.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getAttachmentsPermissions().values())));
        permissionGroups.add(new SimplePermissionGroup("timeTracking", i18nHelper.getText("admin.permission.group.time.tracking.permissions"), getPermission(i18nHelper, scheme, schemePermissions.getTimeTrackingPermissions().values())));

        contextMap.add(CONTEXT_PERMISSION_GROUPS, permissionGroups);

        return contextMap.toMap();
    }

    private List<SimplePermission> getPermission(I18nHelper i18nHelper, GenericValue scheme, Collection<Permission> permissions)
    {
        List<SimplePermission> returnPermissions = new ArrayList<SimplePermission>(permissions.size());
        for (Permission permission : permissions)
        {
            final List<GenericValue> entities = getEntities(scheme, permission);
            final List<String> entityDisplays = new ArrayList<String>(entities.size());
            for (GenericValue entity : entities)
            {
                final String typeStr = entity.getString(ENTITY_TYPE);
                final SecurityType type = securityTypeManager.getSecurityType(typeStr);
                final StringBuilder sb = new StringBuilder(type.getDisplayName());
                final String paramater = entity.getString(ENTITY_PARAMETER);
                if (StringUtils.isNotBlank(paramater))
                {
                    sb.append(" (").append(type.getArgumentDisplay(paramater)).append(")");
                }
                else if (!NON_PARAM_PERMISSION_TYPES.contains(type.getType()))
                {
                    sb.append(" (").append(i18nHelper.getText("admin.common.words.anyone")).append(")");
                }
                entityDisplays.add(sb.toString());

            }
            returnPermissions.add(new SimplePermission(Permissions.getShortName(Integer.valueOf(permission.getId())), permission.getName(), permission.getDescription(), entityDisplays));

        }

        return returnPermissions;
    }

    private List<GenericValue> getEntities(GenericValue scheme, Permission permission)
    {
        try
        {
            return permissionSchemeManager.getEntities(scheme, Long.valueOf(permission.getId()));
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private GenericValue getProjectPermissionsScheme(final Project project)
    {
        try
        {
            return EntityUtil.getOnly(permissionSchemeManager.getSchemes(project.getGenericValue()));
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimplePermissionGroup
    {
        private final String id;
        private final String name;
        private final List<SimplePermission> permissions;

        public SimplePermissionGroup(String id, String name, List<SimplePermission> permissions)
        {
            this.id = id;
            this.name = name;
            this.permissions = permissions;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public List<SimplePermission> getPermissions()
        {
            return permissions;
        }
    }

    public static class SimplePermission
    {
        private final String shortName;
        private final String name;
        private final String description;
        private final List<String> entities;

        public SimplePermission(String shortName, String name, String description, List<String> entities)
        {
            this.shortName = shortName;
            this.name = name;
            this.description = description;
            this.entities = entities;
        }

        public String getShortName()
        {
            return shortName;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public List<String> getEntities()
        {
            return entities;
        }
    }

}
