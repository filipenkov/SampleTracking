package com.atlassian.crowd.integration.rest.service;

import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.integration.Constants;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Tests the {@link RestCrowdClient}.
 */
public class RestCrowdClientTest {

    private static final int HTTP_PORT = 14966;

    private RestCrowdClient restCrowdClient;

    @Before
    public void setUp() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTIES_FILE_BASE_URL, "http://localhost:" + HTTP_PORT);
        properties.setProperty(Constants.PROPERTIES_FILE_APPLICATION_NAME, "applicationName");

        final ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromProperties(properties);

        restCrowdClient = new RestCrowdClient(clientProperties);
    }

    @Test(expected = IncrementalSynchronisationNotAvailableException.class)
    public void testFalseIncrementalSyncFlagFails() throws Exception {
        final HttpServer server = createSingleFileHttpServer("event",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<events incrementalSynchronisationAvailable=\"false\" newEventToken=\"8047511070899496297:12\"/>");

        server.start();
        try
        {
            restCrowdClient.getCurrentEventToken();
        }
        finally
        {
            server.stop(0);
        }
    }

    @Test(expected = IncrementalSynchronisationNotAvailableException.class)
    public void testMissingIncrementalSyncFlagFails() throws Exception {
        final HttpServer server = createSingleFileHttpServer("event",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<events newEventToken=\"8047511070899496297:12\"/>");

        server.start();
        try
        {
            restCrowdClient.getCurrentEventToken();
        }
        finally
        {
            server.stop(0);
        }
    }

    private HttpServer createSingleFileHttpServer(String path, final String responseXml) throws IOException
    {
        InetSocketAddress addr = new InetSocketAddress(HTTP_PORT);
        HttpServer server = HttpServer.create(addr, 0);
        server.setExecutor(Executors.newCachedThreadPool());

        server.createContext("/rest/usermanagement/1/" + path, new HttpHandler()
        {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException
            {
                if (!httpExchange.getRequestMethod().equalsIgnoreCase("get"))
                {
                    httpExchange.sendResponseHeaders(404, 0);
                    httpExchange.close();
                    return;
                }

                final byte[] responseBytes = responseXml.getBytes();

                final Headers responseHeaders = httpExchange.getResponseHeaders();
                responseHeaders.set("X-Embedded-Crowd-Version", "any");
                responseHeaders.set("Content-Type", "application/xml");
                httpExchange.sendResponseHeaders(200, responseBytes.length);
                final OutputStream responseBody = httpExchange.getResponseBody();
                try
                {
                        responseBody.write(responseBytes);
                }
                finally
                {
                    responseBody.close();
                }
            }
        });

        return server;
    }
}
