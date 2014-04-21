package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.util.Function;

public class ComponentOption extends AbstractOption implements Option
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final Function<ProjectComponent, Option> FUNCTION = new Function<ProjectComponent, Option>()
    {
        public ComponentOption get(final ProjectComponent input)
        {
            return new ComponentOption(input);
        }
    };
    // ------------------------------------------------------------------------------------------------- Type Properties
    private ProjectComponent component;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public ComponentOption(ProjectComponent component)
    {
        this.component = component;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return component != null ? component.getId().toString() : "";
    }

    public String getName()
    {
        return component.getName();
    }

    public String getDescription()
    {
        return component.getDescription();
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods
}
