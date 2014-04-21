package com.atlassian.config.setup;

import java.util.List;

public interface SetupPersister
{
    /**
     * Different configuration constants for setting up Confluence.
     */
    String SETUP_TYPE_INITIAL = "initial";
    String SETUP_TYPE_INSTALL = "install"; // refers to quick setup
    String SETUP_TYPE_CUSTOM = "custom";
    /**
     * An instruction to tell the Setup to install demo content
     */
    String SETUP_INSTALL_DEMO_DATA = "demo";
    /**
     *  confluence bootstrapManager states
     */
    String SETUP_STATE_COMPLETE = "complete";

    List getUncompletedSteps();

    List getCompletedSteps();

    /**
     * @return the bootstrapManager type of the bootstrapManager process - initial, custom or install.
     */
    String getSetupType();

    void setSetupType(String setupType);

    /**
     * Ensures that the bootstrapManager is written to a complete state
     * by overriding all other remaining operations.
     */
    void finishSetup() throws SetupException;

    void progessSetupStep();

    String getCurrentStep();

    boolean isDemonstrationContentInstalled();

    void setDemonstrationContentInstalled();
}
