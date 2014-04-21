package com.atlassian.plugins.rest.common.filter;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.container.ContainerRequest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class ExtensionJerseyFilterTest
{
    private static final String ACCEPT_HEADER = "Accept";

    @Mock
    private ContainerRequest containerRequest;

    private ExtensionJerseyFilter jerseyFilter;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        jerseyFilter = new ExtensionJerseyFilter(Collections.singletonList("/excluded.*"));
    }

    @Test
    public void testDefaultConstructor() throws Exception
    {
        jerseyFilter = new ExtensionJerseyFilter();
        final MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("param1", "value1");
        parameters.add("param2", "value2.1");
        parameters.add("param2", "value2.2");
        testFilter(parameters);
    }

    @Test
    public void testFilterWithoutQueryParameters() throws Exception
    {
        testFilter(new MultivaluedMapImpl());
    }

    @Test
    public void testFilterWithQueryParameters() throws Exception
    {
        final MultivaluedMap<String, String> parameters = new MultivaluedMapImpl();
        parameters.add("param1", "value1");
        parameters.add("param2", "value2.1");
        parameters.add("param2", "value2.2");
        testFilter(parameters);
    }

    @Test
    public void testDoNotFilterWhenExtensionNotMatched() throws Exception
    {
        testDoNotFilter("resource.myextension");
    }

    @Test
    public void testDoNotFilterWhenNoExtension() throws Exception
    {
        testDoNotFilter("resource");
    }

    @Test
    public void testDoNotFilterWhenExcluded() throws Exception
    {
        testDoNotFilter("excluded/resource.json");
    }

    private void testFilter(MultivaluedMap<String, String> queryParameters) throws Exception
    {
        final MultivaluedMapImpl headers = new MultivaluedMapImpl();
        final URI baseUri = new URI("http://localhost:8080/rest");

        when(containerRequest.getAbsolutePath()).thenReturn(new URI("http://localhost:8080/rest/application.json"));
        when(containerRequest.getBaseUri()).thenReturn(baseUri);
        when(containerRequest.getRequestHeaders()).thenReturn(headers);
        when(containerRequest.getQueryParameters()).thenReturn(queryParameters);

        jerseyFilter.filter(containerRequest);

        final List<String> acceptHeader = headers.get(ACCEPT_HEADER);
        assertEquals(1, acceptHeader.size());
        assertEquals(MediaType.APPLICATION_JSON, acceptHeader.get(0));

        verify(containerRequest).setUris(eq(baseUri), argThat(new UriMatcher(new URI("http://localhost:8080/rest/application"), queryParameters)));
    }

    private void testDoNotFilter(String resourceName) throws Exception
    {
        final String baseUri = "http://localhost:8080/rest/";

        when(containerRequest.getBaseUri()).thenReturn(new URI(baseUri));
        when(containerRequest.getAbsolutePath()).thenReturn(new URI(baseUri + resourceName));

        jerseyFilter.filter(containerRequest);

        verify(containerRequest, never()).getRequestHeaders();
        verify(containerRequest, never()).setUris(Matchers.<URI>anyObject(), Matchers.<URI>anyObject());
    }

    private static class UriMatcher extends ArgumentMatcher<URI>
    {
        private final URI requestUri;
        private final MultivaluedMap<String, String> parameters;

        UriMatcher(URI requestUri, MultivaluedMap<String, String> parameters)
        {
            this.requestUri = requestUri;
            this.parameters = parameters;
        }

        public boolean matches(Object actual)
        {
            final URI uri = (URI) actual;

            if (!actual.toString().startsWith(requestUri.toString()))
            {
                return false;
            }

            // check query parameters
            final String queryParametersString = '&' + uri.getQuery() + '&';
            for (Map.Entry<String, List<String>> queryParameter : parameters.entrySet())
            {
                for (String value : queryParameter.getValue())
                {
                    if (!queryParametersString.contains('&' + queryParameter.getKey() + '=' + value + '&'))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
