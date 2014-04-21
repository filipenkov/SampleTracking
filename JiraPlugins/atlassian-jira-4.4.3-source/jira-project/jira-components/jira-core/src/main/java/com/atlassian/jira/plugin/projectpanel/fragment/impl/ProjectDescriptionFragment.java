package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

/**
 * The fragment which displays the description of a project.
 *
 * @since v4.0
 */
public class ProjectDescriptionFragment extends AbstractFragment
{
    private final static String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";

    private final UserFormatManager userFormatManager;

    public ProjectDescriptionFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites, final JiraAuthenticationContext jiraAuthenticationContext, final UserFormatManager userFormatManager)
    {
        super(velocityManager, applicationProperites, jiraAuthenticationContext);
        this.userFormatManager = userFormatManager;
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
        velocityParams.put("leadExists", UserUtils.existsUser(ctx.getProject().getLeadUserName()));
        return velocityParams;
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        return true;
    }
}
