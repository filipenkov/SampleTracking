/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.IssueActionSupport;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkEditBeanSessionHelper;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.query.Query;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class AbstractBulkOperationAction extends IssueActionSupport
{
    private final SearchService searchService;

    public AbstractBulkOperationAction(final SearchService searchService)
    {
        this.searchService = searchService;
    }

    public BulkEditBean getBulkEditBean()
    {
        return getRootBulkEditBean();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="NP_NULL_ON_SOME_PATH", justification="TODO this needs to be fixed")
    public List getColumns() throws Exception
    {
        ColumnLayout columnLayout;

        // JRA-12466: Bulk Edit issue navigator should use the same column configuration as the current search request
        SearchRequest searchRequest = getSearchRequest();
        if (searchRequest != null && searchRequest.isLoaded() && searchRequest.useColumns())
        {
            columnLayout = ComponentAccessor.getFieldManager().getColumnLayoutManager().getColumnLayout(getLoggedInUser(), searchRequest);
        }
        else
        {
            columnLayout = ComponentAccessor.getFieldManager().getColumnLayoutManager().getColumnLayout(getLoggedInUser());
        }

        Query query = searchRequest.getQuery();
        final QueryContext queryContext = searchService.getQueryContext(getLoggedInUser(), query);
        return columnLayout.getVisibleColumnLayoutItems(getLoggedInUser(), queryContext);
    }

    public IssueTableLayoutBean getIssueTableLayoutBean() throws Exception
    {
        IssueTableLayoutBean layoutBean = new IssueTableLayoutBean(getColumns());
        layoutBean.setSortingEnabled(false);
        return layoutBean;
    }

    protected void clearBulkEditBean()
    {
        BulkEditBeanSessionHelper.removeFromSession();
    }

    protected String finishWizard() throws Exception
    {
        clearBulkEditBean();
        return getRedirect("/secure/IssueNavigator.jspa");
    }

    public BulkEditBean getRootBulkEditBean()
    {
        BulkEditBean bean = BulkEditBeanSessionHelper.getFromSession();
        if (bean == null)
        {
            log.warn("Bulk edit bean unexpectedly null. Perhaps session was lost (e.g. when URL used is different to base URL in General Configuration)?");
        }
        return bean;
    }

    /**
     * Deteremine if the current user can disable mail notifications for this bulk operation.
     * <p/>
     * Only global admins or a user who is a project admin of all projects associated with the selected issues
     * can disable bulk mail notifications.
     *
     * @return true     if the user is a global admin or a project admin of all projects associated with the selected issues.
     */
    public boolean isCanDisableMailNotifications()
    {
        // Check for global admin permission
        if (isHasPermission(Permissions.ADMINISTER))
        {
            return true;
        }
        // Check for project admin permission on all projects from selected issue collection
        else
        {
            Collection projects = getBulkEditBean().getProjects();

            for (Iterator iterator = projects.iterator(); iterator.hasNext();)
            {
                GenericValue projectGV = (GenericValue) iterator.next();
                if (!isHasProjectPermission(Permissions.PROJECT_ADMIN, projectGV))
                {
                    return false;
                }
            }

            return true;
        }
    }

    public boolean isSendBulkNotification()
    {
        return getBulkEditBean().isSendBulkNotification();
    }

    public void setSendBulkNotification(boolean sendBulkNotification)
    {
        if (getBulkEditBean() != null)
        {
            getBulkEditBean().setSendBulkNotification(sendBulkNotification);
        }
    }

    protected String redirectToStart(String i18nMessage)
    {
        ActionContext.getSession().put(SessionKeys.SESSION_TIMEOUT_MESSAGE, getText(i18nMessage));
        return getRedirect("SessionTimeoutMessage.jspa");
    }
}
