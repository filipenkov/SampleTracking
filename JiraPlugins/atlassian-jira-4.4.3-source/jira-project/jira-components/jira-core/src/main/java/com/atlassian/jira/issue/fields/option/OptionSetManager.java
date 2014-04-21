package com.atlassian.jira.issue.fields.option;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.NotNull;

import java.util.Collection;

public interface OptionSetManager
{
    // -------------------------------------------------------------------------------------------------- Public Methods
    OptionSet getOptionsForConfig(@NotNull FieldConfig config);

    OptionSet createOptionSet(@NotNull FieldConfig config, Collection optionIds);

    OptionSet updateOptionSet(@NotNull FieldConfig config, Collection optionIds);

    void removeOptionSet(@NotNull FieldConfig config);
}
