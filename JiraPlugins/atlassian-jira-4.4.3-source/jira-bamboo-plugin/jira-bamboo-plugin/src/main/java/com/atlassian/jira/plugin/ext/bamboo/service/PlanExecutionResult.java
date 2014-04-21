package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.jira.plugin.ext.bamboo.model.PlanResultKey;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanExecutionResult
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(PlanExecutionResult.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private final PlanResultKey planResultKey;
    private final List<String> errors = new ArrayList<String>();

    // ---------------------------------------------------------------------------------------------------- Dependencies
    PlanExecutionResult(@Nullable PlanResultKey planResultKey, @NotNull List<String> errors)
    {
        this.planResultKey = planResultKey;
        this.errors.addAll(errors);
    }

    PlanExecutionResult(@Nullable PlanResultKey planResultKey, @NotNull String... errors)
    {
        this.planResultKey = planResultKey;
        Collections.addAll(this.errors, errors);
    }


    public PlanExecutionResult(@Nullable PlanResultKey planResultKey)
    {
        this.planResultKey = planResultKey;
    }

    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
    @Nullable
    public PlanResultKey getPlanResultKey()
    {
        return planResultKey;
    }

    @NotNull
    public List<String> getErrors()
    {
        return errors;
    }
}
