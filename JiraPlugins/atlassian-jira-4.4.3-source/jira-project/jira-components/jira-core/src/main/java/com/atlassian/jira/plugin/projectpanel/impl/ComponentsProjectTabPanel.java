package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentComparator;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Displays the components of a project.
 *
 */
public class ComponentsProjectTabPanel extends AbstractProjectTabPanel
{
    private static final Logger log = Logger.getLogger(ComponentsProjectTabPanel.class);

    private final PermissionHelper permissionHelper;
    private final ProjectComponentManager projectComponentManager;
    private final FieldVisibilityManager fieldVisibilityManager;

    public ComponentsProjectTabPanel(final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext,
            final ProjectComponentManager projectComponentManager, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(authenticationContext);
        this.projectComponentManager = projectComponentManager;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.permissionHelper = new PermissionHelper(permissionManager);
    }

    public boolean showPanel(BrowseContext ctx)
    {
        final Long projectId = ctx.getProject().getId();
        return isComponentsFieldVisible(projectId) && !projectComponentManager.findAllForProject(projectId).isEmpty();
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        List<ProjectComponent> components = getComponents(ctx.getProject());
        final Map<String, Object> params = super.createVelocityParams(ctx);
        params.put("components", components);
        params.put("hasAdminPermission", permissionHelper.hasProjectAdminPermission(authenticationContext.getUser(), ctx.getProject()));
        params.put("tabpanel", this);
        return params;
    }

    /**
     * Returns true if the components field is visible in at least one scheme, false otherwise.
     *
     * @param projectId project ID
     * @return true if the components field is visible in at least one scheme, false otherwise.
     */
    protected boolean isComponentsFieldVisible(Long projectId)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, IssueFieldConstants.COMPONENTS, null);
    }

    /**
     * Returns true if a user with given username exists, false otherwise.
     *
     * @param username username
     * @return true if a user with given username exists, false otherwise.
     */
    public boolean isUserExists(String username)
    {
        return UserUtils.existsUser(username);
    }

    private List<ProjectComponent> getComponents(final Project project)
    {
        List<ProjectComponent> components = Collections.emptyList();
        if (isComponentsFieldVisible(project.getId()))
        {
            try
            {
                components = new ArrayList<ProjectComponent>(projectComponentManager.findAllForProject(project.getId()));
                Collections.sort(components, ProjectComponentComparator.INSTANCE);
            }
            catch (DataAccessException e)
            {
                log.error("Could not retrieve components for project: " + project, e);
            }
        }
        return components;
    }
}
