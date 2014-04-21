package com.atlassian.applinks.ui.velocity;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 3.0
 */
public class VelocityContextFactory
{
    private final InternalHostApplication internalHostApplication;
    private final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory;
    private final InternalTypeAccessor typeAccessor;
    private final I18nResolver i18nResolver;
    private final DocumentationLinker documentationLinker;
    private final OrphanedTrustDetector orphanedTrustDetector;
    private final ApplicationLinkService applicationLinkService;
    private final ManifestRetriever manifestRetriever;
    private final MessageFactory messageFactory;
    private final UserManager userManager;

    public VelocityContextFactory(final InternalHostApplication internalHostApplication,
                                  final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                                  final InternalTypeAccessor typeAccessor, final I18nResolver i18nResolver,
                                  final DocumentationLinker documentationLinker,
                                  @Qualifier("delegatingOrphanedTrustDetector") final OrphanedTrustDetector orphanedTrustDetector,
                                  final ApplicationLinkService applicationLinkService,
                                  final ManifestRetriever manifestRetriever, final MessageFactory messageFactory,
                                  final UserManager userManager)
    {
        this.internalHostApplication = internalHostApplication;
        this.batchedJSONi18NBuilderFactory = batchedJSONi18NBuilderFactory;
        this.typeAccessor = typeAccessor;
        this.i18nResolver = i18nResolver;
        this.documentationLinker = documentationLinker;
        this.orphanedTrustDetector = orphanedTrustDetector;
        this.applicationLinkService = applicationLinkService;
        this.manifestRetriever = manifestRetriever;
        this.messageFactory = messageFactory;
        this.userManager = userManager;
    }

    public ListApplicationLinksContext buildListApplicationLinksContext(final HttpServletRequest request)
    {
        return new ListApplicationLinksContext(internalHostApplication, batchedJSONi18NBuilderFactory, typeAccessor,
                i18nResolver, documentationLinker, orphanedTrustDetector, request.getContextPath());
    }

    public ListEntityLinksContext buildListEntityLinksContext(final HttpServletRequest request,
                                                              final String entityTypeId, final String entityKey)
    {
        return new ListEntityLinksContext(applicationLinkService, manifestRetriever, internalHostApplication,
                batchedJSONi18NBuilderFactory, documentationLinker, messageFactory, typeAccessor, entityTypeId,
                entityKey, request.getContextPath(), userManager.getRemoteUsername(request));
    }

}
