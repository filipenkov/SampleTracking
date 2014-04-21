/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractUserSearchRenderer;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ParameterStore
{
    private User user;
    private List<TextOption> timePeriods;
    private List<Map<String, String>> reporterTypes;
    private List<Map<String, String>> assigneeTypes;
    private I18nHelper i18n;

    public ParameterStore(User user)
    {
        this.user = user;
        i18n = new I18nBean(user);
    }

    public ParameterStore(String userName) throws EntityNotFoundException
    {
        this(UserUtils.getUser(userName));
    }

    /**
     * The time periods for filtering by last updated and date created.
     * All time periods are in minutes
     *
     * @return the collection of possible time options
     */
    public Collection<TextOption> getTimePeriods()
    {
        if (timePeriods == null)
            setTimePeriods();

        return timePeriods;
    }

    private void setTimePeriods()
    {
        timePeriods = EasyList.build(new TextOption("-1h", i18n.getText("time.periods.hour")),
                                     new TextOption("-1d", i18n.getText("time.periods.day")),
                                     new TextOption("-1w", i18n.getText("time.periods.week")),
                                     new TextOption("-4w 2d", i18n.getText("time.periods.month")));
    }

    public List<Map<String, String>> getReporterTypes()
    {
        if (reporterTypes == null)
            setReporterTypes();

        return reporterTypes;
    }

    private void setReporterTypes()
    {
        reporterTypes = new ArrayList<Map<String, String>>();
        reporterTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("reporter.types.anyuser")).add("key", "").add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE).toImmutableMap());
        reporterTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("reporter.types.noreporter")).add("key", DocumentConstants.ISSUE_NO_AUTHOR).add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE ).toImmutableMap());

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (user != null)
        {
            reporterTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("reporter.types.currentuser")).add("key", DocumentConstants.ISSUE_CURRENT_USER).add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE ).toImmutableMap());
        }

        reporterTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("reporter.types.specifyuser")).add("key", DocumentConstants.SPECIFIC_USER).add("related", AbstractUserSearchRenderer.SELECT_LIST_USER ).toImmutableMap());
        reporterTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("reporter.types.specifygroup")).add("key", DocumentConstants.SPECIFIC_GROUP).add("related", AbstractUserSearchRenderer.SELECT_LIST_GROUP ).toImmutableMap());
    }

    public List<Map<String, String>> getAssigneeTypes()
    {
        if (assigneeTypes == null)
            setAsssigneeTypes();

        return assigneeTypes;
    }

    private void setAsssigneeTypes()
    {
        assigneeTypes = new ArrayList<Map<String, String>>();
        assigneeTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("assignee.types.anyuser")).add("key", null).add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE ).toImmutableMap());
        assigneeTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("assignee.types.unassigned")).add("key", DocumentConstants.ISSUE_UNASSIGNED).add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE ).toImmutableMap());

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (user != null)
        {
            assigneeTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("assignee.types.currentuser")).add("key", DocumentConstants.ISSUE_CURRENT_USER).add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE ).toImmutableMap());
        }
        
        assigneeTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("assignee.types.specifyuser")).add("key", DocumentConstants.SPECIFIC_USER).add("related", AbstractUserSearchRenderer.SELECT_LIST_USER ).toImmutableMap());
        assigneeTypes.add(MapBuilder.<String, String>newBuilder().add("value", i18n.getText("assignee.types.specifygroup")).add("key", DocumentConstants.SPECIFIC_GROUP).add("related", AbstractUserSearchRenderer.SELECT_LIST_GROUP ).toImmutableMap());
    }
}
