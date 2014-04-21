package com.atlassian.gadgets.publisher.spi;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;

/**
 * <p>Used to check if a gadget spec that has been loaded from a plugin should appear in the gadget browser.
 * This may be implemented in a number of ways:</p>
 * <ul>
 *   <li>checking a role parameter on the {@code PluginGadgetSpec} that is defined in the {@code ModuleDescriptor}</li>
 *   <li>a {@code showInDirectory} parameter from the {@code ModuleDescriptor}</li>
 *   <li>a way for administrators to only allow certain groups of users to add select groups of gadgets to their
 *       dashboard</li>
 * </ul>
 * 
 * @since 2.0
 */
public interface PluginGadgetSpecProviderPermission
{
    /**
     * <p>Returns the implementation's {@code Vote} on whether the specified gadget spec should be appear in the 
     * gadget browser.</p>
     * 
     * <p>If {@code DENY} is returned, the gadget should not appear, no matter what any other implementations vote
     * may be.</p>
     * 
     * <p>If {@code ALLOW} is returned, the gadget should appear in the gadget browser unless another implementation
     * explicitly denies its appearance.</p>
     *
     * <p>{@code PASS} will be returned if the implementation has no opinion on whether the gadget should show up in the
     * gadget browser.</p>
     * 
     * @param gadgetSpec the gadget spec found in a plugin that will be evaluated
     * @return this implementation's {@code Vote} on whether to the gadget should appear in the gadget browser
     */
    Vote voteOn(PluginGadgetSpec gadgetSpec);
}
