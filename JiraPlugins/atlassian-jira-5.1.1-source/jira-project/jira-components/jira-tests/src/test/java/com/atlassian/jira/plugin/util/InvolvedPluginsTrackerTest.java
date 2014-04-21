package com.atlassian.jira.plugin.util;

import com.atlassian.jira.mock.plugin.MockPlugin;
import com.atlassian.jira.mock.plugin.elements.MockResourceDescriptorBuilder;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptorImpl;
import com.atlassian.plugin.PluginInformation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 */
public class InvolvedPluginsTrackerTest
{

    private MockPlugin pluginA;
    private MockPlugin pluginB;

    @Before
    public void setUp() throws Exception
    {
        PluginInformation pluginInformationA = new PluginInformation();
        pluginInformationA.setVersion("1.0");
        PluginInformation pluginInformationB = new PluginInformation();
        pluginInformationB.setVersion("1.0");
        pluginA = new MockPlugin("nameA", "keyA", pluginInformationA);
        pluginB = new MockPlugin("nameB", "keyB", pluginInformationB);
    }

    @Test
    public void testTrackInvolvedPlugin() throws Exception
    {
        InvolvedPluginsTracker tracker = new InvolvedPluginsTracker();


        assertEquals(0, tracker.getInvolvedPluginKeys().size());

        tracker.trackInvolvedPlugin(pluginA);

        assertEquals(1, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertFalse(tracker.isPluginInvolved(pluginB));

        tracker.trackInvolvedPlugin(pluginB);

        assertEquals(2, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertTrue(tracker.isPluginInvolved(pluginB));

        tracker.trackInvolvedPlugin(pluginA); // its a set

        assertEquals(2, tracker.getInvolvedPluginKeys().size());
        assertTrue(tracker.isPluginInvolved(pluginA));
        assertTrue(tracker.isPluginInvolved(pluginB));

        tracker.clear();
        assertEquals(0, tracker.getInvolvedPluginKeys().size());
        assertFalse(tracker.isPluginInvolved(pluginA));
        assertFalse(tracker.isPluginInvolved(pluginB));
    }

    @Test
    public void testContainsModuleDescriptor()
    {
        pluginA.addModuleDescriptor(new LanguageModuleDescriptorImpl(null, null));

        InvolvedPluginsTracker tracker = new InvolvedPluginsTracker();

        assertEquals(true, tracker.isPluginWithModuleDescriptor(pluginA, LanguageModuleDescriptor.class));
        assertEquals(false, tracker.isPluginWithModuleDescriptor(pluginB, LanguageModuleDescriptor.class));

    }


    @Test
    public void testContainsResourceDescriptors()
    {
        pluginA.addResourceDescriptor(MockResourceDescriptorBuilder.i18n("name", "location"));

        InvolvedPluginsTracker tracker = new InvolvedPluginsTracker();

        assertEquals(true, tracker.isPluginWithResourceType(pluginA, "i18n"));
        assertEquals(false, tracker.isPluginWithResourceType(pluginB, "i18n"));

    }
    @Test
    public void testHashCodeStability()
    {
        InvolvedPluginsTracker tracker = new InvolvedPluginsTracker();

        int emptyHC = tracker.hashCode();

        tracker.trackInvolvedPlugin(pluginA); // add A
        int justA = tracker.hashCode();

        assertTrue(emptyHC != justA);

        tracker.trackInvolvedPlugin(pluginA); // added twice
        assertEquals(justA, tracker.hashCode());

        tracker.trackInvolvedPlugin(pluginB); // add B
        int aAndB = tracker.hashCode();
        assertTrue(emptyHC != aAndB);
        assertTrue(justA != aAndB);

        tracker.clear(); // clear
        assertEquals(emptyHC, tracker.hashCode());

        tracker.trackInvolvedPlugin(pluginB); // add B back (different order)
        tracker.trackInvolvedPlugin(pluginA); // add A back
        assertEquals(aAndB, tracker.hashCode());

        tracker.clear(); // clear
        assertEquals(emptyHC, tracker.hashCode());

        tracker.trackInvolvedPlugin(pluginA); // add A back
        assertEquals(justA, tracker.hashCode());

        // add different version of B
        pluginB.getPluginInformation().setVersion("1.1");
        tracker.trackInvolvedPlugin(pluginB); // add 1.1 of B
        int aAndBv2 = tracker.hashCode();
        assertTrue(emptyHC != aAndBv2);
        assertTrue(justA != aAndBv2);
        assertTrue(aAndB != aAndBv2);

    }


}
