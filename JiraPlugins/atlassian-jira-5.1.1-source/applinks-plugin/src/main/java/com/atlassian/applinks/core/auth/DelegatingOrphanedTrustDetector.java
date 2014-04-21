package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.core.auth.oauth.OAuthOrphanedTrustDetector;
import com.atlassian.applinks.core.auth.trusted.TrustedAppsOrphanedTrustDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates authentication provider-specific {@link OrphanedTrustDetector} implementations
 *
 * @since 3.0
 */
public class DelegatingOrphanedTrustDetector implements OrphanedTrustDetector
{
    private final OAuthOrphanedTrustDetector oAuthOrphanedTrustDetector;
    private final TrustedAppsOrphanedTrustDetector trustedAppsOrphanedTrustDetector;

    public DelegatingOrphanedTrustDetector(final OAuthOrphanedTrustDetector oAuthOrphanedTrustDetector,
                                           final TrustedAppsOrphanedTrustDetector trustedAppsOrphanedTrustDetector)
    {
        this.oAuthOrphanedTrustDetector = oAuthOrphanedTrustDetector;
        this.trustedAppsOrphanedTrustDetector = trustedAppsOrphanedTrustDetector;
    }

    public List<OrphanedTrustCertificate> findOrphanedTrustCertificates()
    {
        final List<OrphanedTrustCertificate> certificates = new ArrayList<OrphanedTrustCertificate>();
        certificates.addAll(trustedAppsOrphanedTrustDetector.findOrphanedTrustCertificates());
        certificates.addAll(oAuthOrphanedTrustDetector.findOrphanedTrustCertificates());
        return certificates;
    }

    public void deleteTrustCertificate(final String id, final OrphanedTrustCertificate.Type type)
    {
        switch (type) {
            case TRUSTED_APPS:
                trustedAppsOrphanedTrustDetector.deleteTrustCertificate(id, type);
                break;
            case OAUTH:
                oAuthOrphanedTrustDetector.deleteTrustCertificate(id, type);
                break;
            case OAUTH_SERVICE_PROVIDER:
                oAuthOrphanedTrustDetector.deleteTrustCertificate(id, type);
                break;
            default:
                throw new IllegalArgumentException("Unsupported OrphanedTrustCertificate.Type: " + type);
        }
    }

    public void addOrphanedTrustToApplicationLink(final String id, final OrphanedTrustCertificate.Type type, final ApplicationId applicationId)
    {
        switch (type) {
            case TRUSTED_APPS:
                trustedAppsOrphanedTrustDetector.addOrphanedTrustToApplicationLink(id, type, applicationId);
                break;
            case OAUTH:
                oAuthOrphanedTrustDetector.addOrphanedTrustToApplicationLink(id, type, applicationId);
                break;
            case OAUTH_SERVICE_PROVIDER:
                oAuthOrphanedTrustDetector.addOrphanedTrustToApplicationLink(id, type, applicationId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported OrphanedTrustCertificate.Type: " + type);
        }
    }
}
