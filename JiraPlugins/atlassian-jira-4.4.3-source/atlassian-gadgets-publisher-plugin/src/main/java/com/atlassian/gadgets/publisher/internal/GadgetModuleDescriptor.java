package com.atlassian.gadgets.publisher.internal;

import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.util.validation.ValidationException;
import com.atlassian.plugin.util.validation.ValidationPattern;

import org.dom4j.Element;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Custom plugin module type for gadget plugin modules.  Gadget plugin modules allow plugins to make gadget XML spec
 * files available to the Atlassian Gadgets system.
 * <p/>
 * Use the gadget plugin module type within a plugin by declaring a {@code <gadget>} element inside its {@code
 * atlassian-plugin.xml}.  There are two required attributes, {@code key}, an identifier for the module that must be
 * unique across all modules (of any type) within the plugin, and {@code location}, which indicates the location of the
 * gadget spec file.  Other standard module attributes and child elements are also supported with the typical behavior,
 * including {@code name}, {@code i18n-name-key}, {@code disabled}, {@code description}, and {@code param}.
 * <p/>
 * There are two major categories of plugin gadgets, published gadgets and external gadgets.
 * <p/>
 * Published gadgets are gadget specs that are packaged as resources within the plugin.  They are served by the
 * Atlassian Gadgets Publisher plugin at a URL of the form {@code http://<hostname>[:<port>]/[<context>/]<path/to/gadget/location.xml}.
 * The {@code location} attribute of published gadgets should be a relative path to the gadget spec file within the
 * plugin.
 * <p/>
 * External gadgets are gadget specs that are hosted on an external web site.  Declaring external gadgets within a
 * plugin makes the application aware of these gadgets, so they can be displayed in a directory of available gadgets,
 * for example.  The {@code location} attribute of external gadgets should be the absolute URL of the gadget spec file,
 * beginning with {@code http} or {@code https}.  External gadgets served through protocols other than HTTP are
 * <em>not</em> supported.
 * <p/>
 * Examples:
 * <pre>
 * {@code
 * <gadget key="my-published-gadget" location="gadgets/my-published-gadget.xml"/>
 * <gadget key="my-external-gadget" location="http://gadgets.example.org/my-external-gadget.xml"/>
 * }
 * </pre>
 * This class is used by the plugin system and should not generally be directly instantiated by the application.
 */
public final class GadgetModuleDescriptor extends AbstractModuleDescriptor<PluginGadgetSpec>
{
    private PluginGadgetSpec pluginGadgetSpec;

    /**
     * Initializes this module descriptor from the specified XML element, parsed from {@code atlassian-plugins.xml}.
     *
     * @param plugin  the plugin that this module descriptor belongs to.  Must not be {@code null} or a {@code
     *                NullPointerException} will be thrown.
     * @param element the XML element specifying this gadget module.  Must contain {@code key} and {@code location}
     *                attributes, or a {@code ValidationException} will be thrown.  Must not be {@code null}, or a
     *                {@code NullPointerException} will be thrown.
     * @throws NullPointerException if any argument is {@code null}.
     * @throws PluginParseException if an error occurs while trying to parse the specified element
     * @throws ValidationException  if a required attribute is missing from the specified element
     */
    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(checkNotNull(plugin, "plugin"), checkNotNull(element, "element"));
        String location = element.attributeValue("location");
        pluginGadgetSpec = new PluginGadgetSpec(plugin, getKey(), location, getParams());
    }

    /**
     * Returns the {@code PluginGadgetSpec} created for this module descriptor.  This method must not be called before
     * the {@link #init(com.atlassian.plugin.Plugin, org.dom4j.Element)} method completes, or an {@code
     * IllegalStateException} will be thrown.
     *
     * @return the {@code PluginGadgetSpec} created for this module descriptor.
     */
    @Override
    public PluginGadgetSpec getModule()
    {
        return pluginGadgetSpec;
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern.rule(test("@location").withError("The location is required"));
    }
}
