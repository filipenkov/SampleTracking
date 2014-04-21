package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.velocity.CommonVelocityKeys;
import com.atlassian.velocity.VelocityManager;
import org.apache.lucene.search.SortComparatorSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractOrderableNavigableFieldImpl extends AbstractOrderableField implements NavigableField
{
    public AbstractOrderableNavigableFieldImpl(String id, String name, VelocityManager velocityManager, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, SearchHandlerFactory searchHandlerFactory)
    {
        super(id, name, velocityManager, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
    }

    protected Map<String, Object> getVelocityParams(I18nHelper isI18nHelper, Map displayParams, Issue issue)
    {
        Map<String, Object> velocityParams = new HashMap<String, Object>();
        velocityParams.put(CommonVelocityKeys.I18N, isI18nHelper);
        velocityParams.put(CommonVelocityKeys.DISPLAY_PARAMS, displayParams);
        velocityParams.put(CommonVelocityKeys.DISPLAY_PARAMETERS, displayParams);
        if (displayParams != null)
        {
            velocityParams.put(CommonVelocityKeys.READ_ONLY, (displayParams.get("readonly") == null ? Boolean.FALSE: displayParams.get("readonly")));
            velocityParams.put(CommonVelocityKeys.TEXT_ONLY, (displayParams.get("textOnly") == null ? Boolean.FALSE: displayParams.get("textOnly")));
            velocityParams.put(CommonVelocityKeys.EXCEL_VIEW, (displayParams.get("excel_view") == null ? Boolean.FALSE: displayParams.get("excel_view")));
            velocityParams.put(CommonVelocityKeys.NO_LINK, (displayParams.get("nolink") == null ? Boolean.FALSE: displayParams.get("nolink")));
            velocityParams.put(CommonVelocityKeys.PREFIX, (displayParams.get("prefix") == null ? "": displayParams.get("prefix")));

        }
        else
        {
            velocityParams.put(CommonVelocityKeys.READ_ONLY, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.TEXT_ONLY, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.EXCEL_VIEW, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.NO_LINK, Boolean.FALSE);
            velocityParams.put(CommonVelocityKeys.PREFIX, "");
        }

        return CompositeMap.of(velocityParams, getVelocityParams(issue));
    }

    /**
     * A default implementation that returns a {@link com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator} from {@link #getSorter()}.
     */
    public SortComparatorSource getSortComparatorSource()
    {
        return new MappedSortComparator(getSorter());
    }

    public String getColumnCssClass()
    {
        return getId();
    }

    public String getHiddenFieldId()
    {
        return getId();
    }

    public String prettyPrintChangeHistory(String changeHistory)
    {
        return changeHistory;
    }

    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        return changeHistory;
    }
}
