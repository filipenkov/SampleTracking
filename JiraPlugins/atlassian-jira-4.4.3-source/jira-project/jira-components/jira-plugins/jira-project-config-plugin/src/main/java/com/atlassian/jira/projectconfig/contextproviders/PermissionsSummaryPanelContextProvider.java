package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginParseException;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 *
 * Provides data to the permissions summary template, on the project configuration page
 *
 * @since v4.4
 */
public class PermissionsSummaryPanelContextProvider implements CacheableContextProvider
{
    private static final String PROJECT_PERMISSIONS_SCHEME = "projectPermissionsScheme";
    private static final String PROJECT_PERMISSION_URL = "projectPermissionUrl";
    
    private static final String ISSUE_SECURITY_SCHEME = "issueSecurityScheme";
    private static final String ISSUE_SECURITY_SCHEME_URL = "issueSecuritySchemeUrl";

    private final ContextProviderUtils providerUtils;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final TabUrlFactory tabUrlFactory;

    public PermissionsSummaryPanelContextProvider(final PermissionSchemeManager permissionSchemeManager,
            final IssueSecuritySchemeManager issueSecuritySchemeManager, final ContextProviderUtils providerUtils,
            TabUrlFactory tabUrlFactory)
    {
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.providerUtils = providerUtils;
        this.tabUrlFactory = tabUrlFactory;
    }

    public void init(Map<String, String> context) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);

        final GenericValue projectGV = providerUtils.getProject().getGenericValue();
        final GenericValue issueSecuritySchemeGv = getIssueSecuritySchemes(projectGV);

        if (issueSecuritySchemeGv != null)
        {
            final SimpleIssueSecurityScheme issueSecuritySchemeBean = gvToIssueSecurityScheme(issueSecuritySchemeGv);
            contextMap.add(ISSUE_SECURITY_SCHEME, issueSecuritySchemeBean);

        }
        contextMap.add(ISSUE_SECURITY_SCHEME_URL, tabUrlFactory.forIssueSecurity());

        final GenericValue projectPermissionsSchemeGV = getProjectPermissionsScheme(projectGV);

        if (projectPermissionsSchemeGV != null)
        {
            final SimpleProjectPermissionsScheme projectPermissionsSchemeBean =
                    gvToProjectPermissionsScheme(projectPermissionsSchemeGV);

            contextMap.add(PROJECT_PERMISSIONS_SCHEME, projectPermissionsSchemeBean);
        }
        contextMap.add(PROJECT_PERMISSION_URL, tabUrlFactory.forPermissions());

        return contextMap.toMap();
    }

    private UrlBuilder createUrlBuilder(final String operation)
    {
        return providerUtils.createUrlBuilder(operation);
    }

    GenericValue getProjectPermissionsScheme(final GenericValue projectGV)
    {
        try
        {
            return EntityUtil.getOnly(permissionSchemeManager.getSchemes(projectGV));
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    GenericValue getIssueSecuritySchemes(final GenericValue projectGV)
    {
        try
        {
            return EntityUtil.getOnly(issueSecuritySchemeManager.getSchemes(projectGV));
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    SimpleProjectPermissionsScheme gvToProjectPermissionsScheme(final GenericValue projectPermssionsGV)
    {
        return new SimpleProjectPermissionsScheme(projectPermssionsGV.getString("id"), projectPermssionsGV.getString("description"),
                projectPermssionsGV.getString("name"));
    }

    SimpleIssueSecurityScheme gvToIssueSecurityScheme(final GenericValue issueSecurityGV)
    {
        return new SimpleIssueSecurityScheme(issueSecurityGV.getString("id"), issueSecurityGV.getString("description"),
                issueSecurityGV.getString("name"));
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleProjectPermissionsScheme
    {
        private final String id;
        private final String description;
        private final String name;

        SimpleProjectPermissionsScheme(final String id, final String description, final String name)
        {
            this.id = id;
            this.description = description;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getDescription()
        {
            return description;
        }

        public String getName()
        {
            return name;
        }
    }

    public static class SimpleIssueSecurityScheme
    {
        private final String id;
        private final String description;
        private final String name;

        SimpleIssueSecurityScheme(final String id, final String description, final String name)
        {
            this.id = id;
            this.description = description;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getDescription()
        {
            return description;
        }

        public String getName()
        {
            return name;
        }
    }
}
