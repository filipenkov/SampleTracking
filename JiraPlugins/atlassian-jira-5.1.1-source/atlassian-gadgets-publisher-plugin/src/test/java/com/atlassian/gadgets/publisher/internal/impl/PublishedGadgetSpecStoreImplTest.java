package com.atlassian.gadgets.publisher.internal.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.internal.GadgetProcessor;
import com.atlassian.gadgets.publisher.internal.GadgetSpecValidator;
import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecNotFoundException;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;
import com.atlassian.gadgets.util.GadgetSpecUrlBuilder;
import com.atlassian.plugin.Plugin;
import com.atlassian.sal.api.ApplicationProperties;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.io.IOUtils.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublishedGadgetSpecStoreImplTest
{
    private static final String BASE_URL = "rest/gadgets/1.0/g/";
    private static final String PLUGIN_KEY = "com.atlassian.test";
    private static final String MODULE_KEY = "test-gadget";
    private static final String GADGET_LOCATION = "path/to/gadget.xml";
    private static final URI GADGET_SPEC_URI = URI.create(BASE_URL + PLUGIN_KEY + ":" + MODULE_KEY + "/" + GADGET_LOCATION);
    private static final PluginGadgetSpec.Key GADGET_SPEC_KEY = new PluginGadgetSpec.Key(PLUGIN_KEY, GADGET_LOCATION);
    private static final String EXTERNAL_GADGET_SPEC_LOCATION = "http://example.org/gadget.xml";

    @Mock GadgetSpecUrlBuilder urlBuilder;
    @Mock GadgetSpecValidator validator;
    @Mock GadgetProcessor gadgetProcessor;
    @Mock PluginGadgetSpecProviderPermission permission;
    @Mock ApplicationProperties applicationProperties;
    @Mock Plugin plugin;

    PublishedGadgetSpecStore publishedGadgetStore;

    @Before
    public void setUp() throws IOException
    {
        publishedGadgetStore = new PublishedGadgetSpecStore(urlBuilder, validator, gadgetProcessor, permission, applicationProperties);
        when(urlBuilder.buildGadgetSpecUrl(PLUGIN_KEY, MODULE_KEY, GADGET_LOCATION)).thenReturn(GADGET_SPEC_URI.toString());
        when(urlBuilder.parseGadgetSpecUrl(GADGET_SPEC_URI.toASCIIString())).thenReturn(GADGET_SPEC_KEY);
        when(validator.isValid(isA(InputStream.class))).thenReturn(true);
        doAnswer(new Answer<Void>()
        {
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                InputStream in = (InputStream) invocationOnMock.getArguments()[0];
                OutputStream out = (OutputStream) invocationOnMock.getArguments()[1];
                copy(in, out);
                return null;
            }
        }).when(gadgetProcessor).process(isA(InputStream.class), isA(OutputStream.class));
        when(plugin.getKey()).thenReturn(PLUGIN_KEY);
        when(plugin.getResourceAsStream(GADGET_LOCATION)).thenAnswer(new Answer<InputStream>()
        {
            public InputStream answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                return getHelloWorldGadget();
            }
        });
        when(applicationProperties.getBaseUrl()).thenReturn("http://application/base");
    }

    @Test
    public void assertThatStoreInitiallyContainsNoUris()
    {
        assertFalse(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreRejectsInvalidGadgetSpec() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);

        when(validator.isValid(isA(InputStream.class))).thenReturn(false);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();
        assertFalse(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    private PluginGadgetSpec newPluginGadgetSpec(String location)
    {
        return new PluginGadgetSpec(plugin, MODULE_KEY, location, ImmutableMap.<String, String>of());
    }

    @Test
    public void assertThatStoreDoesNotContainNonGadgetUri() throws IOException
    {
        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(GADGET_LOCATION));
        publishedGadgetStore.onStart();

        final URI nonGadgetUri = URI.create("http://localhost:8080/non/gadget.xml");
        assertFalse(publishedGadgetStore.contains(nonGadgetUri));
    }

    @Test
    public void assertThatStoreDoesNotContainInvalidGadgetUri() throws IOException
    {
        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(GADGET_LOCATION));
        publishedGadgetStore.onStart();

        final URI invalidGadgetUri = URI.create("http://localhost:8080/dashboards/rest/gadgets/1.0/g/invalid");
        assertFalse(publishedGadgetStore.contains(invalidGadgetUri));
    }

    @Test
    @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
    public void assertThatStoreDoesNotContainGadgetUriWhenGadgetSpecUrlBuilderThrowsGadgetSpecUriNotAllowedException()
        throws IOException
    {
        final URI otherServerGadgetUri =
            URI.create("http://otherhost:8080/dashboards/rest/gadgets/1.0/g/" + PLUGIN_KEY + "/" + GADGET_LOCATION);
        when(urlBuilder.parseGadgetSpecUrl(otherServerGadgetUri.toASCIIString()))
            .thenThrow(new GadgetSpecUriNotAllowedException("expected exception"));

        assertFalse(publishedGadgetStore.contains(otherServerGadgetUri));

        verify(urlBuilder).parseGadgetSpecUrl(otherServerGadgetUri.toASCIIString());
    }

    @Test
    public void assertThatStoreDoesNotContainExternalGadgetSpecs()
    {
        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(EXTERNAL_GADGET_SPEC_LOCATION));
        publishedGadgetStore.onStart();

        assertFalse(publishedGadgetStore.contains(URI.create(EXTERNAL_GADGET_SPEC_LOCATION)));
    }

    @Test
    public void assertThatStoreContainsStoredGadgetUriWhichIsPermitted() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertTrue(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreContainsStoredGadgetUriWhichIsPermittedAddedAfterStartup() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.onStart();
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);

        assertTrue(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreDoesContainStoredGadgetUriWhichPermissionsPassOn() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.PASS);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertTrue(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreDoesNotContainStoredGadgetUriWhichIsDenied() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.DENY);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertFalse(publishedGadgetStore.contains(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreInitiallyHasNoEntries()
    {
        assertTrue(isEmpty(publishedGadgetStore.entries()));
    }

    @Test
    public void assertThatStoreEntriesContainsStoredGadgetUriWhenItIsAllowed() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertThat(publishedGadgetStore.entries(), hasItem(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreEntriesContainsStoredGadgetUriWhenThePermissionPasses() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.PASS);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertThat(publishedGadgetStore.entries(), hasItem(GADGET_SPEC_URI));
    }

    @Test
    public void assertThatStoreEntriesDoesNotContainStoredGadgetUriWhenItIsDenied() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.DENY);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        assertThat(publishedGadgetStore.entries(), not(hasItem(GADGET_SPEC_URI)));
    }
    
    @Test(expected = PublishedGadgetSpecNotFoundException.class)
    public void assertThatWritingAbsentGadgetSpecThrowsPublishedGadgetSpecNotFoundException() throws IOException
    {
        publishedGadgetStore.writeGadgetSpecTo(PLUGIN_KEY, GADGET_LOCATION, new ByteArrayOutputStream());
    }

    @Test
    public void assertThatWritingGadgetSpecByKeyEqualsOriginalSpecData() throws IOException
    {
        final String expectedSpec= IOUtils.toString(getHelloWorldGadget());

        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(GADGET_LOCATION));
        publishedGadgetStore.onStart();
        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        publishedGadgetStore.writeGadgetSpecTo(PLUGIN_KEY, GADGET_LOCATION, written);
        assertThat(written.toString(), is(equalTo(expectedSpec)));
    }

    @Test(expected = NullPointerException.class)
    public void assertThatWritingGadgetSpecWithNullUriThrowsNullPointerException() throws IOException
    {
        publishedGadgetStore.writeGadgetSpecTo(null, new NullOutputStream());
    }

    @Test(expected = NullPointerException.class)
    public void assertThatWritingGadgetSpecToNullOutputStreamThrowsNullPointerException() throws IOException
    {
        publishedGadgetStore.writeGadgetSpecTo(GADGET_SPEC_URI, null);
    }

    @Test(expected = GadgetSpecUriNotAllowedException.class)
    public void assertThatWritingGadgetSpecWithInvalidUriThrowsGadgetSpecUriNotAllowedException() throws IOException
    {
        publishedGadgetStore.writeGadgetSpecTo(URI.create("http://example.org/invalid/gadget/uri"),
                                               new NullOutputStream());
    }

    @Test(expected = GadgetSpecUriNotAllowedException.class)
    public void assertThatWritingGadgetSpecWithMissingUriThrowsGadgetSpecUriNotAllowedException() throws IOException
    {
        publishedGadgetStore.writeGadgetSpecTo(URI.create(BASE_URL + PLUGIN_KEY + ":missing-gadget/missing/gadget.xml"),
                                               new NullOutputStream());
    }

    @Test(expected = IOException.class)
    public void assertThatWritingGadgetSpecByUriPropagatesIOException() throws IOException
    {
        OutputStream output = new OutputStream()
        {
            @Override
            public void write(int b) throws IOException
            {
                throw new IOException();
            }
        };
        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(GADGET_LOCATION));
        publishedGadgetStore.onStart();
        publishedGadgetStore.writeGadgetSpecTo(GADGET_SPEC_URI, output);
    }

    @Test
    public void assertThatWritingGadgetSpecByUriEqualsOriginalSpecData() throws IOException
    {
        final byte[] expectedBytes = toByteArray(getHelloWorldGadget());

        publishedGadgetStore.pluginGadgetSpecEnabled(newPluginGadgetSpec(GADGET_LOCATION));
        publishedGadgetStore.onStart();
        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        publishedGadgetStore.writeGadgetSpecTo(GADGET_SPEC_URI, written);
        assertThat(written.toByteArray(), is(equalTo(expectedBytes)));
    }
    
    @Test
    public void verifyThatProcessedGadgetSpecIsCached() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, new NullOutputStream());
        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, new NullOutputStream());
        
        // the stream will be accessed once if the processed gadget spec is cached
        verify(plugin, times(1)).getResourceAsStream(GADGET_LOCATION);
    }
    
    @Test
    public void verifyThatProcessedGadgetSpecIsRemovedFromCacheWhenGadgetSpecModuleIsDisabled() throws IOException
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, new NullOutputStream());
        
        publishedGadgetStore.pluginGadgetSpecDisabled(gadgetSpec);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        
        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, new NullOutputStream());
        
        // the stream will be accessed once for each time the plugin gadget spec is enabled and written
        verify(plugin, times(2)).getResourceAsStream(GADGET_LOCATION);
    }
    
    @Test
    public void assertThatDifferentBaseUrlsProduceDifferentProcessedGadgetSpecs() throws IOException
    {
        when(applicationProperties.getBaseUrl())
            // twice for when the plugin is enabled (once for the lookup and another for the replacement
            .thenReturn("http://application/base").thenReturn("http://application/base")
             // for the first time the gadget is written (we only need one here because only a lookup will occur)
            .thenReturn("http://application/base")
             // and for the last time the gadget is written
            .thenReturn("http://different.application/base").thenReturn("http://different.application/base");
        
        doAnswer(writeSpecWithBaseUrlReplaced()).when(gadgetProcessor).process(isA(InputStream.class), isA(OutputStream.class));

        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();

        ByteArrayOutputStream firstBaseUrlStream = new ByteArrayOutputStream();
        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, firstBaseUrlStream);
        
        ByteArrayOutputStream secondBaseUrlStream = new ByteArrayOutputStream();
        publishedGadgetStore.writeGadgetSpecTo(plugin.getKey(), GADGET_LOCATION, secondBaseUrlStream);
        
        assertThat(firstBaseUrlStream.toString(), is(not(equalTo(secondBaseUrlStream.toString()))));
    }
    
    @Test
    public void assertThatLastModifiedDateIsThePluginsLoadedDate()
    {
        PluginGadgetSpec gadgetSpec = newPluginGadgetSpec(GADGET_LOCATION);
        when(permission.voteOn(gadgetSpec)).thenReturn(Vote.ALLOW);
        publishedGadgetStore.pluginGadgetSpecEnabled(gadgetSpec);
        publishedGadgetStore.onStart();
        
        Date datePluginWasLoaded = new Date(123456789000L);
        when(plugin.getDateLoaded()).thenReturn(datePluginWasLoaded);

        assertThat(publishedGadgetStore.getLastModified(GADGET_SPEC_URI), is(equalTo(datePluginWasLoaded)));
    }
    
    private Answer<Void> writeSpecWithBaseUrlReplaced()
    {
        return new ReplaceBaseUrl();
    }
    
    private final class ReplaceBaseUrl implements Answer<Void>
    {
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable
        {
            InputStream in = (InputStream) invocationOnMock.getArguments()[0];
            OutputStream out = (OutputStream) invocationOnMock.getArguments()[1];
            String spec = IOUtils.toString(in).replaceAll("__ATLASSIAN_BASE_URL__", applicationProperties.getBaseUrl());
            write(spec, out);
            return null;
        }
    }

    private InputStream getHelloWorldGadget()
    {
        return getClass().getResourceAsStream("hello.xml");
    }
}
