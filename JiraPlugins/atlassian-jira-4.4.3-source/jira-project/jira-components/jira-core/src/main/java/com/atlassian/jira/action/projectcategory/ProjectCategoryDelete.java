package com.atlassian.jira.action.projectcategory;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.JiraNonWebActionSupport;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class ProjectCategoryDelete extends JiraNonWebActionSupport
{
    private Long id = null;

    @Override
    protected void doValidation()
    {
        // Confirm that the project category id supplied is actually a real project category
        try
        {
            if ((null == getId()) || (null == ManagerFactory.getProjectManager().getProjectCategory(getId())))
            {
                addErrorMessage(getText("admin.errors.must.specify.category.to.delete"));
            }
            else
            {
                // Confirm that the Project Category doesn't have linked Projects
                final Collection projectsFromProjectCategory = ManagerFactory.getProjectManager().getProjectsFromProjectCategory(getProjectCategory());
                if ((null != projectsFromProjectCategory) && !projectsFromProjectCategory.isEmpty())
                {
                    addErrorMessage(getText("admin.errors.currently.projects.linked.to.category"));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.must.specify.category.to.delete"));
        }
    }

    private GenericValue getProjectCategory() throws GenericEntityException
    {
        return ManagerFactory.getProjectManager().getProjectCategory(getId());
    }

    @Override
    protected String doExecute() throws Exception
    {
        final GenericValue projectCategory = ManagerFactory.getProjectManager().getProjectCategory(getId());
        projectCategory.remove();
        ManagerFactory.getProjectManager().refresh();

        return getResult();
    }

    public Long getId()
    {
        return id;
    }

    public void setId(final Long id)
    {
        this.id = id;
    }
}
