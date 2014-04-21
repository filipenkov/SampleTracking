package com.atlassian.crowd.service.client;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.integration.Constants;
import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.removeEnd;

/**
 * This bean is a container for the application's crowd.properties.
 */
public class ClientPropertiesImpl extends AbstractClientProperties
{
    private final Logger logger = Logger.getLogger(this.getClass());
    
    protected ClientPropertiesImpl()
    {
    }

    public void updateProperties(Properties properties)
    {
        applicationName = loadAndLogPropertyString(properties, Constants.PROPERTIES_FILE_APPLICATION_NAME);
        applicationPassword = loadPropertyString(properties, Constants.PROPERTIES_FILE_APPLICATION_PASSWORD);
        applicationAuthenticationURL = loadAndLogPropertyString(properties, Constants.PROPERTIES_FILE_APPLICATION_LOGIN_URL);

        cookieTokenKey = loadPropertyString(properties, Constants.PROPERTIES_FILE_COOKIE_TOKENKEY);

        // use default value for cookie name if none supplied
        if (cookieTokenKey == null)
        {
            cookieTokenKey = Constants.COOKIE_TOKEN_KEY;
        }

        sessionTokenKey = loadAndLogPropertyString(properties, Constants.PROPERTIES_FILE_SESSIONKEY_TOKENKEY);
        sessionLastValidation = loadAndLogPropertyString(properties, Constants.PROPERTIES_FILE_SESSIONKEY_LASTVALIDATION);
        sessionValidationInterval = loadPropertyLong(properties, Constants.PROPERTIES_FILE_SESSIONKEY_VALIDATIONINTERVAL, true);

        httpProxyHost = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_PROXY_HOST);
        httpProxyPort = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_PROXY_PORT);
        httpProxyUsername = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_PROXY_USERNAME);
        httpProxyPassword = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_PROXY_PASSWORD);

        httpMaxConnections = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_MAX_CONNECTIONS);
        httpTimeout = loadPropertyString(properties, Constants.PROPERTIES_FILE_HTTP_TIMEOUT);
        socketTimeout = loadPropertyString(properties, Constants.PROPERTIES_FILE_SOCKET_TIMEOUT);

        PasswordCredential credentials = new PasswordCredential(applicationPassword);
        applicationAuthenticationContext = new ApplicationAuthenticationContext();
        applicationAuthenticationContext.setName(applicationName);
        applicationAuthenticationContext.setCredential(credentials);

        baseURL = loadBaseURL(properties);
    }

    private long loadPropertyLong(Properties properties, String propertyName, boolean logProperty)
    {
        long propertyValue = 0L;
        String propertyValueAsString;
        if (logProperty)
        {
            propertyValueAsString = loadAndLogPropertyString(properties, propertyName);
        }
        else
        {
            propertyValueAsString = loadPropertyString(properties, propertyName);
        }

        if (propertyValueAsString != null)
        {
            propertyValue = Long.parseLong(propertyValueAsString);
        }

        return propertyValue;
    }

    public static String loadPropertyString(Properties properties, String propertyName)
    {
        String propertyValue = StringUtils.trimToNull(System.getProperty("crowd.property." + propertyName));

        if (propertyValue == null)
        {
            propertyValue = StringUtils.trimToNull(loadPropertyFromEnv(propertyName));
        }

        if (propertyValue == null && properties != null && properties.containsKey(propertyName))
        {
            propertyValue = StringUtils.trimToNull(properties.getProperty(propertyName));
        }

        return propertyValue;
    }

    private static String loadPropertyFromEnv(String propertyName)
    {
        // Convert property to environment variable name
        if (Boolean.getBoolean(Constants.USE_ENVIRONMENT_VARIABLES))
        {
            String envPropertyName = "CROWD_PROPERTY_" + propertyName.toUpperCase(Locale.ENGLISH).replace(".", "_");
            return System.getenv(envPropertyName);
        }
        return null;
    }

    protected String loadAndLogPropertyString(Properties properties, String propertyName)
    {
        String propertyValue = loadPropertyString(properties, propertyName);

        if (propertyValue != null)
        {
            logger.info("Loading property: '" + propertyName + "' : '" + propertyValue + "'");
        }
        else
        {
            logger.info("Failed to find value for property: " + propertyName);
        }

        return propertyValue;
    }

    private String loadBaseURL(Properties properties)
    {
        String baseURL = loadPropertyString(properties, Constants.PROPERTIES_FILE_BASE_URL);
        if (StringUtils.isBlank(baseURL))
        {
            baseURL = generateBaseURL(properties);
        }

        return StringUtils.removeEnd(baseURL, "/");
    }

    // We only want to trim /services from the path portion of the URI, not anywhere else.
    private String generateBaseURL(Properties properties)
    {
        final String propertyUrl = loadPropertyString(properties, Constants.PROPERTIES_FILE_SECURITY_SERVER_URL);
        // This hackery only exists because of unit testing craziness that I cannot untangle. In the real world I
        // cannot imagine any case in which you would get here.
        if (propertyUrl == null)
        {
            return null;
        }

        try
        {
            final URI uri = new URI(propertyUrl);
            final URI truncatedUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), truncatePath(uri.getPath()), uri.getQuery(), uri.getFragment());
            return truncatedUri.toString();
        }
        catch (URISyntaxException e)
        {
            return propertyUrl;
        }
    }

    private String truncatePath(final String originalPath)
    {
        final String services = "/" + Constants.CROWD_SERVICE_LOCATION;
        // handle both /services/ and /services
        final String path = removeEnd(originalPath, "/");
        final int firstSlash = StringUtils.lastIndexOf(path, "/" + Constants.CROWD_SERVICE_LOCATION);

        if (firstSlash != -1)
        {
            return path.substring(0, firstSlash);
        }
        else
        {
            return path;
        }
    }

    public static ClientPropertiesImpl newInstanceFromResourceLocator(ResourceLocator resourceLocator)
    {
        Properties properties = resourceLocator.getProperties();
        return newInstanceFromProperties(properties);
    }

    public static ClientPropertiesImpl newInstanceFromProperties(Properties properties)
    {
        ClientPropertiesImpl clientProperties = new ClientPropertiesImpl();
        clientProperties.updateProperties(properties);
        return clientProperties;
    }
}
