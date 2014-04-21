package com.atlassian.crowd.model.application;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Represents the type of an application.
 */
public enum ApplicationType
{
    CROWD("Crowd"),
    GENERIC_APPLICATION("Generic Application"),
    PLUGIN("Plugin"),
    JIRA("JIRA"),
    CONFLUENCE("Confluence"),
    BAMBOO("Bamboo"),
    FISHEYE("FishEye"),
    CRUCIBLE("Crucible");

    private final String displayName;

    ApplicationType(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * Returns the display name of the application type.
     *
     * @return display name of the application type
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns the list of application types that can be added by the user to Crowd as an application.
     *
     * @return list of application types that can be added by the user to Crowd as an application.
     */
    public static List<ApplicationType> getCreatableAppTypes()
    {
        return ImmutableList.of(JIRA, CONFLUENCE, BAMBOO, FISHEYE, CRUCIBLE, GENERIC_APPLICATION);
    }
}
