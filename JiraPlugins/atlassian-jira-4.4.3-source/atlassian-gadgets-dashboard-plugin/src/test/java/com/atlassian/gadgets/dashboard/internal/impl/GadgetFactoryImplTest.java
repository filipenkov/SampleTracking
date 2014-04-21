package com.atlassian.gadgets.dashboard.internal.impl;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.internal.GadgetFactory;
import com.atlassian.gadgets.dashboard.spi.GadgetStateFactory;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static com.atlassian.gadgets.GadgetState.gadget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class GadgetFactoryImplTest
{
    @Mock GadgetStateFactory stateFactory;
    @Mock GadgetSpecFactory specFactory;
    
    GadgetFactory factory;
    GadgetRequestContext gadgetRequestContext;
    
    @Before
    public void setUp()
    {
        factory = new GadgetFactoryImpl(stateFactory, specFactory, null, null);
        gadgetRequestContext = gadgetRequestContext().build();
    }
    
    @Test
    public void assertThatCanCreateGadget() throws GadgetParsingException, URISyntaxException
    {
        GadgetId gadgetId = GadgetId.valueOf("1000");
        String gadgetUrl = "http://www.example.com/monkey.xml";
        String gadgetTitle = "Dancing Monkey";
        GadgetSpec spec = GadgetSpec.gadgetSpec(URI.create(gadgetUrl)).title(gadgetTitle).build();

        when(stateFactory.createGadgetState(URI.create(gadgetUrl)))
            .thenReturn(gadget(gadgetId).specUri(gadgetUrl).build());
        when(specFactory.getGadgetSpec(isA(GadgetState.class), isA(GadgetRequestContext.class))).thenReturn(spec);

        Gadget gadget = factory.createGadget(gadgetUrl, gadgetRequestContext);
        
        assertThat(gadget.getId(), is(equalTo(gadgetId)));
        assertThat(gadget.getTitle(), is(equalTo(gadgetTitle)));
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void assertThatIllegalUrlThrowsInvalidGadgetSpecUrlException()
    {
        factory.createGadget("invalid url", gadgetRequestContext);
    }
}
