package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLinkResponseHandler;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.google.common.collect.ImmutableSet;
import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @since 3.2
 */
public class OAuthApplinksResponseHandler<R> implements ApplicationLinkResponseHandler<R>
{
    private static final Set<String> TOKEN_PROBLEMS = ImmutableSet.of(
            // see: http://wiki.oauth.net/ProblemReporting
            OAuth.Problems.TOKEN_EXPIRED,
            OAuth.Problems.TOKEN_REJECTED,
            OAuth.Problems.TOKEN_REVOKED
    );

    private final ApplicationLinkResponseHandler<R> applicationLinkResponseHandler;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final OAuthRequest wrappedRequest;
    private final ApplicationId applicationId;
    private final String username;
    private final boolean followRedirects;
    public static final int MAX_REDIRECTS = 3;

    private static final Logger log = LoggerFactory.getLogger(OAuthApplinksResponseHandler.class);

    public OAuthApplinksResponseHandler(
            ApplicationLinkResponseHandler<R> applicationLinkResponseHandler,
            ConsumerTokenStoreService consumerTokenStoreService,
            OAuthRequest wrappedRequest,
            ApplicationId applicationId,
            String username,
            boolean followRedirects)
    {
        this.applicationLinkResponseHandler = applicationLinkResponseHandler;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.wrappedRequest = wrappedRequest;
        this.applicationId = applicationId;
        this.username = username;
        this.followRedirects = followRedirects;
    }

    public R credentialsRequired(Response response) throws ResponseException
    {
        return applicationLinkResponseHandler.credentialsRequired(response);
    }

    public R handle(Response response) throws ResponseException
    {
        final String value = response.getHeaders().get("WWW-Authenticate");
        if (!StringUtils.isBlank(value))
        {
            for (OAuth.Parameter parameter : OAuthMessage.decodeAuthorization(value))
            {
                if ("oauth_problem".equals(parameter.getKey()))
                {
                    log.debug("OAuth request rejected by peer.\n" +
                            "Our OAuth request header: Authorization: " + wrappedRequest.getHeaders().get("Authorization") + "\n" +
                            "Full OAuth response header: WWW-Authenticate: " + value);
                    if (TOKEN_PROBLEMS.contains(parameter.getValue()))
                    {
                        try
                        {
                            consumerTokenStoreService.removeConsumerToken(applicationId, username);
                        }
                        catch (RuntimeException e)
                        {
                            log.error("Failed to delete consumer token for user '" + username + "'.", e);
                        }
                        return applicationLinkResponseHandler.credentialsRequired(response);
                    }
                    else if (OAuth.Problems.TIMESTAMP_REFUSED.equals(parameter.getValue()))
                    {
                        log.warn("Peer rejected the timestamp on our OAuth request. " +
                                "This might be due to a replay attack, but it's more " +
                                "likely our system clock is not synchronized with the " +
                                "server's clock. " +
                                "You may turn on debug logging to log the full contents " +
                                "of the OAuth response headers.");
                    }
                }
            }
        }
        else if (response.getStatusCode() >= 300 && response.getStatusCode() < 400 && followRedirects)
        {
            String location = response.getHeader("location");
            if (location != null)
            {
                if (wrappedRequest.getRedirects() < MAX_REDIRECTS)
                {
                    wrappedRequest.setRedirects(wrappedRequest.getRedirects() + 1);
                    wrappedRequest.setUrl(location);
                    return wrappedRequest.execute(this);
                }
                else
                {
                    log.warn("Maximum of " + MAX_REDIRECTS + " redirects reached. Not following redirect to '" + location + "' , returning response instead.");
                }
            }
            else
            {
                log.warn("HTTP response returned redirect code " + response.getStatusCode() + " but did not provide a location header");
            }
        }
        return applicationLinkResponseHandler.handle(response);
    }
}
