package com.atlassian.applinks.core.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class URIUtil
{
    private static final Pattern REDUNDANT_SLASHES = Pattern.compile("//+");

    public static String concatenate(final String base, final String... paths)
    {
        return StringUtils.stripEnd(base, "/") +
                removeRedundantSlashes("/" + StringUtils.join(paths, "/"));
    }

    public static URI concatenate(final URI base, final String... paths) throws URISyntaxException
    {
        return new URI(concatenate(base.toASCIIString(), paths));
    }

    public static URI concatenate(final URI base, final URI... paths)
    {
        try
        {
            final String[] pathStrings = Iterables.toArray(
                    Lists.transform(Lists.newArrayList(paths), new Function<URI, String>()
                    {
                        public String apply(@Nullable URI from)
                        {
                            return from.toASCIIString();
                        }
                    }),
                    String.class);
            return concatenate(base, pathStrings);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Failed to concatenate URIs", e);
        }
    }

    /**
     * Equivalent to {@code URLEncoder.encode(string, "UTF-8");}
     */
    public static String utf8Encode(final String string)
    {
        try
        {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 not installed!?", e);
        }
    }

    public static String utf8Decode(final String string)
    {
        try
        {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 not installed!?", e);
        }
    }

    /**
     * Equivalent to {@code URLEncoder.encode(uri.toASCIIString(), "UTF-8")}
     */
    public static String utf8Encode(final URI uri)
    {
        return utf8Encode(uri.toASCIIString());
    }

    public static URI uncheckedToUri(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(String.format("Failed to convert %s to URI (%s)", uri, e.getReason()), e);
        }
    }

    /**
     * ONLY to be used if the caller is <strong>certain</strong> that the base and path will form a valid URI when concatenated.
     */
    public static URI uncheckedConcatenateAndToUri(final String base, final String... paths)
    {
        return uncheckedToUri(concatenate(base, paths));
    }

    /**
     * ONLY to be used if the caller is <strong>certain</strong> that the base and path will form a valid URI when concatenated.
     */
    public static URI uncheckedConcatenate(final URI base, final String... paths)
    {
        try
        {
            return concatenate(base, paths);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(String.format("Failed to concatenate %s to form URI (%s)", base, e.getReason()), e);
        }
    }

    /**
     * ONLY to be used if the caller is <strong>certain</strong> that the supplied String is a valid URI.
     */
    public static URI uncheckedCreate(final String uri)
    {
        try
        {
            return new URI(uri);
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(String.format("%s is not a valid URI (%s)", uri, e.getReason()), e);
        }
    }
    /**
     * <p>
     * Reduces sequences of more than one consecutive forward slash ("/") to a
     * single slash (see: https://studio.atlassian.com/browse/PLUG-597).
     * </p>
     *
     * @param path  any string, including {@code null} (e.g. {@code "foo//bar"})
     * @return  the input string, with all sequences of more than one
     * consecutive slash removed (e.g. {@code "foo/bar"})
     */
    public static String removeRedundantSlashes(final String path)
    {
        return path == null ? null : REDUNDANT_SLASHES.matcher(path).replaceAll("/");
    }

    public static URI copyOf(final URI uri)
    {
        if (uri == null)
        {
            return null;
        }

        try
        {
            return new URI(uri.toASCIIString());
        }
        catch (URISyntaxException e)
        {
            //this should never happen, but is there a better way to copy URIs?
            throw new RuntimeException("Failed to copy URI: " + uri.toASCIIString());
        }
    }


}
