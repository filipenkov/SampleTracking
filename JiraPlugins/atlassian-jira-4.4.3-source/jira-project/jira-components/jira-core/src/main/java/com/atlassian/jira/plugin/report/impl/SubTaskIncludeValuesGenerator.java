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
public class SubTaskIncludeValuesGenerator implements ValuesGenerator
{
    static final class Options
    {
        static final String ONLY_SELECTED = "onlySelected";
        static final String SELECTED_AND_BLANK = "selectedAndBlank";
        static final String ALL = "all";

        static String getDescription(String option, I18nHelper i18nHelper)
        {
            if (Options.ONLY_SELECTED.equals(option))
            {
                return i18nHelper.getText("report.subtasks.include.selected.only");
            }
            if (Options.SELECTED_AND_BLANK.equals(option))
            {
                return i18nHelper.getText("report.subtasks.include.selected.none");
            }
            if (Options.ALL.equals(option))
            {
                return i18nHelper.getText("report.subtasks.include.all");
            }
            return "Unknown option: " + option;
        }
    }

    /**
     * Returns a new map of applicable options. If sub-tasks are disabled returns a single option that says that this
     * select box is not relevant.
     *
     * @param userParams map of user parameters
     *
     * @return new map of applicable options
     */
    public Map getValues(Map userParams)
    {
        User u = (User) userParams.get("User");
        I18nHelper i18nHelper = new I18nBean(u);

        Map result = new ListOrderedMap();
        result.put(Options.ONLY_SELECTED, Options.getDescription(Options.ONLY_SELECTED, i18nHelper));
        result.put(Options.SELECTED_AND_BLANK, Options.getDescription(Options.SELECTED_AND_BLANK, i18nHelper));
        result.put(Options.ALL, Options.getDescription(Options.ALL, i18nHelper));
        return Collections.unmodifiableMap(result);
    }
}
