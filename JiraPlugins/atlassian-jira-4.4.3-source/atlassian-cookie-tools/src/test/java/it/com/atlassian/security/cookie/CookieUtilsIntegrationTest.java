package it.com.atlassian.security.cookie;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.junit.Before;
import org.junit.Test;

import static com.atlassian.security.cookie.CookieTestServlet.COOKIE_NAME;
import static com.atlassian.security.cookie.CookieTestServlet.COOKIE_VALUE;
import static org.junit.Assert.assertEquals;

public class CookieUtilsIntegrationTest
{
    private static final String COOKIE_HEADER = "Set-Cookie";
    private static final String PORT = System.getProperty("http.port", "5990");
    private static final String CONTEXT = System.getProperty("context.path", "/refapp");

    private Client client;
    private UriBuilder uriBuilder;

    @Before
    public void setUp()
    {
        client = new Client();
        uriBuilder = UriBuilder.fromUri("http://localhost/").port(Integer.parseInt(PORT)).path(CONTEXT)
            .path("plugins").path("servlet").path("cookies");
    }

    @Test
    public void testCookieWithHttpOnly() throws Exception
    {
        URI uri = uriBuilder.queryParam("httpOnly", "true").build();

        ClientResponse response = client.resource(uri).get(ClientResponse.class);
        List<String> cookies = response.getMetadata().get(COOKIE_HEADER);
        assertEquals(1, cookies.size());
        assertEquals(COOKIE_NAME + "=" + COOKIE_VALUE + "; HttpOnly", cookies.get(0));
    }

    @Test
    public void testCookieWithoutHttpOnly() throws Exception
    {
        URI uri = uriBuilder.build();

        ClientResponse response = client.resource(uri).get(ClientResponse.class);
        List<String> cookies = response.getMetadata().get(COOKIE_HEADER);
        assertEquals(1, cookies.size());
        assertEquals(COOKIE_NAME + "=" + COOKIE_VALUE, cookies.get(0));
    }
}
