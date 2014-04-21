package com.atlassian.annotations;

import java.lang.annotation.Documented;

/**
 * The annotated element is part of a product's SPI contract with plugins.
 * <p>
 * This element is designed for plugins to <em>implement</em>.
 * <p>
 * Clients of <code>@PublicSpi</code> can expect
 * that <b>programs compiled against a given version will remain binary compatible with later versions of the
 * <code>@PublicSpi</code></b> as per each product's API policy (clients should refer to each product's API policy for
 * the exact guarantee---usually binary compatibility is guaranteed at least across minor versions).
 * <p/>
 * Note: <code>@PublicSpi</code> interfaces and classes are specifically designed to be implemented/extended by clients.
 * Hence, the guarantee of binary compatibility is different to that of <code>@PublicApi</code> elements (if an element
 * is both <code>@PublicApi</code> and <code>@PublicSpi</code>, both guarantees applie).
 * <p/>
 *
 * @see PublicApi
 */
@Documented
public @interface PublicSpi
{
}
