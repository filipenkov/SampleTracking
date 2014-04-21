package com.atlassian.jira.plugin.profile;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.projectpanel.impl.VersionDrillDownRenderer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A user profile panel that displays the users personal roadmap
 *
 * @since v4.1
 */
public class RoadmapUserProfilePanel implements ViewProfilePanel, OptionalUserProfilePanel
{
    private static final Logger log = Logger.getLogger(RoadmapUserProfilePanel.class);


    private final VersionDrillDownRenderer panelRenderer;
    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final VelocityRequestContextFactory requestContextFactory;
    private final ProjectManager projectManager;
    private final UserProjectHistoryManager projectHistoryManager;
    private final PermissionManager permissionManager;
    private final WebResourceManager webResourceManager;
    private final FieldVisibilityManager fieldVisibilityManager;
    private ViewProfilePanelModuleDescriptor moduleDescriptor;

    public RoadmapUserProfilePanel(VersionDrillDownRenderer panelRenderer, JiraAuthenticationContext authenticationContext,
                                   VersionManager versionManager, VelocityRequestContextFactory requestContextFactory,
                                   ProjectManager projectManager, UserProjectHistoryManager projectHistoryManager,
                                   PermissionManager permissionManager, WebResourceManager webResourceManager,
                                   FieldVisibilityManager fieldVisibilityManager)
    {
        this.panelRenderer = panelRenderer;
        this.authenticationContext = authenticationContext;
        this.versionManager = versionManager;
        this.requestContextFactory = requestContextFactory;
        this.projectManager = projectManager;
        this.projectHistoryManager = projectHistoryManager;
        this.permissionManager = permissionManager;
        this.webResourceManager = webResourceManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }


    public void init(ViewProfilePanelModuleDescriptor moduleDescriptor)
    {

        this.moduleDescriptor = moduleDescriptor;
    }

    /**
     * Only displaythe panel if the it is the current user looking at their own profile,
     * and assignee and fix for are visable in a project that has versions
     *
     * @param profileUser The profile being requested
     * @param currentUser The current user
     * @return true if pofile user equals current user, otherwise false
     */
    public boolean showPanel(User profileUser, User currentUser)
    {
        if (profileUser.equals(currentUser))
        {
            for (Project project : getApplicableProjects(currentUser))
            {
                if (!versionManager.getVersionsUnreleased(project.getId(), true).isEmpty())
                {
                    if ((fieldVisibilityManager.isFieldVisible(project.getId(), IssueFieldConstants.FIX_FOR_VERSIONS, FieldVisibilityManager.ALL_ISSUE_TYPES)) &&
                            fieldVisibilityManager.isFieldVisible(project.getId(), IssueFieldConstants.ASSIGNEE, FieldVisibilityManager.ALL_ISSUE_TYPES))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getHtml(User profileUser)
    {
        final User user = authenticationContext.getLoggedInUser();
        final Project project = getProject(user);
        final Map<String, Object> params = new HashMap<String, Object>();
        webResourceManager.requireResource("jira.webresources:expandoSupport");

        final BrowseContext ctx = new BrowseProjectContext(user, project);


        params.put("textUtils", new TextUtils());
        params.put("isAjaxExpand", isAjaxExpand());
        params.put("browsecontext", ctx);
        params.put("project", ctx.getProject());
        params.put("versionManager", versionManager);
        params.put("panelRenderer", panelRenderer);
        params.put("browseableProjects", getApplicableProjects(user));

        return moduleDescriptor.getHtml(VIEW_TEMPLATE, params);

    }

    private Collection<Project> getApplicableProjects(User user)
    {
        return permissionManager.getProjectObjects(Permissions.BROWSE, user);
    }

    private boolean isAjaxExpand()
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();

        return StringUtils.isNotBlank(requestContext.getRequestParameter("expandVersion")) && StringUtils.isNotBlank(requestContext.getRequestParameter("contentOnly"));

    }

    private Project getProject(User currentUser)
    {
        final VelocityRequestContext requestContext = requestContextFactory.getJiraVelocityRequestContext();
        String projectId = requestContext.getRequestParameter("pid");
        if (StringUtils.isNotBlank(projectId))
        {
            final Project project = projectManager.getProjectObj(Long.parseLong(projectId));
            if (permissionManager.hasPermission(Permissions.BROWSE, project, currentUser))
            {
                projectHistoryManager.addProjectToHistory(currentUser, project);
                return project;
            }
        }
        return projectHistoryManager.getCurrentProject(Permissions.BROWSE, currentUser);

    }
}
