package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.User;
import org.apache.commons.collections.map.ListOrderedMap;

import java.util.Collections;
import java.util.Map;

/**
 * Generator for sub-task inclusion select box options.
 *
 * @since v3.11
 */
public class UserSubTaskIncludeValuesGenerator implements ValuesGenerator
{
    static final class Options
    {
        static final String ONLY_ASSIGNED = "onlyAssigned";
        static final String ASSIGNED_AND_UNASSIGNED = "assignedAndUnassigned";

        static String getDescription(String option, I18nHelper i18nHelper)
        {
            if (Options.ONLY_ASSIGNED.equals(option))
            {
                return i18nHelper.getText("report.subtasks.user.include.selected.only");
            }
            if (Options.ASSIGNED_AND_UNASSIGNED.equals(option))
            {
                return i18nHelper.getText("report.subtasks.user.include.selected.and.unassigned");
            }
            return "Unknown option: " + option;
        }
    }

    /**
     * Returns a new map of applicable options. If sub-tasks are disabled returns a single option that says that this
     * select box is not relevant.
     *
     * @param userParams map of user parameters
     * @return new map of applicable options
     */
    public Map getValues(Map userParams)
    {
        User u = (User) userParams.get("User");
        I18nHelper i18nHelper = new I18nBean(u);

        Map result = new ListOrderedMap();
        result.put(Options.ONLY_ASSIGNED, Options.getDescription(Options.ONLY_ASSIGNED, i18nHelper));
        result.put(Options.ASSIGNED_AND_UNASSIGNED, Options.getDescription(Options.ASSIGNED_AND_UNASSIGNED, i18nHelper));
        return Collections.unmodifiableMap(result);
    }
}