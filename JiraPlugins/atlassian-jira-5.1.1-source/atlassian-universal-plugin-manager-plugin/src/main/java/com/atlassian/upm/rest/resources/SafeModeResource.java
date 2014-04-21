package com.atlassian.upm.rest.resources;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.atlassian.upm.ConfigurationStoreException;
import com.atlassian.upm.MissingSavedConfigurationException;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginModuleStateUpdateException;
import com.atlassian.upm.PluginStateUpdateException;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_ERROR_REENABLING_PLUGIN_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_ERROR_REENABLING_PLUGIN_MODULE_JSON;
import static com.atlassian.upm.rest.MediaTypes.SAFE_MODE_FLAG_JSON;
import static com.atlassian.upm.permission.Permission.GET_SAFE_MODE;
import static com.atlassian.upm.permission.Permission.MANAGE_SAFE_MODE;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;

/**
 * Provide a REST resources for the Safe Mode
 */
@Path("/safe-mode")
public class SafeModeResource
{
    private final RepresentationFactory representationFactory;
    private final PluginAccessorAndController accessor;
    private final UpmUriBuilder uriBuilder;
    private final PermissionEnforcer permissionEnforcer;

    public SafeModeResource(RepresentationFactory representationFactory, PluginAccessorAndController accessor, UpmUriBuilder uriBuilder,
        PermissionEnforcer permissionEnforcer)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.accessor = checkNotNull(accessor, "accessor");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
    }

    @GET
    @Produces(SAFE_MODE_FLAG_JSON)
    public Response get()
    {
        permissionEnforcer.enforcePermission(GET_SAFE_MODE);
        return ok(retrieveSafeModeFlagEntity()).build();
    }

    /**
     * REST service to trigger or exit from "Safe Mode".
     *
     * @param keepState A flag used to indicate if the current state of the plugins system will be kept, or if the
     * saved configuration will be restored when exiting from safe mode.
     * @param flag {@code SafeModeFlag} to indicate if "Safe Mode" will be triggered or exited
     */
    @PUT
    @Consumes(SAFE_MODE_FLAG_JSON)
    public Response put(@QueryParam("keepState") boolean keepState,
        SafeModeFlag flag)
    {
        permissionEnforcer.enforcePermission(MANAGE_SAFE_MODE);
        if (flag.isEnabled())
        {
            if (accessor.isSafeMode())
            {
                return status(CONFLICT)
                    .entity(representationFactory.createI18nErrorRepresentation("upm.safeMode.error.already.entered.safeMode"))
                    .type(ERROR_JSON).build();
            }
            if (!triggerSafeMode())
            {
                return status(INTERNAL_SERVER_ERROR)
                    .entity(representationFactory.createI18nErrorRepresentation(
                        "upm.safeMode.error.cannot.go.to.safe.mode"
                    )).type(ERROR_JSON).build();
            }
        }
        else
        {
            if (!accessor.isSafeMode())
            {
                return status(CONFLICT)
                    .entity(representationFactory.createI18nErrorRepresentation("upm.safeMode.error.already.exited.safeMode"))
                    .type(ERROR_JSON).build();
            }
            exitSafeMode(keepState);
        }
        return ok(retrieveSafeModeFlagEntity()).type(SAFE_MODE_FLAG_JSON).build();
    }

    private SafeModeFlag retrieveSafeModeFlagEntity()
    {
        if (accessor.isSafeMode())
        {
            return new SafeModeFlag(true, ImmutableMap.<String, URI>of(
                "exit-safe-mode-restore", uriBuilder.buildExitSafeModeUri(false),
                "exit-safe-mode-keep", uriBuilder.buildExitSafeModeUri(true)));
        }
        else
        {
            return new SafeModeFlag(false, ImmutableMap.<String, URI>of("safe-mode", uriBuilder.buildSafeModeUri()));
        }
    }

    private boolean triggerSafeMode()
    {
        try
        {
            return accessor.enterSafeMode();
        }
        catch (ConfigurationStoreException e)
        {
            throw new WebApplicationException(status(FORBIDDEN)
                .entity(representationFactory.createErrorRepresentation(
                    "upm.safeMode.error.cannot.save.configuration",
                    e.getMessage()))
                .type(ERROR_JSON)
                .build());
        }
    }

    private void exitSafeMode(boolean keepState)
    {
        try
        {
            accessor.exitSafeMode(keepState);
        }
        catch (MissingSavedConfigurationException msce)
        {
            throw new WebApplicationException(msce, status(INTERNAL_SERVER_ERROR)
                .entity(representationFactory.createErrorRepresentation(
                    "System failed to restore from Safe Mode.  Plugin system configuration from prior to entering safe mode is missing",
                    "upm.safeMode.error.missing.configuration"
                )).type(ERROR_JSON).build()
            );
        }
        catch (PluginStateUpdateException psue)
        {
            throw new WebApplicationException(psue, status(INTERNAL_SERVER_ERROR)
                .entity(representationFactory.createSafeModeErrorReenablingPluginRepresentation(psue.getPlugin()))
                .type(SAFE_MODE_ERROR_REENABLING_PLUGIN_JSON).build()
            );
        }
        catch (PluginModuleStateUpdateException pmsue)
        {
            throw new WebApplicationException(pmsue, status(INTERNAL_SERVER_ERROR)
                .entity(representationFactory.createSafeModeErrorReenablingPluginModuleRepresentation(pmsue.getPlugin(), pmsue.getPluginModule()))
                .type(SAFE_MODE_ERROR_REENABLING_PLUGIN_MODULE_JSON).build()
            );
        }

    }

    public static class SafeModeFlag
    {
        @JsonProperty private boolean enabled;
        @JsonProperty final Map<String, URI> links;

        @JsonCreator
        public SafeModeFlag(@JsonProperty("enabled") boolean enabled, @JsonProperty("links") Map<String, URI> links)
        {
            this.enabled = enabled;
            this.links = links == null ? Collections.<String, URI>emptyMap() : ImmutableMap.copyOf(links);
        }

        /**
         * A boolean flag that indicates whether safe mode will be enabled or not
         *
         * @return {@code true} if safe mode will be triggered, {@code false} otherwise
         */
        public boolean isEnabled()
        {
            return enabled;
        }
    }
}
