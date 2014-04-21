package com.atlassian.applinks.spi.application;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;

/**
 * This SPI interface allows additional {@link EntityType}s to be registered via the administrative UI and made
 * available to consumers via the various applinks services. Implementations should be registered in
 * {@code atlassian-plugin.xml} using the {@code applinks-entity-type} module type, for example:
 * <pre>
 * {@code
 * <applinks-entity-type key="myExtensionEntityType" class="my.company.applinks.extension.MyEntityType"/>
 * }
 * </pre>
 * New {@link EntityType}s may be registered for both the {@link ApplicationType}s provided by the applinks API and/or
 * {@link NonAppLinksEntityType} provided by applinks extension plugins.
 *
 * @since 3.0
 */
public interface NonAppLinksEntityType extends EntityType, IdentifiableType
{
}
