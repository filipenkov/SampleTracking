package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLink;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.event.ApplicationLinksIDChangedEvent;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestAdaptor;
import com.atlassian.applinks.core.auth.AuthenticationConfigurator;
import com.atlassian.applinks.core.net.BasicHTTPAuthRequestFactory;
import com.atlassian.applinks.core.rest.AbstractResource;
import com.atlassian.applinks.core.rest.ApplicationLinkResource;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.client.EntityLinkClient;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ApplicationLinkEntity;
import com.atlassian.applinks.core.rest.model.ConfigurationFormValuesEntity;
import com.atlassian.applinks.core.rest.model.ErrorListEntity;
import com.atlassian.applinks.core.rest.model.UpgradeApplicationLinkRequestEntity;
import com.atlassian.applinks.core.rest.model.UpgradeApplicationLinkResponseEntity;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.commons.lang.ObjectUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.atlassian.applinks.core.rest.util.RestUtil.*;

/**
 * @since 3.0
 */
@Path("upgrade")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Singleton
@InterceptorChain({ContextInterceptor.class, AdminApplicationLinksInterceptor.class})
public class UpgradeApplicationLinkUIResource extends AbstractResource
{
    private final MutatingApplicationLinkService applicationLinkService;
    private final MutatingEntityLinkService entityLinkService;
    private final ManifestRetriever manifestRetriever;
    private final RequestFactory<Request<Request<?, com.atlassian.sal.api.net.Response>, com.atlassian.sal.api.net.Response>> requestFactory;
    private final I18nResolver i18nResolver;
    private final PluginAccessor pluginAccessor;
    private final EventPublisher eventPublisher;
    private final InternalHostApplication internalHostApplication;
    private final AuthenticationConfigurator authenticationConfigurator;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private final EntityLinkClient entityLinkClient;

    public UpgradeApplicationLinkUIResource(final RestUrlBuilder restUrlBuilder,
                                            final RequestFactory<Request<Request<?, com.atlassian.sal.api.net.Response>, com.atlassian.sal.api.net.Response>> requestFactory,
                                            final MutatingApplicationLinkService applicationLinkService,
                                            final MutatingEntityLinkService entityLinkService,
                                            final AuthenticationConfigurator authenticationConfigurator,
                                            final AuthenticationConfigurationManager authenticationConfigurationManager,
                                            final EventPublisher eventPublisher,
                                            final I18nResolver i18nResolver,
                                            final InternalHostApplication internalHostApplication,
                                            final ManifestRetriever manifestRetriever,
                                            final PluginAccessor pluginAccessor,
                                            final EntityLinkClient entityLinkClient,
                                            final InternalTypeAccessor typeAccessor)
    {
        super(restUrlBuilder, typeAccessor, requestFactory, applicationLinkService);
        this.applicationLinkService = applicationLinkService;
        this.entityLinkService = entityLinkService;
        this.authenticationConfigurator = authenticationConfigurator;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
        this.eventPublisher = eventPublisher;
        this.i18nResolver = i18nResolver;
        this.internalHostApplication = internalHostApplication;
        this.manifestRetriever = manifestRetriever;
        this.pluginAccessor = pluginAccessor;
        this.requestFactory = requestFactory;
        this.entityLinkClient = entityLinkClient;
    }

    /**
     * Used for curl-based testing only.
     */
    @POST
    @Path("test/{applinkId}")
    public Response testUpgrade(@PathParam("applinkId") final String id,
                                @QueryParam("twoWay") final boolean twoWay,
                                @QueryParam("username") final String username,
                                @QueryParam("password") final String password,
                                @QueryParam("shareUserbase") final boolean shareUserbase,
                                @QueryParam("trustEachOther") final boolean trustEachOther,
                                @QueryParam("links") final boolean reciprocateEntityLinks)
            throws TypeNotInstalledException
    {
        return upgrade(id, new UpgradeApplicationLinkRequestEntity(new ConfigurationFormValuesEntity(trustEachOther, shareUserbase), twoWay, password, reciprocateEntityLinks, username, null));
    }

