package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableDefaultColumnLayout;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.web.action.AbstractViewIssueColumns;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebSudoRequired
public class ViewIssueColumns extends AbstractViewIssueColumns
{
    private EditableDefaultColumnLayout editableDefaultColumnLayout;
    private final ApplicationProperties applicationProperties;
    private final UserPreferencesManager userPreferencesManager;

    public ViewIssueColumns(IssueSearcherManager issueSearcherManager, ApplicationProperties applicationProperties,
            UserPreferencesManager userPreferencesManager, SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.applicationProperties = applicationProperties;
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (getRemoteUser() != null && ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, getRemoteUser()))
        {
            return runOperation();
        }
        else
        {
            return "securitybreach";
        }
    }

    @Override
    public String getActionLocation(String prefix)
    {
        return prefix + "admin/" + getActionName() + ".jspa";
    }

    @Override
    protected List<NavigableField> getAvailableNavigatableFields() throws FieldException
    {
        return new ArrayList<NavigableField>(getFieldManager().getAllAvailableNavigableFields());
    }

    @Override
    protected EditableColumnLayout getColumnLayout()
    {
        if (editableDefaultColumnLayout == null)
        {
            try
            {
                editableDefaultColumnLayout = getColumnLayoutManager().getEditableDefaultColumnLayout();
            }
            catch (ColumnLayoutStorageException e)
            {
                log.error("Error while retrieving column layout.", e);
                addErrorMessage(getText("admin.errors.error.retrieving.column.layout"));
            }
        }
        return editableDefaultColumnLayout;
    }

    @Override
    protected void store()
    {
        try
        {
            getColumnLayoutManager().storeEditableDefaultColumnLayout((EditableDefaultColumnLayout) getColumnLayout());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error("The was an error storing the default column layout.", e);
            addErrorMessage(getText("admin.errors.error.storing.column.layout"));
        }
    }

    public boolean isUsingDefaultColumns()
    {
        try
        {
            return !getColumnLayoutManager().hasDefaultColumnLayout();
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.not.check.column.layout.default"));
            return true;
        }
    }

    public List<ColumnLayoutItem> getDefaultColumns()
    {
        try
        {
            return getColumnLayoutManager().getDefaultColumnLayout().getColumnLayoutItems();
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.not.retrieve.column.layout"));
            return Collections.emptyList();
        }
    }

    @Override
    protected String doRestoreDefault()
    {
        try
        {
            getColumnLayoutManager().restoreDefaultColumnLayout();
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.not.restore.system.default"));
        }
        return getResult();
    }

    public Boolean isActionsAndOperationsShowing()
    {
        return applicationProperties.getOption(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR);
    }

    @Override
    public String doShowActionsColumn()
    {
        applicationProperties.setOption(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR, true);
        userPreferencesManager.clearCache();
        return INPUT;
    }

    @Override
    public String doHideActionsColumn()
    {
        applicationProperties.setOption(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR, false);
        userPreferencesManager.clearCache();
        return INPUT;
    }


}
