package com.atlassian.instrumentation;

import java.io.File;

/**
 * This configures information about the registry.  It allows the externalisation of how the registry should act
 */
public interface RegistryConfiguration
{

    /**
     * @return the name to use for this registry
     */
    String getRegistryName();

    /**
     * @return true if the CPU costs should be collected.
     */
    boolean isCPUCostCollected();

    /**
     * Related instrumentation code can want to store file on disk in home directory.   This tells it where it can place those
     * files for this registry.
     *
     * @return a directory related to this registry
     */
    File getRegistryHomeDirectory();

}
