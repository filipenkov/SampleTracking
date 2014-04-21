package com.atlassian.crowd.embedded.api;

/**
 * Simple object to store synchronisation information for synchronisable directories
 */
public class DirectorySynchronisationInformation
{
    private final DirectorySynchronisationRoundInformation lastRound;
    private final DirectorySynchronisationRoundInformation activeRound;

    public DirectorySynchronisationInformation(DirectorySynchronisationRoundInformation lastRound, DirectorySynchronisationRoundInformation activeRound)
    {
        this.lastRound  = lastRound;
        this.activeRound = activeRound;
    }

    /**
     * Information of the last completed synchronisation or null if directory has never been synchronised.
     *
     * @return information of the last completed synchronisation
     */
    public DirectorySynchronisationRoundInformation getLastRound()
    {
        return lastRound;
    }

    /**
     * Information of the currently running synchronisation or null if directory is not being synchronised.
     *
     * @return information of the currently running synchronisation
     */
    public DirectorySynchronisationRoundInformation getActiveRound()
    {
        return activeRound;
    }

    /**
     * Is the directory currently synchronising.
     *
     * @return true if the directory is currently synchronising
     */
    public boolean isSynchronising()
    {
        return activeRound != null;
    }
}
