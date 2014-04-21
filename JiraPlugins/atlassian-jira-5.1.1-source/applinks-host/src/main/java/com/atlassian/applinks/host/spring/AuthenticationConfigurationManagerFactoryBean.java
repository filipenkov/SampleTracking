package com.atlassian.applinks.host.spring;

import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Exposes the {@link AuthenticationConfigurationManager} component to the
 * host application.
 *
 * @since   3.0
 */
@Component
public class AuthenticationConfigurationManagerFactoryBean extends AbstractAppLinksServiceFactoryBean
{
    @Autowired
    public AuthenticationConfigurationManagerFactoryBean(final OsgiContainerManager osgiContainerManager)
    {
        super(osgiContainerManager, AuthenticationConfigurationManager.class);
    }
}
