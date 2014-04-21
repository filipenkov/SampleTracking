package com.atlassian.gadgets.plugins;

/**
 * A listener for gadget spec modules that are enabled and disabled in plugins.
 */
public interface PluginGadgetSpecEventListener
{
    /**
     * Called when a gadget spec is enabled from a plugin.
     *
     * @param pluginGadgetSpec the gadget spec that was enabled.  Must not be {@code null}, or a {@code
     *                         NullPointerException} will be thrown.
     * @throws NullPointerException if {@code pluginGadgetSpec} is {@code null}
     */
    void pluginGadgetSpecEnabled(PluginGadgetSpec pluginGadgetSpec);

    /**
     * Called when a gadget spec is disabled from a plugin.
     *
     * @param pluginGadgetSpec the gadget spec that was enabled.  Must not be {@code null}, or a {@code
     *                         NullPointerException} will be thrown.
     * @throws NullPointerException if {@code pluginGadgetSpec} is {@code null}
     */
    void pluginGadgetSpecDisabled(PluginGadgetSpec pluginGadgetSpec);
}
