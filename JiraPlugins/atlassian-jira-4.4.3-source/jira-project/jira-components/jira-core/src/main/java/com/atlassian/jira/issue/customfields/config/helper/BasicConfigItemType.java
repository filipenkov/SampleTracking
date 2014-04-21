package com.atlassian.jira.issue.customfields.config.helper;

import com.atlassian.jira.issue.fields.config.FieldConfigItemType;

public interface BasicConfigItemType extends FieldConfigItemType
{
    BasicConfigDescriptor getBasicConfigDescriptor();
}
