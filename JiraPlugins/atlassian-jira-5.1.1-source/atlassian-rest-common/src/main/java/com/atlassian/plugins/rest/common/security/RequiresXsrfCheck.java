package com.atlassian.plugins.rest.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation used to indicate that a method needs XSRF protection checking
 *
 * @since 2.4
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface RequiresXsrfCheck
{
}
