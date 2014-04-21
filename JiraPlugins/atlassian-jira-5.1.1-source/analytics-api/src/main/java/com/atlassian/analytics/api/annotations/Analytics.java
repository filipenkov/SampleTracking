package com.atlassian.analytics.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides the event name for an analytics event.
 * @since 2.12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Analytics {
    /**
     * Provides the event name for an analytics event.
     */
    String value() default "";
}
