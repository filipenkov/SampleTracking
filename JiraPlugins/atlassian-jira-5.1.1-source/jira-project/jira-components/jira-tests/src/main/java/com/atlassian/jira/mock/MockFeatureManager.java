package com.atlassian.jira.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.plugin.profile.DarkFeatures;

import java.util.*;

public class MockFeatureManager implements FeatureManager
{
    private DarkFeatures darkFeatures;
    private Set<String> enabledFeatures;

    public MockFeatureManager()
    {
        darkFeatures = new DarkFeatures(Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET);
        enabledFeatures = new HashSet<String>();
    }

    @Override
    public boolean isEnabled(String featureKey)
    {
        return enabledFeatures.contains(featureKey);
    }

    @Override
    public boolean isEnabled(CoreFeatures coreFeature)
    {
        return isEnabled(coreFeature.featureKey());
    }

    public void enable(CoreFeatures coreFeature)
    {
        enabledFeatures.add(coreFeature.featureKey());
    }

    @Override
    public Set<String> getEnabledFeatureKeys()
    {
        return enabledFeatures;
    }

    @Override
    public DarkFeatures getDarkFeatures()
    {
        return darkFeatures;
    }

    @Override
    public void enableUserDarkFeature(User user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().add(feature);
    }

    @Override
    public void disableUserDarkFeature(User user, String feature)
    {
        darkFeatures.getUserEnabledFeatures().remove(feature);
    }

    @Override
    public void enableSiteDarkFeature(String feature)
    {
        darkFeatures.getSiteEnabledFeatures().add(feature);
    }

    @Override
    public void disableSiteDarkFeature(String feature)
    {
        darkFeatures.getSiteEnabledFeatures().remove(feature);
    }
}
