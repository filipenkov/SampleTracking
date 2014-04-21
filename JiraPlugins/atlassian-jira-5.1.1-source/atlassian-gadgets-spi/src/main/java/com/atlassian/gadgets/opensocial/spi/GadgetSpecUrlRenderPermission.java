package com.atlassian.gadgets.opensocial.spi;

import com.atlassian.gadgets.Vote;

/**
 * <p>Used to check if a gadget spec URI is allowed to render on a dashboard.
 * This may be implemented in a number of ways:</p>
 * <ul>
 *   <li>for static dashboards, always return {@code Vote.DENY} unless the spec
 *       is on a whitelist;</li>
 *   <li>for dashboards with the directory plugin, an implementation might check
 *       that the gadget is in the directory, thus allowing admins to remove
 *       malicious gadgets from all dashboards (the directory plugin has a
 *       {@code GadgetSpecUrlRenderPermissionImpl} that does this);</li>
 *   <li>check against a user-specific white/black list of allowable gadget spec URIs.</li>
 * </ul>
 * <p>Syntactically invalid URIs must never be allowed to render.</p>
 * 
 * @since 2.0
 */
public interface GadgetSpecUrlRenderPermission
{
    /**
     * <p>Returns the implementation's {@code Vote} on whether the specified gadget
     * spec should be allowed to render.</p>
     * 
     * <p>If {@code DENY} is returned, the gadget URI is explicitly forbidden to render, no matter what any other
     * implementations vote may be.</p>
     * 
     * <p>If {@code ALLOW} is returned, the gadget URI is explicitly allowed to render unless another implementation
     * explicitly denies the rendering permission.</p>
     *
     * <p>{@code PASS} will be returned if the implementation has no opinion on whether the URI may render.</p>
     * 
     * @param gadgetSpecUri the gadget spec URI to evaluate
     * @return this implementation's {@code Vote} on whether to render the gadget
     */
    Vote voteOn(String gadgetSpecUri);
}
