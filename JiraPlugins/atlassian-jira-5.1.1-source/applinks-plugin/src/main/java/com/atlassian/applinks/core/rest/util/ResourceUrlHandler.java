package com.atlassian.applinks.core.rest.util;

public class ResourceUrlHandler
{
    private static final String SERVLET_CONTEXT = "/plugins/servlet/applinks/";
    private static final String REST_CONTEXT = "/rest/applinks/1.0/";

    private final String baseUrl;

    public ResourceUrlHandler(final String baseUrl)
    {
        if (baseUrl == null || baseUrl.length() == 0) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }
        this.baseUrl = trimTailingSlash(baseUrl);
    }

    public String rest(final String context) {
        return baseUrl + REST_CONTEXT + trimLeadingSlash(context);
    }

    public String servlet(final String context) {
        return baseUrl + SERVLET_CONTEXT + trimLeadingSlash(context);
    }

    private static String trimTailingSlash(final String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static String trimLeadingSlash(final String s) {
        return s.startsWith("/") ? s.substring(1, s.length()) : s;
    }

}
