package com.atlassian.plugins.rest.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to indicate that a method allows Cors preflight and simple cross-origin requests.  See
 * http://www.w3.org/TR/cors for more information.
 *
 * @since 2.6
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE})
public @interface CorsAllowed
{
}
