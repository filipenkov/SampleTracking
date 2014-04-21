package com.atlassian.jira.issue.customfields.config.helper;

import java.util.List;

public interface BasicConfigDescriptor
{
    String getTitle();
    String getInstructions();
    List getConfigFields();
}
