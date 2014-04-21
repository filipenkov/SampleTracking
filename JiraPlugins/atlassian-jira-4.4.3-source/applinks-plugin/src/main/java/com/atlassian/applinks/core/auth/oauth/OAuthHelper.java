package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.Check;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

import static com.atlassian.applinks.api.auth.Anonymous.createAnonymousRequest;
import static com.google.common.collect.Iterables.transform;

/**
 * A helper class that detects, if the remote application has the Atlassian OAuth plugin installed and can also fetch
 * the consumer information from this application.
 * <p/>
 * since 3.0
 */
public class OAuthHelper
{
    private static final String CONSUMER_INFO_PATH = "/plugins/servlet/oauth/consumer-info";

    private OAuthHelper()
    {
        /** singleton **/
    }

    public static boolean isOAuthPluginInstalled(final ApplicationLink applicationLink)
    {
        final boolean oAuthPluginInstalled = false;
        try
        {
            final Consumer consumer = fetchConsumerInformation(applicationLink);
            return consumer.getKey() != null;
        }
        catch (ResponseException e)
        {
            //ignored
        }
        return oAuthPluginInstalled;
    }

    public static Consumer fetchConsumerInformation(final ApplicationLink applicationLink) throws ResponseException
    {
        final Request<?, ?> request = createAnonymousRequest(applicationLink, Request.MethodType.GET,
                URIUtil.uncheckedConcatenate(applicationLink.getRpcUrl(), CONSUMER_INFO_PATH).toString());
        request.setHeader("Accept", "application/xml");
        final ConsumerInformationResponseHandler handler = new ConsumerInformationResponseHandler();
        request.execute(handler);
        return handler.getConsumer();
    }

    private static class ConsumerInformationResponseHandler implements ResponseHandler
    {
        private Consumer consumer;

        public void handle(final Response response) throws ResponseException
        {
            if (response.getStatusCode() != HttpServletResponse.SC_OK)
            {
                throw new ResponseException("Server responded with an error");
            }
            final String contentTypeHeader = response.getHeader("Content-Type");
            if (contentTypeHeader != null && !contentTypeHeader.toLowerCase().startsWith("application/xml"))
            {
                throw new ResponseException("Server sent an invalid response");
            }
            try
            {
                final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                final Document doc = docBuilder.parse(response.getResponseBodyAsStream());

                final String consumerKey = doc.getElementsByTagName("key").item(0).getTextContent();
                final String name = doc.getElementsByTagName("name").item(0).getTextContent();
                final PublicKey publicKey = RSAKeys.fromPemEncodingToPublicKey(doc.getElementsByTagName("publicKey").item(0).getTextContent());

                String description = null;
                if (doc.getElementsByTagName("description").getLength() > 0)
                {
                    description = doc.getElementsByTagName("description").item(0).getTextContent();
                }
                URI callback = null;
                if (doc.getElementsByTagName("callback").getLength() > 0)
                {
                    callback = new URI(doc.getElementsByTagName("callback").item(0).getTextContent());
                }

                consumer = Consumer.key(consumerKey)
                        .name(name)
                        .publicKey(publicKey)
                        .description(description)
                        .callback(callback)
                        .build();
            }
            catch (ParserConfigurationException e)
            {
                throw new ResponseException("Unable to parse consumer information", e);
            }
            catch (SAXException e)
            {
                throw new ResponseException("Unable to parse consumer information", e);
            }
            catch (IOException e)
            {
                throw new ResponseException("Unable to parse consumer information", e);
            }
            catch (DOMException e)
            {
                throw new ResponseException("Unable to parse consumer information", e);
            }
            catch (URISyntaxException e)
            {
                throw new ResponseException("Unable to parse consumer information, callback is not a valid URL", e);
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new ResponseException("Unable to parse consumer information, no RSA providers are installed", e);
            }
            catch (InvalidKeySpecException e)
            {
                throw new ResponseException("Unable to parse consumer information, the public key is not a validly encoded RSA public key", e);
            }
        }

        public Consumer getConsumer()
        {
            return consumer;
        }
    }

    /**
     * Converts the {@code Request} to an {@code OAuthMessage}.
     *
     * @param request {@code Request} to be converted to an {@code OAuthMessage}
     * @return {@code OAuthMessage} converted from the {@code Request}
     */
    public static OAuthMessage asOAuthMessage(final com.atlassian.oauth.Request request)
    {
        Check.notNull(request, "request");
        return new OAuthMessage(
            request.getMethod().name(),
            request.getUri().toString(),
            // We'd rather not do the copy, but since we need a Collection of these things we don't have much choice
            ImmutableList.copyOf(asOAuthParameters(request.getParameters()))
        );
    }

    /**
     * Converts the list of {@code Request.Parameter}s to {@code OAuth.Parameter}s.
     *
     * @param requestParameters {@code Request.Parameter}s to be converted to {@code OAuth.Parameter}s
     * @return {@code OAuth.Parameter}s converted from the {@code Request.Parameter}s
     */
    public static Iterable<OAuth.Parameter> asOAuthParameters(final Iterable<com.atlassian.oauth.Request.Parameter> requestParameters)
    {
        Check.notNull(requestParameters, "requestParameters");
        return transform(requestParameters, toOAuthParameters);
    }

    /**
     * Converts the list of {@code OAuth.Parameter}s to {@code Request.Parameter}s.
     *
     * @param oauthParameters {@code OAuth.Parameter}s to be converted to {@code Request.Parameter}s
     * @return {@code Request.Parameter}s converted from the {@code OAuth.Parameter}s
     */
    public static Iterable<com.atlassian.oauth.Request.Parameter> fromOAuthParameters(final List<? extends Map.Entry<String, String>> oauthParameters)
    {
        Check.notNull(oauthParameters, "oauthParameters");
        return transform(oauthParameters, toRequestParameters);
    }

    private static final Function<Map.Entry<String, String>, com.atlassian.oauth.Request.Parameter> toRequestParameters = new Function<Map.Entry<String, String>, com.atlassian.oauth.Request.Parameter>()
    {
        public com.atlassian.oauth.Request.Parameter apply(final Map.Entry<String, String> p)
        {
            Check.notNull(p, "parameter");
            return new com.atlassian.oauth.Request.Parameter(p.getKey(), p.getValue());
        }
    };

    private static final Function<com.atlassian.oauth.Request.Parameter, OAuth.Parameter> toOAuthParameters = new Function<com.atlassian.oauth.Request.Parameter, OAuth.Parameter>()
    {
        public OAuth.Parameter apply(final com.atlassian.oauth.Request.Parameter p)
        {
            Check.notNull(p, "parameter");
            return new OAuth.Parameter(p.getName(), p.getValue());
        }
    };
}
