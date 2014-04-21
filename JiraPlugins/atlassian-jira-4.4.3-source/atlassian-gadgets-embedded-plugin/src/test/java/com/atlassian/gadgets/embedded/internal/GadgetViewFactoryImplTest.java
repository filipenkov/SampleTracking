package com.atlassian.gadgets.embedded.internal;

import java.net.URI;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.gadgets.view.GadgetRenderingException;
import com.atlassian.gadgets.view.GadgetViewFactory;
import com.atlassian.gadgets.view.ModuleId;
import com.atlassian.gadgets.view.RenderedGadgetUriBuilder;
import com.atlassian.gadgets.view.View;
import com.atlassian.gadgets.view.ViewType;

import com.google.common.collect.ImmutableSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.gadgets.GadgetState.gadget;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class GadgetViewFactoryImplTest
{
    ViewType fakeViewType;
    GadgetSpec gadgetSpec;
    @Mock GadgetSpecFactory gadgetSpecFactory;
    @Mock RenderedGadgetUriBuilder renderedUriBuilder;
    GadgetRequestContext gadgetRequestContext = GadgetRequestContext.NO_CURRENT_REQUEST;
    URI SPEC_URI = URI.create("http://gadget/url");
    GadgetState gadgetState = gadget(GadgetId.valueOf("1")).specUri(SPEC_URI).build();
    
    GadgetViewFactory gadgetViewFactory;

    @Before
    public void setUp()
    {
        fakeViewType = ViewType.createViewType("fakeViewType");
        gadgetViewFactory = new GadgetViewFactoryImpl(gadgetSpecFactory, renderedUriBuilder);
        gadgetSpec = GadgetSpec.gadgetSpec(SPEC_URI)
            .viewsNames(ImmutableSet.<String>of("default"))
            .build();
        when(gadgetSpecFactory.getGadgetSpec(gadgetState, gadgetRequestContext)).thenReturn(gadgetSpec);
    }

    @After
    public void tearDown()
    {
        ViewType.removeViewType(fakeViewType);
    }

    @Test(expected = GadgetRenderingException.class)
    public void assertThatCreateGadgetViewThrowsGadgetRenderingExceptionForUnsupportedViewType()
    {
        gadgetViewFactory.createGadgetView(gadgetState,
                                           ModuleId.valueOf(1),
                                           new View.Builder().viewType(fakeViewType).build(),
                                           gadgetRequestContext);
    }

    @Test
    public void assertThatGadgetCannotBeRenderedInUnsupportedViewType()
    {
        assertFalse(gadgetViewFactory.canRenderInViewType(gadgetState, fakeViewType, gadgetRequestContext));
    }

    @Test
    public void assertThatGadgetCanBeRenderedInDefaultViewType()
    {
        assertTrue(gadgetViewFactory.canRenderInViewType(gadgetState, ViewType.DEFAULT, gadgetRequestContext));
    }
}
