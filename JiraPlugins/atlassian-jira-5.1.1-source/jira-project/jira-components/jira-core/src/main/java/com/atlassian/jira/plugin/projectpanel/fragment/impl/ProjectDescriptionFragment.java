package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.user.UserUtils;

import java.util.Map;

/**
 * The fragment which displays the description of a project.
 *
 * @since v4.0
 */
public class ProjectDescriptionFragment extends AbstractFragment
{
    private final static String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";
    static final String CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY = "projectDescriptionRenderer";

    private final UserFormatManager userFormatManager;
    private final ProjectDescriptionRenderer projectDescriptionRenderer;

    public ProjectDescriptionFragment(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext jiraAuthenticationContext, final UserFormatManager userFormatManager,
            final ProjectDescriptionRenderer projectDescriptionRenderer)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.userFormatManager = userFormatManager;
        this.projectDescriptionRenderer = projectDescriptionRenderer;
    }

    public String getId()
    {
        return "projectdescription";
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("userFormat", userFormatManager.getUserFormat(ProfileLinkUserFormat.TYPE));
        velocityParams.put("leadExists", UserUtils.userExists(ctx.getProject().getLeadUserName()));
        velocityParams.put(CONTEXT_PROJECT_DESCRIPTION_RENDERER_KEY, projectDescriptionRenderer);
        return velocityParams;
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        return true;
    }
}
