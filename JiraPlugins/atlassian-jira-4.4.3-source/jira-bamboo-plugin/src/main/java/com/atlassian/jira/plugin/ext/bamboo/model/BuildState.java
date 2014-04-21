package com.atlassian.jira.plugin.ext.bamboo.model;

import org.jetbrains.annotations.Nullable;

import java.util.TreeMap;

/**
 * Simple enumeration of build states.
 */
public enum BuildState
{
    /**
     * The build state is unknown
     */
    UNKNOWN("Unknown"),

    /**
     * The Build was successful
     */
    SUCCESS("Successful"),

    /**
     * The Build failed
     */
    FAILED("Failed");

    /**
     * BuildState mapping with case-insensitive keys.
     * This map breaks identity of compareTo == 0 and equals operations. 
     */
    private static final TreeMap<String, BuildState> BUILD_STATE_MAPPING = new TreeMap<String, BuildState>(String.CASE_INSENSITIVE_ORDER)
    {
        {
            for (BuildState buildState : BuildState.values())
            {
                put(buildState.state, buildState);
            }
        }
    }; 

    private final String state;

    BuildState(final String state)
    {
        this.state = state;
    }

    public boolean is(@Nullable final String stateName)
    {
        return state.equals(stateName);
    }

    /**
     * Returns the appropriate enum value from the given state string
     * @param state  Case insensitive string representation of BuildState
     * @return BuildState object corresponding to input state
     */
    public static BuildState getInstance(String state)
    {
        BuildState buildState = BUILD_STATE_MAPPING.get(state);
        if (buildState == null)
        {
            throw new IllegalArgumentException("There is no BuildState called '" + state + "'");
        }

        return buildState;
    }

    @Override
    public String toString()
    {
        return state;
    }
}