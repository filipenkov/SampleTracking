package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;

import java.util.List;

/**
 * Components implementing this interface retrieve a list of 'orphaned' trust certificates (that is, they are stored
 * locally but are not related to a configured {@link ApplicationLink})
 *
 * @since 3.0
 */
public interface OrphanedTrustDetector
{

    /**
     * @return a list of {@link OrphanedTrustCertificate}s
     */
    List<OrphanedTrustCertificate> findOrphanedTrustCertificates();

    /**
     *
     * @param id the id/key of an {@link OrphanedTrustCertificate}
     * @param type the {@link OrphanedTrustCertificate.Type} of the target certificate to delete
     */
    void deleteTrustCertificate(String id, OrphanedTrustCertificate.Type type);

    /**
     * Register an existing trust relation ship against an Application Link.
     *
     * @param id  id the id/key of an {@link OrphanedTrustCertificate}
     * @param type   type the {@link OrphanedTrustCertificate.Type} of the target certificate to register
     * @param applicationId  the id of the application link where the trust relationship belongs to.
     *
     * @since 3.2
     */
    void addOrphanedTrustToApplicationLink(String id, OrphanedTrustCertificate.Type type, ApplicationId applicationId);

}
