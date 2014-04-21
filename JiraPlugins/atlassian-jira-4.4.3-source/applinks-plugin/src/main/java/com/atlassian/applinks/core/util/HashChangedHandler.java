package com.atlassian.applinks.core.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class manages the last modified date of a single HTTP resource.
 */
public class HashChangedHandler
{
//    private int hashCode;
    private String etag;
//    private static final int ONE_SECOND_MILLIS = 1000;

    public HashChangedHandler(int hashCode)
    {
        modified(hashCode);
    }
    /**
     * Check whether we need to generate a response for this request. Set the necessary headers on the response, and if
     * we don't need to provide content, set the response status to 304.
     *
     * If this method returns true, the caller should not perform any more processing on the request.
     *
     * @return true if we don't need to provide any data to satisfy this request
     */
    public boolean checkRequest(HttpServletRequest request, HttpServletResponse response)
    {
        return checkRequest(request, response, etag);
    }

    private void modified(int hashCode)
    {
        etag = calculateEtag(hashCode);
    }

    private static String calculateEtag(int hashCode)
    {
        return "\"" + hashCode + "\"";
    }

    /**
     * This static method is used when the resource being served by the servlet keeps track of the object hashcode,
     * and so no state needs to be maintained by this handler.
     */
    public static boolean checkRequest(HttpServletRequest request, HttpServletResponse response, int hashCode)
    {
        return checkRequest(request, response, calculateEtag(hashCode));
    }

    private static boolean checkRequest(HttpServletRequest request, HttpServletResponse response, String etagString)
    {
        if ("true".equals(System.getProperty("atlassian.disable.caches", "false")))
                return false;

//        response.setDateHeader("Last-Modified",lastModified);
        response.setHeader("ETag",etagString);

        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (noConditionalGetHeadersFound(ifModifiedSince, ifNoneMatch)
//            || isContentModifiedSince(ifModifiedSince, lastModified)
            || !etagMatches(ifNoneMatch, etagString))
        {
            return false;
        }
        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        return true;
    }

    private static boolean etagMatches(String ifNoneMatch, String etagString)
    {
        return ifNoneMatch != null && ifNoneMatch.equals(etagString);
    }

//    private static boolean isContentModifiedSince(long ifModifiedSince, long lastModified)
//    {
//        return ifModifiedSince != -1 && ifModifiedSince < lastModified;
//    }

    private static boolean noConditionalGetHeadersFound(long ifModifiedSince, String ifNoneMatch)
    {
        return ifModifiedSince == -1 && ifNoneMatch == null;
    }
}