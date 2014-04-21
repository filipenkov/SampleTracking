package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a proxy for {@link ApplicationLinkService}
 *
 * @since 3.0
 */
@Component
public class ApplicationLinkServiceFactoryBean extends AbstractAppLinksServiceFactoryBean
{
    @Autowired
    public ApplicationLinkServiceFactoryBean(final OsgiContainerManager osgiContainerManager)
    {
        super(osgiContainerManager, MutatingApplicationLinkService.class);
    }
}