    @POST
    @Path("ual/{applinkId}")
    public Response upgrade(@PathParam("applinkId") final String id,
                            final UpgradeApplicationLinkRequestEntity upgradeApplicationLinkRequestEntity)
            throws TypeNotInstalledException
    {
        final ApplicationId applicationId = new ApplicationId(id);
        final MutableApplicationLink applicationLink = applicationLinkService.getApplicationLink(applicationId);
        if (applicationLink == null)
        {
            return notFound(i18nResolver.getText("applinks.notfound", id));
        }
        else
        {
            String error;
            if (manifestRetriever.getApplicationStatus(applicationLink.getRpcUrl(), applicationLink.getType()) ==
                    ApplicationStatus.UNAVAILABLE)
            {
                error = i18nResolver.getText("applinks.legacy.upgrade.error.offline");
            }
            else
            {
                try
                {
                    final Manifest manifest = manifestRetriever
                            .getManifest(applicationLink.getRpcUrl(), applicationLink.getType());
                    if (!applicationLink.getId().equals(manifest.getId()))
                    {
                        if (manifest.getAppLinksVersion() != null && manifest.getAppLinksVersion().getMajor() >= 3)
                        {
                            return performUalUpgrade(applicationLink, upgradeApplicationLinkRequestEntity, manifest);
                        }
                        else
                        {
                            error = i18nResolver.getText("applinks.legacy.upgrade.error.legacy");
                        }
                    }
                    else
                    {
                        LOG.info("The application id '" + applicationLink.getId() + "' of the application link stored and the remote application are equal, no upgrade required.");
                        return Response.ok().build();
                    }
                }
                catch (ManifestNotFoundException e)
                {
                    error = i18nResolver.getText("applinks.legacy.upgrade.error.manifest",
                            TypeId.getTypeId(
                            applicationLink.getType()).toString(),
                            applicationLink.getId().toString());
                }
            }
            return badRequest(error);
        }
    }

