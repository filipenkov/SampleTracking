package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.plugin.util.TabPanelUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.DateFieldFormat;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.SessionKeys;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Displays the versions of a project.
 */
public class VersionsProjectTabPanel extends AbstractProjectTabPanel
{
    private static final Logger log = Logger.getLogger(VersionsProjectTabPanel.class);
    private static final int SUBSET_DEFAULT_VALUE = 20;

    private final VersionManager versionManager;
    private final ApplicationProperties applicationProperties;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final DateFieldFormat dateFieldFormat;
    private final PermissionHelper permissionHelper;

    public VersionsProjectTabPanel(final JiraAuthenticationContext authenticationContext,
            final VersionManager versionManager, final ApplicationProperties applicationProperties,
            final PermissionManager permissionManager, final FieldVisibilityManager fieldVisibilityManager,
            DateFieldFormat dateFieldFormat)
    {
        super(authenticationContext);
        this.versionManager = versionManager;
        this.applicationProperties = applicationProperties;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.dateFieldFormat = dateFieldFormat;
        this.permissionHelper = new PermissionHelper(permissionManager);
    }

    public String getHtml(BrowseContext ctx)
    {
        setSubset();
        return super.getHtml(ctx);
    }

    public boolean showPanel(BrowseContext ctx)
    {
        final Long projectId = ctx.getProject().getId();
        return isFixForVersionsFieldVisible(projectId) && !versionManager.getVersions(projectId).isEmpty();
    }

    public DateFieldFormat getDateFieldFormat()
    {
        return dateFieldFormat;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Project project = ctx.getProject();
        List<Version> versions = getVersions(project);
        final Map<String, Object> params = super.createVelocityParams(ctx);
        params.put("versionsCount", versions.size());
        final Integer subset = getSubset();
        final Collection versionToDisplay = TabPanelUtil.subSetCollection(versions, subset);
        params.put("versions", versionToDisplay);
        params.put("hasAdminPermission", permissionHelper.hasProjectAdminPermission(authenticationContext.getUser(), project));
        params.put("showingAll", versions.size() < SUBSET_DEFAULT_VALUE);        
        params.put("subset", subset);
        params.put("dateFieldFormat", dateFieldFormat);
        
        return params;
    }

    private List<Version> getVersions(final Project project)
    {
        List<Version> versions = Collections.emptyList();
        if (isFixForVersionsFieldVisible(project.getId()))
        {
            try
            {
                versions = new ArrayList<Version>(versionManager.getVersionsUnarchived(project.getId()));
                //sort the versions in the same order as in the project admin view.
                Collections.reverse(versions);
            }
            catch (DataAccessException e)
            {
                log.error("Could not retrieve versions for project: " + project, e);
            }
        }
        return versions;
    }

    /**
     * Returns true if the fixfor versions field is visible in at least one scheme, false otherwise.
     *
     * @param projectId project ID
     * @return true if the fixfor versions field is visible in at least one scheme, false otherwise.
     */
    protected boolean isFixForVersionsFieldVisible(Long projectId)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(projectId, IssueFieldConstants.FIX_FOR_VERSIONS, null);
    }

    private void setSubset()
    {
        //if the request param is set, update the session value.
        final String subsetReq = getSubsetRequestParam();
        if (StringUtils.isNotEmpty(subsetReq))
        {
            ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_REPORT_SUBSET, new Integer(subsetReq));
        }
    }

    private Integer getSubset()
    {
        Integer subset = (Integer) ActionContext.getSession().get(SessionKeys.VERSION_BROWSER_REPORT_SUBSET);
        if (subset == null)
        {

            String requestParameter = getSubsetRequestParam();
            if (StringUtils.isEmpty(requestParameter))
            {
                subset = SUBSET_DEFAULT_VALUE;
            }
            else
            {
                subset = new Integer(requestParameter);
            }
            ActionContext.getSession().put(SessionKeys.VERSION_BROWSER_REPORT_SUBSET, subset);
        }
        return subset;
    }

    private String getSubsetRequestParam()
    {
        VelocityRequestContextFactory vf = new DefaultVelocityRequestContextFactory(applicationProperties);
        VelocityRequestContext context = vf.getJiraVelocityRequestContext();
        return context.getRequestParameter("subset");
    }
}
