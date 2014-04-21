package com.atlassian.gadgets;

/**
 * Models permission and validation checks on gadget specs.
 */
public interface GadgetSpecUrlChecker
{
    /**
     * Returns normally if the specified gadget spec is permitted to render.
     * @param gadgetSpecUri the gadget spec to render
     * @throws GadgetSpecUriNotAllowedException if the URI is not allowed to render
     * @throws NullPointerException if {@code gadgetSpecUri} is null
     */
    void assertRenderable(String gadgetSpecUri);
}
