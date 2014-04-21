package com.atlassian.security.auth.trustedapps;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

/**
 * DefaultRequestValidor aggregates IP and URL matchers and throws an exception if they do not match.
 */
public class DefaultRequestValidator implements RequestValidator
{
    private final IPMatcher ipMatcher;
    private final URLMatcher urlMatcher;

    public DefaultRequestValidator(IPMatcher ipMatcher, URLMatcher urlMatcher)
    {
        Null.not("ipMatcher", ipMatcher);
        Null.not("urlMatcher", urlMatcher);

        this.ipMatcher = ipMatcher;
        this.urlMatcher = urlMatcher;
    }

    public void validate(HttpServletRequest request) throws InvalidRequestException
    {
        validateRemoteRequestIP(request);
        validateXForwardedFor(request);
        validateRequestURL(request);
    }

    private void validateRemoteRequestIP(HttpServletRequest request) throws InvalidIPAddressException
    {
        final String remoteAddr = request.getRemoteAddr();
        if (!ipMatcher.match(remoteAddr))
        {
            throw new InvalidRemoteAddressException(remoteAddr);
        }
    }

    private void validateXForwardedFor(HttpServletRequest request) throws InvalidXForwardedForAddressException
    {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(forwardedFor, ",");
            while (tokenizer.hasMoreTokens())
            {
                String token = tokenizer.nextToken();
                if (token.trim().length() > 0)
                {
                    if (!ipMatcher.match(token.trim()))
                    {
                        throw new InvalidXForwardedForAddressException(token);
                    }
                }
            }
        }
    }

    private void validateRequestURL(HttpServletRequest request) throws InvalidRequestUrlException
    {
        final String pathInfo = getPathInfo(request);
        if (!urlMatcher.match(pathInfo))
        {
            throw new InvalidRequestUrlException(pathInfo);
        }
    }

    private String getPathInfo(HttpServletRequest request)
    {
        String context = request.getContextPath();
        String uri = request.getRequestURI();
        if (context != null && context.length() > 0) return uri.substring(context.length());
        else return uri;
    }
}