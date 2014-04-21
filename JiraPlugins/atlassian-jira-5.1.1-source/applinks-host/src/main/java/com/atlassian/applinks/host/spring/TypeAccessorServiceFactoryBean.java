package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a proxy for {@link TypeAccessor}.
 *
 * @since 3.0
 */
@Component
public class TypeAccessorServiceFactoryBean extends AbstractAppLinksServiceFactoryBean
{
    @Autowired
    public TypeAccessorServiceFactoryBean(final OsgiContainerManager osgiContainerManager)
    {
        super(osgiContainerManager, TypeAccessor.class);
    }
}
