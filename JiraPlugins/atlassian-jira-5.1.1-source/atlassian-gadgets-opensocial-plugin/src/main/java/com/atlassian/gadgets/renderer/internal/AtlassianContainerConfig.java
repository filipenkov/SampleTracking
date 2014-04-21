package com.atlassian.gadgets.renderer.internal;

import com.atlassian.gadgets.view.ViewType;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shindig.common.ContainerConfig;
import org.apache.shindig.common.ContainerConfigException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * Overrides the default implementation of {@link ContainerConfig} so that we can use our own configuration and
 * have a chance to customize it for the specific deployment of the application using {@link ApplicationProperties}. 
 */
@Singleton
public class AtlassianContainerConfig implements ContainerConfig
{
    final class Containers
    {
        static final String DEFAULT = "default";
        static final String ATLASSIAN = "atlassian";
    }
    
    static final String MAKE_REQUEST_PATH = "/plugins/servlet/gadgets/makeRequest";
    static final String RPC_RELAY_PATH = "/plugins/servlet/gadgets/rpc-relay";
    static final String SOCIAL_PATH = "/plugins/servlet/social";
    static final String JS_URI_PATH = "/plugins/servlet/gadgets/js";
    
    private final ApplicationProperties applicationProperties;

    // Store the config as a JSONObject because it makes it easier to return JSON objects and arrays
    private final JSONObject config;
    
    @Inject
    public AtlassianContainerConfig(ApplicationProperties applicationProperties) throws ContainerConfigException
    {
        this.applicationProperties = applicationProperties;
        this.config = new JSONObject(createConfig());
    }

    private Map<String, Object> createConfig()
    {
        ImmutableMap.Builder<String, Object> configBuilder = ImmutableMap.builder();
        // Container must be an array; this allows multiple containers
        // to share configuration.
        configBuilder.put("gadgets.container", new JSONArray(ImmutableList.of(Containers.ATLASSIAN, Containers.DEFAULT)));

        // Set of regular expressions to validate the parent parameter. This is
        // necessary to support situations where you want a single container to support
        // multiple possible host names (such as for localized domains, such as
        // <language>.example.org. If left as null, the parent parameter will be
        // ignored; otherwise, any requests that do not include a parent
        // value matching this set will return a 404 error.
        configBuilder.put("gadgets.parent", JSONObject.NULL);

        // Since "shindig.locked-domain.enabled" is set to false in the shindig.property file,
        // the following two properties will not be checked inside "HashLockedDomainService".
        // "locked-domain" is a feature that renders gadgets on their sub-domains which we can't support.
        // This page explains "lockedDomain" feature:
        // http://code.google.com/apis/gadgets/docs/reference.html#lockeddomain
        // Since there isn't any other documentation on what these two configuration properties are in Shindig,
        // instead of removing these two lines, we keep them for future references.

        // Should all gadgets be forced on to a locked domain?
        // configBuilder.put("gadgets.lockedDomainRequired", false);

        // DNS domain on which gadgets should render.
        // configBuilder.put("gadgets.lockedDomainSuffix", "-a.example.com:8080");

        // Various urls generated throughout the code base.
        // iframeBaseUri will automatically have the host inserted
        // if locked domain is enabled and the implementation supports it.
        // query parameters will be added.
        configBuilder.put("gadgets.iframeBaseUri", "/gadgets/ifr");

        configBuilder.put("gadgets.jsUriTemplate", jsPath() + "/%js%");

        // We don't set the "gadgets.securityTokenType" property because it's only used
        // by DefaultSecurityTokenDecoder which we've overwritten with CustomBlobCrypterSecurityTokenDecoder.

        // This config data will be passed down to javascript. Please
        // configure your object using the feature name rather than
        // the javascript name.

        // Only configuration for required features will be used.
        // See individual feature.xml files for configuration details.
        ImmutableMap<String, JSONObject> features = ImmutableMap.<String, JSONObject>builder()
                .put("core.io", new JSONObject(ImmutableMap.of(
                                // Note: /proxy is an open proxy. Be careful how you expose this!
                                // Currently, proxy is disabled in Atlassian Gadgets.
                                "proxyUrl", "%rawurl%",
                                "jsonProxyUrl", makeRequestPath())))
                .put("atlassian.util", new JSONObject(ImmutableMap.of(
                                        // feature that overrides parts of core.io
                                        "baseUrl", baseUrl())))
                .put("views", new JSONObject(ImmutableMap.of(
                                        ViewType.DEFAULT.getCanonicalName(), new JSONObject(ImmutableMap.of(
                                                                                "isOnlyVisible", false,
                                                                                "urlTemplate", "http://localhost/gadgets/profile?{var}",
                                                                                "aliases", new JSONArray(ViewType.DEFAULT.getAliases()))),
                                        ViewType.CANVAS.getCanonicalName(), new JSONObject(ImmutableMap.of(
                                                                                "isOnlyVisible", true,
                                                                                "urlTemplate", "http://localhost/gadgets/canvas?{var}",
                                                                                "aliases", new JSONArray(ViewType.CANVAS.getAliases()))))))
                .put("rpc",  new JSONObject(ImmutableMap.of(
                                        "parentRelayUrl", rpcRelayPath(),
                                        // If true, this will use the legacy ifpc wire format when making rpc
                                        // requests.
                                        "useLegacyProtocol", false)))
                .put("skins", new JSONObject(ImmutableMap.of(
                                        "properties", new JSONObject(ImmutableMap.builder().
                                                                        put("BG_COLOR", "").
                                                                         put("BG_IMAGE", "").
                                                                        put("BG_POSITION", "").
                                                                        put("BG_REPEAT", "").
                                                                        put("FONT_COLOR", "").
                                                                        put("ANCHOR_COLOR", "").build()))))
                .put("opensocial-0.8", new JSONObject(ImmutableMap.of(
                                        "impl", "rpc",
                                        "path", socialPath(),
                                        "domain", "atlassian",
                                        "enableCaja", false,
                                        "supportedFields", new JSONObject(ImmutableMap.of(
                                                            "person", new JSONArray(Lists.newArrayList("id")),
                                                            "activity", new JSONArray(Lists.newArrayList("id","title")))))))
                .build();

        configBuilder.put("gadgets.features", new JSONObject(features));
        return configBuilder.build();
    }

