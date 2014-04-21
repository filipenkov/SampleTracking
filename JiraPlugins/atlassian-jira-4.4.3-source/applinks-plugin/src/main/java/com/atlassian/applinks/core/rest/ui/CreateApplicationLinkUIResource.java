package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.applinks.core.auth.oauth.OAuthAuthenticatorProviderPluginModule;
import com.atlassian.applinks.core.auth.trusted.TrustedAppsAuthenticationProviderPluginModule;
import com.atlassian.applinks.core.net.BasicHTTPAuthRequestFactory;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.core.rest.AbstractResource;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ApplicationLinkEntity;
import com.atlassian.applinks.core.rest.model.CreateApplicationLinkRequestEntity;
import com.atlassian.applinks.core.rest.model.CreatedApplicationLinkEntity;
import com.atlassian.applinks.core.rest.model.ManifestEntity;
import com.atlassian.applinks.core.rest.model.OrphanedTrust;
import com.atlassian.applinks.core.rest.model.ResponseInfoEntity;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.core.util.Holder;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.StaticUrlApplicationType;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.AuthenticationResponseException;
import com.atlassian.applinks.spi.link.LinkCreationResponseException;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.link.NotAdministratorException;
import com.atlassian.applinks.spi.link.ReciprocalActionException;
import com.atlassian.applinks.spi.link.RemoteErrorListException;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.atlassian.applinks.core.rest.util.RestUtil.badFormRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.badRequest;
import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;
import static com.atlassian.applinks.core.rest.util.RestUtil.serverError;

/**
 * This rest end point handles requests from the add and edit application link form.
 *
 * @since 3.0
 */
