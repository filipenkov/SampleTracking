package com.atlassian.jira.plugin.ext.bamboo.model;

import org.apache.commons.lang.Validate;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public enum LifeCycleState
{
    /**
     * The build pending, as long as build is queued
     */
    PENDING("Pending"),

    /**
     * The build queued, as long as agent not started build process
     */
    QUEUED("Queued"),

    /**
     * The build is really building, so once checkout started on agent
     */
    IN_PROGRESS("InProgress"),

    /**
     * The Build finished
     */
    FINISHED("Finished"),

    /**
     * The Build was not built
     */
    NOT_BUILT("NotBuilt");

    public final static EnumSet<LifeCycleState> ACTIVE_STATES = EnumSet.of(PENDING, QUEUED, IN_PROGRESS);
    public final static EnumSet<LifeCycleState> FINAL_STATES = EnumSet.of(FINISHED, NOT_BUILT);

    private final String state;

    LifeCycleState(final String state)
    {
        this.state = state;
    }

    /**
     * LifeCycleState mapping
     */
    private static final Map<String, LifeCycleState> LIFE_CYCLE_STATE_MAPPING = new HashMap<String, LifeCycleState>()
    {
        {
            for (LifeCycleState lifeCycleState : LifeCycleState.values())
            {
                put(lifeCycleState.state, lifeCycleState);
            }
        }
    };

    /**
     * Returns the appropriate enum value from the given state string
     *
     * @param state
     *
     * @return LifeCycleState object corresponding to input state
     */
    public static LifeCycleState getInstance(String state)
    {
        LifeCycleState lifeCycleState = LIFE_CYCLE_STATE_MAPPING.get(state);
        Validate.notNull(lifeCycleState, "There is no LifeCycleState called '" + state + "'");
        return lifeCycleState;
    }

    public String getValue()
    {
        return state;
    }

    /**
     * Is required because of Hibernate
     *
     * @return
     */
    @Override
    public String toString()
    {
        return state;
    }

    public static boolean isPending(LifeCycleState lifeCycleState)
    {
        return lifeCycleState == PENDING;
    }

    public static boolean isQueued(LifeCycleState lifeCycleState)
    {
        return lifeCycleState == QUEUED;
    }

    public static boolean isWaiting(LifeCycleState lifeCycleState)
    {
        return isPending(lifeCycleState) || isQueued(lifeCycleState);
    }

    public static boolean isActive(LifeCycleState lifeCycleState)
    {
        return ACTIVE_STATES.contains(lifeCycleState);
    }

    public static boolean isFinalized(LifeCycleState lifeCycleState)
    {
        return FINAL_STATES.contains(lifeCycleState);
    }

    public static boolean isInProgress(LifeCycleState lifeCycleState)
    {
        return lifeCycleState == IN_PROGRESS;
    }
    
    public static boolean isFinished(LifeCycleState lifeCycleState)
    {
        return lifeCycleState == FINISHED;
    }

    public static boolean isNotBuilt(LifeCycleState lifeCycleState)
    {
        return lifeCycleState == NOT_BUILT;
    }
}
