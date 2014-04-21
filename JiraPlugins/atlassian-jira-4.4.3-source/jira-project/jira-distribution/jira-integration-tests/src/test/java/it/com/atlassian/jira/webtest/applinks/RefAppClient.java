package it.com.atlassian.jira.webtest.applinks;

import com.atlassian.jira.functest.framework.jerseyclient.ApacheClientFactoryImpl;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * Class used for configuring the RefApp for UAL testing.
 *
 * @since v4.3
 */
public class RefAppClient
{
    /**
     * The base URL for the refapp.
     */
    private final String baseURL;

    /**
     * The client factory.
     */
    private final ApacheClientFactoryImpl clientFactory;

    /**
     * The client.
     */
    private Client client;

    /**
     * The username.
     */
    private String loginAs = "admin";

    /**
     * Creates a new RefApp.
     */
    public RefAppClient(String baseURL)
    {
        this.baseURL = baseURL;
        this.clientFactory = new ApacheClientFactoryImpl();
    }

    public RefAppClient loginAs(String username)
    {
        this.loginAs = username;
        return this;
    }

    /**
     * Creates a new Charlie in the RefApp.
     *
     * @param key the key
     * @param name the name
     * @return this
     */
    public RefAppClient createCharlie(String key, String name)
    {
        createResource().path("admin").path(key).path(name).post();
        return this;
    }

    protected final WebResource createResource()
    {
        return resourceRoot(baseURL).path("rest").path("charlie").path("1");
    }

    /**
     * Creates a WebResource for the given URL. The relevant authentication parameters are added to the resource, if
     * applicable.
     *
     * @param url a String containing a URL
     * @return a WebResource, with optional authentication parameters
     */
    protected WebResource resourceRoot(String url)
    {
        WebResource resource = client().resource(url);
        if (loginAs != null)
        {
            resource = resource.queryParam("os_authType", "basic")
                    .queryParam("os_username", loginAs)
                    .queryParam("os_password", loginAs);
        }

        return resource;
    }

    /**
     * Returns a lazily-initialised client.
     *
     * @return a Client
     */
    protected Client client()
    {
        if (client == null)
        {
            client = clientFactory.create();
        }

        return client;
    }
}
