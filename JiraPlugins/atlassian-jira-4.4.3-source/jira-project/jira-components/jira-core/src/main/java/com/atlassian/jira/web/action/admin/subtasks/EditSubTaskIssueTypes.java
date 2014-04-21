package com.atlassian.jira.web.action.admin.subtasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class EditSubTaskIssueTypes extends JiraWebActionSupport
{
    private final SubTaskManager subTaskManager;
    private final ConstantsManager constantsManager;

    private String id;
    private String name;
    private Long sequence;
    private String description;
    private String iconurl;

    public EditSubTaskIssueTypes(SubTaskManager subTaskManager, ConstantsManager constantsManager)
    {
        this.subTaskManager = subTaskManager;
        this.constantsManager = constantsManager;
    }

    public String doDefault() throws Exception
    {
        if (!isSubtasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.disabled"));
            return getResult();
        }

        if (!TextUtils.stringSet(getId()))
        {
            addErrorMessage(getText("admin.errors.no.id.set"));
            return getResult();
        }
        else
        {
            final GenericValue subTaskIssueTypeGV = subTaskManager.getSubTaskIssueTypeById(getId());
            setName(subTaskIssueTypeGV.getString("name"));
            setSequence(subTaskIssueTypeGV.getLong("sequence"));
            setDescription(subTaskIssueTypeGV.getString("description"));
            setIconurl(subTaskIssueTypeGV.getString("iconurl"));
        }

        return INPUT;
    }

    protected void doValidation()
    {
        // Ensure sub tasks are turned on
        if (!isSubtasksEnabled())
        {
            addErrorMessage(getText("admin.errors.subtasks.are.disabled"));
            return;
        }

        if (!TextUtils.stringSet(getId()))
        {
            addErrorMessage(getText("admin.errors.no.id.set"));
        }
        // Ensure that the name is set
        else if (!TextUtils.stringSet(getName()))
        {
            addError("name", getText("admin.errors.specify.a.name.for.this.new.sub.task.issue.type"));
        }
        else
        {
            // Ensure that an issue type with that name does not already exist
            // WARNING: ensure that we allow the same name to be kept!
            final IssueConstant subTaskIssueType = constantsManager.getIssueConstantByName("IssueType", getName());
            if (subTaskIssueType != null)
            {
                // Check if this is NOT the issue type that is being edited
                if (!getId().equals(subTaskIssueType.getId()))
                {
                    // A duplicate name has been entered
                    addError("name", getText("admin.errors.issue.type.with.this.name.already.exists"));
                }
            }
        }

        // Check that icon URL is set
        if (!TextUtils.stringSet(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.must.specify.a.url.for.the.icon"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        subTaskManager.updateSubTaskIssueType(getId(), getName(), getSequence(), getDescription(), getIconurl());

        return getRedirect("ManageSubTasks.jspa");
    }

    private boolean isSubtasksEnabled()
    {
        return subTaskManager.isSubTasksEnabled();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public void setSequence(Long sequence)
    {
        this.sequence = sequence;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getIconurl()
    {
        return iconurl;
    }

    public void setIconurl(String iconurl)
    {
        this.iconurl = iconurl;
    }
}
