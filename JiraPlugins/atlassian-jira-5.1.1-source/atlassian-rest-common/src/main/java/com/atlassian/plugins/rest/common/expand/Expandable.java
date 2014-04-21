package com.atlassian.plugins.rest.common.expand;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

/**
 * This is an annotation to design field on REST resources that can be expanded.
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface Expandable
{
    /**
     * The value to match with the expansion parameter to determine whether the given field should be expanded.
     * @return the parameter value to match for expansion.
     */
    String value() default "";
}
