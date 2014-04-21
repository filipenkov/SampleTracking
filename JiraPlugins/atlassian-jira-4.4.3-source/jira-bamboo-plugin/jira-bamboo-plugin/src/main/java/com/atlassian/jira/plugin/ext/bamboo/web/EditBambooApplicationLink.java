package com.atlassian.jira.plugin.ext.bamboo.web;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.plugin.ext.bamboo.applinks.BambooApplicationLinkManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

@WebSudoRequired
public class EditBambooApplicationLink extends BambooWebActionSupport
{
    private static final Logger log = Logger.getLogger(EditBambooApplicationLink.class);

    // ------------------------------------------------------------------------------------------------- Type Properties

    private ApplicationLink applicationLink;
    private Iterable<String> associatedKeys;
    // ---------------------------------------------------------------------------------------------------- Dependencies

    private final ProjectManager projectManager;

    // ---------------------------------------------------------------------------------------------------- Constructors

    public EditBambooApplicationLink(BambooApplicationLinkManager applinkManager, WebResourceManager webResourceManager, ProjectManager projectManager)
    {
        super(applinkManager, webResourceManager);
        this.projectManager = projectManager;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods

    @Override
    public String doDefault() throws Exception
    {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }

        associatedKeys = getApplinkManager().getProjects(applicationLink.getId().get());

        return INPUT;
    }

    @Override
    public void doValidation()
    {
        super.doValidation();

        for (String projectKey : associatedKeys)
        {
            Project project = projectManager.getProjectObjByKey(projectKey.toUpperCase());
            if (project == null)
            {
                addError("associatedKeys", getText("bamboo.config.project.doesNotExist", projectKey));
            }
            else
            {
                if (getApplinkManager().hasAssociatedApplicationLink(projectKey) &&
                        !getApplinkManager().isAssociated(projectKey, applicationLink.getId()))
                {
                    addError("associatedKeys", getText("bamboo.config.projectKey.duplicate", projectKey));
                }
            }
        }
    }

    @Override
    @com.atlassian.jira.security.xsrf.RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        if (!hasPermissions())
        {
            return PERMISSION_VIOLATION_RESULT;
        }
        if (hasAnyErrors())
        {
            return INPUT;
        }

        getApplinkManager().unassociateAll(applicationLink.getId());
        for (String projectKey : associatedKeys)
        {
            getApplinkManager().associate(projectKey.toUpperCase(), applicationLink.getId());
        }

        return getRedirect(ViewBambooApplicationLinks.JSPA_PATH);
    }

    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators

    public String getApplicationId()
    {
        return applicationLink.getId().get();
    }

    public void setApplicationId(String applicationId)
    {
        if (applicationId != null)
        {
            applicationLink = getApplinkManager().getBambooApplicationLink(applicationId);
        }
    }

    public String getAssociatedKeys()
    {
        return Joiner.on(" ").join(getSorter().sort(ImmutableList.copyOf(associatedKeys)));
    }

    public void setAssociatedKeys(String associatedKeys)
    {
        //don't bother storing null or empty project keys. this can happen from consecutive spaces for example.
        this.associatedKeys = Iterables.filter(ImmutableList.of(associatedKeys.split(" ")), new Predicate<String>()
        {
            public boolean apply(String key)
            {
                return key != null && !key.isEmpty();
            }
        });
    }

    public String getName()
    {
        if (applicationLink == null)
        {
            return "";
        }
        else
        {
            return applicationLink.getName();
        }
    }

    public String getHost()
    {
        if (applicationLink == null)
        {
            return "";
        }
        else
        {
            return applicationLink.getDisplayUrl().toASCIIString();
        }
    }
}