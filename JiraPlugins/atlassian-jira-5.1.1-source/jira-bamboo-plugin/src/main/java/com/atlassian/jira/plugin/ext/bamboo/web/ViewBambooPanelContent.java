package com.atlassian.jira.plugin.ext.bamboo.web;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.ext.bamboo.PluginConstants;
import com.atlassian.jira.plugin.ext.bamboo.panel.BambooBuildResultsTabPanel;
import com.atlassian.jira.plugin.ext.bamboo.panel.BambooPanelHelper;
import com.atlassian.jira.plugin.ext.bamboo.service.BambooServerAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.sal.api.ApplicationProperties;
import com.opensymphony.module.sitemesh.RequestConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * A single action to pull out HTML from a Bamboo builds response.
 */
public class ViewBambooPanelContent extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger(ViewBambooPanelContent.class);

    // ------------------------------------------------------------------------------------------------- Type Properties
    private String projectKey;
    private Long versionId;
    private String issueKey;
    private boolean showRss;
    private String selectedSubTab;

    private String bambooHtml;
    private String redirectUrl;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final BambooServerAccessor bambooServerAccessor;
    private final VersionManager versionManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchProvider searchProvider;
    private final ApplicationProperties applicationProperties;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ViewBambooPanelContent(JiraAuthenticationContext authenticationContext,
                                  BambooServerAccessor bambooServerAccessor,
                                  SearchProvider searchProvider,
                                  VersionManager versionManager,
                                  PermissionManager permissionManager,
                                  IssueManager issueManager,
                                  ProjectManager projectManager,
                                  ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.bambooServerAccessor = bambooServerAccessor;
        this.searchProvider = searchProvider;
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    @Override
    protected String doExecute() throws Exception
    {
        ActionContext.getRequest().setAttribute(RequestConstants.DECORATOR, "none");

        try
        {
            final String selectedSubTab = getSelectedSubTab();
            final String actionUrl;
            if (BambooPanelHelper.SUB_TAB_PLAN_STATUS.equals(selectedSubTab))
            {
                actionUrl = BambooBuildResultsTabPanel.VIEW_PLANS_ACTION_URL;
            }
            else
            {
                actionUrl = BambooBuildResultsTabPanel.VIEW_BUILD_RESULTS_ACTION_URL;
            }

            final Map extraParams = EasyMap.build(BambooPanelHelper.SELECTED_SUB_TAB_KEY, selectedSubTab,
                                                  "showRss", String.valueOf(showRss));

            if (issueKey != null)
            {
                final MutableIssue issueObject = issueManager.getIssueObject(issueKey);
                if (issueObject != null && permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, issueObject, authenticationContext.getUser()))
                {
                    setBambooHtml(bambooServerAccessor.getHtmlFromAction(BambooBuildResultsTabPanel.VIEW_BUILD_RESULTS_ACTION_URL,
                            issueObject.getProjectObject(), EasyList.build(issueKey), extraParams));
                }
            }
            else if (versionId != null)
            {
                final Version version = versionManager.getVersion(versionId);
                if (version != null && permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, version.getProjectObject(), authenticationContext.getUser()))
                {
                    final Collection<String> issueKeys = getIssueKeys(version);
                    if (issueKeys != null && !issueKeys.isEmpty())
                    {
                        final Date releaseDate = version.getReleaseDate();
                        if (version.isReleased() && releaseDate != null)
                        {
                            log.debug("Adding version release date " + releaseDate);

                            // Add the build date
                            Calendar c = Calendar.getInstance();
                            c.setTime(releaseDate);
                            c.add(Calendar.DAY_OF_MONTH, 1);
                            Date oneReleaseDate = c.getTime();

                            extraParams.put("releasedVersionTimestamp", String.valueOf(oneReleaseDate.getTime()));
                        }

                        setBambooHtml(bambooServerAccessor.getHtmlFromAction(actionUrl, version.getProjectObject(), issueKeys, extraParams));
                    }
                    else
                    {
                        setBambooHtml(getText("bamboo.panel.buildByDate.noJiraIssues"));
                    }
                }
            }
            else if (projectKey != null)
            {
                Project project = projectManager.getProjectObjByKey(projectKey);
                if (project != null && permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, project, authenticationContext.getUser()))
                {
                    bambooHtml = bambooServerAccessor.getHtmlFromAction(actionUrl, project, extraParams);
                }
            }
        }
        catch (CredentialsRequiredException e)
        {
            log.info("Credentials are required but cannot be found. Redirecting to authorization page.");

            String baseLinkUrl = ActionContext.getRequest().getParameter("baseLinkUrl");
            String jiraBaseUrl = applicationProperties.getBaseUrl();
            String redirect = e.getAuthorisationURI(URI.create(jiraBaseUrl + baseLinkUrl + "?selectedTab=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + PluginConstants.BAMBOO_RELEASE_TABPANEL_MODULE_KEY)).toASCIIString();
            setRedirectUrl(redirect);

            return PERMISSION_VIOLATION_RESULT;
        }
        catch (Exception e)
        {
            //let's never leave the Builds tab spinning infinitely. Tell the user that an error occurred whenever it happens.
            log.warn("Unable to to connect to Bamboo server. Nothing will be shown.", e);
            addErrorMessage(getText("bamboo.panel.connection.error"));
            return ERROR;
        }
        
        return super.doExecute();
    }

    private Collection<String> getIssueKeys(Version version)
    {
        try
        {
            final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder().where().
                    project(version.getProjectObject().getId()).and().fixVersion(version.getId()).endWhere();

            final SearchResults results = searchProvider.search(builder.buildQuery(), authenticationContext.getUser(), PagerFilter.getUnlimitedFilter());
            final Collection<Issue> issues = results.getIssues();
            final Collection<String> issueKeys = CollectionUtils.collect(issues, new Transformer()
            {
                public Object transform(Object o)
                {
                    Issue issue = (Issue) o;
                    return issue.getKey();
                }
            });

            return issueKeys;
        }
        catch (SearchException e)
        {
            log.warn("Unable to get all issues from version " + version + ". Returning null.", e);
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Private helpers



    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
    public String getSelectedSubTab()
    {
        return selectedSubTab;
    }

    public void setSelectedSubTab(String selectedSubTab)
    {
        this.selectedSubTab = selectedSubTab;
    }

    public String getBambooHtml()
    {
        return bambooHtml;
    }

    private void setBambooHtml(String bambooHtml)
    {
        this.bambooHtml = bambooHtml == null ? "" : bambooHtml;
    }

    public String getIssueKey()
    {
        return issueKey;
    }

    public void setIssueKey(String issueKey)
    {
        this.issueKey = issueKey;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public boolean isShowRss()
    {
        return showRss;
    }

    public void setShowRss(boolean showRss)
    {
        this.showRss = showRss;
    }

    public String getRedirectUrl()
    {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }
}
