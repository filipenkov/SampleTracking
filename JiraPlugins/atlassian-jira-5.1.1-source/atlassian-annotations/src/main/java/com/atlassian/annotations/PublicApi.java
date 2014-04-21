package com.atlassian.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element is part of a product's API contract with plugins.
 * <p>
 * This element is designed for plugins to <em>consume</em> (call its methods).
 * <p>
 * Clients of <code>@PublicApi</code> can expect
 * that <b>programs compiled against a given version will remain binary compatible with later versions of the
 * <code>@PublicApi</code></b> as per each product's API policy <b>as long as the client does not implement/extend
 * <code>@PublicApi</code> interfaces or classes</b> (refer to each product's API policy for the exact
 * guarantee---usually binary compatibility is guaranteed at least across minor versions).
 * <p/>
 * Note: since <code>@PublicApi</code> interfaces and classes are not designed to be implemented or extended by clients,
 * we may perform certain types of binary-incompatible changes to these classes and interfaces, but these will not
 * affect well-behaved clients that do not extend/implement these types (in general, only classes and interfaces
 * annotated with {@link PublicSpi} are safe to extend/implement).
 *
 * @see PublicSpi
 * @see <a href="http://java.sun.com/docs/books/jls/second_edition/html/binaryComp.doc.html">binary compatibility</a>
 */
@Documented
@Retention (RetentionPolicy.CLASS)
public @interface PublicApi
{
}
