package com.atlassian.applinks.core.auth.trusted;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.TrustedAppsAuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.security.auth.trustedapps.Application;
import com.atlassian.security.auth.trustedapps.ApplicationRetriever;
import com.atlassian.security.auth.trustedapps.RequestConditions;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * Plugin component used to share the configuration logic between all 3
 * servlets and the plugin module.
 *
 * @since   v3.0
 */
public class TrustConfigurator
{
    /**
     * <p>
     * 10 seconds is the default certificate timeout. Note that this is our
     * only protection against replay attacks, but we cannot make it very small
     * or we'll run into network latency problems and clock synchronisation
     * issues.
     * </p>
     * See:
     * <li>http://confluence.atlassian.com/display/JIRAKB/Jira+Issues+Macro+Fails+to+Display+due+to+'Failed+to+Login+Trusted+Application'+Error</li>
     * <li>https://paste.atlassian.com/view/1292</li>
     * <li>https://paste.atlassian.com/view/1293</li>
     */
    public static final long DEFAULT_CERTIFICATE_TIMEOUT = 10000L;

    protected final TrustedApplicationsConfigurationManager trustedAppsManager;
    protected final AuthenticationConfigurationManager configurationManager;

    public TrustConfigurator(final AuthenticationConfigurationManager configurationManager,
                             final TrustedApplicationsConfigurationManager trustedAppsManager)
    {
        this.configurationManager = configurationManager;
        this.trustedAppsManager = trustedAppsManager;
    }

    public void updateInboundTrust(final ApplicationLink appLink, final RequestConditions requestConditions)
            throws ConfigurationException
    {
        final Application application = getApplicationCertificate(appLink);
        trustedAppsManager.addTrustedApplication(application, requestConditions);
        appLink.putProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID, application.getID());
    }

    private Application getApplicationCertificate(final ApplicationLink appLink) throws ConfigurationException
    {
        checkNotNull(appLink, "applicationLink");
        try
        {
             return trustedAppsManager.getApplicationCertificate(appLink.getRpcUrl().toString());
        }
        catch (ApplicationRetriever.RetrievalException re)
        {
            throw new ConfigurationException("Unable to retrieve the application's certificate: " + re.getMessage(), re);
        }
    }

    public void issueInboundTrust(final ApplicationLink appLink)
            throws ConfigurationException
    {
        final Application application = getApplicationCertificate(appLink);
        checkNotNull(appLink, "applicationLink");
        try
        {
            Iterables.find(trustedAppsManager.getTrustedApplications(), new Predicate<TrustedApplication>()
            {
                public boolean apply(@Nullable final TrustedApplication input)
                {
                    return input.getID().equals(application.getID());
                }
            });
        }
        catch (NoSuchElementException ex)
        {
            trustedAppsManager.addTrustedApplication(application, RequestConditions
                    .builder()
                    .setCertificateTimeout(TrustConfigurator.DEFAULT_CERTIFICATE_TIMEOUT)
                    .build());
        }
        appLink.putProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID, application.getID());
    }

    public boolean inboundTrustEnabled(ApplicationLink applicationLink)
    {
        return applicationLink.getProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID) != null;
    }

    public void revokeInboundTrust(final ApplicationLink appLink)
    {
        final Object value = appLink.getProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID);
        if (value != null)
        {
            trustedAppsManager.deleteApplication(value.toString());
        }
        appLink.removeProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID);
    }

    public void configureOutboundTrust(final ApplicationLink link, final Action action)
    {
        if (Action.ENABLE == checkNotNull(action))
        {
            issueOutboundTrust(link);
        }
        else
        {
            revokeOutboundTrust(link);
        }
    }

    public void issueOutboundTrust(final ApplicationLink link)
    {
        checkNotNull(link, "applicationLink");
        configurationManager.registerProvider(link.getId(), TrustedAppsAuthenticationProvider.class, ImmutableMap.<String, String>of());
    }

    public void revokeOutboundTrust(final ApplicationLink link)
    {
        checkNotNull(link, "applicationLink");
        configurationManager.unregisterProvider(link.getId(), TrustedAppsAuthenticationProvider.class);
    }

    public static class ConfigurationException extends Exception
    {
        public ConfigurationException(String message)
        {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
