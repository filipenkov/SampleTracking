package com.atlassian.applinks.core;

import static com.atlassian.applinks.spi.application.TypeId.getTypeId;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.event.ApplicationLinkAddedEvent;
import com.atlassian.applinks.api.event.ApplicationLinkDeletedEvent;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestFactoryFactory;
import com.atlassian.applinks.core.auth.AuthenticationConfigurator;
import com.atlassian.applinks.core.event.BeforeApplicationLinkDeletedEvent;
import com.atlassian.applinks.core.link.DefaultApplicationLink;
import com.atlassian.applinks.core.link.InternalApplicationLink;
import com.atlassian.applinks.core.link.InternalEntityLinkService;
import com.atlassian.applinks.core.net.BasicHTTPAuthRequestFactory;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.core.rest.ApplicationLinkResource;
import com.atlassian.applinks.core.rest.client.ApplicationLinkClient;
import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.rest.model.ApplicationLinkEntity;
import com.atlassian.applinks.core.rest.model.ErrorListEntity;
import com.atlassian.applinks.core.rest.ui.AuthenticationResource;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.AuthenticationResponseException;
import com.atlassian.applinks.spi.link.LinkCreationResponseException;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.NotAdministratorException;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.spi.link.RemoteErrorListException;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.NotificationException;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.core.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultApplicationLinkService implements MutatingApplicationLinkService
{
    private static final Logger LOG = LoggerFactory.getLogger(DefaultApplicationLinkService.class);
    private static final int CREATE_APPLICATION_LINK_SOCKET_TIMEOUT = 60000;

    private final ApplicationLinkRequestFactoryFactory requestFactoryFactory;
    private final PropertyService propertyService;
    private final InternalEntityLinkService entityLinkService;
    private final InternalTypeAccessor typeAccessor;
    private final ApplicationLinkClient applicationLinkClient;
    private final EventPublisher eventPublisher;
    private final InternalHostApplication internalHostApplication;
    private final RequestFactory<Request<Request<?, Response>,Response>> requestFactory;
    private final RestUrlBuilder restUrlBuilder;
    private final ManifestRetriever manifestRetriever;
    private final AuthenticationConfigurator authenticationConfigurator;

    static final String APPLICATION_IDS = "application.ids";
    private final Lock applicationIdsLock = new ReentrantLock();

    public DefaultApplicationLinkService(final PropertyService propertyService,
                                         final ApplicationLinkRequestFactoryFactory requestFactoryFactory,
                                         final InternalEntityLinkService entityLinkService,
                                         final InternalTypeAccessor typeAccessor,
                                         final ApplicationLinkClient applicationLinkClient,
                                         final EventPublisher eventPublisher,
                                         final InternalHostApplication internalHostApplication,
                                         final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                                         final RestUrlBuilder restUrlBuilder,
                                         final ManifestRetriever manifestRetriever,
                                         final AuthenticationConfigurator authenticationConfigurator)
    {
        this.requestFactoryFactory = requestFactoryFactory;
        this.propertyService = propertyService;
        this.entityLinkService = entityLinkService;
        this.typeAccessor = typeAccessor;
        this.applicationLinkClient = applicationLinkClient;
        this.eventPublisher = eventPublisher;
        this.internalHostApplication = internalHostApplication;
        this.requestFactory = requestFactory;
        this.restUrlBuilder = restUrlBuilder;
        this.manifestRetriever = manifestRetriever;
        this.authenticationConfigurator = authenticationConfigurator;
    }

    public InternalApplicationLink getApplicationLink(final ApplicationId id) throws TypeNotInstalledException
    {
        if (!getApplicationIds().contains(id))
        {
            return null;
        }

        return retrieveApplicationLink(id);
    }

    public void changeApplicationId(final ApplicationId oldId, final ApplicationId newId) throws TypeNotInstalledException
    {
        applicationIdsLock.lock();
        try
        {
            final List<ApplicationId> applicationIds = getApplicationIds();

            if (!applicationIds.contains(Preconditions.checkNotNull(oldId)))
            {
                throw new IllegalArgumentException("Application with server ID " +
                        oldId.toString() + " does not exist.");
            }
            else
            {
                final ApplicationLinkProperties oldProperties = propertyService.getApplicationLinkProperties(oldId);
                final ApplicationLinkProperties newProperties = propertyService.getApplicationLinkProperties(Preconditions.checkNotNull(newId));

                // copy app properties over:
                newProperties.setProperties(oldProperties);
                if (!applicationIds.contains(newId))
                {
                    applicationIds.add(newId);
                }
                else
                {
                    LOG.warn("There is already an Application Link registered with the ID '" + newId + "'. We are merging the upgraded NON-UAL Application Link with this existing Application Link.");
                }

                setApplicationIds(applicationIds);

                final InternalApplicationLink from = retrieveApplicationLink(oldId);
                final InternalApplicationLink to = retrieveApplicationLink(newId);

                entityLinkService.migrateEntityLinks(from, to);

                // remove the old properties
                oldProperties.remove();
                applicationIds.remove(oldId);
                setApplicationIds(applicationIds);
            }
        }
        finally
        {
            applicationIdsLock.unlock();
        }
    }

    public void makePrimary(final ApplicationId id) throws TypeNotInstalledException
    {
        final InternalApplicationLink internalApplicationLink = getApplicationLink(id);
        final Iterable<InternalApplicationLink> applicationLinksOfType = getInternalApplicationLinks(internalApplicationLink.getType().getClass());
        for (final InternalApplicationLink link : applicationLinksOfType)
        {
            if (link.getId().equals(id))
            {
                link.setPrimaryFlag(true);
            }
            else
            {
                link.setPrimaryFlag(false);
            }
        }
    }

    public InternalApplicationLink addApplicationLink(final ApplicationId id, final ApplicationType type,
                                                      final ApplicationLinkDetails details)
    {
        //Note that reciprocation of create is currently done in ApplicationLinkClientResource
        try
        {
            applicationIdsLock.lock();

            final List<ApplicationId> applicationIds = getApplicationIds();

            if (applicationIds.contains(id))
            {
                throw new IllegalArgumentException("Application with server ID " + id + " is already configured");
            }
            
            final boolean onlyLinkOfItsType = Iterables.isEmpty(getApplicationLinks(type.getClass()));
            //Ensure an application link of this type exists.
            final ApplicationLinkProperties applicationLinkProperties = propertyService.getApplicationLinkProperties(id);

            applicationLinkProperties.setType(getTypeId(type));
            applicationLinkProperties.setName(findSuitableName(details.getName()));
            applicationLinkProperties.setDisplayUrl(details.getDisplayUrl());
            applicationLinkProperties.setRpcUrl(details.getRpcUrl());

            // The following two lines add the application link to the PluginSettings
            applicationIds.add(id);
            setApplicationIds(applicationIds);

            final InternalApplicationLink addedAppLink =
                new DefaultApplicationLink(id,
                                           type,
                                           applicationLinkProperties,
                                           requestFactoryFactory,
                                           eventPublisher);

            if (details.isPrimary() || onlyLinkOfItsType)
            {
                try
                {
                    makePrimary(id);
                }
                catch (TypeNotInstalledException e)
                {
                    //This is actually impossible, because we pass in the application type class.
                    LOG.warn("Failed to make new application link the primary application link", e);
                }
            }
            eventPublisher.publish(new ApplicationLinkAddedEvent(addedAppLink));
            return addedAppLink;
        }
        finally
        {
            applicationIdsLock.unlock();
        }
    }
    
    /**
     * Checks whether 'name' is already the name of an application link. If yes, appends
     * " - 2" to the name and increments the figure
     * @param name
     * @return
     */
    private String findSuitableName(final String name)
    {
        Iterable<ApplicationLink> allApplicationLinks = getApplicationLinks();
        if (!isNameInUse(name, null, allApplicationLinks))
        {
            return name;
        }
        
        // If the string is in the form "Refapp - 2", then remove " - 2"
        String root = name.replace(" - [0-9]+$", "");
        // Proposes names using the counter i
        String proposedName;
        int i = 2;
        do
        {
            proposedName = String.format("%s - %d", root, i);
            i++;
        }
        while (isNameInUse(proposedName, null, allApplicationLinks));
        return proposedName;
    }

    /**
     * Checks whether an Applink already exists with this name
     * @param name the name of the applink
     * @param id An application link to exclude (can be null if no applink has to be excluded)
     * @param allApplicationLinks The applinks to search into
     * @return true if an Applinks with 'name' and not 'id' was found among 'allApplicationLinks'
     */
    private boolean isNameInUse(final String name, final ApplicationId id, final Iterable<? extends ApplicationLink> allApplicationLinks)
    {
        try
        {
            Iterables.find(allApplicationLinks, new Predicate<ApplicationLink>()
            {
                public boolean apply(ApplicationLink appLink)
                {
                    return appLink.getName().equals(name) && !appLink.getId().equals(id);
                }
            });
        }
        catch (NoSuchElementException nsee)
        {
            return false;
        }
        return true;
    }

    /**
     * Checks whether an application link already exists with this name
     * @param name Name of the application link
     * @param id Applink to be excluded from the result. If null, it means no link will be excluded.
     * @return true if an Applink already exist with this name and another 'id'.
     */
    public boolean isNameInUse(final String name, final ApplicationId id)
    {
        Iterable<InternalApplicationLink> allApplicationLinks = getInternalApplicationLinks();
        return isNameInUse(name, id, allApplicationLinks);
    }
    
    public void deleteReciprocatedApplicationLink(final ApplicationLink link) throws ReciprocalActionException,
            CredentialsRequiredException
    {
        applicationLinkClient.deleteReciprocalLinkFrom(link);
        deleteApplicationLink(link);
    }

    public void deleteApplicationLink(final ApplicationLink link)
    {
        try
        {
            applicationIdsLock.lock();
            final List<ApplicationId> applicationIds = getApplicationIds();

            // cascade delete of entities related to this application link
            entityLinkService.deleteEntityLinksFor(link);

            if (applicationIds.remove(link.getId()))
            {
                final ApplicationLinkProperties appLinkProperties = propertyService.getApplicationLinkProperties(link.getId());
                final ApplicationType deletedType = typeAccessor.loadApplicationType(appLinkProperties.getType());
                boolean wasPrimary = link.isPrimary();
                try
                {
                    eventPublisher.publish(new BeforeApplicationLinkDeletedEvent(link));
                }
                catch (NotificationException e)
                {
                    LOG.error("An error occurred when broadcasting an " + BeforeApplicationLinkDeletedEvent.class.getName() + " event for application link with id '" + link.getId() + " ' and name '" + link.getName() + "'", e);
                }

                appLinkProperties.remove();

                setApplicationIds(applicationIds);

                if (wasPrimary)
                {
                    final Iterator<InternalApplicationLink> linkIterator = getInternalApplicationLinks(deletedType.getClass()).iterator();
                    if (linkIterator.hasNext())
                    {
                        final ApplicationLink newPrimaryApplicationLink = linkIterator.next();
                        try
                        {
                            makePrimary(newPrimaryApplicationLink.getId());
                        }
                        catch (TypeNotInstalledException ex)
                        {
                             //This is actually impossible, because we pass in the application type class.
                            LOG.warn("Failed to make new application link the primary application link", ex);
                        }
                    }
                }
                eventPublisher.publish(new ApplicationLinkDeletedEvent(link));
            }
        }
        finally
        {
            applicationIdsLock.unlock();
        }
    }

    private InternalApplicationLink retrieveApplicationLink(final ApplicationId id) throws TypeNotInstalledException
    {
        final ApplicationLinkProperties properties = propertyService.getApplicationLinkProperties(Preconditions.checkNotNull(id));

        final ApplicationType type = typeAccessor.loadApplicationType(properties.getType());

        if (type == null)
        {
            LOG.debug("Couldn't load type {} for application link with id {}. Type is not installed?", properties.getType(), id.get());
            throw new TypeNotInstalledException(properties.getType().get());
        }

        return new DefaultApplicationLink(id, type, properties,
                requestFactoryFactory, eventPublisher);
    }

    public Iterable<ApplicationLink> getApplicationLinks()
    {
        return Iterables.filter(getInternalApplicationLinks(), ApplicationLink.class);
    }

    public Iterable<InternalApplicationLink> getInternalApplicationLinks()
    {
        return new Iterable<InternalApplicationLink>()
        {
            Iterator<ApplicationId> applicationIds = getApplicationIds().iterator();

            public Iterator<InternalApplicationLink> iterator()
            {
                return new Iterator<InternalApplicationLink>()
                {
                    private InternalApplicationLink next;

                    public boolean hasNext()
                    {
                        return peek() != null;
                    }

                    public InternalApplicationLink next()
                    {
                        return pop();
                    }

                    private synchronized InternalApplicationLink peek()
                    {
                        while (next == null && applicationIds.hasNext())
                        {
                            try
                            {
                                next = retrieveApplicationLink(applicationIds.next());
                                break;
                            }
                            catch (TypeNotInstalledException e)
                            {
                                // ignore -- try to load next link
                            }
                        }
                        return next;
                    }

                    private synchronized InternalApplicationLink pop()
                    {
                        final InternalApplicationLink popped = peek();
                        next = null;
                        return popped;
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException("Remove operation not allowed");
                    }
                };
            }
        };
    }

    public Iterable<ApplicationLink> getApplicationLinks(final Class<? extends ApplicationType> type)
    {
        final Iterable<ApplicationLink> internalLinks = Iterables.filter(getInternalApplicationLinks(type), ApplicationLink.class);
        final ArrayList<ApplicationLink> unsortedLinks = Lists.newArrayList(internalLinks);
        Collections.sort(unsortedLinks, new Comparator<ApplicationLink>()
        {
            public int compare(final ApplicationLink applicationLink, final ApplicationLink applicationLink1)
            {
                if (applicationLink.isPrimary())
                {
                    return -1;
                }
                return 1;
            }
        });
        return unsortedLinks;
    }

    public Iterable<InternalApplicationLink> getInternalApplicationLinks(final Class<? extends ApplicationType> type)
    {
        Preconditions.checkNotNull(type);
        return Iterables.filter(this.getInternalApplicationLinks(), new Predicate<ApplicationLink>()
        {
            public boolean apply(final ApplicationLink input)
            {
                return type.isAssignableFrom(input.getType().getClass());
            }
        });

    }

    public ApplicationLink getPrimaryApplicationLink(final Class<? extends ApplicationType> type)
    {
        final Iterator<ApplicationLink> iterator = getApplicationLinks(type).iterator();

        if (!iterator.hasNext())
        {
            // no applications of this type configured
            return null;
        }

        while (iterator.hasNext())
        {
            final ApplicationLink application = iterator.next();
            if (application.isPrimary())
            {
                return application;
            }
        }

        throw new IllegalStateException("There are application links of type " + type + " configured, but none are " +
                "marked as primary");
    }

    @SuppressWarnings("unchecked")
    private List<ApplicationId> getApplicationIds()
    {
        List<String> list = (List<String>) propertyService.getGlobalAdminProperties().getProperty(APPLICATION_IDS);
        if (list == null)
        {
            list = new ArrayList<String>();
        }
        return new ArrayList<ApplicationId>(Lists.transform(list, new Function<String, ApplicationId>()
        {
            public ApplicationId apply(final String from)
            {
                return new ApplicationId(from);
            }
        }));
    }

    private void setApplicationIds(final List<ApplicationId> applicationIds)
    {
        //NB: we have to copy the transformed list from Lists.transform(), otherwise the persistence fails in Confluence which uses Bandana and xStreams to persists plugin settings.
        propertyService.getGlobalAdminProperties().putProperty(APPLICATION_IDS, new ArrayList<String>(Lists.transform(applicationIds, new Function<ApplicationId, String>()
        {
            public String apply(final ApplicationId from)
            {
                return from.get();
            }
        })));
    }

    public void createReciprocalLink(final URI remoteRpcUrl, final URI customLocalRpcUrl, final String username, final String password) throws ReciprocalActionException {
        final URI localRpcUrl;
        if (customLocalRpcUrl != null) {
            localRpcUrl = customLocalRpcUrl;
        } else {
            localRpcUrl = internalHostApplication.getBaseUrl();
        }
        try {
            final boolean adminUser = isAdminUserInRemoteApplication(remoteRpcUrl, username, password);
            if (!adminUser) {
                throw new NotAdministratorException();
            }
        } catch (ResponseException ex) {
            throw new AuthenticationResponseException();
        }
        final ApplicationLinkEntity linkBackToMyself = new ApplicationLinkEntity(
                internalHostApplication.getId(),
                getTypeId(internalHostApplication.getType()),
                internalHostApplication.getName(),
                internalHostApplication.getBaseUrl(),
                internalHostApplication.getType().getIconUrl(),
                localRpcUrl,
                false,
                Link.self(createSelfLinkFor(internalHostApplication.getId())));

        final String url;
        try {
            ApplicationLinkResource resource = restUrlBuilder.getUrlFor(RestUtil.getBaseRestUri(remoteRpcUrl), ApplicationLinkResource.class);
            url = resource.updateApplicationLink(internalHostApplication.getId().toString(), null).toString();
        } catch (TypeNotInstalledException e) {
            throw new AssertionError(RestUrlBuilder.class.getName() + " must never throw " +
                    TypeNotInstalledException.class.getName());
        }
        final Request<Request<?, Response>, Response> request =
                new BasicHTTPAuthRequestFactory<Request<Request<?, Response>, Response>>(
                        requestFactory,
                        username,
                        password)
                        .createRequest(Request.MethodType.PUT, url);

        // bump up the socket timeout, as application link creation requests can take a *long* time, as this will
        // trigger a request from the remote application back to this one, and also trigger an ApplicationLinkAddedEvent
        // in registered listeners in the remote application, synchronously! The FishEye plugin is one such example
        // of this - see FECRU-534
        request.setSoTimeout(CREATE_APPLICATION_LINK_SOCKET_TIMEOUT);

        ErrorListEntity errorListEntity;
        final ClassLoader currentContextClassloader = Thread.currentThread().getContextClassLoader();
        final ChainingClassLoader chainingClassLoader = new ChainingClassLoader(currentContextClassloader,
                ClassLoaderUtils.class.getClassLoader(), ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(chainingClassLoader); // APL-837
        try {
            errorListEntity = request
                    .setEntity(linkBackToMyself)
                    .executeAndReturn(new ReturningResponseHandler<Response, ErrorListEntity>()
                    {
                        public ErrorListEntity handle(final Response response) throws ResponseException
                        {
                            // 201 means we created a new application link.
                            // 200 means there already is an application link and we just updated this one.
                            return !response.isSuccessful() ?
                                    response.getEntity(ErrorListEntity.class) :
                                    null;
                        }
                    });
        } catch (ResponseException ex) {
            final String message = "After creating the 2-Way link an error occurred when reading the response from the remote application.";
            LOG.debug(message, ex);
            throw new LinkCreationResponseException(message, ex);
        } catch (RuntimeException ex) {
            final String message = "An error occurred when trying to create the application link in the remote application.";
            LOG.debug(message, ex);
            throw new ReciprocalActionException(message, ex);
        } finally {
            Thread.currentThread().setContextClassLoader(currentContextClassloader);
        }

        if (errorListEntity != null) {
            throw new RemoteErrorListException(errorListEntity.getErrors());
        }
    }

    public boolean isAdminUserInRemoteApplication(final URI url, final String username, final String password)
            throws ResponseException
    {
        final URI uri = URIUtil.uncheckedConcatenate(url, RestUtil.REST_APPLINKS_URL);
        final AuthenticationResource restUrl = restUrlBuilder.getUrlFor(uri, AuthenticationResource.class);
        return requestFactory
                .createRequest(Request.MethodType.GET,
                        restUrl.getIsAdminUser().toString())
                .addBasicAuthentication(username, password)
                .executeAndReturn(new ReturningResponseHandler<Response,Boolean>()
                {
                    public Boolean handle(final Response restResponse) throws ResponseException
                    {
                        return restResponse.isSuccessful();
                    }
                });
    }

    public URI createSelfLinkFor(final ApplicationId id)
    {
        try
        {
            URI baseUri;
            HttpContext context = CurrentContext.getContext();
            if (context != null)
            {
                baseUri = context.getUriInfo().getBaseUri();
            }
            else
            {
                baseUri = internalHostApplication.getBaseUrl();
            }
            final ApplicationLinkResource applicationLinkResource = restUrlBuilder.getUrlFor(
                    baseUri,
                    ApplicationLinkResource.class);
            final String idString = id.get();
            final javax.ws.rs.core.Response applicationLink = applicationLinkResource.getApplicationLink(idString);
            return restUrlBuilder.getURI(applicationLink);
        }
        catch (TypeNotInstalledException e)
        {
            // this should _never_ happen, as com.atlassian.plugins.rest.common.util.RestUrlBuilder.getUrlFor() is just returning a proxy stub
            throw new IllegalStateException(String.format("Failed to load application %s as the %s type is not installed",
                    id.get(), e.getType()));
        }
    }

    public ApplicationLink createApplicationLink(final ApplicationType type, final ApplicationLinkDetails linkDetails) throws ManifestNotFoundException {
        final Manifest manifest = manifestRetriever.getManifest(linkDetails.getRpcUrl(), type);
        return addApplicationLink(manifest.getId(), type, linkDetails);
    }

    public void configureAuthenticationForApplicationLink(final ApplicationLink applicationLink,
                                                          final AuthenticationScenario authenticationScenario,
                                                          final String username, final String password) throws AuthenticationConfigurationException {
        authenticationConfigurator.configureAuthenticationForApplicationLink(
                applicationLink,
                authenticationScenario,
                new BasicHTTPAuthRequestFactory<Request<Request<?, Response>, Response>>(
                        requestFactory,
                        username,
                        password)
        );
    }

}
