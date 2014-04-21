package com.atlassian.plugins.rest.module.util;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @since   2.2
 */
public class RestUrlBuilderImplTest
{
    private RestUrlBuilderImpl urlBuilder;

    @Before
    public void setup()
    {
        urlBuilder = new RestUrlBuilderImpl();
    }

    @Test
    public void testGetUrlForWithBaseUrl() throws Exception
    {
        assertEquals("http://base2/dummy/sub",
                urlBuilder.getUrlFor(new URI("http://base2"), FooResource.class).subResource().toString());
    }
}
