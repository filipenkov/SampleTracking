package com.atlassian.plugins.rest.common.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures the interceptor chain to execute when dispatching to a resource method.  Classes will be instantiated
 * per request and autowired by the plugin's container.  The sole exception to this are default interceptors provided by
 * the framework, which are singletons.  The annotation lookup goes method -> class -> package -> default interceptors.
 *
 * @since 2.0
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorChain
{
    Class<? extends ResourceInterceptor>[] value();
}
