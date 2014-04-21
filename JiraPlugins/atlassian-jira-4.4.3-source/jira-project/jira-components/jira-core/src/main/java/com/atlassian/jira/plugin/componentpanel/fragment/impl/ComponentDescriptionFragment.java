package com.atlassian.jira.plugin.componentpanel.fragment.impl;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.AbstractFragment;
import com.atlassian.jira.plugin.userformat.ProfileLinkUserFormat;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * The fragment which displays the description of a project.
 *
 * @since v4.0
 */
public class ComponentDescriptionFragment extends AbstractFragment implements ComponentTabPanelFragment
{
    private final static String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/componentpanels/fragments/summary/";

    private final UserFormatManager userFormatManager;

    public ComponentDescriptionFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites, final JiraAuthenticationContext jiraAuthenticationContext, final UserFormatManager userFormatManager)
    {
        super(velocityManager, applicationProperites, jiraAuthenticationContext);
        this.userFormatManager = userFormatManager;
    }

    public String getId()
    {
        return "componentdescription";
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        try
        {
            ProjectComponent component = ((BrowseComponentContext) ctx).getComponent();
            return !(StringUtils.isBlank(component.getDescription()) && (component.getLead() == null));
        }
        catch (ClassCastException e)
        {
            log.error("The supplied context must be of type BrowseComponentContext", e);
            throw new IllegalArgumentException("The supplied context must be of type BrowseComponentContext", e);
        }
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("userFormat", userFormatManager.getUserFormat(ProfileLinkUserFormat.TYPE));
        return velocityParams;
    }

}
