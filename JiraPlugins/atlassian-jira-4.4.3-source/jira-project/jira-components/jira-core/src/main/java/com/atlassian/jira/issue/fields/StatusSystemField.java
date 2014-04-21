package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.StatusSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.StatusStatisticsMapper;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.comparator.OFBizFieldComparator;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class StatusSystemField extends NavigableFieldImpl  implements SearchableField
{
    private final StatusStatisticsMapper statusStatisticsMapper;
    private final ConstantsManager constantsManager;
    private final SearchHandlerFactory searchHandlerFactory;

    public StatusSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, StatusStatisticsMapper statusStatisticsMapper, ConstantsManager constantsManager, StatusSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.STATUS, "issue.field.status", "issue.column.heading.status", ORDER_DESCENDING, new OFBizFieldComparator(IssueFieldConstants.STATUS), velocityManager, applicationProperties, authenticationContext);
        this.statusStatisticsMapper = statusStatisticsMapper;
        this.constantsManager = constantsManager;
        this.searchHandlerFactory = searchHandlerFactory;
    }

    public LuceneFieldSorter getSorter()
    {
        return statusStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        // Create status object

        Status status = issue.getStatusObject();
        velocityParams.put(getId(), status);
        return renderTemplate("status-columnview.vm", velocityParams);
    }

    private Long getStatusTypeIdByName(String stringValue) throws FieldValidationException
    {
        for (Iterator iterator = constantsManager.getStatuses().iterator(); iterator.hasNext();)
        {
            GenericValue statusGV = (GenericValue) iterator.next();
            if (stringValue.equalsIgnoreCase(statusGV.getString("name")))
            {
                return Long.valueOf(statusGV.getString("id"));
            }
        }

        throw new FieldValidationException("Invalid status name '" + stringValue + "'.");
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }

    /**
     * Return an internationalized value for the changeHistory item - a status name in this case.
     *
     * @param changeHistory     name of status
     * @param i18nHelper        used to translate the status name
     * @return String
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (TextUtils.stringSet(changeHistory))
        {
            Long statusId = getStatusTypeIdByName(changeHistory);

            if (statusId != null)
            {
                Status status = constantsManager.getStatusObject(statusId.toString());
                if (status != null)
                {
                    return status.getNameTranslation(i18nHelper);
                }
            }
        }
        // Otherwise return the original string
        return changeHistory;
    }
}
