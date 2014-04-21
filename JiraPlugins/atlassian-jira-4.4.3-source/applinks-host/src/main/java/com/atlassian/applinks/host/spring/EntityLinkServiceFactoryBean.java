package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a proxy for {@link EntityLinkService}
 *
 * @since 3.0
 */
@Component
public class EntityLinkServiceFactoryBean extends AbstractAppLinksServiceFactoryBean
{
    @Autowired
    public EntityLinkServiceFactoryBean(final OsgiContainerManager osgiContainerManager)
    {
        super(osgiContainerManager, MutatingEntityLinkService.class);
    }
}