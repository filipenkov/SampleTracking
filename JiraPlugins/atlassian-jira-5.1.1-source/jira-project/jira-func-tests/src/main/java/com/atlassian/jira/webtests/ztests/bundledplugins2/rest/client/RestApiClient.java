package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.jerseyclient.ApacheClientFactoryImpl;
import com.atlassian.jira.functest.framework.jerseyclient.JerseyClientFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.util.EnumSet;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Abstract base class for REST API clients.
 *
 * @since v4.3
 */
public abstract class RestApiClient<T extends RestApiClient<T>>
{
    /**
     * Logger for this client.
     */
    private static final Logger log = Logger.getLogger(RestApiClient.class);

    /**
     * The REST plugin version to test.
     */
    public static final String REST_VERSION = "2";

    /**
     * The JerseyClientFactory used to access the REST API.
     */
    private final JerseyClientFactory clientFactory;

    /**
     * The JIRA environment data
     */
    private final JIRAEnvironmentData environmentData;

    /**
     * The user to log in as.
     */
    private String loginAs = FunctTestConstants.ADMIN_USERNAME;

    private String loginPassword = loginAs;

    /**
     * The version of the REST plugin to test.
     */
    private String version;

    /**
     * Lazily-instantiated Jersey client.
     */
    private Client client = null;

    /**
     * Constructs a new RestApiClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    protected RestApiClient(JIRAEnvironmentData environmentData)
    {
        this(environmentData, REST_VERSION);
    }

    /**
     * Constructs a new RestApiClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     * @param version a String containing the version to test against
     */
    protected RestApiClient(JIRAEnvironmentData environmentData, String version)
    {
        DefaultClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJaxbJsonProvider.class);
        this.clientFactory = new ApacheClientFactoryImpl(config);
        this.environmentData = environmentData;
        this.version = version;
    }

    /**
     * Ensures that this client does not authenticate when making a request.
     *
     * @return this
     */
    @SuppressWarnings ("unchecked")
    public T anonymous()
    {
        loginAs = null;
        loginPassword = null;
        return (T) this;
    }

    /**
     * Makes this client authenticate as the given user. If this method is not called, this client will authenticate as
     * "{@value com.atlassian.jira.functest.framework.FunctTestConstants#ADMIN_USERNAME}".
     *
     * @param username a String containing the username
     * @return this
     */
    @SuppressWarnings ("unchecked")
    public T loginAs(String username)
    {
        return loginAs(username, username);
    }

    /**
     * Makes this client authenticate as the given <tt>username</tt> and <tt>password</tt>. If this method is not
     * called, this client will authenticate as
     * "{@value com.atlassian.jira.functest.framework.FunctTestConstants#ADMIN_USERNAME}".
     *
     * @param username a String containing the username
     * @param password a String containing the passoword
     * @return this
     */
    @SuppressWarnings ("unchecked")
    public T loginAs(String username, String password)
    {
        loginAs = username;
        loginPassword = password;
        return (T) this;
    }

    /**
     * Creates the resource that corresponds to the root of the REST API.
     *
     * @return a WebResource for the REST API root
     */
    protected WebResource createResource()
    {
        return resourceRoot(environmentData.getBaseUrl().toExternalForm()).path("rest").path("api").path(version);
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
                    .queryParam("os_username", percentEncode(loginAs))
                    .queryParam("os_password", percentEncode(loginPassword));
        }

        return resource;
    }

    /**
     * Returns the Jersey client to use.
     *
     * @return a Client
     */
    private Client client()
    {
        if (client == null)
        {
            client = clientFactory.create();
            if (log.isDebugEnabled())
            {
                client.addFilter(new LoggingFilter());
            }
            if (StringUtils.isNotBlank(environmentData.getTenant()))
            {
                client.addFilter(new AtlassianTenantFilter(environmentData.getTenant()));
            }
        }

        return client;
    }

    protected Response toResponse(Method method)
    {
        ClientResponse clientResponse = method.call();
        if (clientResponse.getStatus() == 200)
        {
            final Response response = new Response(clientResponse.getStatus(), null);
            clientResponse.close();
            return response;
        }

        return errorResponse(clientResponse);
    }

    protected <T> Response<T> toResponse(Method method, Class<T> clazz)
    {
        ClientResponse clientResponse = method.call();
        if (clientResponse.getStatus() < 300)
        {
            T object = clientResponse.getEntity(clazz);
            return new Response<T>(clientResponse.getStatus(), null, object);
        }

        return errorResponse(clientResponse);
    }

    protected Response errorResponse(ClientResponse clientResponse)
    {
        Errors entity = null;
        if (clientResponse.hasEntity() && APPLICATION_JSON_TYPE.isCompatible(clientResponse.getType()))
        {
            try
            {
                entity = clientResponse.getEntity(Errors.class);
            }
            catch (Exception e) { log.debug("Failed to deserialise Errors from response", e); }
        }

        final Response response = new Response(clientResponse.getStatus(), entity);
        clientResponse.close();
        return response;
    }

    /**
     * Adds the expand query param to the given WebResource. The name of the attributes to expand must exactly match the
     * name of the enum instances that are passed in.
     *
     * @param resource a WebResource
     * @param expands an EnumSet containing the attributes to expand
     * @return the input WebResource, with added expand parameters
     */
    protected WebResource expanded(WebResource resource, EnumSet<?> expands)
    {
        if (expands.isEmpty())
        {
            return resource;
        }

        return resource.queryParam("expand", percentEncode(StringUtils.join(expands, ",")));
    }

    /**
     * Constructs an EnumSet from a var-args param.
     *
     * @param cls the Enum class object
     * @param expand the enum instances to expand
     * @param <E> the Enum class
     * @return an EnumSet
     */
    protected static <E extends Enum<E>> EnumSet<E> setOf(Class<E> cls, E... expand)
    {
        return expand.length == 0 ? EnumSet.noneOf(cls) : EnumSet.of(expand[0], expand);
    }

    /**
     * Percent-encode the % when stuffing it into a query param. Otherwise it may not get escaped properly, as per <a
     * href="https://extranet.atlassian.com/x/v4Qlbw">this EAC blog</a>.
     *
     * @param queryParam the query param value
     * @return a String with % replaced by %25
     */
    protected static String percentEncode(String queryParam)
    {
        return queryParam == null ? null : queryParam.replace("%", "%25");
    }

    /**
     * Method interface to use with getResponse.
     */
    public static interface Method
    {
        ClientResponse call();
    }
}
