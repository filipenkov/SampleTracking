/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.vcs.cvsimpl.CvsRepositoryUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;

@WebSudoRequired
public class DeleteRepository extends RepositoryActionSupport
{
    private boolean confirmed;

    public DeleteRepository(RepositoryManager repositoryManager, CvsRepositoryUtil cvsRepositoryUtil)
    {
        super(repositoryManager, cvsRepositoryUtil);
    }


    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (getId() == null || !isConfirmed())
        {
            addErrorMessage(getText("admin.errors.repositories.confirm.deletion"));
        }

        // Check that there are no projects associated with the repository
        if (!isDeletable())
        {
            addErrorMessage(getText("admin.errors.repositories.cannot.delete.repository"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            getRepositoryManager().removeRepository(getId());
        }
        catch (Exception e)
        {
            log.error("Error occurred while removing the repository with id '" + getId() + "'.", e);
            addErrorMessage(getText("admin.errors.repositories.error.occured.removing"));
            return getResult();
        }

        return getRedirect("ViewRepositories.jspa");
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public boolean isDeletable()
    {
        try
        {
            return getRepositoryManager().getProjectsForRepository(getRepositoryManager().getRepository(getId())).isEmpty();
        }
        catch (GenericEntityException e)
        {
            log.error("Error occurred while retrieving the repository with id '" + getId() + "'.", e);
            addErrorMessage(getText("admin.errors.repositories.error.occured.removing.repository","'" + getId() + "'"));
            return false;
        }
    }
}
