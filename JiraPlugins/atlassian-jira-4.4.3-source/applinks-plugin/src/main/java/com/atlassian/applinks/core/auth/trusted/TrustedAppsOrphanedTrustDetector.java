package com.atlassian.applinks.core.auth.trusted;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.security.auth.trustedapps.TrustedApplication;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsConfigurationManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Finds orphaned Trusted Applications certificates (that is, they are stored
 * locally but are not related to a configured {@link ApplicationLink})
 *
 * @since 3.0
 */
public class TrustedAppsOrphanedTrustDetector implements OrphanedTrustDetector
{
    private final ApplicationLinkService applicationLinkService;
    private final TrustedApplicationsConfigurationManager trustedApplicationsConfigurationManager;
    private final TrustConfigurator trustConfigurator;
    private static final Logger log = LoggerFactory.getLogger(TrustedAppsOrphanedTrustDetector.class);

    public TrustedAppsOrphanedTrustDetector(final ApplicationLinkService applicationLinkService,
                                            final TrustedApplicationsConfigurationManager trustedApplicationsConfigurationManager,
                                            final TrustConfigurator trustConfigurator)
    {
        this.applicationLinkService = applicationLinkService;
        this.trustedApplicationsConfigurationManager = trustedApplicationsConfigurationManager;
        this.trustConfigurator = trustConfigurator;
    }

    public List<OrphanedTrustCertificate> findOrphanedTrustCertificates()
    {
        final List<OrphanedTrustCertificate> orphanedTrustCertificates = new ArrayList<OrphanedTrustCertificate>();

        final Set<String> recognisedIds = new HashSet<String>();
        for (final ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            final String id = (String) link.getProperty(AbstractTrustedAppsServlet.TRUSTED_APPS_INCOMING_ID);
            if (id != null)
            {
                recognisedIds.add(id);
            }
        }

        for (final TrustedApplication trustedApp : trustedApplicationsConfigurationManager.getTrustedApplications())
        {
            if (!recognisedIds.contains(trustedApp.getID()))
            {
                orphanedTrustCertificates.add(
                        new OrphanedTrustCertificate(trustedApp.getID(), trustedApp.getName(), OrphanedTrustCertificate.Type.TRUSTED_APPS)
                );
            }
        }

        return orphanedTrustCertificates;
    }

    public void deleteTrustCertificate(final String id, final OrphanedTrustCertificate.Type type)
    {
        checkCertificateType(type);

        trustedApplicationsConfigurationManager.deleteApplication(id);
    }

    private void checkCertificateType(final OrphanedTrustCertificate.Type type)
    {
        if (type != OrphanedTrustCertificate.Type.TRUSTED_APPS)
        {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public void addOrphanedTrustToApplicationLink(final String id, final OrphanedTrustCertificate.Type type, final ApplicationId applicationId)
    {
        checkCertificateType(type);
        try
        {
            ApplicationLink applicationLink = applicationLinkService.getApplicationLink(applicationId);
            trustConfigurator.issueInboundTrust(applicationLink);
            log.debug("Associated Trusted Apps configuration for Application Link id='" + applicationLink.getId() + "' and name='" + applicationLink.getName() + "'");
        }
        catch (TypeNotInstalledException e)
        {
            throw new RuntimeException("An application of the type " +  e.getType() +" is not installed!", e);
        }
        catch (TrustConfigurator.ConfigurationException e)
        {
            throw new RuntimeException("Failed to add Trusted Apps configuration for Application Link with id '"+ applicationId +'"', e);
        }
    }
}
