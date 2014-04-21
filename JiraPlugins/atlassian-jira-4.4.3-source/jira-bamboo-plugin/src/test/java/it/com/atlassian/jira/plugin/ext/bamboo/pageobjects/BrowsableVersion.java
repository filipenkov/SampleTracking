package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import org.apache.log4j.Logger;

import javax.inject.Inject;

public class BrowsableVersion
{
    private static final Logger log = Logger.getLogger(BrowsableVersion.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties

    private String id;
    private String name;
    private String projectKey;

    // ---------------------------------------------------------------------------------------------------- Dependencies

    @Inject
    private PageBinder pageBinder;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public BrowsableVersion(final String id, final String name, final String projectKey)
    {
        this.id = id;
        this.name = name;
        this.projectKey = projectKey;
    }

    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public BrowseVersionSummaryPage getBrowseVersionSummaryPage()
    {
        return pageBinder.navigateToAndBind(BrowseVersionSummaryPage.class, this);
    }
}
