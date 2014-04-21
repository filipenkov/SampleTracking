package com.atlassian.plugins.rest.module.jersey;

import java.util.List;
import java.util.Map;

class HeaderHelper
{
    static String getSingleHeaderValue(final Map<String, List<String>> headerMap, final String headerName, final String defaultValue)
    {
        String value = defaultValue;
        List<String> contentTypeHeaders = headerMap.get(headerName);
        if (contentTypeHeaders != null && contentTypeHeaders.size() == 1)
        {
            value = contentTypeHeaders.get(0);
        }
        return value;
    }
}
