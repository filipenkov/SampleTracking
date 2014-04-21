package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.Function;

public class VersionOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final Function<Version, Option> FUNCTION = new Function<Version, Option>()
    {
        public Option get(final Version input)
        {
            return new VersionOption(input);
        }
    };
    // ------------------------------------------------------------------------------------------------- Type Properties
    private Version version;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public VersionOption(Version version)
    {
        this.version = version;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return version != null ? version.getId().toString() : "";
    }

    public String getName()
    {
        return version.getName();
    }

    public String getDescription()
    {
        return version.getDescription();
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}
