package com.atlassian.upm.rest.resources.updateall;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.token.TokenManager;

import static com.atlassian.upm.rest.MediaTypes.TASK_ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.UPDATE_ALL_REQUEST_JSON;
import static com.atlassian.upm.rest.resources.UpmResources.validateToken;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * A controller resource that allows all plugins to be updated.
 */
@Path("/updates/all")
public class UpdateAllResource
{
    private final AsynchronousTaskManager taskManager;
    private final PacClient pacClient;
    private final PluginInstaller pluginInstaller;
    private final RepresentationFactory representationFactory;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PluginDownloadService pluginDownloadService;
    private final PermissionEnforcer permissionEnforcer;
    private final AuditLogService auditLogger;
    private final UpmUriBuilder uriBuilder;
    private final UserManager userManager;
    private final TokenManager tokenManager;
    private final PluginLicenseRepository licenseRepository;

    public UpdateAllResource(PluginAccessorAndController pluginAccessorAndController,
                             AsynchronousTaskManager taskManager,
                             PacClient pacClient,
                             PluginDownloadService pluginDownloadService,
                             PluginInstaller pluginInstaller,
                             RepresentationFactory representationFactory,
                             PermissionEnforcer permissionEnforcer,
                             AuditLogService auditLogger,
                             UpmUriBuilder uriBuilder,
                             UserManager userManager,
                             TokenManager tokenManager,
                             PluginLicenseRepository licenseRepository)
    {
        this.userManager = checkNotNull(userManager, "userManager");
        this.tokenManager = checkNotNull(tokenManager, "tokenManager");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.taskManager = checkNotNull(taskManager, "taskManager");
        this.pacClient = checkNotNull(pacClient, "pacClient");
        this.pluginDownloadService = checkNotNull(pluginDownloadService, "pluginDownloadService");
        this.pluginInstaller = checkNotNull(pluginInstaller, "pluginInstaller");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
    }

    @POST
    @Consumes(UPDATE_ALL_REQUEST_JSON)
    public Response updateAll(@QueryParam("token") String token)
    {
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_INSTALL);
        validateToken(token, userManager.getRemoteUsername(), APPLICATION_JSON, tokenManager, representationFactory);
        
        if (pluginAccessorAndController.isSafeMode())
        {
            return Response.status(Status.CONFLICT).entity(representationFactory.createI18nErrorRepresentation("upm.update.error.safe.mode")).type(
                TASK_ERROR_JSON).build();
        }
        return taskManager.executeAsynchronousTask(
            new UpdateAllTask(pacClient, pluginDownloadService, pluginAccessorAndController, pluginInstaller,
                               auditLogger, uriBuilder, licenseRepository, userManager.getRemoteUsername()));
    }
}
