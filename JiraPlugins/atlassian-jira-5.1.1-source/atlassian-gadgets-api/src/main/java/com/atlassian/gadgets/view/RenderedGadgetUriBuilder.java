package com.atlassian.gadgets.view;

import java.net.URI;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;

/**
 * Builds URLs to the rendered gadget.
 */
public interface RenderedGadgetUriBuilder
{
    /**
     * Returns the URL to the rendered gadget.
     *
     * @param gadgetState          gadget used to insert the gadget's spec URL
     * @param view                 the view to render the gadget in
     * @param gadgetRequestContext context for this request
     * @return URL to the rendered gadget
     * @deprecated as of 2.0, use {@link #build(GadgetState, ModuleId, View, GadgetRequestContext)}
     */
    @Deprecated
    URI build(GadgetState gadgetState, View view, GadgetRequestContext gadgetRequestContext);

    /**
     * Returns the URL to the rendered gadget.
     *
     * @param gadgetState          gadget used to insert the gadget's spec URL
     * @param moduleId             the moduleId to use in the URL.  Should be unique for the HTML page that the gadget
     *                             will be rendered in.
     * @param view                 the view to render the gadget in
     * @param gadgetRequestContext context for this request
     * @return URL to the rendered gadget
     * @since 2.0
     */
    URI build(GadgetState gadgetState, ModuleId moduleId, View view, GadgetRequestContext gadgetRequestContext);
}
