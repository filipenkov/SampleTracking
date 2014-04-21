package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.I18nBean;

/**
 * Renders the Project Admin link
 *
 * @since v4.4
 */
public class ProjectAdminMenuFragment implements MenuFragment
{
    private final PermissionManager permissionManager;
    private final VelocityRequestContextFactory requestContextFactory;
    private final I18nBean.CachingFactory i18nFactory;

    public ProjectAdminMenuFragment(PermissionManager permissionManager, VelocityRequestContextFactory requestContextFactory, I18nBean.CachingFactory i18nFactory)
    {
        this.permissionManager = permissionManager;
        this.requestContextFactory = requestContextFactory;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public String getId()
    {
        return "project-admin-menu-frag";
    }

    @Override
    public String getHtml(BrowseContext ctx)
    {
        final String projectKey = JiraUrlCodec.encode(ctx.getProject().getKey());
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        final String baseUrl = requestContext.getBaseUrl();
        final I18nHelper i18n = i18nFactory.getInstance(ctx.getUser());


        return "<a id=\"project-admin-link\" href=\"" + baseUrl + "/plugins/servlet/project-config/" + projectKey + "\" class=\"lnk icon-tools\">" + i18n.getText("common.concepts.administer.project") + "</a>";

    }

    @Override
    public boolean showFragment(BrowseContext ctx)
    {
        final Project project = ctx.getProject();
        return  permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, ctx.getUser());

    }
}
