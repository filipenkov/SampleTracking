package com.atlassian.plugins.rest.common.multipart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration for multi part form parsing
 *
 * @since 2.4
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface MultipartConfigClass
{
    /**
     * The class containing the multipart configuration.  This class must have a noarg constructor.
     *
     * @return The multipart configuration class
     */
    Class<? extends MultipartConfig> value();
}