@Path ("applicationlinkForm")
@Consumes ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Singleton
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class CreateApplicationLinkUIResource extends AbstractResource
{
    private final MutatingApplicationLinkService applicationLinkService;
    private final ManifestRetriever manifestRetriever;
    private final InternalHostApplication internalHostApplication;
    private final I18nResolver i18nResolver;
    private static final Logger LOG = LoggerFactory.getLogger(CreateApplicationLinkUIResource.class);
    private final OrphanedTrustDetector orphanedTrustDetector;
    private final PluginAccessor pluginAccessor;

    public CreateApplicationLinkUIResource(
            final MutatingApplicationLinkService applicationLinkService,
            final RequestFactory requestFactory,
            final InternalHostApplication internalHostApplication,
            final I18nResolver i18nResolver,
            final InternalTypeAccessor typeAccessor,
            final ManifestRetriever manifestRetriever,
            final RestUrlBuilder restUrlBuilder,
            @Qualifier ("delegatingOrphanedTrustDetector")
            final OrphanedTrustDetector orphanedTrustDetector,
            final PluginAccessor pluginAccessor)
    {
        super (restUrlBuilder, typeAccessor, requestFactory, applicationLinkService);
        this.i18nResolver = i18nResolver;
        this.internalHostApplication = internalHostApplication;
        this.applicationLinkService = applicationLinkService;
        this.manifestRetriever = manifestRetriever;
        this.orphanedTrustDetector = orphanedTrustDetector;
        this.pluginAccessor = pluginAccessor;
    }

    @GET
    @Path("manifest")
    public javax.ws.rs.core.Response tryToFetchManifest(@QueryParam("url") String url)
    {
        if (StringUtils.isBlank(url))
        {
            return badRequest(i18nResolver.getText("applinks.error.rpcurl"));
        }

        final URI manifestUrl;
        Manifest manifest;
        try
        {
            LOG.debug("URL received '" + url + "'");
            manifestUrl = new URL(url).toURI();
            manifest = manifestRetriever.getManifest(manifestUrl);
        }
        catch (ManifestNotFoundException e)
        {
            manifest = null;
            final Throwable responseException = e.getCause();
            if (responseException != null)
            {
                if (responseException instanceof IOException)
                {
                    return ok(new ResponseInfoEntity(i18nResolver.getText("applinks.warning.unknown.host")));
                }
            }
        }
        catch (Exception e)
        {
            Pattern p = Pattern.compile("http(s)?:/[^/].*");
            Matcher m = p.matcher(url);
            if (m.matches())
            {
                LOG.warn("The url '" + url + "' is missing the double slashes after the protocol. Is there a proxy server in the middle that has replaced the '//' with a single '/'?");
            }
            LOG.debug("Invalid URL url='"+url+"'", e);
            return badRequest(i18nResolver.getText("applinks.error.url.invalid", url, e.getMessage()));
        }

        if (manifest != null)
        {
            try
            {
                if (typeAccessor.loadApplicationType(manifest.getTypeId()) == null)
                {
                    throw new TypeNotInstalledException(manifest.getTypeId().get());
                }
                // Check if a matching app link already exists
                final ApplicationLink existingAppLink = applicationLinkService.getApplicationLink(manifest.getId());
                if (existingAppLink != null)
                {
                    if (existingAppLink.getDisplayUrl().equals(manifest.getUrl()))
                    {
                        return notFound(i18nResolver.getText("applinks.error.applink.exists"));
                    }
                    return notFound(i18nResolver.getText("applinks.error.applink.exists.with.different.url"));
                }
            }
            catch (TypeNotInstalledException e)
            {
                return badRequest(String.format(i18nResolver.getText("applinks.error.remote.type.not.installed", e.getType())));
            }
        }
        else
        {
            return ok(new ResponseInfoEntity());
        }

        if (manifest.getId().equals(internalHostApplication.getId()))
        {
            return notFound(i18nResolver.getText("applinks.error.applink.itsme"));
        }
        return ok(new ManifestEntity(manifest));
    }

    @POST
    @Path ("createStaticUrlAppLink")
    public javax.ws.rs.core.Response createStaticUrlAppLink(@QueryParam ("typeId") final String typeId) throws Exception
    {
        final StaticUrlApplicationType type = (StaticUrlApplicationType) typeAccessor.loadApplicationType(typeId);
        Manifest manifest = manifestRetriever.getManifest(type.getStaticUrl(), type);
        ApplicationLinkDetails details = ApplicationLinkDetails.builder().name(type.getI18nKey())
                .displayUrl(type.getStaticUrl()).rpcUrl(type.getStaticUrl()).isPrimary(true).build();
        final ApplicationLink createdApplicationLink = applicationLinkService.addApplicationLink(manifest.getId(), type, details);
        return ok(new CreatedApplicationLinkEntity(toApplicationLinkEntity(createdApplicationLink), true));
    }

    @POST
    @Path("createAppLink")
    public javax.ws.rs.core.Response createApplicationLink(final CreateApplicationLinkRequestEntity applicationLinkRequest)
    {
        final ApplicationLinkEntity applicationLink = applicationLinkRequest.getApplicationLink();
        final URI remoteRpcUrl = applicationLink.getRpcUrl();

        if (StringUtils.isEmpty(applicationLink.getName().trim()))
        {
            return badFormRequest(Lists.newArrayList(i18nResolver.getText("applinks.error.appname")), Lists.newArrayList("application-name"));
        }

        if (StringUtils.isEmpty(applicationLink.getTypeId().get()) || typeAccessor.loadApplicationType(applicationLink.getTypeId()) == null)
        {
            return badFormRequest(Lists.newArrayList(i18nResolver.getText("applinks.error.apptype")), Lists.newArrayList("application-types"));
        }

        try
        {
            //Let's see if an application link with this RPC url already exists.
            Iterables.find(applicationLinkService.getApplicationLinks(), new Predicate<ApplicationLink>()
            {
                public boolean apply(@Nullable final ApplicationLink input)
                {
                    return (input.getRpcUrl().equals(applicationLink.getRpcUrl()));
                }
            });
            return badRequest(i18nResolver.getText("applinks.error.rpcurl.exists"));
        }
        catch (NoSuchElementException ex)
        {
            //We expect that no application link with this URL exists.
        }

        if (applicationLinkRequest.createTwoWayLink())
        {
            try {
                applicationLinkService.createReciprocalLink(remoteRpcUrl, applicationLinkRequest.isCustomRpcURL() ? applicationLinkRequest.getRpcUrl() : null, applicationLinkRequest.getUsername(), applicationLinkRequest.getPassword());
            } catch (final NotAdministratorException exception) {
                return badFormRequest(Lists.newArrayList(i18nResolver.getText("applinks.error.unauthorized")), Lists.newArrayList("authorization"));
            } catch (final LinkCreationResponseException exception) {
                return serverError(i18nResolver.getText("applinks.error.response"));
            } catch (final AuthenticationResponseException exception) {
                return serverError(i18nResolver.getText("applinks.error.authorization.response"));
            } catch (final RemoteErrorListException exception) {
                ArrayList<String> errors = Lists.newArrayList(i18nResolver.getText("applinks.error.general"));
                errors.addAll(exception.getErrors());
                return RestUtil.badRequest(errors.toArray(new String[0]));
            } catch (final ReciprocalActionException exception) {
                return serverError(i18nResolver.getText("applinks.error.general"));
            }
        }

        final ApplicationType type = typeAccessor.loadApplicationType(applicationLink.getTypeId().get());
        final ApplicationLink createdApplicationLink;
        try {
            createdApplicationLink = applicationLinkService.createApplicationLink(type, applicationLink.getDetails());
        } catch (ManifestNotFoundException e) {
            return serverError(i18nResolver.getText("applinks.error.incorrect.application.type"));
        }

        boolean autoConfigurationSuccessful = true;
        if (applicationLinkRequest.createTwoWayLink())
        {
            try
            {
                final boolean shareUserbase = applicationLinkRequest.getConfigFormValues().shareUserbase();
                final boolean trustEachOther = applicationLinkRequest.getConfigFormValues().trustEachOther();

                applicationLinkService.configureAuthenticationForApplicationLink(
                        createdApplicationLink,
                        new AuthenticationScenario()
                        {
                            public boolean isCommonUserBase()
                            {
                                return shareUserbase;
                            }

                            public boolean isTrusted()
                            {
                                return trustEachOther;
                            }
                        },
                        applicationLinkRequest.getUsername(),
                        applicationLinkRequest.getPassword());

            }
            catch (AuthenticationConfigurationException e)
            {
                LOG.warn("Error during auto-configuration of authentication providers for application link '" + createdApplicationLink + "'", e);
                autoConfigurationSuccessful = false;
            }
        }

        if (applicationLinkRequest.getOrphanedTrust() != null)
        {
            OrphanedTrust orphanedTrust = applicationLinkRequest.getOrphanedTrust();
            final OrphanedTrustCertificate.Type certificateType;
            try
            {
                certificateType = OrphanedTrustCertificate.Type.valueOf(orphanedTrust.getType());
                orphanedTrustDetector.addOrphanedTrustToApplicationLink(orphanedTrust.getId(), certificateType, createdApplicationLink.getId());

                if (applicationLinkRequest.createTwoWayLink())
                {
                    AutoConfiguringAuthenticatorProviderPluginModule providerPluginModule = getAutoConfigurationPluginModule(certificateType);
                    if (providerPluginModule != null)
                    {
                        providerPluginModule.enable(getAuthenticatedRequestFactory(applicationLinkRequest), createdApplicationLink);
                    }
                    else
                    {
                        LOG.warn("Failed to find an authentication type for the orphaned trust certificate type='" + orphanedTrust.getType() + "' and id='" + orphanedTrust.getId() + "' that supports auto-configuration");
                    }
                }
            }
            catch (Exception e)
            {
                LOG.error("Failed to add orphaned trust certificate with type='" + orphanedTrust.getType() + "' and id='" + orphanedTrust.getId() + "'", e);
            }
        }
        return ok(new CreatedApplicationLinkEntity(toApplicationLinkEntity(createdApplicationLink), autoConfigurationSuccessful));
    }

    private AutoConfiguringAuthenticatorProviderPluginModule getAutoConfigurationPluginModule(final OrphanedTrustCertificate.Type certificateType)
    {
        OrphanedTrustCertificate.Type type = OrphanedTrustCertificate.Type.valueOf(certificateType.name());
        AutoConfiguringAuthenticatorProviderPluginModule providerPluginModule;
        if (type == OrphanedTrustCertificate.Type.OAUTH)
        {
            providerPluginModule = findAutoConfiguringAuthenticationProviderModule(OAuthAuthenticatorProviderPluginModule.class);
            if (providerPluginModule != null)
            {
                return providerPluginModule;
            }
        }
        if (type == OrphanedTrustCertificate.Type.TRUSTED_APPS)
        {
            providerPluginModule = findAutoConfiguringAuthenticationProviderModule(TrustedAppsAuthenticationProviderPluginModule.class);
            if (providerPluginModule != null)
            {
                return providerPluginModule;
            }
        }
        return null;
    }

    private AutoConfiguringAuthenticatorProviderPluginModule findAutoConfiguringAuthenticationProviderModule(final Class<? extends AuthenticationProviderPluginModule> authProviderClass)
    {
        List<AuthenticationProviderModuleDescriptor> authenticationProviderModuleDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class);
        for (AuthenticationProviderModuleDescriptor authenticationProviderModuleDescriptor : authenticationProviderModuleDescriptors)
        {
            if (authProviderClass.isAssignableFrom(authenticationProviderModuleDescriptor.getModule().getClass()))
            {
                return (AutoConfiguringAuthenticatorProviderPluginModule) authenticationProviderModuleDescriptor.getModule();
            }
        }
        return null;
    }

    private BasicHTTPAuthRequestFactory<Request<Request<?, Response>, Response>> getAuthenticatedRequestFactory(final CreateApplicationLinkRequestEntity applicationLinkRequest)
    {
        return new BasicHTTPAuthRequestFactory<Request<Request<?, Response>,Response>>(
                requestFactory,
                applicationLinkRequest.getUsername(),
                applicationLinkRequest.getPassword());
    }

    @GET
    @Path("details")
    public javax.ws.rs.core.Response verifyTwoWayLinkDetails(
            @QueryParam("remoteUrl") final URI remoteUrl,
            @QueryParam("username") final String username,
            @QueryParam("password") final String password,
            @QueryParam("rpcUrl") final URI rpcurl)
            throws TypeNotInstalledException
    {
        boolean isAdminUser;
        try
        {
            isAdminUser = applicationLinkService.isAdminUserInRemoteApplication(remoteUrl, username, password);
        }
        catch (ResponseException e)
        {
            // The first time we connect to the remote server, so we assume any errors are
            // caused by errors connecting TO the remote server
            LOG.error("Error occurred while checking credentials.", e);
            return serverError(i18nResolver.getText("applinks.error.authorization.response"));
        }

        if (isAdminUser)
        {
            final String applicationType = i18nResolver.getText(internalHostApplication.getType().getI18nKey());
            try
            {
                if (isRpcUrlValid(remoteUrl, rpcurl, username, password))
                {
                    return ok();
                }
                else
                {
                    return badRequest(i18nResolver.getText("applinks.error.url.reciprocal.rpc.url.invalid", internalHostApplication.getName(), applicationType, rpcurl));
                }
            }
            catch (ResponseException e)
            {
                // Since we have already connected to the server before, we assume any errors are
                // caused by errors connecting FROM the remote server back to this instance.
                LOG.error("Error occurred while checking reciprocal link.", e);
                return badRequest(i18nResolver.getText("applinks.error.url.reciprocal.rpc.url.invalid", internalHostApplication.getName(), applicationType, rpcurl));
            }
        }
        else
        {
            return badFormRequest(Lists.newArrayList(i18nResolver.getText("applinks.error.unauthorized")), Lists.newArrayList("reciprocal-link-password"));
        }
    }

    private boolean isRpcUrlValid(final URI url, final URI rpcUrl, final String username, final String password)
            throws ResponseException
    {
        // We send the rpcUrl parameter in a query parameter. For pre-3.4 versions of the REST resource, we also
        //  send it in the path.
        // TODO If we know the server is using applinks 3.4, the rpcUrl path parameter can be empty
        String pathUrl = getUrlFor(URIUtil.uncheckedConcatenate(url, RestUtil.REST_APPLINKS_URL), AuthenticationResource.class).rpcUrlIsReachable(internalHostApplication.getId().get(), rpcUrl, null).toString();

        String urlWithQuery = pathUrl + "?url=" + URIUtil.utf8Encode(rpcUrl);

        final Request request = requestFactory.createRequest(Request.MethodType.GET, urlWithQuery);
        request.addBasicAuthentication(username, password);

        final Holder<Boolean> rpcUrlValid = new Holder<Boolean>(false);

        request.execute(new ResponseHandler<Response>()
        {
            public void handle(final Response restResponse) throws ResponseException
            {
                if (restResponse.isSuccessful())
                {
                    rpcUrlValid.set(true);
                }
            }
        });
        return rpcUrlValid.get();
    }
}
