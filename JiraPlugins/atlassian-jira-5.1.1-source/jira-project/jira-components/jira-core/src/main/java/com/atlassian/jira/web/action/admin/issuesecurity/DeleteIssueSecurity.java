/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

@WebSudoRequired
public class DeleteIssueSecurity extends SchemeAwareIssueSecurityAction
{
    private Long id;
    private boolean confirmed = false;

        private final PermissionTypeManager permTypeManager;

    public DeleteIssueSecurity(PermissionTypeManager permTypeManager)
    {
        this.permTypeManager = permTypeManager;
    }

    /**
     * Validates that a permission id has been passed and that the delete has been confirmed
     */
    protected void doValidation()
    {
        if (id == null)
            addErrorMessage(getText("admin.errors.issuesecurity.specify.permission.to.delete"));
        if (!confirmed)
            addErrorMessage(getText("admin.errors.issuesecurity.confirm.deletion"));
    }

    /**
     * Deletes the specified permission
     * @return String indicating result of action
     * @throws Exception
     * @see com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl
     */
    protected String doExecute() throws Exception
    {
        getSchemeManager().deleteEntity(getId());

        ManagerFactory.getIssueSecurityLevelManager().clearUsersLevels();

        if (getSchemeId() == null)
            return getRedirect("ViewIssueSecuritySchemes.jspa");
        else
            return getRedirect("EditIssueSecurities!default.jspa?schemeId=" + getSchemeId());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Gets a issue security object based on the id
     * @return The issue security object
     * @throws GenericEntityException
     * @see com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl
     */
    private GenericValue getIssueSecurity() throws GenericEntityException
    {
        return getSchemeManager().getEntity(id);
    }

    public String getIssueSecurityDisplayName() throws GenericEntityException
    {
        return getType(getIssueSecurity().getString("type")).getDisplayName();
    }

    /**
     * Get the permission parameter. This is a value such as the group that has the permission or the current reporter
     * @return The value of the parameter field of the permission object
     * @throws GenericEntityException
     */
    public String getIssueSecurityParameter() throws GenericEntityException
    {
        String param =getIssueSecurity().getString("parameter");
        String type = getIssueSecurity().getString("type");
        return permTypeManager.getSecurityType(type).getArgumentDisplay(param);
    }

    /**
     * Get the name of the permission
     * @return The name of the permission
     * @see SchemePermissions
     */
    public String getIssueSecurityName() throws GenericEntityException
    {
        IssueSecurityLevelManager secur = ManagerFactory.getIssueSecurityLevelManager();

        return secur.getIssueSecurityName(getIssueSecurity().getLong("security"));
    }

    public boolean isConfirmed()
    {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed)
    {
        this.confirmed = confirmed;
    }
}
