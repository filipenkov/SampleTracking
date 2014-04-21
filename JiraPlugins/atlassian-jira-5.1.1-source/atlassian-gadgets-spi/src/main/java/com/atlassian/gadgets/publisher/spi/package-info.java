/**
 * <p>Contains the service provider interfaces (SPIs) that host applications <b>must</b> implement in
 * order to use the dashboard plugin(s).</p>
 *
 * <p>This package contains classes related to publishing gadgets.</p>
 *
 * <p>Parameters are non-null by default, as declared by the package annotation.  Return values should also be
 * considered to be non-null unless they are annotated with {@link javax.annotation.Nullable}.  Unfortunately, there is
 * no way to declare this with annotations at present.</p>
 */
package com.atlassian.gadgets.publisher.spi;