package com.atlassian.upm.impl;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.upm.Configuration;
import com.atlassian.upm.ConfigurationStore;
import com.atlassian.upm.PluginConfiguration;
import com.atlassian.upm.PluginModuleConfiguration;
import com.atlassian.upm.test.MapBackedPluginSettings;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginSettingsConfigurationStoreTest
{
    ConfigurationStore configurationStore;
    @Mock PluginSettingsFactory pluginSettingsFactory;
    private Map<String, String> settingsMap;
    private Configuration config;

    @Before
    public void setup()
    {
        settingsMap = new HashMap<String, String>();
        when(pluginSettingsFactory.createGlobalSettings()).thenReturn(new MapBackedPluginSettings(settingsMap));
        configurationStore = new PluginSettingsConfigurationStore(pluginSettingsFactory);

        Collection<PluginModuleConfiguration> pluginModuleConfigurations = new ArrayList<PluginModuleConfiguration>();
        pluginModuleConfigurations.add(new PluginModuleConfiguration("This is a complete key", false, "Test Plugin Module Configuration"));
        Collection<PluginConfiguration> pluginConfigurations = new ArrayList<PluginConfiguration>();
        pluginConfigurations.add(new PluginConfiguration("Test Plugin Configuration Key", true, "Test Plugin Configuration", pluginModuleConfigurations));
        config = new Configuration("Test Configuration", "Test Comment", new Date(), pluginConfigurations);
    }

    @Test
    public void testSavingAndLoadingPreservesConfiguration() throws Exception
    {
        configurationStore.saveConfiguration(config);
        assertThat(config, is(equalsTo(configurationStore.getSavedConfiguration())));
    }

    @Test
    public void testLoadingUncompressedConfiguration() throws Exception
    {
        // Save an uncompressed configuration
        ObjectMapper mapper = new ObjectMapper(new MappingJsonFactory());
        addStringToMap(mapper.writeValueAsString(config));
        assertThat(config, is(equalsTo(configurationStore.getSavedConfiguration())));
    }

    @Test
    public void testCompressionReducingSizeOfStoredConfiguration() throws Exception
    {
        String uncompressedConfigStore = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("longConfiguration.txt"))).readLine();

        addStringToMap(uncompressedConfigStore);
        Configuration oldConfiguration = configurationStore.getSavedConfiguration();
        configurationStore.removeSavedConfiguration();
        configurationStore.saveConfiguration(oldConfiguration);
        String compressedConfigStore = (String) settingsMap.get("com.atlassian.upm.ConfigurationStore:configuration:upm_configuration");
        assertThat(compressedConfigStore.length(), lessThan(uncompressedConfigStore.length()));
    }

    @Test
    public void testCompressedConfigurationUnderBandanaFieldLengthLimit() throws Exception
    {
        // See UPM-1061
        String uncompressedConfigStore = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("longConfiguration.txt"))).readLine();

        addStringToMap(uncompressedConfigStore);
        Configuration oldConfiguration = configurationStore.getSavedConfiguration();
        configurationStore.removeSavedConfiguration();
        configurationStore.saveConfiguration(oldConfiguration);
        String compressedConfigStore = (String) settingsMap.get("com.atlassian.upm.ConfigurationStore:configuration:upm_configuration");
        assertThat(compressedConfigStore.length(), lessThan(100000));
    }

    @Test (expected = org.codehaus.jackson.JsonParseException.class)
    public void testLoadingUnparseableUncompressedJSONThrowsException() throws Exception
    {
        String uncompressedConfigStore = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("longConfiguration.txt"))).readLine().substring(2);
        addStringToMap(uncompressedConfigStore);
        configurationStore.getSavedConfiguration();
    }

    @Test (expected = org.codehaus.jackson.JsonParseException.class)
    public void testLoadingUnparseableCompressedJSONThrowsException() throws Exception
    {
        String uncompressedConfigStore = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("longConfiguration.txt"))).readLine();
        addStringToMap(uncompressedConfigStore);
        Configuration oldConfiguration = configurationStore.getSavedConfiguration();
        configurationStore.removeSavedConfiguration();
        configurationStore.saveConfiguration(oldConfiguration);
        String unparseableConfiguration = settingsMap.get("com.atlassian.upm.ConfigurationStore:configuration:upm_configuration").substring(2);
        addStringToMap(unparseableConfiguration);
        configurationStore.getSavedConfiguration();
    }

    @Test (expected = org.codehaus.jackson.map.JsonMappingException.class)
    public void testLoadingUnmappableUncompressedJSONThrowsException() throws Exception
    {
        String uncompressedConfigStore = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("longConfiguration.txt"))).readLine().replaceAll("completeKey", "key");
        addStringToMap(uncompressedConfigStore);
        configurationStore.getSavedConfiguration();
    }

    private void addStringToMap(String config)
    {
        // This will break if KEY_PREFIX or UPM_CONFIGURATION_KEY change, but is the easiest way to test loading an uncompressed configuration
        settingsMap.put("com.atlassian.upm.ConfigurationStore:configuration:upm_configuration", config);
    }

    static Matcher<? super Configuration> equalsTo(Configuration configuration)
    {
        return new ConfigurationMatcher(configuration);
    }

    static void reportMismatch(String name, Matcher<?> matcher, Object item, Description mismatchDescription, boolean firstMismatch)
    {
        if (!firstMismatch)
        {
            mismatchDescription.appendText(", ");
        }
        mismatchDescription.appendText(name).appendText(" ");
        matcher.describeMismatch(item, mismatchDescription);
    }

    private static class ConfigurationMatcher extends TypeSafeDiagnosingMatcher<Configuration>
    {
        private final Configuration configA;
        private final Matcher<? super String> title;
        private final Matcher<? super String> comment;
        private final Matcher<? super Date> date;

        ConfigurationMatcher(Configuration configuration)
        {
            configA = configuration;
            title = is(equalTo(configA.getTitle()));
            comment = is(equalTo(configA.getComment()));
            date = is(equalTo(configA.getSaveDate()));
        }

        @Override
        public boolean matchesSafely(Configuration configB, Description mismatchDescription)
        {
            boolean matches = true;
            mismatchDescription.appendText("{");
            if (!title.matches(configB.getTitle()))
            {
                reportMismatch("title", title, configB.getTitle(), mismatchDescription, matches);
                matches = false;
            }
            if (!comment.matches(configB.getComment()))
            {
                reportMismatch("comment", comment, configB.getComment(), mismatchDescription, matches);
                matches = false;
            }
            if (!date.matches(configB.getSaveDate()))
            {
                reportMismatch("saveDate", date, configB.getSaveDate(), mismatchDescription, matches);
                matches = false;
            }
            Iterator<PluginConfiguration> configAPluginConfigurations = configA.getPlugins().iterator();
            Iterator<PluginConfiguration> configBPluginConfigurations = configB.getPlugins().iterator();

            while (configAPluginConfigurations.hasNext() && configBPluginConfigurations.hasNext())
            {
                PluginConfigurationMatcher matcher = new PluginConfigurationMatcher(configAPluginConfigurations.next());
                PluginConfiguration pluginB = configBPluginConfigurations.next();
                if (!matcher.matches(pluginB))
                {
                    reportMismatch("pluginConfiguration", matcher, pluginB, mismatchDescription, matches);
                    matches = false;
                }
            }
            
            Matcher<? super Boolean> pluginsRemaining = is(equalTo(configAPluginConfigurations.hasNext()));
            if (!pluginsRemaining.matches(configBPluginConfigurations.hasNext()))
            {
                reportMismatch("pluginConfigurationListSizeDifferent", pluginsRemaining, configBPluginConfigurations.hasNext(), mismatchDescription, matches);
                matches = false;
            }

            mismatchDescription.appendText("}");
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText("{title ")
                       .appendDescriptionOf(title)
                       .appendText(", comment ")
                       .appendDescriptionOf(comment)
                       .appendText(", date ")
                       .appendDescriptionOf(date);
            for(PluginConfiguration pluginConfiguration : configA.getPlugins())
            {
                description.appendText(", pluginConfiguration ");
                PluginConfigurationMatcher matcher = new PluginConfigurationMatcher(pluginConfiguration);
                description.appendDescriptionOf(matcher);
            }
            description.appendText("}");
        }
    }

    private static class PluginConfigurationMatcher extends TypeSafeDiagnosingMatcher<PluginConfiguration>
    {

        private final PluginConfiguration pluginConfigA;
        private final Matcher<? super String> key;
        private final Matcher<? super Boolean> enabled;
        private final Matcher<? super String> name;

        PluginConfigurationMatcher(PluginConfiguration pluginConfiguration)
        {
            pluginConfigA = pluginConfiguration;
            key = is(equalTo(pluginConfigA.getKey()));
            enabled = is(equalTo(pluginConfigA.isEnabled()));
            name = is(equalTo(pluginConfigA.getName()));
        }

        @Override
        protected boolean matchesSafely(PluginConfiguration pluginConfigB, Description mismatchDescription)
        {
            boolean matches = true;
            mismatchDescription.appendText("{");
            if (!key.matches(pluginConfigB.getKey()))
            {
                reportMismatch("key", key, pluginConfigB.getKey(), mismatchDescription, matches);
                matches = false;
            }
            if (!enabled.matches(pluginConfigB.isEnabled()))
            {
                reportMismatch("enabled", enabled, pluginConfigB.isEnabled(), mismatchDescription, matches);
                matches = false;
            }
            if (!name.matches(pluginConfigB.getName()))
            {
                reportMismatch("name", name, pluginConfigB.getName(), mismatchDescription, matches);
                matches = false;
            }
            Iterator<PluginModuleConfiguration> pluginConfigAModuleConfigurations = pluginConfigA.getModules().iterator();
            Iterator<PluginModuleConfiguration> pluginConfigBModuleConfigurations = pluginConfigB.getModules().iterator();

            while (pluginConfigAModuleConfigurations.hasNext() && pluginConfigBModuleConfigurations.hasNext())
            {
                PluginModuleConfigurationMatcher matcher = new PluginModuleConfigurationMatcher(pluginConfigAModuleConfigurations.next());
                PluginModuleConfiguration pluginModuleB = pluginConfigBModuleConfigurations.next();
                if (!matcher.matches(pluginModuleB))
                {
                    reportMismatch("pluginModuleConfiguration", matcher, pluginModuleB, mismatchDescription, matches);
                    matches = false;
                }
            }

            Matcher<? super Boolean> pluginsModulesRemaining = is(equalTo(pluginConfigAModuleConfigurations.hasNext()));
            if (!pluginsModulesRemaining.matches(pluginConfigBModuleConfigurations.hasNext()))
            {
                reportMismatch("pluginModuleConfigurationListSizeDifferent", pluginsModulesRemaining, pluginConfigBModuleConfigurations.hasNext(), mismatchDescription, matches);
                matches = false;
            }

            mismatchDescription.appendText("}");
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText("{key ")
                       .appendDescriptionOf(key)
                       .appendText(", enabled ")
                       .appendDescriptionOf(enabled)
                       .appendText(", name ")
                       .appendDescriptionOf(name);

            for(PluginModuleConfiguration pluginModuleConfiguration: pluginConfigA.getModules())
            {
                description.appendText(", pluginModuleConfiguration ");
                PluginModuleConfigurationMatcher matcher = new PluginModuleConfigurationMatcher(pluginModuleConfiguration);
                description.appendDescriptionOf(matcher);
            }

            description.appendText("}");
        }
    }

    private static class PluginModuleConfigurationMatcher extends TypeSafeDiagnosingMatcher<PluginModuleConfiguration>
    {
        private final PluginModuleConfiguration pluginModuleConfigA;
        private final Matcher<? super String> completeKey;
        private final Matcher<? super Boolean> enabled;
        private final Matcher<? super String> name;

        PluginModuleConfigurationMatcher(PluginModuleConfiguration pluginModuleConfiguration)
        {
            pluginModuleConfigA = pluginModuleConfiguration;
            completeKey = is(equalTo(pluginModuleConfigA.getCompleteKey()));
            enabled = is(equalTo(pluginModuleConfigA.isEnabled()));
            name = is(equalTo(pluginModuleConfigA.getName()));
        }

        @Override
        protected boolean matchesSafely(PluginModuleConfiguration pluginModuleConfigB, Description mismatchDescription)
        {
            boolean matches = true;
            mismatchDescription.appendText("{");
            if (!completeKey.matches(pluginModuleConfigB.getCompleteKey()))
            {
                reportMismatch("completeKey", completeKey, pluginModuleConfigB.getCompleteKey(), mismatchDescription, matches);
                matches = false;
            }
            if (!enabled.matches(pluginModuleConfigB.isEnabled()))
            {
                reportMismatch("enabled", enabled, pluginModuleConfigB.isEnabled(), mismatchDescription, matches);
                matches = false;
            }
            if (!name.matches(pluginModuleConfigB.getName()))
            {
                reportMismatch("name", name, pluginModuleConfigB.getName(), mismatchDescription, matches);
                matches = false;
            }

            mismatchDescription.appendText("}");
            return matches;
        }

        public void describeTo(Description description)
        {
            description.appendText("{completeKey ")
                       .appendDescriptionOf(completeKey)
                       .appendText(", enabled ")
                       .appendDescriptionOf(enabled)
                       .appendText(", name ")
                       .appendDescriptionOf(name)
                       .appendText("}");
        }
    }
}
