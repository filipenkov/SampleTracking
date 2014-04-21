package com.atlassian.jira.web.action.admin.enterprise;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.AbstractViewIssueColumns;

import java.util.ArrayList;
import java.util.List;

public class ViewSearchRequestIssueColumns extends AbstractViewIssueColumns
{
    private Long filterId;
    private EditableSearchRequestColumnLayout editableSearchRequestColumnLayout;

    public ViewSearchRequestIssueColumns(IssueSearcherManager issueSearcherManager, final SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (getRemoteUser() != null && ManagerFactory.getPermissionManager().hasPermission(Permissions.USE, getRemoteUser()))
        {
            if (getFilterId() == null)
            {
                addErrorMessage(getText("admin.errors.no.filter.id.provided"));
                return getResult();
            }

            return runOperation();
        }
        else
        {
            return "securitybreach";
        }
    }

    @Override
    protected String doRestoreDefault()
    {
        try
        {
            getColumnLayoutManager().restoreSearchRequestColumnLayout(getSearchRequest());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.not.remove.column.layout"));
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.net.retrieve.search","'" + getFilterId() + "'"));
        }
        return getResult();
    }

    @Override
    protected void store()
    {
        try
        {
            getColumnLayoutManager().storeEditableSearchRequestColumnLayout(editableSearchRequestColumnLayout);
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error("The was an error storing the search request's column layout.", e);
            addErrorMessage(getText("admin.errors.could.not.store.column.layout"));
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
        return new ArrayList<NavigableField>(getFieldManager().getAvailableNavigableFields(getRemoteUser()));
    }

    @Override
    protected EditableColumnLayout getColumnLayout()
    {
        if (editableSearchRequestColumnLayout == null)
        {
            try
            {
                editableSearchRequestColumnLayout = getColumnLayoutManager().getEditableSearchRequestColumnLayout(getRemoteUser(), getSearchRequest());
            }
            catch (ColumnLayoutStorageException e)
            {
                log.error(e, e);
                addErrorMessage(getText("admin.errors.could.not.retrieve.column.layout"));
            }
            catch (DataAccessException e)
            {
                log.error(e, e);
                addErrorMessage(getText("admin.errors.could.net.retrieve.search","'" + getFilterId() + "'."));
            }
        }
        return editableSearchRequestColumnLayout;
    }

    @Override
    public String doShowActionsColumn() throws Exception
    {
        return INPUT;
    }

    @Override
    public String doHideActionsColumn() throws Exception
    {
        return INPUT;
    }

    public Long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(Long filterId)
    {
        this.filterId = filterId;
    }

    public boolean isUsingDefaultColumns()
    {
        try
        {
            return !getColumnLayoutManager().hasColumnLayout(getSearchRequest());
        }
        catch (ColumnLayoutStorageException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.not.check.if.default.column.layout"));
            return true;
        }
        catch (DataAccessException e)
        {
            log.error(e, e);
            addErrorMessage(getText("admin.errors.could.net.retrieve.search","'" + getFilterId() + "'."));
            return true;
        }
    }

    @Override
    public SearchRequest getSearchRequest()
    {
        final JiraServiceContext ctx = new JiraServiceContextImpl(getRemoteUser());
        return ComponentManager.getInstance().getSearchRequestService().getFilter(ctx, getFilterId());
    }

    @Override
    protected String redirectToView()
    {
        return getRedirect(getActionName() + "!default.jspa?filterId=" + getFilterId());
    }
}
