package com.atlassian.jira.plugin.report.impl;

import com.atlassian.configurable.EnabledCondition;

/**
 * Condition that returns false for enterptise edition
 *
 * @since v3.11
 */
public class NonEnterpriseEditionCondition implements EnabledCondition
{
    /**
     * Returns false for enterptise edition
     *
     * @return false for enterptise edition
     */
    public boolean isEnabled()
    {
        return false;
    }

}
