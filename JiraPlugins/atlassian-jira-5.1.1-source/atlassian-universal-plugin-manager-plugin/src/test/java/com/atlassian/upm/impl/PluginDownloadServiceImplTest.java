package com.atlassian.upm.impl;

import java.net.URI;

import com.atlassian.plugins.client.service.ClientContextFactory;
import com.atlassian.plugins.service.ClientContext;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.RelativeURIException;
import com.atlassian.upm.UnsupportedProtocolException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PluginDownloadServiceImplTest
{
    private final String USERNAME = "admin";
    private final String PASSWORD = "admin";

    @Mock ClientContextFactory clientContextFactory;
    private PluginDownloadServiceImpl downloader;

    @Mock PluginDownloadService.ProgressTracker progressTracker;

    @Before
    public void setUp()
    {
        downloader = new PluginDownloadServiceImpl(clientContextFactory);
        when(clientContextFactory.getClientContext()).thenReturn(new ClientContext.Builder().build());
    }
    
    @Test(expected = UnsupportedProtocolException.class)
    public void assertThatExceptionIsThrownForUnsupportedURIProtocol() throws Exception
    {
        final String invalidURIValue = "unsupported://example.com/some/file";
        final URI invalidURI = URI.create(invalidURIValue);

        downloader.downloadPlugin(invalidURI, USERNAME, PASSWORD, progressTracker);
    }

    @Test(expected = RelativeURIException.class)
    public void assertThatExceptionIsThrownForRelativeURI() throws Exception
    {
        final String relativeURIValue = "/relative/uri";
        final URI relativeURI = URI.create(relativeURIValue);

        downloader.downloadPlugin(relativeURI, USERNAME, PASSWORD, progressTracker);
    }
}