    /**
     * Returns an {@code Object} whose {@code toString} method evaluates with the base URL from the 
     * {@code ApplicationProperties}.  This is done so that if the base URL is changed at runtime we don't need to
     * rebuild the configuration or somehow be notified that it has changed. 
     *  
     * @return Object whose toString method will always evaluate using the latest base URL
     */
    private Object baseUrl()
    {
        return new Object()
        {
            @Override
            public String toString ()
            {
                return applicationProperties.getBaseUrl();
            }
        };
    }

    /**
     * Returns an {@code Object} whose {@code toString} method evaluates to the correct makeRequest path using the 
     * {@code ApplicationProperties}.  This is done so that if the base URL is changed at runtime we don't need to
     * rebuild the configuration or somehow be notified that it has changed. 
     *  
     * @return Object whose toString method will always evaluate using the latest base URL
     */
    private Object makeRequestPath()
    {
        return new Object()
        {
            @Override
            public String toString()
            {
                return URI.create(applicationProperties.getBaseUrl()).getPath() + MAKE_REQUEST_PATH;
            }
        };
    }

    private Object socialPath()
    {
        return new Object()
        {
            @Override
            public String toString()
            {
                return URI.create(applicationProperties.getBaseUrl()).getPath() + SOCIAL_PATH;
            }
        };
    }

    private Object jsPath()
    {
        return new Object()
        {
            @Override
            public String toString()
            {
                return URI.create(applicationProperties.getBaseUrl()).getPath() + JS_URI_PATH;
            }
        };
    }

    /**
     * Returns an {@code Object} whose {@code toString} method evaluates to the correct rpc-relay path using the 
     * {@code ApplicationProperties}.  This is done so that if the base URL is changed at runtime we don't need to
     * rebuild the configuration or somehow be notified that it has changed. 
     *  
     * @return Object whose toString method will always evaluate using the latest base URL
     */
    private Object rpcRelayPath()
    {
        return new Object()
        {
            @Override
            public String toString()
            {
                return URI.create(applicationProperties.getBaseUrl()).getPath() + RPC_RELAY_PATH;
            }
        };
    }

    /**
     * @return The set of all containers that are currently registered.
     */
    public Collection<String> getContainers()
    {
        return ImmutableList.of(Containers.DEFAULT, Containers.ATLASSIAN);
    }

    /**
     * Fetches a configuration parameter as a JSON object, array, string, or number, ensuring that it can be safely
     * passed to javascript without any additional filtering.
     *
     * @param container the name of the container whose configuration should be retrieved.  If a value that is not equal
     *                  to {@link AtlassianContainerConfig.Containers#ATLASSIAN} or {@link
     *                  AtlassianContainerConfig.Containers#DEFAULT} is specified, the return value will always be
     *                  {@code null}.
     * @param parameter the value to fetch. May be specified as an x-path like object reference such as
     *                  "gadgets/features/views".
     * @return a configuration parameter as a JSON object or null if not set or can't be interpreted as JSON.
     */
    public Object getJson(String container, String parameter)
    {
        if (!Containers.ATLASSIAN.equals(container) && !Containers.DEFAULT.equals(container))
        {
            return null;
        }
        if (parameter == null)
        {
            return config;
        }

        JSONObject data = config;
        try
        {
            for (String param : parameter.split("/"))
            {
                Object next = data.get(param);
                if (next instanceof JSONObject)
                {
                    data = (JSONObject) next;
                }
                else
                {
                    return next;
                }
            }
            return data;
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    public String get(String container, String parameter)
    {
        Object data = getJson(container, parameter);
        return data == null ? null : data.toString();
    }

    public JSONObject getJsonObject(String container, String parameter)
    {
        Object data = getJson(container, parameter);
        return data instanceof JSONObject ? (JSONObject) data : null;
    }

    public JSONArray getJsonArray(String container, String parameter)
    {
        Object data = getJson(container, parameter);
        return data instanceof JSONArray ? (JSONArray) data : null;
    }
}
