package com.atlassian.jira.scheme;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public abstract class AbstractDeleteScheme extends AbstractSchemeAwareAction
{
    private boolean confirmed = false;
    private String name;
    private String description = null;

    public String doDefault() throws Exception
    {
        // Check to see that the scheme still exists
        if (getScheme() == null)
        {
            addErrorMessage(getText("admin.errors.deletescheme.nonexistent.scheme"));
            return INPUT;
        }

        name = getScheme().getString("name");
        if (TextUtils.stringSet(getScheme().getString("description")))
            description = getScheme().getString("description");
        return super.doDefault();
    }

    protected void doValidation()
    {
        if (getSchemeId() == null)
        {
            addError("schemeId", getText("admin.errors.deletescheme.no.scheme.specified"));
        }
        if (getSchemeManager().getDefaultSchemeObject() != null && getSchemeManager().getDefaultSchemeObject().getId().equals(getSchemeId()))
        {
            addErrorMessage(getText("admin.errors.deletescheme.cannot.delete.default"));
        }
        if (!confirmed)
        {
            addErrorMessage(getText("admin.errors.deletescheme.confirmation"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        // Handle concurrency gracefully.
        // if someone else has concurrently deleted this and you are tryint to delete, then just don't do anything.
        if (getScheme() != null)
        {
            //If there are projects already attached then reattach to the default scheme
            List<GenericValue> projects = getProjects(getScheme());

            for (GenericValue project : projects)
            {
                getSchemeManager().removeSchemesFromProject(project);
                getSchemeManager().addDefaultSchemeToProject(project);
            }

            getSchemeManager().deleteScheme(getSchemeId());
        }

        return returnCompleteWithInlineRedirect(getRedirectURL());
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<GenericValue> getProjects(GenericValue scheme) throws GenericEntityException
    {
        return ManagerFactory.getPermissionSchemeManager().getProjects(scheme);
    }
}
