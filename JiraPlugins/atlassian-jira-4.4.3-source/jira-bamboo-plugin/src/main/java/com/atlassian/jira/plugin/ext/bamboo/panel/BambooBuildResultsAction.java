package com.atlassian.jira.plugin.ext.bamboo.panel;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.project.Project;

import java.util.Date;
import java.util.Map;

/**
 * One item in the 'Subversion Commits' tab.
 */ 
public class BambooBuildResultsAction extends AbstractIssueAction implements IssueAction
{
    private static final String BAMBOO_PLUGIN_MODULE_KEY = "bamboo-build-results-tabpanel";
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final Project project;
    private final String issueKey;
    private final BambooPanelHelper bambooPanelHelper;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public BambooBuildResultsAction(BambooPanelHelper bambooPanelHelper,
                                    Issue issue,
                                    IssueTabPanelModuleDescriptor descriptor)
    {
        super(descriptor);
        this.bambooPanelHelper = bambooPanelHelper;
        this.project = issue.getProjectObject();
        this.issueKey = issue.getKey();
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods   
    @Override
    public Date getTimePerformed()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void populateVelocityParams(Map velocityParams)
    {

        String baseLinkUrl = "/browse/" + issueKey +
                             "?selected=" + BambooPanelHelper.BAMBOO_PLUGIN_KEY + ":" + BAMBOO_PLUGIN_MODULE_KEY;
        String queryString = "issueKey=" + issueKey;

        velocityParams.put("issueKey", issueKey);

        bambooPanelHelper.prepareVelocityContext(velocityParams, BAMBOO_PLUGIN_MODULE_KEY,
                                                 baseLinkUrl, queryString, null, project);
    }

    @Override
    public boolean isDisplayActionAllTab()
    {
        return false;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
