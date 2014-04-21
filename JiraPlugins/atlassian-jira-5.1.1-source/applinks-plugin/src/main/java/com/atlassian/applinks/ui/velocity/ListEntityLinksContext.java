package com.atlassian.applinks.ui.velocity;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.docs.DocumentationLinker;
import com.atlassian.applinks.core.rest.util.ResourceUrlHandler;
import com.atlassian.applinks.core.util.MessageFactory;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.applinks.ui.AbstractApplinksServlet;
import com.atlassian.applinks.ui.BatchedJSONi18NBuilderFactory;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * This class lists all objects that are used in the velocity templates in the list entity
 * links screen.
 *
 * @since 3.0
 */
public class ListEntityLinksContext extends AbstractVelocityContext
{
    private final String username;
    private final ApplicationLinkService linkService;
    private final ManifestRetriever manifestRetriever;
    private final DocumentationLinker documentationLinker;
    private final MessageFactory messageFactory;
    private String type;
    private String typeLabel;
    private String name;
    private String key;

    /* (non-javadoc)
     * Use VelocityContextFactory to construct
     */
    ListEntityLinksContext(final ApplicationLinkService linkService,
                           final ManifestRetriever manifestRetriever,
                           final InternalHostApplication internalHostApplication,
                           final BatchedJSONi18NBuilderFactory batchedJSONi18NBuilderFactory,
                           final DocumentationLinker documentationLinker,
                           final MessageFactory messageFactory,
                           final InternalTypeAccessor typeAccessor,
                           final String typeId,
                           final String key,
                           final String contextPath,
                           final String username
    )
    {
        super(contextPath, internalHostApplication, batchedJSONi18NBuilderFactory, typeAccessor, documentationLinker);
        this.username = username;
        this.linkService = linkService;
        this.manifestRetriever = manifestRetriever;
        this.documentationLinker = documentationLinker;
        this.messageFactory = messageFactory;
        this.key = key;
        parsePathParams(typeId);
    }

    private void parsePathParams(final String typeId)
    {
        if (!internalHostApplication.doesEntityExist(key, typeAccessor.loadEntityType(typeId).getClass()))
        {
            throw new AbstractApplinksServlet.BadRequestException(messageFactory.newLocalizedMessage(
                    String.format("No entity exists with key %s of type %s", key, typeId)));
        }
        final EntityType entityType = typeAccessor.loadEntityType(typeId);
        assertPermission(entityType.getClass(), key);

        final EntityReference entityReference = internalHostApplication.toEntityReference(key, entityType.getClass());
        type = typeId;
        typeLabel = messageFactory.newI18nMessage(entityType.getShortenedI18nKey()).toString();
        name = entityReference.getName();
    }

    public String getType()
    {
        return type;
    }

    public String getTypeLabel()
    {
        return typeLabel;
    }

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }

    private void assertPermission(final Class<? extends EntityType> entityType, final String key)
    {
        if (!internalHostApplication.canManageEntityLinksFor(
                internalHostApplication.toEntityReference(key, entityType)))
        {
            throw new AbstractApplinksServlet.UnauthorizedException(messageFactory.newI18nMessage("applinks.entity.list.no.permission"));
        }
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getUserName()
    {
        return username;
    }

    public List<ApplicationOption> getApplications()
    {
        return Lists.<ApplicationOption>newArrayList(
                Iterables.transform(linkService.getApplicationLinks(), new Function<ApplicationLink, ApplicationOption>()
                {
                    public ApplicationOption apply(final ApplicationLink from)
                    {
                        boolean isUal = false;
                        try
                        {
                            isUal = manifestRetriever.getManifest(
                                    from.getRpcUrl(), from.getType()).getAppLinksVersion() != null;
                        }
                        catch (ManifestNotFoundException e)
                        {
                        }
                        return new ApplicationOption(
                                from.getId(),
                                from.getName(),
                                TypeId.getTypeId(from.getType()),
                                from.getType().getI18nKey(),
                                isUal,
                                from.getType().getIconUrl() == null ? null : from.getType().getIconUrl().toString());
                    }
                }
                ));
    }

    public ResourceUrlHandler getUrls()
    {
        return new ResourceUrlHandler(internalHostApplication.getBaseUrl().toString());
    }

    public String getApplicationType()
    {
        return TypeId.getTypeId(internalHostApplication.getType()).get();
    }

    @HtmlSafe
    public String getApplinksI18n()
    {
        return batchedJSONi18NBuilderFactory.builder()
                .withProperties("applinks")
                .withPluggableApplinksModules().build();
    }

    @HtmlSafe
    public String getApplinksDocs()
    {
        return batchedJSONi18NBuilderFactory.builder()
                .withProperties("applinks.docs")
                .with("applinks.docs.root", internalHostApplication.getDocumentationBaseUrl().toASCIIString())
                .build();
    }

    public DocumentationLinker getDocumentationLinker()
    {
        return documentationLinker;
    }

    public static class ApplicationOption
    {
        private final String id;
        private final String name;
        private final String typeId;
        private final String typeI18nKey;
        private final boolean isUal;
        private final String iconUrl;

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getTypeId()
        {
            return typeId;
        }

        public String getTypeI18nKey()
        {
            return typeI18nKey;
        }

        public boolean isUal()
        {
            return isUal;
        }

        public String getIconUrl()
        {
            return iconUrl;
        }

        public ApplicationOption(final ApplicationId id, final String name, final TypeId typeId, final String typeI18nKey,
                                 final boolean isUal, final String iconUrl)
        {
            this.id = id.get();
            this.name = name;
            this.typeId = typeId.get();
            this.typeI18nKey = typeI18nKey;
            this.isUal = isUal;
            this.iconUrl = iconUrl;
        }
    }

}
