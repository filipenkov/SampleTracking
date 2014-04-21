/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.option;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

public class AssigneeOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String optionName;
    private String displayName;
    private boolean optionEnabled;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public AssigneeOption(String optionName, String displayName, boolean optionEnabled)
    {
        this.optionName = optionName;
        this.displayName = displayName;
        this.optionEnabled = optionEnabled;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return null;
    }

    public String getName()
    {
        return null;
    }

    public String getDescription()
    {
        return null;
    }

    public String getImagePath()
    {
        return null;
    }

    public String getImagePathHtml()
    {
        return StringEscapeUtils.escapeHtml(getImagePath());
    }

    public String getCssClass()
    {
        return null;
    }

    public List getChildOptions()
    {
        return null;
    }

    public String getOptionName()
    {
        return optionName;
    }

    public void setOptionName(String optionName)
    {
        this.optionName = optionName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public boolean isOptionEnabled()
    {
        return optionEnabled;
    }

    public void setOptionEnabled(boolean optionEnabled)
    {
        this.optionEnabled = optionEnabled;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}
