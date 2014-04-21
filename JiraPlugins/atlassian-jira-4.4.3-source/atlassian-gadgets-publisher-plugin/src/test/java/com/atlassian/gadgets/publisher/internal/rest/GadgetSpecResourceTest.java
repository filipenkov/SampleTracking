package com.atlassian.gadgets.publisher.internal.rest;

import java.io.OutputStream;

import javax.ws.rs.core.Response;

import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecWriter;
import com.atlassian.plugin.Plugin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecResourceTest
{
    private static final String TEST_PLUGIN_KEY = "test.plugin";
    private static final String TEST_RESOURCE_PATH = "/p@th/to/prefs.xml";

    @Mock PublishedGadgetSpecWriter writer;
    @Mock Plugin plugin;

    private GadgetSpecResource resource;

    @Before
    public void setUp() throws Exception
    {
        resource = new GadgetSpecResource(writer);
        when(plugin.getKey()).thenReturn(TEST_PLUGIN_KEY);
    }

    @Test
    public void verifyThatPluginAndSpecPathAreParsedProperly() throws Exception
    {

        final Response response = resource.getGadgetSpec(TEST_PLUGIN_KEY, TEST_RESOURCE_PATH);

        assertThat(response.getStatus(), is(equalTo(Response.Status.OK.getStatusCode())));
        verify(writer).writeGadgetSpecTo(same(TEST_PLUGIN_KEY), same(TEST_RESOURCE_PATH), isA(OutputStream.class));
    }
}
