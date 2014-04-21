package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.auth.AuthenticatorAccessor;
import com.atlassian.applinks.core.concurrent.ConcurrentExecutor;
import com.atlassian.applinks.core.rest.AbstractResource;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.rest.model.ApplicationLinkState;
import com.atlassian.applinks.core.rest.model.LinkAndAuthProviderEntity;
import com.atlassian.applinks.core.rest.model.ListEntity;
import com.atlassian.applinks.core.rest.model.WebItemEntityList;
import com.atlassian.applinks.core.rest.model.WebPanelEntityList;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.core.webfragment.WebFragmentContext;
import com.atlassian.applinks.core.webfragment.WebFragmentHelper;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.auth.IncomingTrustAuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.resource.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.atlassian.applinks.core.rest.util.RestUtil.ok;

/**
 * This rest end point is used when displaying application links.
 *
 * @since 3.0
 */
@Path ("listApplicationlinks")
@Consumes ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Singleton
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class ListApplicationLinksUIResource extends AbstractResource
{
    private static final Logger LOG = LoggerFactory.getLogger(ListApplicationLinksUIResource.class);

    private final MutatingApplicationLinkService applicationLinkService;
    private final InternalHostApplication internalHostApplication;
    private final ManifestRetriever manifestRetriever;
    private final I18nResolver i18nResolver;
    private final WebFragmentHelper webFragmentHelper;
	private final AuthenticatorAccessor authenticatorAccessor;
    private final ConcurrentExecutor executor;

    public ListApplicationLinksUIResource
    (
            final MutatingApplicationLinkService applicationLinkService,
            final InternalHostApplication internalHostApplication,
            final ManifestRetriever manifestRetriever,
            final I18nResolver i18nResolver,
            final WebFragmentHelper webFragmentHelper,
            final RestUrlBuilder restUrlBuilder,
            final AuthenticatorAccessor authenticatorAccessor,
            final RequestFactory requestFactory,
            final InternalTypeAccessor typeAccessor,
            final ConcurrentExecutor executor)
    {
        super (restUrlBuilder, typeAccessor, requestFactory, applicationLinkService);
        this.applicationLinkService = applicationLinkService;
        this.internalHostApplication = internalHostApplication;
        this.manifestRetriever = manifestRetriever;
        this.i18nResolver = i18nResolver;
        this.webFragmentHelper = webFragmentHelper;
		this.authenticatorAccessor = authenticatorAccessor;
		this.executor = executor;
    }

    private LinkAndAuthProviderEntity getLinkAndAuthProviderEntity(final ApplicationLink applicationLink)
    {
        // contact the peer in a background job:
        final Future<ApplicationLinkState> linkStateFuture = getApplicationLinkState(applicationLink);

        final Set<Class<? extends AuthenticationProvider>> configuredOutgoingAuthenticationProviders = new LinkedHashSet<Class<? extends AuthenticationProvider>>();
        final Set<Class<? extends AuthenticationProvider>> configuredIncomingAuthenticationProviders = new LinkedHashSet<Class<? extends AuthenticationProvider>>();
        for (AuthenticationProviderPluginModule authenticationProviderPluginModule: authenticatorAccessor.getAllAuthenticationProviderPluginModules())
        {
            AuthenticationProvider authenticationProvider = authenticationProviderPluginModule.getAuthenticationProvider(applicationLink);
            if (authenticationProvider != null)
            {
                configuredOutgoingAuthenticationProviders.add(authenticationProviderPluginModule.getAuthenticationProviderClass());
            }
            if (authenticationProviderPluginModule instanceof IncomingTrustAuthenticationProviderPluginModule)
            {
                if (((IncomingTrustAuthenticationProviderPluginModule) authenticationProviderPluginModule).incomingEnabled(applicationLink))
                {
                        configuredIncomingAuthenticationProviders.add(authenticationProviderPluginModule.getAuthenticationProviderClass());
                }
            }
        }
        boolean hasIncomingAuthenticationProviders = true;
        boolean hasOutgoingAuthenticationProviders = true;
        try
        {
            final Manifest manifest = manifestRetriever.getManifest(
                    applicationLink.getRpcUrl(), applicationLink.getType());
            hasIncomingAuthenticationProviders = Sets.intersection(
                Sets.<Class<? extends AuthenticationProvider>>newHashSet(internalHostApplication.getSupportedInboundAuthenticationTypes()),
                manifest.getOutboundAuthenticationTypes()).size() > 0;

            hasOutgoingAuthenticationProviders = Sets.intersection(
                Sets.<Class<? extends AuthenticationProvider>>newHashSet(internalHostApplication.getSupportedOutboundAuthenticationTypes()),
                manifest.getInboundAuthenticationTypes()).size() > 0;
        }
        catch (ManifestNotFoundException e)
        {
            /**
             * If the manifest is cannot be retrieved, we assume there are authentication providers configured in
             * both directions, but later on in the configuration UI we show a message
             * saying that we weren't able to display the configuration information, because there is no manifest.
             */
        }

        final WebFragmentContext context = new WebFragmentContext.Builder().applicationLink(applicationLink).build();
        final WebItemEntityList webItems = webFragmentHelper
                .getWebItemsForLocation(WebFragmentHelper.APPLICATION_LINK_LIST_OPERATION, context);
        final WebPanelEntityList webPanels = webFragmentHelper
                .getWebPanelsForLocation(WebFragmentHelper.APPLICATION_LINK_LIST_OPERATION, context);


        return new LinkAndAuthProviderEntity(
                toApplicationLinkEntity(applicationLink),
                configuredOutgoingAuthenticationProviders,
                configuredIncomingAuthenticationProviders,
                hasOutgoingAuthenticationProviders,
                hasIncomingAuthenticationProviders,
                webItems.getItems(),
                webPanels.getWebPanels(),
                collectApplicationLinkStateResult(linkStateFuture),
                getEntityTypeIdsForApplication(applicationLink));
    }

    private Future<ApplicationLinkState> getApplicationLinkState(final ApplicationLink applicationLink)
    {
        return executor.submit(new CurrentContextAwareCallable<ApplicationLinkState>()
        {
            @Override
            public ApplicationLinkState callWithContext() throws Exception
            {
                if (manifestRetriever.getApplicationStatus(applicationLink.getRpcUrl(), applicationLink.getType()) ==
                        ApplicationStatus.UNAVAILABLE)
                {
                    return ApplicationLinkState.OFFLINE;
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
                                return ApplicationLinkState.UPGRADED_TO_UAL;
                            }
                            else
                            {
                                return ApplicationLinkState.UPGRADED;
                            }
                        }
                    }
                    catch (ManifestNotFoundException e)
                    {
                        // unknown application type
                        LOG.error("The {} application type failed to produce a " +
                                "Manifest for Application Link {}, so we cannot " +
                                "determine the link status.", TypeId.getTypeId(
                                applicationLink.getType()).toString(),
                                applicationLink.getId().toString());
                    }
                    return ApplicationLinkState.OK;
                }
            }
        });
    }

    private ApplicationLinkState collectApplicationLinkStateResult(final Future<ApplicationLinkState> linkStateFuture)
    {
        try
        {
            return linkStateFuture.get();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    private List<Callable<LinkAndAuthProviderEntity>> createJobs(final Iterable<ApplicationLink> applicationLinks)
    {
        return Lists.transform(
                ImmutableList.copyOf(applicationLinks),
                new Function<ApplicationLink, Callable<LinkAndAuthProviderEntity>>()
                {
                    public Callable<LinkAndAuthProviderEntity> apply(@Nullable final ApplicationLink applicationLink)
                    {
                        return new CurrentContextAwareCallable<LinkAndAuthProviderEntity>()
                        {
                            @Override
                            public LinkAndAuthProviderEntity callWithContext() throws Exception
                            {
                                return getLinkAndAuthProviderEntity(applicationLink);
                            }
                        };
                    }
                });
    }

    private abstract static class CurrentContextAwareCallable<T> implements Callable<T>
    {
        private final HttpContext httpContext;
        private final HttpServletRequest httpServletRequest;

        private CurrentContextAwareCallable()
        {
            this.httpContext = CurrentContext.getContext();
            this.httpServletRequest = CurrentContext.getHttpServletRequest();
        }

        public final T call() throws Exception
        {
            final HttpContext oldContext = CurrentContext.getContext();
            final HttpServletRequest oldRequest = CurrentContext.getHttpServletRequest();
            CurrentContext.setContext(httpContext);
            CurrentContext.setHttpServletRequest(httpServletRequest);
            try
            {
                return callWithContext();
            }
            finally
            {
                CurrentContext.setContext(oldContext);
                CurrentContext.setHttpServletRequest(oldRequest);
            }
        }

        public abstract T callWithContext() throws Exception;
    }

    @GET
    public Response getApplicationLinks()
    {

        try
        {
            final List<LinkAndAuthProviderEntity> links = new ArrayList<LinkAndAuthProviderEntity>();
            for (final Future<LinkAndAuthProviderEntity> future : executor.invokeAll(createJobs(applicationLinkService.getApplicationLinks())))
            {
                links.add(future.get());
            }
            Collections.sort(links, new Comparator<LinkAndAuthProviderEntity>()
            {
                public int compare(final LinkAndAuthProviderEntity e1, final LinkAndAuthProviderEntity e2)
                {
                    final int compareByType = e1.getApplication().getTypeId().get().compareTo(e2.getApplication().getTypeId().get());
                    if (compareByType != 0)
                    {
                        return compareByType;
                    }
                    else
                    {
                        //Compare by name
                        return e1.getApplication().getName().compareTo(e2.getApplication().getName());
                    }
                }
            });
            return ok(new ListEntity<LinkAndAuthProviderEntity>(links));
        }
        catch (Exception e)
        {
            LOG.error("Error occurred when retrieving list of application links", e);
            return RestUtil.serverError(i18nResolver.getText("applinks.error.retrieving.application.link.list", e.getMessage()));
        }
    }

    private Set<String> getEntityTypeIdsForApplication(final ApplicationLink applicationLink)
    {
        return Sets.newHashSet(
                Iterables.transform(typeAccessor.getEnabledEntityTypesForApplicationType(applicationLink.getType()),
                        new Function<EntityType, String>()
                {
                    public String apply(@Nullable EntityType from)
                    {
                        return TypeId.getTypeId(from).get();
                    }
                }));
    }
}
