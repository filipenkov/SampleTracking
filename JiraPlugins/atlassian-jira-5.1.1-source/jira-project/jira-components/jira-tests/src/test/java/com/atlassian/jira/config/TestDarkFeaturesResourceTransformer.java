package com.atlassian.jira.config;

import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.profile.DarkFeatures;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith (ListeningMockitoRunner.class)
public class TestDarkFeaturesResourceTransformer
{
    @Mock
    FeatureManager featureManager;

    @Test
    public void checkJavascriptSyntaxWithNoFeaturesEnabled() throws Exception
    {
        mockDarkFeatures(Collections.<String>emptySet());
        assertThat(darkFeaturesResourceTransformer().getEnabledFeatureKeysAsJS(), equalTo("[]"));
    }

    @Test
    public void checkJavascriptSyntaxWithOneFeatureEnabled() throws Exception
    {
        mockDarkFeatures(Sets.newHashSet("feat_1"));
        assertThat(darkFeaturesResourceTransformer().getEnabledFeatureKeysAsJS(), equalTo("['feat_1']"));
    }

    @Test
    public void checkJavascriptSyntaxWithSeveralFeaturesEnabled() throws Exception
    {
        mockDarkFeatures(Sets.newLinkedHashSet(Arrays.asList("feat_1", "feat_2")));
        assertThat(darkFeaturesResourceTransformer().getEnabledFeatureKeysAsJS(), equalTo("['feat_1','feat_2']"));
    }

    private void mockDarkFeatures(Set<String> stringSet)
    {
        DarkFeatures darkFeatures = new DarkFeatures(stringSet, Collections.<String>emptySet(), Collections.<String>emptySet());
        when(featureManager.getDarkFeatures()).thenReturn(darkFeatures);
    }

    private DarkFeaturesResourceTransformer darkFeaturesResourceTransformer()
    {
        return new DarkFeaturesResourceTransformer(featureManager);
    }
}
