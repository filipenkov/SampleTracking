package com.atlassian.gadgets.view;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;

/**
 * A factory which provides a way to create {@link ViewComponent}s for gadgets.  A host
 * application should use this for embedding individual gadgets in the desired locations by
 * creating a view and then calling the {@link ViewComponent#writeTo} method.
 */
public interface GadgetViewFactory
{
    /**
     * <p>Returns a {@code ViewComponent} that will render the {@code GadgetState}, customizing the view based
     * on the {@code Locale} specified in the {@code GadgetRequestContext} and the desired {@code ViewType}.</p>
     *
     * <p>This can be used by host applications to place gadgets anywhere they choose.</p>
     *
     * @param state state of the gadget to be rendered by the {@code ViewComponent}
     * @param view desired {@code View} to use when rendering the gadget
     * @param gadgetRequestContext context info for this request
     * to make changes to the gadget state; {@code false} if the gadget is read-only for the current viewer
     * @return a {@code ViewComponent} that will render the {@code GadgetState}
     * @throws GadgetParsingException thrown if there is a problem parsing the gadget spec
     * @throws GadgetRenderingException thrown if the gadget does not define a &lt;Content&gt; section for the
     * {@code view}
     * @deprecated as of 2.0, use {@link #createGadgetView(com.atlassian.gadgets.GadgetState, ModuleId, View,com.atlassian.gadgets.GadgetRequestContext)}
     */
    @Deprecated
    ViewComponent createGadgetView(GadgetState state, View view,
                                   GadgetRequestContext gadgetRequestContext)
            throws GadgetParsingException, GadgetRenderingException;

    /**
     * Returns a {@code ViewComponent} that will render the {@code GadgetState}, customizing the view based on the
     * {@code Locale} specified in the {@code GadgetRequestContext} and the desired {@code ViewType}.
     * <p/>
     * This can be used by host applications to place gadgets anywhere they choose.
     *
     * @param state                state of the gadget to be rendered by the {@code ViewComponent}
     * @param moduleId             the moduleId to use in the gadget view.  Should be unique for the HTML page that the
     *                             gadget will be rendered in
     * @param view                 desired {@code View} to use when rendering the gadget
     * @param gadgetRequestContext context info for this request to make changes to the gadget state; {@code false} if
     *                             the gadget is read-only for the current viewer
     * @return a {@code ViewComponent} that will render the {@code GadgetState}
     * @throws GadgetParsingException   thrown if there is a problem parsing the gadget spec
     * @throws GadgetRenderingException thrown if the gadget does not define a &lt;Content&gt; section for the {@code
     *                                  view}
     * @since 2.0
     */
    ViewComponent createGadgetView(GadgetState state,
                                   ModuleId moduleId,
                                   View view,
                                   GadgetRequestContext gadgetRequestContext)
        throws GadgetParsingException, GadgetRenderingException;

    /**
     * Returns {@code true} if the gadget represented by {@code state} can be rendered with the given {@code ViewType} for
     * the {@code locale} specified in the {@code GadgetRequestContext}.
     *
     * @param state state of the gadget to check if we can render
     * @param viewType view type to check the gadget can be rendered in
     * @param gadgetRequestContext the context of this request
     * @return {@code true} if the gadget can be rendered with the {@code view} in the {@code locale}, {@code false}
     * otherwise
     * @throws GadgetParsingException thrown if there is a problem parsing the gadget spec
     */
    boolean canRenderInViewType(GadgetState state, ViewType viewType, GadgetRequestContext gadgetRequestContext)
            throws GadgetParsingException;
}
