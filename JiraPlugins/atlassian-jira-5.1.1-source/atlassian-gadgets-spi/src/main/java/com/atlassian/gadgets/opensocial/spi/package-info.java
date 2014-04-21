/**
 * <p>Contains the service provider interfaces (SPIs) for OpenSocial requests. The interfaces in this subpackage are <b>optional</b>. The AG opensocial-plugin
 * will successfully load if these services are not provided. However, certain features (in this case the opensocial feature)
 * will not function properly.
 *
 * <p>Parameters are non-null by default, as declared by the package annotation.  Return values should also be
 * considered to be non-null unless they are annotated with {@link javax.annotation.Nullable}.
 */
@ParametersAreNonnullByDefault
package com.atlassian.gadgets.opensocial.spi;

import javax.annotation.ParametersAreNonnullByDefault;