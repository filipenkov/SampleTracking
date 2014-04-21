package com.atlassian.gadgets.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Utility class for dealing with URIs. 
 */
public class Uri
{
    private Uri()
    {
        throw new AssertionError("noninstantiable");
    }

    /**
     * Returns the decoded part of the URI.  Always uses UTF-8 encoding which is required for all JVMs to support and
     * so doesn't require you to catch an UnsupportedEncodingException.
     * 
     * @param uriComponent part of the URI to decode
     * @return the decoded part of the URI
     */
    public static String decodeUriComponent(String uriComponent)
    {
        try
        {
            return URLDecoder.decode(uriComponent, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error("JREs are required to support UTF-8");
        }
    }

    /**
     * Returns the encoded part of the URI.  Always uses UTF-8 encoding which is required for all JVMs to support and
     * so doesn't require you to catch an UnsupportedEncodingException.
     * 
     * @param uriComponent part of the URI to encode
     * @return the encoded part of the URI
     */
    public static String encodeUriComponent(String uriComponent)
    {
        try
        {
            return URLEncoder.encode(uriComponent, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new Error("JREs are required to support UTF-8");
        }
    }
    
    /**
     * Returns {@code true} if the gadget URI is a valid {@link URI} and begins with http:// or https://, 
     * {@code false} otherwise.
     * 
     * @param gadgetUri URI to validate
     * @return {@code true} if the gadget URI is a valid {@link URI} and begins with http:// or https://, {@code false}
     *         otherwise.
     */
    public static boolean isValid(String gadgetUri)
    {
        if (isBlank(gadgetUri))
        {
            return false;
        }
        
        try
        {
            URI uri = new URI(gadgetUri).normalize();
            if (uri.isAbsolute() && !"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
            {
                return false;
            }
        }
        catch (URISyntaxException e)
        {
            return false;
        }
        return true;
    }
    
    /**
     * Attempts to convert the {@code gadgetUri} to a {@code URI} object.  If the URI is an invalid URI or does not
     * begin with http:// or https://, throws an {@code InvalidGadgetSpecUriException}.
     *  
     * @param gadgetUri value to be converted to a {@code URI} object 
     * @return {@code gadgetUri} converted to a {@code URI} object
     * @throws com.atlassian.gadgets.GadgetSpecUriNotAllowedException thrown if the value is not a valid {@code URI} or does not start with
     *                                       http:// or https://
     */
    public static URI create(String gadgetUri) throws GadgetSpecUriNotAllowedException
    {
        if (!isValid(gadgetUri))
        {
            throw new GadgetSpecUriNotAllowedException("gadget spec url must be a valid url beginning with either http: or https:");
        }
        return URI.create(gadgetUri);
    }


    /**
     * Returns a "/"-terminated {@code String}.
     * @param url the URL to be "/"-terminated.
     * @return a "/"-terminated {@String} representing the URL
     */
    public static String ensureTrailingSlash(String url)
    {
        return url.endsWith("/") ? url : url + "/";
    }

    /**
     * Resolves a possibly relative URI against a specified base. Resolution occurs according to the rules
     * defined in {@see java.net.URI.resolve(java.net.URI)}.
     * @param baseUrl the base URI to resolve against
     * @param possiblyRelativeUri the relative URI to be resolved. If this is already absolute, it will not be resolved against the base
     * @return the resolved URI
     */
    public static URI resolveUriAgainstBase(String baseUrl, URI possiblyRelativeUri)
    {
        checkNotNull(baseUrl, "baseUrl");
        checkNotNull(possiblyRelativeUri, "possiblyRelativeUri");
        return possiblyRelativeUri.isAbsolute()
               ? possiblyRelativeUri
               : URI.create(ensureTrailingSlash(baseUrl)).resolve(possiblyRelativeUri);
    }

    /**
     * Resolves a possibly relative URI (in String form) against a specified base. Resolution occurs according to the rules
     * defined in {@see java.net.URI.resolve(java.net.URI)}.
     * @param baseUrl the base URI to resolve against
     * @param possiblyRelativeUri the relative URI to be resolved. If this is already absolute, it will not be resolved against the base
     * @return the resolved URI
     */
    public static URI resolveUriAgainstBase(String baseUrl, String possiblyRelativeUri)
    {
        return resolveUriAgainstBase(baseUrl, URI.create(possiblyRelativeUri));
    }

    /**
     * Relativize a possibly absolute URI against a specified base. Relativization occurs according to the rules
     * defined in {@see java.net.URI.relativize(java.net.URI)}.
     * @param baseUrl the base URI to relativize against
     * @param possiblyAbsoluteUri the absolute URI to be relativize. If this is already relative, it will not be relativize against the base
     * @return the relativized URI
     */
    public static URI relativizeUriAgainstBase(String baseUrl, URI possiblyAbsoluteUri)
    {
        checkNotNull(baseUrl, "baseUrl");
        checkNotNull(possiblyAbsoluteUri, "possiblyAbsoluteUri");
        return possiblyAbsoluteUri.isAbsolute()
               ? URI.create(ensureTrailingSlash(baseUrl)).relativize(possiblyAbsoluteUri)
               : possiblyAbsoluteUri;
    }

    /**
     * Relativize a possibly absolute URI against a specified base. Relativization occurs according to the rules
     * defined in {@see java.net.URI.relativize(java.net.URI)}.
     * @param baseUrl the base URI to relativize against
     * @param possiblyAbsoluteUri the absolute URI to be relativize. If this is already relative, it will not be relativize against the base
     * @return the relativized URI
     */
    public static URI relativizeUriAgainstBase(String baseUrl, String possiblyAbsoluteUri)
    {
        return relativizeUriAgainstBase(baseUrl, URI.create(possiblyAbsoluteUri));
    }
}
