package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.util.OutlookDate;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Displays the top 3 versions which are unreleased and not archived for a project.
 *
 * @since v4.0
 */
public class DueVersionsFragment extends AbstractFragment implements ComponentTabPanelFragment
{
    private static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/summary/";
    private static final Logger log = Logger.getLogger(DueVersionsFragment.class);
    private static final Integer DEFAULT_DISPLAY_VERSION_COUNT = 3;

    private final ApplicationProperties applicationProperties;
    private final VersionManager versionManager;

    public DueVersionsFragment(final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext jiraAuthenticationContext, final VersionManager versionManager)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.applicationProperties = applicationProperties;
        this.versionManager = versionManager;
    }

    public String getId()
    {
        return "dueversions";
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("versions", getVersions(ctx));
        velocityParams.put("SFM_HIDE", OutlookDate.SmartFormatterModes.HIDE_TIME);
        return velocityParams;
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        return getDisplayIssueCount() > 0 && isFixForVersionsFieldVisible(ctx) && !getVersions(ctx).isEmpty();
    }

    Collection<Version> getVersions(BrowseContext ctx)
    {
        // TODO: filter the versions down to the specific component, if one was specified
        final Integer count = getDisplayIssueCount();
        List<Version> versions = new ArrayList<Version>(versionManager.getVersionsUnreleased(ctx.getProject().getId(), false));
        return versions.size() > count ? versions.subList(0, count) : versions;
    }

    /**
     * Returns true if the fixfor versions field is visible in at least one scheme, false otherwise.
     *
     * @param ctx the context containing the current project
     * @return true if the fixfor versions field is visible in at least one scheme, false otherwise.
     */
    boolean isFixForVersionsFieldVisible(BrowseContext ctx)
    {
        FieldVisibilityManager visibility = new FieldVisibilityBean();
        return !visibility.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.FIX_FOR_VERSIONS);
    }

    /**
     * Attempts to resolve the number of issues to display from the application property {@link
     * com.atlassian.jira.config.properties.APKeys#JIRA_PROJECT_SUMMARY_MAX_ISSUES}. Failing that, returns a hard-coded
     * default {@link #DEFAULT_DISPLAY_VERSION_COUNT}.
     *
     * @return the number of issues to display in this fragment.
     */
    Integer getDisplayIssueCount()
    {
        String displayIssueCount = applicationProperties.getDefaultBackedString(APKeys.JIRA_PROJECT_SUMMARY_MAX_ISSUES);
        try
        {
            if (displayIssueCount == null)
            {
                return DEFAULT_DISPLAY_VERSION_COUNT;
            }
            else
            {
                final Integer intDisplayIssueCount = Integer.valueOf(displayIssueCount);
                if (intDisplayIssueCount < 0)
                {
                    return DEFAULT_DISPLAY_VERSION_COUNT;
                }
                return intDisplayIssueCount;
            }
        }
        catch (final NumberFormatException e)
        {
            log.warn("Invalid value for application property '" + APKeys.JIRA_PROJECT_SUMMARY_MAX_ISSUES + "': " + displayIssueCount);
            return DEFAULT_DISPLAY_VERSION_COUNT;
        }
    }

}
