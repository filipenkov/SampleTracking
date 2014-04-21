package com.atlassian.upm.rest.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.atlassian.plugins.PacException;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartHandler;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.representations.RepresentationFactory;
import com.atlassian.upm.rest.resources.install.InstallFromFileTask;
import com.atlassian.upm.rest.resources.install.InstallFromUriTask;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.token.TokenManager;

import com.google.common.collect.ImmutableList;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.plugins.rest.common.MediaTypes.MULTIPART_MIXED;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.rest.MediaTypes.ERROR_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALLED_PLUGINS_COLLECTION_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_URI_JSON;
import static com.atlassian.upm.rest.MediaTypes.TASK_ERROR_JSON;
import static com.atlassian.upm.rest.resources.UpmResources.validateToken;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MEDIA_TYPE_WILDCARD;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

/**
 * Provides REST resources for retrieving some info on all plugins in the current configuration and for installing new
 * plugins
 */
@Path("/")
public class InstalledPluginCollectionResource
{
    private final RepresentationFactory representationFactory;
    private final PluginDownloadService pluginDownloadService;
    private final PluginInstaller pluginInstaller;
    private final SelfUpdateController selfUpdateController;
    private final AsynchronousTaskManager taskManager;
    private final PermissionEnforcer permissionEnforcer;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final LocaleResolver localeResolver;
    private final AuditLogService auditLogger;
    private final TokenManager tokenManager;
    private final UserManager userManager;
    private final PacClient pacClient;

    public InstalledPluginCollectionResource(RepresentationFactory representationFactory,
                                             PluginDownloadService pluginDownloadService,
                                             PluginInstaller pluginInstaller,
                                             SelfUpdateController selfUpdateController,
                                             AsynchronousTaskManager taskManager,
                                             PermissionEnforcer permissionEnforcer,
                                             PluginAccessorAndController pluginAccessorAndController,
                                             LocaleResolver localeResolver,
                                             AuditLogService auditLogger,
                                             TokenManager tokenManager,
                                             UserManager userManager,
                                             PacClient pacClient)
    {
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
        this.pluginDownloadService = checkNotNull(pluginDownloadService, "pluginDownloadService");
        this.pluginInstaller = checkNotNull(pluginInstaller, "pluginInstaller");
        this.selfUpdateController = checkNotNull(selfUpdateController, "selfUpdateController");
        this.taskManager = checkNotNull(taskManager, "taskManager");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.localeResolver = checkNotNull(localeResolver, "localeResolver");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
        this.tokenManager = checkNotNull(tokenManager, "tokenManager");
        this.userManager = checkNotNull(userManager, "userManager");
        this.pacClient = checkNotNull(pacClient, "pacClient");
    }

