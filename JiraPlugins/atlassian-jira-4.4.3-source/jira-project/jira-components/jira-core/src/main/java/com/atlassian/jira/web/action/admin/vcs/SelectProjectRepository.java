/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.vcs.Repository;
import com.atlassian.jira.vcs.RepositoryManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@WebSudoRequired
public class SelectProjectRepository extends JiraWebActionSupport
{
    private Long pid;
    private String[] repoIds;
    private List ids;
    private List repositories;
    private final RepositoryManager repositoryManager;

    public SelectProjectRepository(RepositoryManager repositoryManager)
    {
        this.repositoryManager = repositoryManager;
    }

    public String doDefault() throws Exception
    {
        // Initialize the current repositories
        final Collection repositories = getRepositoryManager().getRepositoriesForProject(getProject());
        if (repositories != null)
        {
            repoIds = new String[repositories.size()];

            int i = 0;
            for (Iterator iterator = repositories.iterator(); iterator.hasNext(); i++)
            {
                Repository repository = (Repository) iterator.next();
                repoIds[i] = repository.getId().toString();
            }
        }

        return super.doDefault();
    }

    protected void doValidation()
    {
        // Must have a valid project
        if (null == getProject())
        {
            addErrorMessage(getText("admin.errors.repositories.must.specify.project"));
        }

        if (repoIds == null || repoIds.length == 0)
        {
            addError("repositoryIds", getText("admin.errors.repositories.please.select.repository"));
        }
        else
        {
            convertRepositoryIds();

            // Check that if 'None' was selected no other item was selected at the same time
            for (int i = 0; i < ids.size(); i++)
            {
                Long id = (Long) ids.get(i);

                // 'None' has id < 0
                if (id.longValue() < 0L)
                {
                    if (ids.size() > 1)
                    {
                        addError(getRepositoryIdsControlName(), getText("admin.errors.repositories.cannot.select.none"));
                        break;
                    }
                    else
                    {
                        ids = Collections.EMPTY_LIST;
                    }
                }
            }
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getRepositoryManager().setProjectRepositories(getProject(), ids);
        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/summary");
    }

    public GenericValue getProject()
    {
        return ManagerFactory.getProjectManager().getProject(getProjectId());
    }

    public Long getProjectId()
    {
        return pid;
    }

    public void setProjectId(Long pid)
    {
        this.pid = pid;
    }

    public void setRepositoryIds(String prids)
    {
        setRepoIds(new String[]{prids});
    }

    public String getRepositoryIds()
    {
        if (repoIds != null && repoIds.length > 0)
            return repoIds[0];
        else
            return null;
    }

    protected void convertRepositoryIds()
    {
        if (ids == null)
        {
            ids = new ArrayList();

            // Convert the ids to Longs
            for (int i = 0; i < repoIds.length; i++)
            {
                String prid = repoIds[i];
                ids.add(new Long(prid));
            }
        }
    }

    public Collection getRepositories()
    {
        if (repositories == null)
        {
            repositories = new ArrayList(getRepositoryManager().getRepositories());
            Collections.sort(repositories);
        }

        return repositories;
    }

    protected String[] getRepoIds()
    {
        return repoIds;
    }

    protected void setRepoIds(String[] repoIds)
    {
        this.repoIds = repoIds;
    }

    protected String getRepositoryIdsControlName()
    {
        return "repositoryIds";
    }

    protected RepositoryManager getRepositoryManager()
    {
        return repositoryManager;
    }
}
