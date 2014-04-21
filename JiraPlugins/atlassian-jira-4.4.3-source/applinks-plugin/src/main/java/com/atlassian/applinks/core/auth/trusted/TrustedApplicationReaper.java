package com.atlassian.applinks.core.auth.trusted;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.event.BeforeApplicationLinkDeletedEvent;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Listens for {@link com.atlassian.applinks.core.event.BeforeApplicationLinkDeletedEvent}s
 * and removes inbound Trusted Apps configuration when an application link is
 * being removed.
 *
 * @since v3.0
 */
public class TrustedApplicationReaper implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final TrustedApplicationsConfigurationManager trustedAppsManager;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TrustedApplicationReaper(final EventPublisher eventPublisher,
                  final TrustedApplicationsConfigurationManager trustedAppsManager)
    {
        this.eventPublisher = eventPublisher;
        this.trustedAppsManager = trustedAppsManager;
        eventPublisher.register(this);
    }

    @EventListener
    public void onApplicationLinkDeleted(final BeforeApplicationLinkDeletedEvent deletedEvent)
    {
        final ApplicationLink link = deletedEvent.getApplicationLink();
        final Object value = link.getProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID);
        if (value != null)
        {
            trustedAppsManager.deleteApplication(value.toString());
            logger.debug("Removed certificate (trusted apps Id: {}) for deleted application link {}",
                    value.toString(),
                    link.getId());
        }
    }

    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
