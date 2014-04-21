package com.atlassian.plugins.rest.common.multipart.jersey;

import com.atlassian.plugins.rest.common.multipart.MultipartHandler;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@Provider
public class MultipartHandlerInjectableProvider extends SingletonTypeInjectableProvider<Context, MultipartHandler>
{
    /**
     * Construct a new instance with the Type and the instance.
     * @param handler the instance.
     */
    public MultipartHandlerInjectableProvider(MultipartHandler handler)
    {
        super(MultipartHandler.class, handler);
    }
}
