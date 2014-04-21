package com.atlassian.applinks.spi.application;

import com.atlassian.applinks.api.ApplicationType;

/**
 * This SPI interface allows additional {@link ApplicationType}s to be registered via the administrative UI and made
 * available to consumers via the various applinks services. Implementations should be registered in
 * {@code atlassian-plugin.xml} using the {@code applinks-application-type} module type, for example:
 * <pre>
 * {@code
 * <applinks-application-type key="myExtensionApplicationType" class="my.company.applinks.extension.MyApplicationType"/>
 * }
 * </pre>
 *
 * @since 3.0
 */
public interface NonAppLinksApplicationType extends ApplicationType, IdentifiableType
{
}
