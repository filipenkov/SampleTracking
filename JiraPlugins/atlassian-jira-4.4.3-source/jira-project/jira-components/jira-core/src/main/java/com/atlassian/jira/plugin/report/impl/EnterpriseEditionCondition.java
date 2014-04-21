package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;

/**
 * Condition that returns true for enterptise edition
 *
 * @since v3.11
 */
public class EnterpriseEditionCondition implements EnabledCondition
{
    /**
     * Returns true for enterptise edition
     *
     * @return true for enterptise edition
     */
    public boolean isEnabled()
    {
        return true;
    }
}