    private Response performUalUpgrade(final MutableApplicationLink oldApplicationLink,
                                       final UpgradeApplicationLinkRequestEntity upgradeApplicationLinkRequestEntity,
                                       final Manifest manifest)
            throws TypeNotInstalledException
    {
        final List<String> warnings = new ArrayList<String>();
        RequestFactory authenticatedRequestFactory = null;
        if (upgradeApplicationLinkRequestEntity.isCreateTwoWayLink())
        {
            try
            {
                if (!applicationLinkService.isAdminUserInRemoteApplication(
                        oldApplicationLink.getRpcUrl(),
                        upgradeApplicationLinkRequestEntity.getUsername(),
                        upgradeApplicationLinkRequestEntity.getPassword()))
                {
                    return badFormRequest(
                            Lists.newArrayList(i18nResolver.getText("applinks.error.unauthorized")),
                            Lists.newArrayList("authorization"));
                }
                else
                {
                    authenticatedRequestFactory = new BasicHTTPAuthRequestFactory<Request<Request<?, com.atlassian.sal.api.net.Response>, com.atlassian.sal.api.net.Response>>(
                            requestFactory,
                            upgradeApplicationLinkRequestEntity.getUsername(),
                            upgradeApplicationLinkRequestEntity.getPassword());
                }
            }
            catch (ResponseException ex)
            {
                return serverError(i18nResolver.getText("applinks.error.authorization.response"));
            }
        }

        // change the server ID:
        applicationLinkService.changeApplicationId(oldApplicationLink.getId(), manifest.getId());
        final ApplicationLink newApplicationLink = applicationLinkService.getApplicationLink(manifest.getId());

        if (upgradeApplicationLinkRequestEntity.isCreateTwoWayLink())
        {
            final URI localRpcUrl = (URI) ObjectUtils.defaultIfNull(
                    upgradeApplicationLinkRequestEntity.getRpcUrl(),
                    internalHostApplication.getBaseUrl());

            final Request createTwoWayLinkRequest = authenticatedRequestFactory
                    .createRequest(Request.MethodType.PUT,
                            URIUtil.uncheckedConcatenate(
                                    newApplicationLink.getRpcUrl(),
                                    RestUtil.REST_APPLINKS_URL,
                                    ApplicationLinkResource.CONTEXT,
                                    internalHostApplication.getId().toString()
                            ).toString()
                    );

            final ApplicationLinkEntity linkBackToMyself = new ApplicationLinkEntity(
                    internalHostApplication.getId(),
                    TypeId.getTypeId(internalHostApplication.getType()),
                    internalHostApplication.getName(),
                    internalHostApplication.getBaseUrl(),
                    internalHostApplication.getType().getIconUrl(),
                    localRpcUrl,
                    false,
                    createSelfLinkFor(internalHostApplication.getId()));

            createTwoWayLinkRequest.setEntity(linkBackToMyself);

            try
            {
                createTwoWayLinkRequest.execute(new ResponseHandler<com.atlassian.sal.api.net.Response>()
                {
                    public void handle(final com.atlassian.sal.api.net.Response response) throws ResponseException
                    {
                        // 201 means we created a new application link.
                        // 200 means there already is an application link and we just updated this one.
                        if (!response.isSuccessful())
                        {
                            try
                            {
                                final ErrorListEntity listEntity = response.getEntity(ErrorListEntity.class);
                                warnings.addAll(listEntity.getErrors());
                            }
                            catch (RuntimeException re)
                            {
                                LOG.warn("Could not parse the peer's response to " +
                                        "upgrade application link \"" + oldApplicationLink.getName() +
                                        "\" to a bi-directional link. Status code: " +
                                        response.getStatusCode() + ".");
                                throw re;
                            }
                        }
                    }
                });
            }
            catch (ResponseException ex)
            {
                LOG.debug("After creating the 2-Way link an error occurred when reading the response from the remote application.", ex);
                warnings.add(i18nResolver.getText("applinks.error.response"));
            }
            catch (RuntimeException ex)
            {
                LOG.debug("An error occurred when trying to create the application link in the remote application.", ex);
                warnings.add(i18nResolver.getText("applinks.error.general"));
            }

            try
            {
                disableAutoConfigurableAuthenticationProviders(newApplicationLink,
                        authenticatedRequestFactory);
            }
            catch (AuthenticationConfigurationException e)
            {
                LOG.warn("Unable to reset existing authentication configuration: " + e.getMessage());
                warnings.add(i18nResolver.getText("applinks.ual.upgrade.autoconfiguration.delete.failed", e.getMessage()));
            }

            try
            {
                authenticationConfigurator.configureAuthenticationForApplicationLink(
                        newApplicationLink,
                        new AuthenticationScenario()
                        {
                            public boolean isCommonUserBase()
                            {
                                return upgradeApplicationLinkRequestEntity.getConfigFormValues().shareUserbase();
                            }

                            public boolean isTrusted()
                            {
                                return upgradeApplicationLinkRequestEntity.getConfigFormValues().trustEachOther();
                            }
                        },
                        authenticatedRequestFactory
                );
            }
            catch (AuthenticationConfigurationException e)
            {
                LOG.warn("Could not configure authentication providers for application link '" + newApplicationLink.getName() + "' ", e);
                warnings.add(i18nResolver.getText("applinks.link.create.autoconfiguration.failed"));
            }
        }

        if (upgradeApplicationLinkRequestEntity.isReciprocateEntityLinks())
        {
            reciprocateEntityLinks(newApplicationLink, authenticatedRequestFactory, warnings);
        }

        // emit event
        eventPublisher.publish(new ApplicationLinksIDChangedEvent(
                newApplicationLink, oldApplicationLink.getId()));

        LOG.info("Successfully upgraded Application Link {} (old application id: {} to new application id: {})",
                new Object[]{newApplicationLink.getName(), oldApplicationLink.getId(), newApplicationLink.getId()});

        return ok(new UpgradeApplicationLinkResponseEntity(toApplicationLinkEntity(newApplicationLink), warnings));
    }

