package com.atlassian.jira.web.action.user;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableUserColumnLayout;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.web.action.AbstractViewIssueColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewUserIssueColumns extends AbstractViewIssueColumns
{
    private String name;
    private User profileUser;
    private EditableUserColumnLayout editableUserColumnLayout;
    private EmailFormatter emailFormatter;
    private GroupPermissionChecker groupPermissionChecker;
    private final UserPreferencesManager userPreferencesManager;

    public ViewUserIssueColumns()
    {
        this(ComponentManager.getComponentInstanceOfType(IssueSearcherManager.class), ComponentManager.getComponentInstanceOfType(EmailFormatter.class), ComponentManager.getComponentInstanceOfType(GroupPermissionChecker.class), ComponentAccessor.getUserPreferencesManager(), ComponentManager.getComponentInstanceOfType(SearchService.class), ComponentManager.getComponentInstanceOfType(SearchSortUtil.class));
    }

    public ViewUserIssueColumns(IssueSearcherManager issueSearcherManager, EmailFormatter emailFormatter, GroupPermissionChecker groupPermissionChecker, UserPreferencesManager userPreferencesManager, final SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.emailFormatter = emailFormatter;
        this.groupPermissionChecker = groupPermissionChecker;
        this.userPreferencesManager = userPreferencesManager;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (getLoggedInUser() != null && ManagerFactory.getPermissionManager().hasPermission(Permissions.USE, getLoggedInUser()))
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
        return prefix + getActionName() + ".jspa";
    }

    @Override
    protected List<NavigableField> getAvailableNavigatableFields() throws FieldException
    {
        return new ArrayList<NavigableField>(getFieldManager().getAvailableNavigableFields(getLoggedInUser()));
    }

    @Override
    protected EditableColumnLayout getColumnLayout()
    {
        if (editableUserColumnLayout == null)
        {
            try
            {
                editableUserColumnLayout = getColumnLayoutManager().getEditableUserColumnLayout(getUser());
            }
            catch (ColumnLayoutStorageException e)
            {
                log.error(e, e);
                addErrorMessage(getText("admin.errors.user.could.not.retrieve.column.layout"));
            }
            catch (Exception e)
            {
                log.error(e, e);
            }
        }
        return editableUserColumnLayout;
    }

    @Override
    protected void store()
    {
        try
        {
            getColumnLayoutManager().storeEditableUserColumnLayout((EditableUserColumnLayout) getColumnLayout());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error("The was an error storing the user's column layout.", e);
            addErrorMessage(getText("admin.errors.user.error.restoring.default.column.layout"));
        }
    }

    @Override
    protected String doRestoreDefault()
    {
        try
        {
            getColumnLayoutManager().restoreUserColumnLayout(getUser());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.user.could.not.restore.default"));
        }
        return getResult();
    }

    public User getUser()
    {
        if (profileUser == null)
        {
            if (name == null)
            {
                profileUser = getLoggedInUser();
            }
            else
            {
                profileUser = UserUtils.getUser(name);
            }
        }
        return profileUser;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<ColumnLayoutItem> getDefaultColumns()
    {
        try
        {
            return getColumnLayoutManager().getDefaultColumnLayout(getLoggedInUser()).getColumnLayoutItems();
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.user.could.not.retrieve.column.layout"));
            return Collections.emptyList();
        }
    }

    public boolean isUsingDefaultColumns()
    {
        try
        {
            return !getColumnLayoutManager().hasColumnLayout(getLoggedInUser());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.user.could.not.check.if.default"));
            return true;
        }
    }

    public boolean isHasViewGroupPermission(String group, User user)
    {
        return groupPermissionChecker.hasViewGroupPermission(group, user);
    }

    public String getDisplayEmail(String email)
    {
        return emailFormatter.formatEmailAsLink(email, getLoggedInUser());
    }

    public Boolean isActionsAndOperationsShowing()
    {
        return getUserPreferences().getBoolean(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR);
    }


    @Override
    public String doShowActionsColumn() throws AtlassianCoreException
    {
        getUserPreferences().setBoolean(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR, true);
        userPreferencesManager.clearCache();
        return INPUT;
    }

    @Override
    public String doHideActionsColumn() throws AtlassianCoreException
    {
        getUserPreferences().setBoolean(PreferenceKeys.USER_SHOW_ACTIONS_IN_NAVIGATOR, false);
        userPreferencesManager.clearCache();
        return INPUT;
    }

    //JRADEV-2996 If the key is the same as the name can lead to double escaping
    @Override
    public String getText(String keyName)
    {
        final List<NavigableField> fields = getAddableColumns();
        for (NavigableField field: fields)
        {
            if (field.getName().equals(keyName))
            {
                return field.getName();
            }
        }
        return super.getText(keyName);
    }

}
