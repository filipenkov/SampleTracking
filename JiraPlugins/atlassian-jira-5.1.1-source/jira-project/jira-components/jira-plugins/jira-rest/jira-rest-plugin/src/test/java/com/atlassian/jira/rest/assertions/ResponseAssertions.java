package com.atlassian.jira.rest.assertions;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Simple assertions for the {@link Response} object.
 *
 * @since v4.4
 */
public class ResponseAssertions
{
    private static final String CACHE_CHECK = "Cache-Control";

    private ResponseAssertions()
    {
    }

    public static void assertStatus(Response.Status expected, Response actual)
    {
        assertEquals(expected.getStatusCode(), actual.getStatus());
    }

    public static void assertResponseBody(Object body, Response response)
    {
        assertEquals(format("response.body != %s.", body), body, response.getEntity());
    }

    public static void assertCache(CacheControl control, Response response)
    {
        List<Object> object = response.getMetadata().get(CACHE_CHECK);
        assertNotNull("No cache control set.", object);
        assertFalse("No cache control set.", object.isEmpty());
        assertEquals("Unexpected cache control.", 1, object.size());
        assertEquals("Cache control is wrong.", control, object.get(0));
    }

    public static void assertResponseCacheNever(Response response)
    {
        assertCache(never(), response);
    }
}