    private void reciprocateEntityLinks(final ApplicationLink applicationLink, final RequestFactory authenticatedRequestFactory,
                                        final List<String> warnings)
            throws TypeNotInstalledException
    {
        final ApplicationLinkRequestFactory applicationLinkRequestFactory = new ApplicationLinkRequestFactory()
        {
            public ApplicationLinkRequest createRequest(Request.MethodType methodType, String url)
                    throws CredentialsRequiredException
            {
                return new ApplicationLinkRequestAdaptor(authenticatedRequestFactory.createRequest(methodType, url));
            }

            public URI getAuthorisationURI(URI callback)
            {
                return null;
            }

            public URI getAuthorisationURI()
            {
                return null;
            }
        };
        for (final EntityReference entityReference : internalHostApplication.getLocalEntities())
        {
            for (final EntityLink entityLink : entityLinkService.getEntityLinksForKey(
                    entityReference.getKey(),
                    entityReference.getType().getClass()))
            {
                if (applicationLink.equals(entityLink.getApplicationLink()))
                {
                    try
                    {
                        entityLinkClient.createEntityLinkFrom(
                                entityLink,
                                entityReference.getType(),
                                entityReference.getKey(),
                                applicationLinkRequestFactory);
                    }
                    catch (CredentialsRequiredException e)
                    {
                        // should never happen, as we're using a non-applink, pre-authenticated RequestFactory
                        throw new RuntimeException("Unexpected CredentialsRequiredException", e);
                    }
                    catch (ReciprocalActionException e)
                    {
                        final String warning = i18nResolver.getText("applinks.ual.upgrade.reciprocate.entitylinks.failed",
                                i18nResolver.getText(entityLink.getType().getI18nKey()),        // from "Charlie"
                                entityLink.getKey(),                                            // "FOO"
                                applicationLink.getName(),                                      // on "Remote RefApp"
                                i18nResolver.getText(entityReference.getType().getI18nKey()),   // to "Charlie"
                                entityReference.getKey());                                      // "BAR" on this server
                        warnings.add(warning);
                        LOG.error(warning, e);
                    }
                }
            }
        }
    }

    protected void disableAutoConfigurableAuthenticationProviders(final ApplicationLink applicationLink,
                                                                  final RequestFactory requestFactory)
            throws AuthenticationConfigurationException
    {
        for (final AutoConfiguringAuthenticatorProviderPluginModule module :
                pluginAccessor.getEnabledModulesByClass(AutoConfiguringAuthenticatorProviderPluginModule.class))
        {
            if (authenticationConfigurationManager.isConfigured(
                    applicationLink.getId(), module.getAuthenticationProviderClass()))
            {
                module.disable(requestFactory, applicationLink);
            }
        }
    }

    /**
     * Performs a "Legacy Upgrade". I.e. merely changes an AppLink's server ID.
     *
     * @param id
     * @return
     * @throws TypeNotInstalledException
     */
    @POST
    @Path("legacy/{applinkId}")
    public Response upgrade(@PathParam("applinkId") final String id) throws TypeNotInstalledException
    {
        final ApplicationId applicationId = new ApplicationId(id);
        final MutableApplicationLink applicationLink = applicationLinkService.getApplicationLink(applicationId);
        if (applicationLink == null)
        {
            return notFound(i18nResolver.getText("applinks.notfound", id));
        }
        else
        {
            String error;
            if (manifestRetriever.getApplicationStatus(applicationLink.getRpcUrl(), applicationLink.getType()) ==
                    ApplicationStatus.UNAVAILABLE)
            {
                error = i18nResolver.getText("applinks.legacy.upgrade.error.offline");
            }
            else
            {
                try
                {
                    final Manifest manifest = manifestRetriever
                            .getManifest(applicationLink.getRpcUrl(), applicationLink.getType());
                    if (!applicationLink.getId().equals(manifest.getId()))
                    {
                        if (manifest.getAppLinksVersion() != null && manifest.getAppLinksVersion().getMajor() >= 3)
                        {
                            error = i18nResolver.getText("applinks.legacy.upgrade.error.ual");
                        }
                        else
                        {
                            applicationLinkService.changeApplicationId(applicationId, manifest.getId());

                            // emit event
                            eventPublisher.publish(new ApplicationLinksIDChangedEvent(
                                    applicationLinkService.getApplicationLink(manifest.getId()), applicationId));

                            LOG.info("Successfully upgraded Application Link to non-UAL peer {} (old application id: {} to new application id: {})",
                                                        new Object[]{applicationLink.getName(), applicationId, manifest.getId()});
                            return Response.ok(new UpgradeApplicationLinkResponseEntity(toApplicationLinkEntity(
                                                applicationLinkService.getApplicationLink(manifest.getId())), Collections.<String>emptyList())).build();
                        }
                    }
                    else
                    {
                        // the IDs are the same.. nothing to upgrade
                        return Response.ok().build();
                    }
                }
                catch (ManifestNotFoundException e)
                {
                    error = i18nResolver.getText("applinks.legacy.upgrade.error.manifest",
                            TypeId.getTypeId(
                            applicationLink.getType()).toString(),
                            applicationLink.getId().toString());
                }
            }
            return badRequest(error);
        }
    }
}
