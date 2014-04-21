package com.atlassian.plugins.rest.common.multipart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for injecting a cached file.  Must be on a parameter of type MultipartFormPart
 *
 * @since 2.4
 */
@Target(ElementType.PARAMETER)
@Retention (RetentionPolicy.RUNTIME)
public @interface MultipartFormParam
{
    /**
     * The name of the file parameter
     * 
     * @return The name of the file parameter
     */
    String value();
}
