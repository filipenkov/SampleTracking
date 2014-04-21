package com.atlassian.instrumentation;

import java.io.File;

/**
 * A default for us mostly for testing
 */
class DefaultRegistryConfiguration implements RegistryConfiguration
{

    @Override
    public String getRegistryName()
    {
        return "default-registry";
    }

    @Override
    public boolean isCPUCostCollected()
    {
        return true;
    }

    @Override
    public File getRegistryHomeDirectory()
    {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
