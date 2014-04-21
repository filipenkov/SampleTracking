package com.atlassian.jira.plugin.versionpanel.fragment.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.projectpanel.fragment.impl.AbstractFragment;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * The fragment which displays the description of a version.
 *
 * @since v4.0
 */
public class VersionDescriptionFragment extends AbstractFragment implements VersionTabPanelFragment
{
    private final static String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/versionpanels/fragments/summary/";

    public VersionDescriptionFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(velocityManager, applicationProperites, jiraAuthenticationContext);
    }

    public String getId()
    {
        return "versiondescription";
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        try
        {
            final Version version = ((BrowseVersionContext) ctx).getVersion();
            return !(StringUtils.isBlank(version.getDescription()) && (version.getReleaseDate() == null));
        }
        catch (ClassCastException e)
        {
            log.error("The supplied context must be of type BrowseVersionContext", e);
            throw new IllegalStateException("The supplied context must be of type BrowseVersionContext", e);
        }
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("SFM_HIDE", OutlookDate.SmartFormatterModes.HIDE_TIME);
        return velocityParams;
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }
}