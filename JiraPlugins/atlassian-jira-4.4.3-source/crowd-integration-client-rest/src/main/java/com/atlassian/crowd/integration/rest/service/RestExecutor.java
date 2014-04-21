package com.atlassian.crowd.integration.rest.service;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCrowdServiceException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.crowd.integration.rest.service.util.ShutdownIgnoringMultiThreadedHttpConnectionManager;
import com.atlassian.crowd.service.client.ClientProperties;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This class provides primitive building blocks for using a REST API.
 */
class RestExecutor
{
    private static final String INVALID_REST_SERVICE_MSG_FORMAT = "The following URL does not specify a valid Crowd User Management REST service: %s";
    private static final String EMBEDDED_CROWD_VERSION_NAME = "X-Embedded-Crowd-Version";
    private static final Logger logger = Logger.getLogger(RestExecutor.class);

    private final ShutdownIgnoringMultiThreadedHttpConnectionManager connectionManager = new ShutdownIgnoringMultiThreadedHttpConnectionManager();

    private final String baseUrl;
    private final HttpClient client;

    /**
     * Constructs a new REST Crowd Client Executor instance.
     *
     * @param clientProperties connection parameters
     */
    RestExecutor(ClientProperties clientProperties)
    {
        try {
            final URI uri = new URI(clientProperties.getBaseURL(), false);
            baseUrl = createBaseUrl(clientProperties.getBaseURL());

            // Use ShutdownIgnoringMultiThreadedHttpConnectionManager because of FISH-411.
            client = new HttpClient(connectionManager);
            client.getParams().setAuthenticationPreemptive(true);
            final Credentials credentials = new UsernamePasswordCredentials(clientProperties.getApplicationName(), clientProperties.getApplicationPassword());
            client.getState().setCredentials(new AuthScope(uri.getHost(), -1), credentials);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(NumberUtils.toInt(clientProperties.getHttpTimeout(), 5000));
            client.getHttpConnectionManager().getParams().setSoTimeout(NumberUtils.toInt(clientProperties.getSocketTimeout(), 10 * 60 * 1000));
            client.getHttpConnectionManager().getParams().setMaxTotalConnections(NumberUtils.toInt(clientProperties.getHttpMaxConnections(), MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS));
            // Also set per host connection limit because HttpClient has a default maximum connections per host of 2
            // (see client.getHttpConnectionManager().getParams().getMaxConnectionsPerHost())
            client.getHttpConnectionManager().getParams().setMaxConnectionsPerHost(client.getHostConfiguration(),
                    NumberUtils.toInt(clientProperties.getHttpMaxConnections(), client.getHttpConnectionManager().getParams().getMaxTotalConnections()));
            initProxyConfiguration(clientProperties, client);
        }
        catch (URIException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    private void initProxyConfiguration(ClientProperties clientProperties, HttpClient client)
    {
        if (clientProperties.getHttpProxyHost() != null)
        {
            client.getHostConfiguration().setProxy(clientProperties.getHttpProxyHost(), NumberUtils.toInt(clientProperties.getHttpProxyPort(), -1));

            if (clientProperties.getHttpProxyUsername() != null && clientProperties.getHttpProxyPassword() != null)
            {
                final Credentials credentials = new UsernamePasswordCredentials(clientProperties.getHttpProxyUsername(), clientProperties.getHttpProxyPassword());
                client.getState().setProxyCredentials(new AuthScope(clientProperties.getHttpProxyHost(), -1), credentials);
            }
        }
    }

    /**
     * Returns the "root" WebResource. This is the resource that's at the / of the crowd-rest-plugin plugin namespace.
     *
     * @param url URL to derive the base URL from
     * @return base URL
     */
    private static String createBaseUrl(String url)
    {
        final StringBuilder sb = new StringBuilder(url);
        if (url.endsWith("/"))
        {
            sb.setLength(sb.length() - 1);
        }
        sb.append("/rest/usermanagement/1");

        return sb.toString();
    }

    /**
     * Creates a get method.
     *
     * @param format request url template
     * @param args template arguments
     * @return get method with the specified url
     */
    MethodExecutor get(String format, Object... args)
    {
        return new MethodExecutor(new GetMethod(buildUrl(baseUrl, format, args)));
    }

    /**
     * Creates a delete method.
     *
     * @param format request url template
     * @param args template arguments
     * @return delete method with the specified url
     */
    MethodExecutor delete(String format, Object... args)
    {
        return new MethodExecutor(new DeleteMethod(buildUrl(baseUrl, format, args)));
    }

    /**
     * Creates a post method with empty body.
     *
     * @param format request url template
     * @param args template arguments
     * @return post method with the specified url and body
     */
    MethodExecutor postEmpty(String format, Object... args)
    {
        final PostMethod method = new PostMethod(buildUrl(baseUrl, format, args));
        try
        {
            method.setRequestEntity(new StringRequestEntity("", "application/xml", "UTF-8"));
        } catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
        return new MethodExecutor(method);
    }

    /**
     * Creates a post method.
     *
     * @param body request body
     * @param format request url template
     * @param args template arguments
     * @return post method with the specified url and body
     */
    MethodExecutor post(Object body, String format, Object... args)
    {
        final PostMethod method = new PostMethod(buildUrl(baseUrl, format, args));
        setBody(method, body);
        return new MethodExecutor(method);
    }

    /**
     * Creates a put method.
     *
     * @param body request body
     * @param format request url template
     * @param args template arguments
     * @return put method with the specified url and body
     */
    MethodExecutor put(Object body, String format, Object... args)
    {
        final PutMethod method = new PutMethod(buildUrl(baseUrl, format, args));
        setBody(method, body);
        return new MethodExecutor(method);
    }

    /**
     * Set a JAXB marshalled message body to post/put method.
     *
     * @param method method to set the body to
     * @param body object that supports JAXB marshalling
     */
    private static void setBody(EntityEnclosingMethod method, Object body)
    {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        JAXB.marshal(body, bs); // TODO: Cache marshallers in the style of JAXBStringReaderProviders in Jersey Client
        method.setRequestEntity(new ByteArrayRequestEntity(bs.toByteArray(), "application/xml"));
    }

    /**
     * Builds a URL based on baseUrl, format string and arguments. String arguments are encoded according to url
     * encoding rules.
     *
     * @param baseUrl beginning of the url that will not be formatted
     * @param format rest of the url that will be formatted
     * @param args arguments for the format string
     * @return URL based on baseUrl, format string and arguments
     */
    static String buildUrl(String baseUrl, String format, Object... args)
    {
        final Object[] encodedArgs = new Object[args.length];
        final int pathArgCount = pathArgumentCount(format);
        try
        {
            for (int i = 0; i < pathArgCount; ++i)
            {
                if (args[i] instanceof String)
                {
                    encodedArgs[i] = URIUtil.encodeWithinPath((String) args[i]);
                }
                else
                {
                    encodedArgs[i] = args[i];
                }
            }
            for (int i = pathArgCount; i < args.length; ++i)
            {
                if (args[i] instanceof String)
                {
                    encodedArgs[i] = URIUtil.encodeWithinQuery((String) args[i]);
                }
                else
                {
                    encodedArgs[i] = args[i];
                }
            }
            final String url = baseUrl + String.format(format, encodedArgs);

            logger.debug("Constructed " + url);

            return url;
        }
        catch (URIException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Counts the amount of format specifiers in the path section of the format string.
     *
     * @param format format string containing zero or more format specifiers
     * @return the amount of format specifiers in the path section of the format string.
     */
    static int pathArgumentCount(String format)
    {
        int queryStart = format.indexOf('?');
        if (queryStart == -1)
        {
            queryStart = format.length();
        }

        int count = 0;
        for (int i = format.indexOf('%'); i != -1 && i < queryStart; i = format.indexOf('%', i + 1))
        {
            ++count;
        }
        return count;
    }

    void shutDown()
    {
        connectionManager.reallyShutdown();
    }

    /**
     * This class takes a method to perform and provides multiple ways to execute
     * it and handle the response.
     */
    class MethodExecutor
    {
        final HttpMethod method;

        /**
         *
         * @param method HTTP method to perform
         */
        MethodExecutor(HttpMethod method)
        {
            this.method = method;
        }

        /**
         * Performs an HTTP request and returns a result.
         *
         * @param returnType type of the result
         * @return entity type
         * @throws com.atlassian.crowd.exception.OperationFailedException if the operation failed for unknown reason
         * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the application does not have permission to perform the operation
         */
        <T> T andReceive(Class<T> returnType)
                throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, CrowdRestException
        {
            method.setRequestHeader("Accept", "application/xml");

            try
            {
                final int statusCode = executeCrowdServiceMethod(method);
                if (!isSuccess(statusCode))
                {
                    throwError(statusCode, method);
                    throw new OperationFailedException(method.getStatusText());
                }

                 // TODO: Cache unmarshallers in the style of JAXBStringReaderProviders in Jersey Client
                return JAXB.unmarshal(method.getResponseBodyAsStream(), returnType);
            }
            catch (IOException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                method.releaseConnection();
            }
        }

        /**
         * Performs an HTTP request and returns true if the operation succeeded and false if HTTP error code 404 was returned.
         *
         * @return true if the operation succeeded and false if HTTP error code 404 was returned
         * @throws IllegalArgumentException if the error code is 400
         * @throws OperationFailedException if the operation failed for unknown reason
         * @throws ApplicationPermissionException if the application does not have permission to perform the operation
         */
        boolean doesExist()
                throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, CrowdRestException
        {
            try
            {
                final int statusCode = executeCrowdServiceMethod(method);

                if (isSuccess(statusCode))
                {
                    return true;
                }
                else if (statusCode == HttpStatus.SC_NOT_FOUND)
                {
                    return false;
                }
                else
                {
                    throwError(statusCode, method);
                    throw new OperationFailedException(method.getStatusText());
                }
            }
            catch (IOException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                method.releaseConnection();
            }
        }

        /**
         * Performs an HTTP request and discards the result.
         *
         * @throws OperationFailedException if the operation failed for unknown reason
         * @throws ApplicationPermissionException if the application does not have permission to perform the operation
         */
        void andCheckResponse()
                throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, CrowdRestException
        {
            method.setRequestHeader("Accept", "application/xml");
            try
            {
                final int statusCode = executeCrowdServiceMethod(method);

                if (!isSuccess(statusCode))
                {
                    throwError(statusCode, method);
                    throw new OperationFailedException(method.getStatusText());
                }
            }
            catch (IOException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                method.releaseConnection();
            }
        }

        /**
         * Return true if the status code is successful (2xx).
         *
         * @param statusCode HTTP status code
         * @return true if the status code is successful (2xx)
         */
        private boolean isSuccess(int statusCode)
        {
            return statusCode >= 200 && statusCode < 300;
        }

        /**
         * Executes the method on a Crowd service. An
         * <tt>OperationFailedException</tt> is thrown if the method was not executed on a valid Crowd
         * service.
         *
         * @param method HttpMethod
         * @return status code
         * @throws InvalidCrowdServiceException if the method was not executed on a valid Crowd REST service.
         */
        int executeCrowdServiceMethod(final HttpMethod method) throws InvalidCrowdServiceException, IOException
        {
            final int statusCode = client.executeMethod(method);
            if (!isCrowdRestService(method))
            {
                throw new InvalidCrowdServiceException(String.format(INVALID_REST_SERVICE_MSG_FORMAT, method.getURI().toString()));
            }
            return statusCode;
        }

        /**
         * Returns <tt>true</tt> if the response comes from a valid Crowd REST service, otherwise false.
         *
         * @param method HttpMethod after execution of the method
         * @return <tt>true</tt> if the response comes from a valid Crowd REST service.
         */
        private boolean isCrowdRestService(final HttpMethod method)
        {
            // a valid Crowd REST service would have the Embedded Crowd version in the response header
            return method.getResponseHeader(EMBEDDED_CROWD_VERSION_NAME) != null;
        }
    }
    
    /**
     * Throws exception based on HTTP status code
     *
     * @param errorCode HTTP error code
     * @param method HTTP method after execution of the method
     * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the error code is 403 (Forbidden)
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the error code is 401 (Unauthorized)
     * @throws com.atlassian.crowd.integration.rest.service.CrowdRestException for responses with an XML ErrorEntity body
     * @throws OperationFailedException for all other responses
     */
    static void throwError(int errorCode, HttpMethod method)
        throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, CrowdRestException
    {
        try
        {
            if (errorCode == HttpStatus.SC_FORBIDDEN)
            {
                throw new ApplicationPermissionException(method.getResponseBodyAsString());
            }
            else if (errorCode == HttpStatus.SC_UNAUTHORIZED)
            {
                throw new InvalidAuthenticationException(method.getResponseBodyAsString());
            }
            else if (errorCode >= 300)
            {
                ErrorEntity errorEntity = JAXB.unmarshal(method.getResponseBodyAsStream(), ErrorEntity.class);
                throw new CrowdRestException(errorEntity.getMessage(), errorEntity, method.getStatusCode());
            }
        }
        catch (IOException e)
        {
            throw new OperationFailedException(method.getStatusText());
        }
        catch (DataBindingException dbe)
        {
            throw new OperationFailedException(method.getStatusText());
        }
    }
}
