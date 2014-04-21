package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ApplicationLinkInfoEntity;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.link.MutatingEntityLinkService;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.resource.Singleton;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;

/**
 * This rest end point provides additional information about configured authentication providers and entity links for an Application Link.
 *
 * @since 3.0
 */
@Path ("applicationlinkInfo")
@Consumes ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Singleton
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class ApplicationLinkInfoResource
{
    private final PluginAccessor pluginAccessor;
    private final ApplicationLinkService applicationLinkService;
    private final I18nResolver i18nResolver;
    private final MutatingEntityLinkService entityLinkService;
    private final InternalHostApplication internalHostApplication;
    private final TypeAccessor typeAccessor;

    public ApplicationLinkInfoResource
            (final PluginAccessor pluginAccessor,
                    final ApplicationLinkService applicationLinkService,
                    final I18nResolver i18nResolver,
                    final MutatingEntityLinkService entityLinkService,
                    final InternalHostApplication internalHostApplication,
                    final TypeAccessor typeAccessor)
    {
        this.pluginAccessor = pluginAccessor;
        this.applicationLinkService = applicationLinkService;
        this.i18nResolver = i18nResolver;
        this.entityLinkService = entityLinkService;
        this.internalHostApplication = internalHostApplication;
        this.typeAccessor = typeAccessor;
    }

    @GET
    @Path ("id/{id}")
    public Response getConfiguredAuthenticationTypesAndEntityLinksForApplicationLink(@PathParam ("id") final ApplicationId id)
    {
        final ApplicationLink applicationLink;
        final int entityCount;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(id);
            entityCount = Lists.newArrayList(entityLinkService.getEntityLinksForApplicationLink(applicationLink)).size();
        }
        catch (TypeNotInstalledException e)
        {
            return notFound(i18nResolver.getText("applinks.type.not.installed", e.getType()));
        }

        if (applicationLink == null)
        {
            return notFound(i18nResolver.getText("applinks.notfound", id.get()));
        }
        final List<String> configuredAuthProviders = new ArrayList<String>();
        for (AuthenticationProviderPluginModule authenticationProviderPluginModule : pluginAccessor.getEnabledModulesByClass(AuthenticationProviderPluginModule.class))
        {
            final AuthenticationProvider authenticationProvider = authenticationProviderPluginModule.getAuthenticationProvider(applicationLink);
            if (authenticationProvider != null)
            {
                configuredAuthProviders.add(authenticationProviderPluginModule.getAuthenticationProviderClass().getName());
            }
        }
        //Get all entities and filter only the ones that are hosted by this application.
        final List<EntityType> entityTypes = pluginAccessor.getEnabledModulesByClass(EntityType.class);
        final Iterable<EntityType> hostApplicationEntityTypes = Iterables.filter(entityTypes, new Predicate<EntityType>()
        {
            public boolean apply(@Nullable final EntityType input)
            {
                return TypeId.getTypeId(internalHostApplication.getType()).equals(TypeId.getTypeId(typeAccessor.getApplicationType(input.getApplicationType())));
            }
        });
        final Iterable<String> hostAppEntityTypesAsString = Iterables.transform(hostApplicationEntityTypes, new Function<EntityType, String>()
        {
            public String apply(@Nullable final EntityType from)
            {
                return TypeId.getTypeId(from).get();
            }
        });
        //Now lets get the entities of the remote application
        final Iterable<EntityType> remoteApplicationEntityTypes = Iterables.filter(entityTypes, new Predicate<EntityType>()
        {
            public boolean apply(@Nullable final EntityType input)
            {
                 return TypeId.getTypeId(applicationLink.getType()).equals(TypeId.getTypeId(typeAccessor.getApplicationType(input.getApplicationType())));
            }
        });
        final Iterable<String> remoteApplicationEntityTypesAsString = Iterables.transform(remoteApplicationEntityTypes, new Function<EntityType, String>()
        {
            public String apply(@Nullable final EntityType from)
            {
                return TypeId.getTypeId(from).get();
            }
        });

        return ok(new ApplicationLinkInfoEntity(configuredAuthProviders, entityCount, Lists.newArrayList(hostAppEntityTypesAsString), Lists.newArrayList(remoteApplicationEntityTypesAsString)));
    }
}
