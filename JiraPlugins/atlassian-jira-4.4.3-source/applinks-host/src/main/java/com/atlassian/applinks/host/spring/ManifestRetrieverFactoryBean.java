package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a proxy for {@link com.atlassian.applinks.spi.manifest.ManifestRetriever}
 *
 * @since   3.0
 */
@Component
public class ManifestRetrieverFactoryBean extends AbstractAppLinksServiceFactoryBean {

    @Autowired
    public ManifestRetrieverFactoryBean(final OsgiContainerManager osgiContainerManager)
    {
        super(osgiContainerManager, ManifestRetriever.class);
    }
}