    /**
     * Retrieves a JSON representation of the plugins in the current configuration. Anyone who has access to UPM has
     * permission to access this resource method.
     * 
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @GET
    @Produces(INSTALLED_PLUGINS_COLLECTION_JSON)
    public Response get(@Context HttpServletRequest request)
    {
        permissionEnforcer.enforceAdmin();

        // UPM-1623: If the PAC request fails at this point, then we'll just return the list of plugins without any
        // available updates. The front end will still be able to detect that PAC is down via the status resource.
        boolean pacUnavailable = false;
        Iterable<PluginVersion> updates = ImmutableList.of();
        String upmUpdateVersion = null;
        try
        {
            updates = pacClient.getUpdates();

            // UPM-1763 special casing UPM to return the update version if there is a newer one
            for (PluginVersion pv : updates)
            {
                if (pv.getPlugin() != null && "com.atlassian.upm.atlassian-universal-plugin-manager-plugin".equals(pv.getPlugin().getPluginKey()))
                {
                    upmUpdateVersion = pv.getVersion();
                    break;
                }
            }
        }
        catch (PacException e)
        {
            pacUnavailable = true;
        }

        Iterable<Plugin> plugins = pluginAccessorAndController.getPlugins(updates);

        return Response
            .ok(representationFactory.createInstalledPluginCollectionRepresentation(localeResolver.getLocale(request),
                                                                                    plugins,
                                                                                    pacUnavailable,
                                                                                    upmUpdateVersion))
            .header("upm-token", tokenManager.getTokenForUser(userManager.getRemoteUsername())).build();
    }

    /**
     * Retrieves the headers for the current plugins resource, including the upm anti-xsrf token. Anyone who has access
     * to UPM has permission to access this resource method.
     * 
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @HEAD
    public Response head(@Context HttpServletRequest request)
    {
        permissionEnforcer.enforceAdmin();
        return Response.ok().header("upm-token", tokenManager.getTokenForUser(userManager.getRemoteUsername())).build();
    }

    /**
     * Given a plugin URL, installs the plugin.
     * 
     * @param installPluginUri an object representing the URI of the plugin to install
     * @return a {@code Response} for the client with details on the request's success or failure
     */
    @POST
    @Consumes(INSTALL_URI_JSON)
    public Response installFromUri(InstallPluginUri installPluginUri, @QueryParam("token") String token)
    {
        // This sucks a bit, it would be nice to include the plugin in the permission check but at this point we
        // don't really know what the plugin will be.
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_INSTALL);
        checkNotInSafeMode();
        validateToken(token, userManager.getRemoteUsername(), APPLICATION_JSON, tokenManager, representationFactory);
        try
        {
            URI uri = new URI(installPluginUri.getPluginUri());

            if (!uri.isAbsolute())
            {
                return Response
                    .status(BAD_REQUEST)
                    .entity(representationFactory.createI18nErrorRepresentation("upm.pluginInstall.error.invalid.relative.uri"))
                    .type(TASK_ERROR_JSON).build();
            }

            InstallFromUriTask task =
                new InstallFromUriTask(uri,
                                       pluginDownloadService,
                                       auditLogger,
                                       userManager.getRemoteUsername(),
                                       pluginInstaller,
                                       selfUpdateController);
            return taskManager.executeAsynchronousTask(task);
        }
        catch (URISyntaxException e)
        {
            return Response
                .status(BAD_REQUEST)
                .entity(representationFactory.createI18nErrorRepresentation("upm.pluginInstall.error.invalid.uri.syntax"))
                .type(TASK_ERROR_JSON).build();
        }
    }

    @POST
    @Consumes({ MULTIPART_FORM_DATA, MULTIPART_MIXED })
    public Response installFromFileSystem(@Context final MultipartHandler multipartHandler,
                                          @Context final HttpServletRequest request,
                                          @DefaultValue("jar") @QueryParam("type") final String type,
                                          @QueryParam("token") String token)
    {
        // This sucks a bit, it would be nice to include the plugin in the permission check but at this point we
        // don't really know what the plugin will be.
        permissionEnforcer.enforcePermission(MANAGE_PLUGIN_INSTALL);
        checkNotInSafeMode();

        validateToken(token, userManager.getRemoteUsername(), TEXT_HTML, tokenManager, representationFactory);
        // asking for error response to have content type TEXT_HTML because the file upload should always return the
        // response in a textarea

        try
        {
            FilePart filePart = multipartHandler.getFilePart(request, "plugin");
            File plugin = copyFilePartToTemporaryFile(filePart, type);
            Response response =
                taskManager.executeAsynchronousTask(new InstallFromFileTask(option(filePart.getName()),
                                                                            plugin,
                                                                            userManager.getRemoteUsername(),
                                                                            pluginInstaller,
                                                                            selfUpdateController));
            String acceptHeader = request.getHeader("Accept");
            if (acceptHeader != null
                && (acceptHeader.contains(TEXT_HTML) || acceptHeader.contains(MEDIA_TYPE_WILDCARD)))
            {
                return Response.fromResponse(response).type(TEXT_HTML).build();
            }
            else
            {
                return response;
            }
        }
        catch (IOException e)
        {
            return Response.serverError().entity(representationFactory.createErrorRepresentation(e.getMessage()))
                .type(ERROR_JSON).build();
        }
    }

    public static class InstallPluginUri
    {
        @JsonProperty
        private String pluginUri;

        @JsonCreator
        public InstallPluginUri(@JsonProperty("pluginUri") String pluginUri)
        {
            this.pluginUri = pluginUri;
        }

        public String getPluginUri()
        {
            return pluginUri;
        }
    }

    private File copyFilePartToTemporaryFile(FilePart filePart, String type) throws IOException
    {
        String fileName =
            filePart.getName() == null ? null : filePart.getName().contains(File.separator) ? filePart.getName()
                .substring(filePart.getName().lastIndexOf(File.separator) + 1) : filePart.getName();

        final File plugin = File.createTempFile("plugin_", fileName == null ? "." + type : "_" + fileName);

        InputStream in = filePart.getInputStream();
        FileOutputStream out = FileUtils.openOutputStream(plugin);
        try
        {
            copy(in, out);
        }
        finally
        {
            closeQuietly(in);
            closeQuietly(out);
        }

        return plugin;
    }

    private void checkNotInSafeMode()
    {
        if (pluginAccessorAndController.isSafeMode())
        {
            throw new WebApplicationException(Response.status(CONFLICT)
                .entity(representationFactory.createI18nErrorRepresentation("upm.pluginInstall.error.safe.mode"))
                .type(TASK_ERROR_JSON).build());
        }
    }
}
