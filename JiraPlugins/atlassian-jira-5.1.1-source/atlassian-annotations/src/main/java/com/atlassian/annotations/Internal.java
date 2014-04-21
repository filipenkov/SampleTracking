package com.atlassian.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element is an internal implementation detail and will change without notice.
 * <p/>
 * Clients that depend on <code>@Internal</code> classes and interfaces can not expect to be compatible with any version
 * other than the version they were compiled against (even minor version and milestone releases may break binary
 * compatibility with respect to <code>@Internal</code> elements).
 */
@Documented
@Retention (RetentionPolicy.CLASS)
public @interface Internal
{
}
