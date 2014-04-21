package com.atlassian.jira.config;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.easymock.MockType;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static com.atlassian.jira.mock.plugin.elements.MockResourceDescriptorBuilder.feature;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link com.atlassian.jira.config.DefaultFeatureManager}.
 *
 * @since v4.4
 */
public class TestDefaultFeatureManager extends ListeningTestCase
{
    private static final String CORE_ENABLED_FEATURE_KEY = "enabled-feature";
    private static final String CORE_DISABLED_FEATURE_KEY = "disabled-feature";

    @Mock(MockType.NICE)
    private PluginAccessor mockAccessor;

    @Mock(MockType.NICE)
    private EventPublisher mockEventPublisher;

    @Before
    public void initMocks()
    {
        EasyMockAnnotations.initMocks(this);
    }

    @Test
    public void shouldHandleExistingAndNotExistingFeatureKeys()
    {
        EasyMockAnnotations.replayMocks(this);
        final FeatureManager tested = new DefaultFeatureManager(mockAccessor, fromMap(ImmutableMap.of(
                "key1", "true",
                "key2", "value2"
        )));
        assertTrue(tested.isEnabled("key1"));
        assertFalse("Value different than 'true' should be treated as false", tested.isEnabled("key2"));
        assertFalse("Not existing value should be treated as false", tested.isEnabled("key3"));
    }

    @Test
    public void shouldHandleNonStringsAsFalse()
    {
        EasyMockAnnotations.replayMocks(this);
        final Properties props = new Properties();
        props.put("key1", new Object());
        props.put("key2", Boolean.TRUE); // yes, that as well
        final FeatureManager tested = new DefaultFeatureManager(mockAccessor, asContainer(props));
        assertFalse("Non strings should be treated as false", tested.isEnabled("key1"));
        assertFalse("Non strings should be treated as false", tested.isEnabled("key2"));
    }

    @Test
    public void shouldHandleCoreFatures()
    {
        EasyMockAnnotations.replayMocks(this);
        final FeatureManager tested = new DefaultFeatureManager(mockAccessor, fromMap(ImmutableMap.of(
                "com.atlassian.jira.config.CoreFeatures.ON_DEMAND", "true"
        )));
        assertTrue(tested.isEnabled(CoreFeatures.ON_DEMAND));
    }

    @Test
    public void shouldPickUpFeaturesFromPlugins()
    {
        final Map<String,String> props = ImmutableMap.of(
                "key1", "true",
                "key2", "false"
        );
        final Plugin plugin = new MockPlugin("Test", "test-plugin", new PluginInformation())
                .addResourceDescriptor(feature("some-features", "/features.properties"), serialize(props));
        expect(mockAccessor.getEnabledPlugins()).andReturn(ImmutableList.of(plugin)).anyTimes();
        EasyMockAnnotations.replayMocks(this);
        final FeatureManager tested = new DefaultFeatureManager(mockAccessor, mockEventPublisher);
        assertTrue(tested.isEnabled("key1"));
        assertFalse(tested.isEnabled("key2"));
        assertFalse(tested.isEnabled("key3"));
    }

    @Test
    public void pluginFeaturesShouldOverrideCore()
    {
        final Map<String,String> props = ImmutableMap.of(
                CORE_ENABLED_FEATURE_KEY, "false",
                CORE_DISABLED_FEATURE_KEY, "true"
        );
        final Plugin plugin = new MockPlugin("Test", "test-plugin", new PluginInformation())
                .addResourceDescriptor(feature("some-features", "/features.properties"), serialize(props));
        expect(mockAccessor.getEnabledPlugins()).andReturn(ImmutableList.of(plugin)).anyTimes();
        EasyMockAnnotations.replayMocks(this);
        final FeatureManager tested = new DefaultFeatureManager(mockAccessor, mockEventPublisher);
        assertFalse(tested.isEnabled(CORE_ENABLED_FEATURE_KEY));
        assertTrue(tested.isEnabled(CORE_DISABLED_FEATURE_KEY));
    }


    private DefaultFeatureManager.PropertiesContainer asContainer(Properties props)
    {
        return new DefaultFeatureManager.PropertiesContainer(ImmutableList.of(props));
    }

    private DefaultFeatureManager.PropertiesContainer fromMap(Map<String,String> values)
    {
        final Properties props = new Properties();
        props.putAll(values);
        return asContainer(props);
    }

    private static String serialize(Map<String, String> props)
    {
        final StringBuilder result = new StringBuilder();
        for (Map.Entry<String,String> entry : props.entrySet())
        {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        return result.delete(result.length()-1, result.length()).toString();
    }

}
