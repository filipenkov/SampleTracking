package com.atlassian.gadgets.renderer.internal;

import java.net.URI;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.inject.Injector;
import com.google.inject.Provider;

import org.apache.shindig.gadgets.GadgetContext;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetFeatureRegistry;
import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.apache.shindig.gadgets.variables.VariableSubstituter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.GadgetRequestContext.Builder.gadgetRequestContext;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecFactoryImplTest
{
    @Mock GadgetSpecFactory shindigFactory;
    @Mock VariableSubstituter substituter;
    @Mock GadgetFeatureRegistry gadgetFeatureRegistry;
    @Mock Provider<Injector> provider;
    @Mock Injector injector;
    @Mock ApplicationProperties applicationProperties;

    GadgetSpecFactoryImpl gadgetSpecFactory;
    GadgetRequestContext gadgetRequestContext;

    @Before
    public void setUp() throws GadgetException
    {
        when(injector.getInstance(org.apache.shindig.gadgets.GadgetSpecFactory.class)).thenReturn(shindigFactory);
        when(injector.getInstance(VariableSubstituter.class)).thenReturn(substituter);
        when(injector.getInstance(GadgetFeatureRegistry.class)).thenReturn(gadgetFeatureRegistry);
        when(provider.get()).thenReturn(injector);
        when(applicationProperties.getBaseUrl()).thenReturn("http://www.example.com");
        gadgetSpecFactory = new GadgetSpecFactoryImpl(provider, applicationProperties);
        gadgetRequestContext = gadgetRequestContext().build();
    }

    @Test
    public void testGetGadgetSpec() throws GadgetException
    {
        String gadgetTitle = "Gadget Title";
        org.apache.shindig.gadgets.spec.GadgetSpec shindigGadgetSpec = mock(org.apache.shindig.gadgets.spec.GadgetSpec.class);
        ModulePrefs modulePrefs = mock(ModulePrefs.class);
        when(shindigGadgetSpec.getModulePrefs()).thenReturn(modulePrefs);
        when(modulePrefs.getTitle()).thenReturn(gadgetTitle);
        when(shindigFactory.getGadgetSpec(isA(GadgetContext.class))).thenReturn(shindigGadgetSpec);
        when(substituter.substitute(isA(GadgetContext.class),eq(shindigGadgetSpec))).thenReturn(shindigGadgetSpec);
        gadgetSpecFactory.getGadgetSpec(URI.create("http://www.example.com/monkey.xml"),
                gadgetRequestContext);

        GadgetSpec gadgetSpec = gadgetSpecFactory.getGadgetSpec(URI.create("http://www.example.com/monkey.xml"),
                gadgetRequestContext);
        assertEquals(gadgetTitle,gadgetSpec.getTitle());
    }

    @Test(expected=GadgetParsingException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void assertThatGadgetExceptionIsCaughtAndWrappedInGadgetParsingException() throws GadgetException
    {
        when(shindigFactory.getGadgetSpec(isA(GadgetContext.class))).thenThrow(new GadgetException(GadgetException.Code.INTERNAL_SERVER_ERROR));
        gadgetSpecFactory.getGadgetSpec(URI.create("http://www.example.com/monkey.xml"),
                gadgetRequestContext);
    }
}
