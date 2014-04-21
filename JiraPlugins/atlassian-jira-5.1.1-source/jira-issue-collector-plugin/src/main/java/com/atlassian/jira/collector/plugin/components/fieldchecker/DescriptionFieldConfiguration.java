package com.atlassian.jira.collector.plugin.components.fieldchecker;

import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.issue.IssueFieldConstants;

public class DescriptionFieldConfiguration implements FieldConfiguration {
    private static final String CUSTOM_TEMPLATE_ID = "custom";

    @Override
    public String getFieldName() {
        return IssueFieldConstants.DESCRIPTION;
    }

    @Override
    public boolean isUsed(Collector collector)
    {
        if(CUSTOM_TEMPLATE_ID.equals(collector.getTemplate().getId()))
        {
            return collector.getCustomTemplateFields().contains(IssueFieldConstants.DESCRIPTION);
        }

        return true;
    }
}
