package com.atlassian.crowd.embedded.api;

import java.io.Serializable;
import java.util.List;

/**
 * Information of a directory synchronisation round.
 */
public class DirectorySynchronisationRoundInformation
{
    private final long startTime;
    private final long durationMs;
    private final String statusKey;
    private final List<Serializable> statusParameters;

    public DirectorySynchronisationRoundInformation(long startTime, long durationMs, String statusKey, List<Serializable> statusParameters)
    {
        this.startTime = startTime;
        this.durationMs = durationMs;
        this.statusKey = statusKey;
        this.statusParameters = statusParameters;
    }

    /**
     * Returns the time in milliseconds of the directory synchronisation time.
     *
     * @return the time in milliseconds of the directory synchronisation time
     */
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Returns in milliseconds the duration of the synchronisation
     *
     * @return duration of the synchronisation in milliseconds
     */
    public long getDurationMs()
    {
        return durationMs;
    }

    /**
     * Returns a status key that can be used to get a human readable synchronisation status message.
     *
     * @return status key
     */
    public String getStatusKey()
    {
        return statusKey;
    }

    /**
     * Returns parameters for status key retrieved from {@link #getStatusKey()}.
     *
     * @return parameters for status key
     */
    public List<Serializable> getStatusParameters()
    {
        return statusParameters;
    }
}
