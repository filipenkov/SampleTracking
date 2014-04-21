package com.atlassian.jira.mock.upgrade;

import com.atlassian.jira.upgrade.UpgradeTask;

import java.util.Collection;
import java.util.Collections;

public class MockUpgradeTask implements UpgradeTask
{
    private final String version;
    private final String shortDescription;

    public MockUpgradeTask(final String version, final String shortDescription)
    {
        this.version = version;
        this.shortDescription = shortDescription;
    }

    public String getBuildNumber()
    {
        return version;
    }

    public String getShortDescription()
    {
        return shortDescription;
    }

    public void doUpgrade(boolean setupMode)
    {}

    public Collection<String> getErrors()
    {
        return Collections.emptyList();
    }

    @Override
    public String toString()
    {
        return "version: " + version + ", shortDesctription: " + shortDescription;
    }

    public String getClassName()
    {
        return "MockUpgradeTask" + version;
    }
}
