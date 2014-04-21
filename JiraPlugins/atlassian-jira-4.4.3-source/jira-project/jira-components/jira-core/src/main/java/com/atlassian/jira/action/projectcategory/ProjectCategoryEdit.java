package com.atlassian.jira.action.projectcategory;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.action.JiraNonWebActionSupport;
import com.atlassian.jira.entity.ProjectCategoryFactory;
import com.atlassian.jira.project.ProjectManager;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

public class ProjectCategoryEdit extends JiraNonWebActionSupport
{
    private String name;
    private String description;
    private Long id;

    /**
     * Confirm that we actually have <ul><li>a sane project name,</li> <li>an actual project category id.</li></ul>
     */
    @Override
    protected void doValidation()
    {
        // Confirm that the project name has content.
        if (StringUtils.isBlank(getName()))
        {
            addError("name", getText("admin.common.errors.validname"));
        }

        // Make sure description is non-null.
        if (null == getDescription())
        {
            setDescription("");
        }

        // Confirm that the project category id actually maps to a project category.
        if (null == getId())
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else if (null == ManagerFactory.getProjectManager().getProjectCategory(getId()))
        {
            addErrorMessage(getText("admin.errors.project.category.does.not.exist"));
        }
        else
        {
            // Validate that the name is not the name of another project category
            final Collection<GenericValue> projectCategories = ManagerFactory.getProjectManager().getProjectCategories();
            for (final Object element : projectCategories)
            {
                final GenericValue projectCategory = (GenericValue) element;

                //cannot have two categories with a different id and the same name
                if (!getId().equals(projectCategory.getLong("id")) && StringUtils.trimToEmpty(getName()).equals(projectCategory.getString("name")))
                {
                    addError("name", getText("admin.errors.project.category.already.exists", "'" + getName() + "'"));
                    break;
                }
            }
        }
    }

    @Override
    protected String doExecute() throws Exception
    {
        final ProjectManager projectManager = ManagerFactory.getProjectManager();

        ProjectCategoryFactory.Builder builder = new ProjectCategoryFactory.Builder();
        builder.id(getId());
        builder.name(getName());
        builder.description(description);

        projectManager.updateProjectCategory(builder.build());

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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
