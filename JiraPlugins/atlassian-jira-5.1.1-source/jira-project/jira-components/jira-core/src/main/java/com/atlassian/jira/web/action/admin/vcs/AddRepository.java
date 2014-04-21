/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryBrowser;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepository;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@WebSudoRequired
public class AddRepository extends RepositoryActionSupport
{
    private Map projects;

    public AddRepository(RepositoryManager repositoryManager, CvsRepositoryUtil cvsRepositoryUtil)
    {
        super(repositoryManager, cvsRepositoryUtil);
        projects = new HashMap();
    }

    public String doDefault() throws Exception
    {
        setFetchLog(true);
        // Do not use the super's implementation.
        if (isSystemAdministrator())
        {
            return INPUT;
        }
        // Don't show the input page if the user does not have permission
        else
        {
            return ERROR;
        }
    }

    protected void doValidation()
    {
        // Only sys admins can add cvs modules
        if (!isSystemAdministrator())
        {
            addErrorMessage(getText("admin.errors.no.perm.when.creating"));
            return;
        }
        if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.you.must.specify.a.name.for.the.repository"));
        }
        else
        {
            if (getRepositoryManager().getRepository(getName()) != null)
            {
                addError("name", getText("admin.errors.another.repository.with.this.name.already.exists"));
            }
        }

        validateRepositoryParameters();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        Properties cvsProps = new Properties();

        // Add all properties required by the CVS repository
        cvsProps.setProperty(CvsRepository.KEY_LOG_FILE_PATH, getLogFilePath());
        cvsProps.setProperty(CvsRepository.KEY_CVS_ROOT, getCvsRoot());
        cvsProps.setProperty(CvsRepository.KEY_MODULE_NAME, getModuleName());
        if (getPassword() != null)
        {
            cvsProps.setProperty(CvsRepository.KEY_PASSWORD, getPassword());
        }
        cvsProps.setProperty(CvsRepository.KEY_FETCH_LOG, String.valueOf(isFetchLog()));

        cvsProps.setProperty(CvsRepository.KEY_CVS_TIMEOUT, String.valueOf(getTimeoutMillis()));
        try
        {
            if (TextUtils.stringSet(getRepositoryBrowserURL()))
            {
                cvsProps.setProperty(Repository.KEY_REPOSITTORY_BROWSER_TYPE, RepositoryBrowser.VIEW_CVS_TYPE);
                cvsProps.setProperty(ViewCvsBrowser.KEY_BASE_URL, getRepositoryBrowserURL());
                cvsProps.setProperty(ViewCvsBrowser.ROOT_PARAMETER, getRepositoryBrowserRootParam());
            }

            // Create the repository
            getRepositoryManager().createRepository(RepositoryManager.CVS_TYPE, getName(), getDescription(), cvsProps);
        }
        catch (Exception e)
        {
            log.error("Error occurred while creating the repository.", e);
            addErrorMessage(getText("admin.errors.occured.when.creating"));
            return getResult();
        }

        // Redirect to the view screen
        return getRedirect("ViewRepositories.jspa");
    }

    public Collection getProjects(Repository repository)
    {
        if (!projects.containsKey(repository.getId()))
        {
            try
            {
                projects.put(repository.getId(), getRepositoryManager().getProjectsForRepository(repository));
            }
            catch (GenericEntityException e)
            {
                log.error("Error while retrieving projects for repository '" + repository + "'.", e);
                addErrorMessage(getText("admin.errors.occured.when.retrieving", repository));
                return Collections.EMPTY_LIST;
            }
        }

        return (Collection) projects.get(repository.getId());
    }

    public String getViewCVSBaseUrl(Repository repository)
    {
        final RepositoryBrowser repositoryBrowser = repository.getRepositoryBrowser();
        if (repositoryBrowser != null && RepositoryBrowser.VIEW_CVS_TYPE.equals(repositoryBrowser.getType()))
        {
            final ViewCvsBrowser viewCvsBrowser = (ViewCvsBrowser) repositoryBrowser;
            return viewCvsBrowser.getBaseURL();
        }
        else
        {
            return "";
        }
    }

    public String getViewCVSRootParameter(Repository repository)
    {
        final RepositoryBrowser repositoryBrowser = repository.getRepositoryBrowser();
        if (repositoryBrowser != null && RepositoryBrowser.VIEW_CVS_TYPE.equals(repositoryBrowser.getType()))
        {
            final ViewCvsBrowser viewCvsBrowser = (ViewCvsBrowser) repositoryBrowser;
            return viewCvsBrowser.getRootParameter();
        }
        else
        {
            return "";
        }
    }

    public Collection getRepositories() throws GenericEntityException
    {
        final List repositories = new ArrayList(getRepositoryManager().getRepositories());
        Collections.sort(repositories);
        return repositories;
    }

    public boolean isDeletable(Repository repository)
    {
        try
        {
            return getRepositoryManager().getProjectsForRepository(repository).isEmpty();
        }
        catch (Exception e)
        {
            log.error("Error occurred while retrieving projects for repository '" + repository + "'.", e);
            addErrorMessage(getText("admin.errors.occured.when.retrieving", repository));
            return false;
        }
    }
}
