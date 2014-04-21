package com.atlassian.gadgets.embedded.internal;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.view.GadgetRenderingException;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewComponent;
import com.atlassian.gadgets.view.ViewType;

/**
 * Default implementation of {@code GadgetViewFactory}. Uses
 * {@code RenderedGadgetUriBuilder} to render gadget views.
 */
public class GadgetViewFactoryImpl implements GadgetViewFactory
{
    private final RenderedGadgetUriBuilder renderedUriBuilder;
    private final GadgetSpecFactory specFactory;

    public GadgetViewFactoryImpl(GadgetSpecFactory specFactory, RenderedGadgetUriBuilder renderedUriBuilder)
    {
        this.specFactory = specFactory;
        this.renderedUriBuilder = renderedUriBuilder;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated as of 2.0, use {@link com.atlassian.gadgets.view.GadgetViewFactory#createGadgetView(com.atlassian.gadgets.GadgetState,com.atlassian.gadgets.view.ModuleId,com.atlassian.gadgets.view.View,com.atlassian.gadgets.GadgetRequestContext)}
     */
    @Deprecated
    public ViewComponent createGadgetView(GadgetState state,
                                          View view,
                                          GadgetRequestContext gadgetRequestContext)
        throws GadgetParsingException, GadgetRenderingException
    {
        return createGadgetView(state, ModuleId.valueOf(state.getId().value()), view, gadgetRequestContext);
    }

    public ViewComponent createGadgetView(GadgetState state,
                                          ModuleId moduleId,
                                          View view,
                                          GadgetRequestContext gadgetRequestContext)
        throws GadgetParsingException, GadgetRenderingException
    {
        GadgetSpec spec = fetchGadgetSpec(state, gadgetRequestContext);
        if (!canRenderInViewType(spec, view.getViewType()))
        {
            throw new GadgetRenderingException("Gadget does not define a '" + view.getViewType() + "' view", state);
        }

        final String renderedUrl =
            renderedUriBuilder
                .build(state, moduleId, view, gadgetRequestContext)
                .toString();
        return new GadgetViewComponent(moduleId, view.getViewType(), spec, renderedUrl);
    }

    public boolean canRenderInViewType(GadgetState state, ViewType viewType, GadgetRequestContext gadgetRequestContext)
    {
        return canRenderInViewType(fetchGadgetSpec(state, gadgetRequestContext), viewType);
    }

    private boolean canRenderInViewType(GadgetSpec spec, ViewType viewType)
    {
        return spec.supportsViewType(viewType);
    }

    private GadgetSpec fetchGadgetSpec(GadgetState state, GadgetRequestContext gadgetRequestContext)
    {
        return specFactory.getGadgetSpec(state, gadgetRequestContext);
    }
}
