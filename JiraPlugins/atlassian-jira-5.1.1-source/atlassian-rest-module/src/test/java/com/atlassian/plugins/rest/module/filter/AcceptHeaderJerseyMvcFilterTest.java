package com.atlassian.plugins.rest.module.filter;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Testing {@link AcceptHeaderJerseyMvcFilter}
 */
@RunWith(MockitoJUnitRunner.class)
public class AcceptHeaderJerseyMvcFilterTest
{
    private final AcceptHeaderJerseyMvcFilter filter = new AcceptHeaderJerseyMvcFilter();

    @Mock
    private ContainerRequest request;

    @Test
    public void testFilterWithoutTextHtmlContentType()
    {
        final String accept = "some/content-type";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    public void testFilterWithTextHtmlContentTypeFirst()
    {
        final String accept = "text/html,some/content-type";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    public void testFilterWithTextHtmlContentTypeNotFirst()
    {
        final String accept = "some/content-type,text/html,some-other/content-type";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(MediaType.TEXT_HTML + "," + accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    public void testFilterWithWildcardContentTypeNotFirst()
    {
        final String accept = "some/content-type,*/*,some-other/content-type";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(MediaType.TEXT_HTML + "," + MediaType.APPLICATION_XML + "," + accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    public void testFilterWithWildcardContentType()
    {
        final String accept = "*/*,some-other/content-type";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(MediaType.TEXT_HTML + "," + MediaType.APPLICATION_XML + "," + accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    @Test
    public void testFilterWithWildcardOnlyContentType()
    {
        final String accept = "*/*";
        final MultivaluedMap<String, String> headers = getHeadersMap(accept);

        when(request.getRequestHeaders()).thenReturn(headers);
        filter.filter(request);

        assertEquals(MediaType.TEXT_HTML + "," + MediaType.APPLICATION_XML + "," + accept, headers.getFirst(HttpHeaders.ACCEPT));
    }

    private MultivaluedMap<String, String> getHeadersMap(final String value)
    {
        final MultivaluedMapImpl multivaluedMap = new MultivaluedMapImpl();
        multivaluedMap.putSingle(HttpHeaders.ACCEPT, value);
        return multivaluedMap;
    }
}
