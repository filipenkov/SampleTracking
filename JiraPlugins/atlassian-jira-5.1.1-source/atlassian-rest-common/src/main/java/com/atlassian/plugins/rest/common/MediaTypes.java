package com.atlassian.plugins.rest.common;

import javax.ws.rs.core.MediaType;

/**
 * This class holds additional media types to the one present in {@link MediaType}.
 */
public final class MediaTypes
{
    public final static String APPLICATION_JAVASCRIPT = "application/javascript";
    public final static MediaType APPLICATION_JAVASCRIPT_TYPE = new MediaType("application", "javascript");

    public final static String MULTIPART_MIXED = "multipart/mixed";
    public final static MediaType MULTIPART_MIXED_TYPE = new MediaType("multipart", "mixed");

    private MediaTypes()
    {
    }
}
