package com.atlassian.plugins.rest.module.json;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import static javax.ws.rs.core.HttpHeaders.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonWithPaddingResponseFilter implements ContainerResponseFilter
{
    private final String callbackFunctionParameterName;

    public JsonWithPaddingResponseFilter()
    {
        this("jsonp-callback");
    }

    public JsonWithPaddingResponseFilter(String callbackFunctionParameterName)
    {
        Validate.notEmpty(callbackFunctionParameterName);
        this.callbackFunctionParameterName = callbackFunctionParameterName;
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse response)
    {
        if (isJsonWithPadding(request, response))
        {
            response.setContainerResponseWriter(new JsonWithPaddingResponseAdapter(getCallbackFunction(request), response.getContainerResponseWriter()));
        }
        return response;
    }

    private boolean isJsonWithPadding(ContainerRequest request, ContainerResponse response)
    {
        return isJsonResponse(response) && isCallbackRequest(request);
    }

    private boolean isCallbackRequest(ContainerRequest request)
    {
        return StringUtils.isNotBlank(getCallbackFunction(request));
    }

    private String getCallbackFunction(ContainerRequest request)
    {
        return request.getQueryParameters().getFirst(callbackFunctionParameterName);
    }

    private boolean isJsonResponse(ContainerResponse response)
    {
        final MultivaluedMap<String, Object> httpHeaders = response.getHttpHeaders();
        return httpHeaders.containsKey(CONTENT_TYPE) && httpHeaders.getFirst(CONTENT_TYPE).equals(MediaType.APPLICATION_JSON_TYPE);
    }
}
