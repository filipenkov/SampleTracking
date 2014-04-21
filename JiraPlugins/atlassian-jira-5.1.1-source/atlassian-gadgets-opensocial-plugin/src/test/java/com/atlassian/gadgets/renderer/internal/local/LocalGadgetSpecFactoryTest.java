package com.atlassian.gadgets.renderer.internal.local;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.LocalGadgetSpecProvider;

import com.google.common.collect.ImmutableSet;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.GadgetException;
import org.apache.shindig.gadgets.GadgetSpecFactory;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LocalGadgetSpecFactoryTest
{
    private static final URI EXAMPLE_GADGET_URI = URI.create("http://example.org/gadget/spec.xml");
    private static final URI UNKNOWN_GADGET_URI = URI.create("http://example.org/unknown/gadget.xml");
    private static final String EXAMPLE_GADGET_XML =
        "<Module>"
        + "<ModulePrefs title='Example Gadget'/>"
        + "<Content>"
        + "Hello, world!"
        + "</Content>"
        + "</Module>";
    private static final GadgetSpec EXAMPLE_GADGET_SPEC;
    static
    {
        try
        {
            EXAMPLE_GADGET_SPEC = new GadgetSpec(Uri.fromJavaUri(EXAMPLE_GADGET_URI), EXAMPLE_GADGET_XML);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    private static final LocalGadgetSpecProvider EMPTY_PROVIDER = mock(LocalGadgetSpecProvider.class);

    @Mock GadgetSpecFactory fallback;

    @Test(expected = NullPointerException.class)
    public void throwsNullPointerExceptionWhenProvidersIsNull()
    {
        new LocalGadgetSpecFactory(null, fallback);
    }

    @Test(expected = NullPointerException.class)
    public void throwsNullPointerExceptionWhenFallbackIsNull()
    {
        new LocalGadgetSpecFactory(ImmutableSet.<LocalGadgetSpecProvider>of(), null);
    }

    @Test
    public void writesGadgetXmlFromProvider() throws GadgetException
    {
        LocalGadgetSpecFactory factory = createFactory();
        GadgetSpec spec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(spec, is(equalTo(EXAMPLE_GADGET_SPEC)));
    }

    @Test
    public void writesGadgetXmlFromSecondProviderIfNotFoundInFirst() throws GadgetException
    {
        LocalGadgetSpecFactory factory = createFactory(EMPTY_PROVIDER, createExampleProvider());
        GadgetSpec spec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(spec, is(equalTo(EXAMPLE_GADGET_SPEC)));
    }

    @Test
    public void writesGadgetXmlFromSecondProviderIfFirstThrowsARuntimeExceptionFromContains()
        throws GadgetException, IOException
    {
        LocalGadgetSpecProvider throwingProvider =
            new ExampleGadgetProvider("<Module><ModulePrefs title='error'/><Content>Error!</Content></Module>")
            {
                @Override
                public boolean contains(URI gadgetSpecUri)
                {
                    throw new RuntimeException();
                }
            };

        LocalGadgetSpecFactory factory = createFactory(throwingProvider, createExampleProvider());
        GadgetSpec spec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(spec, is(equalTo(new GadgetSpec(Uri.fromJavaUri(EXAMPLE_GADGET_URI), EXAMPLE_GADGET_XML))));
    }

    @Test
    public void writesGadgetXmlFromSecondProviderIfFirstThrowsARuntimeExceptionFromWriteGadgetSpecTo()
        throws GadgetException, IOException
    {
        LocalGadgetSpecProvider throwingProvider =
            new ExampleGadgetProvider("<Module><ModulePrefs title='error'/><Content>Error!</Content></Module>")
            {
                @Override
                public void writeGadgetSpecTo(URI gadgetSpecUri, OutputStream output)
                    throws GadgetSpecUriNotAllowedException, IOException
                {
                    throw new RuntimeException();
                }
            };

        LocalGadgetSpecFactory factory = createFactory(throwingProvider, createExampleProvider());
        GadgetSpec spec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(spec, is(equalTo(EXAMPLE_GADGET_SPEC)));
    }

    @Test(expected = GadgetException.class)
    public void rethrowsIOExceptionFromWriteGadgetSpecToAsGadgetException() throws GadgetException
    {
        LocalGadgetSpecProvider throwingProvider =
            new ExampleGadgetProvider("<Module><ModulePrefs title='error'/><Content>Error!</Content></Module>")
            {
                @Override
                public void writeGadgetSpecTo(URI gadgetSpecUri, OutputStream output)
                    throws GadgetSpecUriNotAllowedException, IOException
                {
                    throw new IOException();
                }
            };

        LocalGadgetSpecFactory factory = createFactory(throwingProvider, createExampleProvider());
        factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
    }

    @Test
    public void usesFallbackWhenNoLocalGadgetSpecProvidersContainUri() throws GadgetException
    {
        LocalGadgetSpecFactory factory = createFactory(EMPTY_PROVIDER, createExampleProvider());
        factory.getGadgetSpec(UNKNOWN_GADGET_URI, false);

        verify(fallback).getGadgetSpec(UNKNOWN_GADGET_URI, false);
    }

    @Test
    public void unicodeGadgetContentIsPreserved() throws GadgetException
    {
        String gadgetWithUnicodeContent =
            "<Module>"
            + "<ModulePrefs title='Unicode Snowman'/>"
            + "<Content>\u2603</Content>"
            + "</Module>";

        LocalGadgetSpecProvider unicodeProvider = new ExampleGadgetProvider(gadgetWithUnicodeContent);
        LocalGadgetSpecFactory factory = createFactory(unicodeProvider);
        GadgetSpec spec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(spec,
                   is(equalTo(new GadgetSpec(Uri.fromJavaUri(EXAMPLE_GADGET_URI), gadgetWithUnicodeContent))));
    }
    
    @Test
    public void assertThatCachedGadgetSpecIsUsedWhenIgnoreCacheIsFalse() throws Exception
    {
        LocalGadgetSpecFactory factory = createFactory();
        GadgetSpec firstReturnedSpec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(factory.getGadgetSpec(EXAMPLE_GADGET_URI, false), is(sameInstance(firstReturnedSpec)));
    }
    
    @Test
    public void assertThatCachedGadgetSpecIsNotUsedWhenLastModifiedDateChanges() throws Exception
    {
        ExampleGadgetProvider provider = createExampleProvider();
        LocalGadgetSpecFactory factory = createFactory(provider);
        GadgetSpec firstReturnedSpec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        provider.setLastModified(new Date(new Date().getTime() + 1000));
        assertThat(factory.getGadgetSpec(EXAMPLE_GADGET_URI, false), is(not(sameInstance(firstReturnedSpec))));
    }
    
    @Test
    public void assertThatCachedGadgetSpecIsNotUsedWhenIgnoreCacheIsTrue() throws Exception
    {
        LocalGadgetSpecFactory factory = createFactory();
        GadgetSpec firstReturnedSpec = factory.getGadgetSpec(EXAMPLE_GADGET_URI, false);
        assertThat(factory.getGadgetSpec(EXAMPLE_GADGET_URI, true), is(not(sameInstance(firstReturnedSpec))));
    }

    private LocalGadgetSpecFactory createFactory(LocalGadgetSpecProvider... providers)
    {
        if (providers.length == 0)
        {
            providers = new LocalGadgetSpecProvider[] { createExampleProvider() };
        }
        LocalGadgetSpecFactory factory =
            new LocalGadgetSpecFactory(ImmutableSet.<LocalGadgetSpecProvider>of(providers), fallback);
        return factory;
    }
    
    private ExampleGadgetProvider createExampleProvider()
    {
        return new ExampleGadgetProvider(EXAMPLE_GADGET_XML);
    }
    
    private static Matcher<? super GadgetSpec> equalTo(final GadgetSpec gadgetSpec)
    {
        return new TypeSafeDiagnosingMatcher<GadgetSpec>()
        {
            @Override
            protected boolean matchesSafely(GadgetSpec item, Description mismatchDescription)
            {
                if (!gadgetSpec.getChecksum().equals(item.getChecksum()))
                {
                    mismatchDescription.appendText(" is ").appendValue(item);
                    return false; 
                }
                return true;
            }

            public void describeTo(Description description)
            {
                description.appendText("equals ").appendValue(gadgetSpec);
            }
        };
    }

    private static class ExampleGadgetProvider implements LocalGadgetSpecProvider
    {
        private final String xml;
        private Date lastModified = new Date();
        
        public ExampleGadgetProvider(String xml)
        {
            this.xml = xml;
        }

        public Iterable<URI> entries()
        {
            return ImmutableSet.of(EXAMPLE_GADGET_URI);
        }

        public boolean contains(URI gadgetSpecUri)
        {
            return EXAMPLE_GADGET_URI.equals(gadgetSpecUri);
        }

        public void writeGadgetSpecTo(URI gadgetSpecUri, OutputStream output)
            throws GadgetSpecUriNotAllowedException, IOException
        {
            if (EXAMPLE_GADGET_URI.equals(gadgetSpecUri))
            {
                output.write(xml.getBytes("UTF-8"));
                return;
            }
            throw new GadgetSpecUriNotAllowedException(String.format("Unknown gadget spec URI: %s", gadgetSpecUri));
        }

        public Date getLastModified(URI gadgetSpecUri)
        {
            return lastModified;
        }
        
        public void setLastModified(Date lastModified)
        {
            this.lastModified = lastModified;
        }
    }
}
