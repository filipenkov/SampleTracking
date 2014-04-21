package com.atlassian.jira.plugin.ext.bamboo.model;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the Status of a Plan on a Bamboo server
 */
public class PlanStatus
{
    private final PlanResultKey planResultKey;
    private final BuildState buildState;
    private final LifeCycleState lifeCycleState;
    private final boolean recoverable;

    public static PlanStatus fromJsonObject(PlanResultKey planResultKey, JSONObject jsonObject) throws JSONException
    {
        final String state = jsonObject.getString("state");
        final String lifecycle = jsonObject.getString("lifeCycleState");

        final BuildState buildState = BuildState.getInstance(state);
        final LifeCycleState lifeCycleState = LifeCycleState.getInstance(lifecycle);

        return new PlanStatus(planResultKey, buildState, lifeCycleState, true);
    }

    public PlanStatus(final PlanResultKey planResultKey, final BuildState buildState, final LifeCycleState lifeCycleState, final boolean recoverable)
    {
        this.planResultKey = planResultKey;
        this.buildState = buildState;
        this.lifeCycleState = lifeCycleState;
        this.recoverable = recoverable;
    }

    /**
     * @return resultKey
     */
    @NotNull
    public PlanResultKey getPlanResultKey()
    {
        return planResultKey;
    }

    /**
     * @return buildState
     */
    @Nullable
    public BuildState getBuildState()
    {
        return buildState;
    }

    /**
     * @return lifeCycleState
     */
    @Nullable
    public LifeCycleState getLifeCycleState()
    {
        return lifeCycleState;
    }

    /**
     * @return is a valid {@link PlanStatus}
     */
    public boolean isValid()
    {
        return lifeCycleState != null && buildState != null;
    }

    /**
     * Was the status of the plan in a recoverable error state?
     * @return recoverable
     */
    public boolean isRecoverable()
    {
        return recoverable;
    }
}
