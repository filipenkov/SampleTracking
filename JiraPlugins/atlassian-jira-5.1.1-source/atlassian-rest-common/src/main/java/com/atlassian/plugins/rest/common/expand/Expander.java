package com.atlassian.plugins.rest.common.expand;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

/**
 * Annotation available for types to declare which is their {@link EntityExpander}
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface Expander
{
    Class<? extends EntityExpander<?>> value();
}
