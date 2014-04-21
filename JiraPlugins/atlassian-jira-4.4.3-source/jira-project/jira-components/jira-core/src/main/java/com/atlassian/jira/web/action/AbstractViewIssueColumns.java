package com.atlassian.jira.web.action;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NameComparator;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.EditableColumnLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.web.action.filter.FilterOperationsAction;
import com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.session.SessionSearchObjectManagerFactory;
import com.atlassian.jira.web.session.SessionSearchRequestManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractViewIssueColumns extends SearchDescriptionEnabledAction implements FilterOperationsAction
{
    private int operation;
    public static final int ADD = 1;
    public static final int DELETE = 2;
    public static final int MOVELEFT = 3;
    public static final int MOVERIGHT = 4;
    public static final int RESTORE = 5;
    public static final int SHOW_ACTIONS = 8;
    public static final int HIDE_ACTIONS = 9;
    private static final int SAMPLE_ISSUES_NUMBER = 5;

    private List<NavigableField> addableColumns;
    private String fieldId;
    private Integer fieldPosition;
    private final FieldManager fieldManager = ManagerFactory.getFieldManager();
    private final SearchProvider searchProvider = ComponentManager.getComponent(SearchProvider.class);
    private final SessionSearchObjectManagerFactory sessionSearchObjectManagerFactory = ComponentManager.getComponent(SessionSearchObjectManagerFactory.class);

    public AbstractViewIssueColumns(IssueSearcherManager issueSearcherManager, SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
    }


    protected String runOperation() throws Exception
    {
        switch (getOperation())
        {
            case ADD:
                return doAdd();

            case DELETE:
                return doDelete();

            case MOVELEFT:
                return doMoveLeft();

            case MOVERIGHT:
                return doMoveRight();

            case RESTORE:
                return doRestoreDefault();
            case SHOW_ACTIONS:
                return doShowActionsColumn();
            case HIDE_ACTIONS:
                return doHideActionsColumn();
            default:
                addErrorMessage(getText("admin.errors.trying.to.perform.invalid.operation"));
        }
        return INPUT;
    }

    private String doAdd() throws Exception
    {
        if (TextUtils.stringSet(getFieldId()))
        {
            if (fieldManager.isNavigableField(getFieldId()))
            {
                NavigableField navigableField = (NavigableField) fieldManager.getNavigableField(getFieldId());
                if (getColumnLayout().contains(navigableField))
                {
                    addError("fieldId", getText("admin.errors.column.already.exists", getText(navigableField.getNameKey())));
                    return ERROR;
                }
                else
                {
                    getColumnLayout().addColumn(navigableField);
                    store();
                    return redirectToView();
                }
            }
            else
            {
                if (fieldManager.getField(getFieldId()) != null)
                {
                    addError("fieldId", getText("admin.errors.column.cannot.be.shown", getText(fieldManager.getField(getFieldId()).getNameKey())));
                }
                else
                {
                    addError("fieldId", getText("admin.errors.column.does.not.exist", "'" + getFieldId() + "'"));
                }
                return ERROR;
            }
        }
        else
        {
            addError("fieldId", getText("admin.errors.select.field"));
            return INPUT;
        }
    }

    protected String redirectToView()
    {
        return getRedirect(getActionName() + "!default.jspa");
    }

    private String doDelete() throws Exception
    {
        if (getFieldPosition() != null)
        {
            final List<ColumnLayoutItem> columnLayoutItems = getColumnLayout().getColumnLayoutItems();
            if (getFieldPosition().intValue() >= 0 && getFieldPosition().intValue() <= (columnLayoutItems.size() - 1))
            {
                final ColumnLayoutItem columnLayoutItem = columnLayoutItems.get(getFieldPosition().intValue());
                getColumnLayout().removeColumn(columnLayoutItem);
                store();
                return redirectToView();
            }
            else
            {
                addErrorMessage(getText("admin.errors.cannot.delete.field", "'" + (getFieldPosition().intValue() + 1) + "'"));
                return ERROR;
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.select.column.to.delete"));
            return INPUT;
        }
    }

    private String doMoveLeft() throws Exception
    {
        if (getFieldPosition() != null)
        {
            if (getFieldPosition().intValue() > 0)
            {
                final ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) getColumnLayout().getColumnLayoutItems().get(getFieldPosition().intValue());
                getColumnLayout().moveColumnLeft(columnLayoutItem);
                store();

                // Set field id so that the column can be highighted in the UI
                setFieldId(columnLayoutItem.getNavigableField().getId());
                return redirectToView();
            }
            else
            {
                addErrorMessage(getText("admin.errors.cannot.move.column.left", "'" + (getFieldPosition().intValue() + 1) + "'"));
                return ERROR;
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.select.column.to.move.left"));
            return INPUT;
        }
    }

    private String doMoveRight() throws Exception
    {
        if (getFieldPosition() != null)
        {
            final List<ColumnLayoutItem> columnLayoutItems = getColumnLayout().getColumnLayoutItems();
            if (getFieldPosition().intValue() < (columnLayoutItems.size() - 1))
            {
                final ColumnLayoutItem columnLayoutItem = columnLayoutItems.get(getFieldPosition().intValue());
                getColumnLayout().moveColumnRight(columnLayoutItem);
                store();

                // Set field id so that the column can be highighted in the UI
                setFieldId(columnLayoutItem.getNavigableField().getId());
                return redirectToView();
            }
            else
            {
                addErrorMessage(getText("admin.errors.cannot.move.column.right", "'" + (getFieldPosition().intValue() + 1) + "'"));
                return ERROR;
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.select.column.to.move.right"));
            return INPUT;
        }
    }

    /**
     * Adds the Actions and Operations drop down column to the issue table
     *
     * @return Should always return INPUT to redirect back the edit columns page.
     * @throws Exception if something bad happens...
     * @since v4.0
     */
    public abstract String doShowActionsColumn() throws Exception;

    /**
     * Removes the Actions and Operations drop down column from the issue table
     *
     * @return Should always return INPUT to redirect back the edit columns page.
     * @throws Exception if something bad happens...
     * @since v4.0
     */
    public abstract String doHideActionsColumn() throws Exception;

    protected abstract String doRestoreDefault();

    protected abstract void store();

    protected FieldManager getFieldManager()
    {
        return fieldManager;
    }

    private int getOperation()
    {
        return operation;
    }

    public void setOperation(int operation)
    {
        this.operation = operation;
    }

    public String getFieldId()
    {
        return fieldId;
    }

    public void setFieldId(String fieldId)
    {
        this.fieldId = fieldId;
    }

    public Integer getFieldPosition()
    {
        return fieldPosition;
    }

    public void setFieldPosition(Integer fieldPosition)
    {
        this.fieldPosition = fieldPosition;
    }

    public List getColumns()
    {
        return getColumnLayout().getColumnLayoutItems();
    }

    public abstract String getActionLocation(String prefix);

    public List getAddableColumns()
    {
        if(addableColumns == null)
        {
            try
            {
                final List<NavigableField> addableColumns = getAvailableNavigatableFields();

                // Remove all the fields that are already displayed
                final List<ColumnLayoutItem> visibleColumnLayoutItems = getColumnLayout().getColumnLayoutItems();
                for (ColumnLayoutItem columnLayoutItem : visibleColumnLayoutItems)
                {
                    addableColumns.remove(columnLayoutItem.getNavigableField());
                }

                // Remove all custom fields that do not have view values
                for (Iterator<NavigableField> iterator = addableColumns.iterator(); iterator.hasNext();)
                {
                    final NavigableField field = iterator.next();
                    if (field instanceof CustomField)
                    {
                        final CustomField customField = (CustomField) field;
                        if (!customField.getCustomFieldType().getDescriptor().isViewTemplateExists() &&
                                !customField.getCustomFieldType().getDescriptor().isColumnViewTemplateExists())
                        {
                            iterator.remove();
                        }
                    }
                }
                final Comparator<Field> nameComparator = new NameComparator(new I18nBean(getRemoteUser()));
                Collections.sort(addableColumns, nameComparator);
                this.addableColumns = addableColumns;
            }
            catch (FieldException e)
            {
                log.error("Error while retrieving available navigatable fields.", e);
                addErrorMessage(getText("admin.errors.error.retrieving.navigatable.fields"));
                this.addableColumns = Collections.emptyList();
            }
        }
        return addableColumns;
    }


    protected abstract List<NavigableField> getAvailableNavigatableFields() throws FieldException;

    public List<Issue> getSampleIssues()
    {
        // If we have a current search request, use issues from it as the sample; otherwise use the "all" search.
        SearchRequest searchRequest = getSearchRequestFromSession();
        if (searchRequest == null)
        {
            searchRequest = new SearchRequest();
        }

        try
        {
            final PagerFilter pagerFilter = new PagerFilter(SAMPLE_ISSUES_NUMBER);
            final List<Issue> searchResults = searchProvider.search(searchRequest.getQuery(), getRemoteUser(), pagerFilter).getIssues();
            return new ArrayList<Issue>(searchResults);
        }
        catch (SearchException e)
        {
            log.error("Error retrieving sample issues for customisation of navigator columns.", e);
            return Collections.emptyList();
        }
    }

    private SearchRequest getSearchRequestFromSession()
    {
        final SessionSearchRequestManager sessionSearchRequestManager = sessionSearchObjectManagerFactory.createSearchRequestManager();
        return sessionSearchRequestManager.getCurrentObject();
    }

    public PagerFilter getPager()
    {
        return new PagerFilter(SAMPLE_ISSUES_NUMBER);
    }

    /**
     * Checks if the field is hidden in the project with id of projectId and the issueType of issue with id of issueId.
     *
     * @param projectId
     * @param id        fieldId
     */
    @Override
    public boolean isFieldHidden(Long projectId, String id, String issueTypeId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("projectId cannot be null.");
        }

        // Lookup the project field layout scheme
        try
        {
            GenericValue project = ManagerFactory.getProjectManager().getProject(projectId);
            FieldManager fieldManager = ManagerFactory.getFieldManager();
            FieldLayout fieldLayout = fieldManager.getFieldLayoutManager().getFieldLayout(project, issueTypeId);
            return fieldLayout.isFieldHidden(id);
        }
        catch (DataAccessException e)
        {
            log.error("Cannot retrieve field layout.", e);
            addErrorMessage(getText("admin.errors.cannot.retrieve.field.layout"));
            return true;
        }
    }

    /**
     * This function retrieve an editable column layout and assumes that the same object is returned during
     * the lifetime of this object
     */
    protected abstract EditableColumnLayout getColumnLayout();

    protected ColumnLayoutManager getColumnLayoutManager()
    {
        return getFieldManager().getColumnLayoutManager();
    }

}
