package com.atlassian.gadgets.publisher.internal;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.util.validation.ValidationException;
import org.dom4j.Element;
import org.dom4j.tree.BaseElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetModuleDescriptorTest
{
    private static final String PLUGIN_KEY = "plugin.key";
    private static final String GADGET_KEY = "test-gadget";
    private static final String GADGET_LOCATION = "path/to/test-gadget.xml";

    @Mock Plugin plugin;
    Element descriptorElement;

    @Before
    public void setUp()
    {
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);

        descriptorElement = new BaseElement("gadget");
        descriptorElement.addAttribute("key", GADGET_KEY);
        descriptorElement.addAttribute("location", GADGET_LOCATION);
    }

    @Test
    public void testGadgetModuleDescriptorCreatesPluginGadgetSpecWithSpecifiedPluginAndLocation()
    {

        GadgetModuleDescriptor descriptor = new GadgetModuleDescriptor();
        descriptor.init(plugin, descriptorElement);

        PluginGadgetSpec spec = descriptor.getModule();
        assertEquals(spec.getKey(), new PluginGadgetSpec.Key(PLUGIN_KEY, GADGET_LOCATION));
        assertEquals(spec.getLocation(), GADGET_LOCATION);
    }

    @Test(expected = NullPointerException.class)
    public void testInitWithNullPluginThrowsNullPointerException()
    {
        new GadgetModuleDescriptor().init(null, descriptorElement);
    }

    @Test(expected = NullPointerException.class)
    public void testInitWithNullElementThrowsNullPointerException()
    {
        new GadgetModuleDescriptor().init(plugin, null);
    }

    @Test(expected = ValidationException.class)
    public void testInitWithMissingKeyThrowsValidationException()
    {
        descriptorElement.remove(descriptorElement.attribute("key"));
        new GadgetModuleDescriptor().init(plugin, descriptorElement);
    }

    @Test(expected = ValidationException.class)
    public void testInitWithMissingLocationThrowsValidationException()
    {
        descriptorElement.remove(descriptorElement.attribute("location"));
        new GadgetModuleDescriptor().init(plugin, descriptorElement);
    }
}
