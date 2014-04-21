/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugins.importer.extensions.ExternalSystemImporterModuleDescriptor;
import com.atlassian.jira.plugins.importer.extensions.ImporterController;
import com.atlassian.jira.plugins.importer.imports.importer.ImportStats;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.plugins.importer.sample.SampleDataImporter;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Path("importer")
public class ImporterResource {
	public static final Logger logger = Logger.getLogger(ImporterResource.class);

	private final JiraAuthenticationContext authenticationContext;
	private final GlobalPermissionManager globalPermissionManager;
	private final PluginAccessor pluginAccessor;

    public ImporterResource(JiraAuthenticationContext authenticationContext, GlobalPermissionManager globalPermissionManager,
                            PluginAccessor pluginAccessor) {
		this.authenticationContext = authenticationContext;
		this.globalPermissionManager = globalPermissionManager;
		this.pluginAccessor = pluginAccessor;
    }

    protected boolean isAdministrator() {
        User currentUser = authenticationContext.getLoggedInUser();
        return (currentUser != null) && globalPermissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    @GET
    @Path("/{externalSystem}/log")
    @Produces({MediaType.TEXT_PLAIN})
    public Response downloadLog(@Context HttpServletRequest req, @PathParam("externalSystem") String externalSystem) {
        if (!isAdministrator()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

        HttpSession session = req.getSession(false);
		final ImporterController controller = getController(externalSystem);
		if (session == null || controller == null) {
			return Response.status(Response.Status.GONE).build();
		}

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);

        JiraDataImporter importer = controller.getImporter();

        return Response.ok(importer.getLog().getImportLog()).cacheControl(cc).build();
    }

	@Nullable
	protected ImporterController getController(String externalSystem) {
		try {
			final ExternalSystemImporterModuleDescriptor moduleDescriptor =
					(ExternalSystemImporterModuleDescriptor) pluginAccessor.getEnabledPluginModule(externalSystem);
			return moduleDescriptor != null ? moduleDescriptor.getModule() : null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@GET
	@Path("/{importer}/configuration")
	@Produces({MediaType.TEXT_PLAIN})
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC_CATCH_EXCEPTION")
	public Response downloadConfiguration(@Context HttpServletRequest req, @PathParam("importer") String importer) {
		if (!isAdministrator()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		HttpSession session = req.getSession(false);
		if (session == null) {
			return Response.status(Response.Status.GONE).build();
		}

		final ImporterController controller = getController(importer);
		if (controller != null) {
			final ImportProcessBean bean = controller.getImportProcessBean(session);
			final String prefix = controller.getId();
			AbstractConfigBean configBean = null;

			if (bean != null) {
				configBean = bean.getConfigBean();
			}

			if (configBean != null) {
				try {
					return Response.ok(getConfigFileText(configBean))
							.header("Content-Disposition",
									String.format("attachment; filename=%s-configuration-%s.txt", prefix,
											DateTimeFormat.forPattern("yyyyMMddHHmm" ).print(new DateTime())))
							.build();
				} catch (Exception e) {
					logger.fatal("Failed to generate configuration", e);
					return Response.serverError().build();
				}
			}
		}

		return  Response.status(Response.Status.NOT_FOUND).build();
	}

	/**
	 * Reads the current configuration as a file formatted as text
	 *
	 * @throws java.io.IOException
	 * @throws org.apache.commons.configuration.ConfigurationException
	 *
	 * @param configBean
	 */
	public String getConfigFileText(AbstractConfigBean configBean) throws IOException, ConfigurationException {
        final Map configCopy = Maps.newHashMap();
		configBean.copyToNewProperties(configCopy);

		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, configCopy);
		return out.toString("UTF-8");
	}

	@GET
	@Path("/{externalSystem}/status")
	@Produces({MediaType.APPLICATION_JSON})
	public Response status(@Context HttpServletRequest req, @PathParam("externalSystem") String externalSystem) {
		if (!isAdministrator()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		HttpSession session = req.getSession(false);
		final ImporterController controller = getController(externalSystem);

		if (session == null || controller == null) {
			return Response.status(Response.Status.GONE).build();
		}

		final JiraDataImporter importer = controller.getImporter();
		final ImportStats stats = importer.getStats();
		final Response.ResponseBuilder responseBuilder = stats != null ? Response.ok(stats) : Response.status(Response.Status.NOT_FOUND);

		CacheControl cc = new CacheControl();
		cc.setNoCache(true);
		return responseBuilder.cacheControl(cc).build();
	}

	@POST
	@Path("/{externalSystem}/abort")
	public Response abort(@Context HttpServletRequest req, @PathParam("externalSystem") String externalSystem) {
		if (!isAdministrator()) {
			return Response.status(Response.Status.FORBIDDEN).build();
		}

		HttpSession session = req.getSession(false);
		final ImporterController controller = getController(externalSystem);
		if (session == null || controller == null) {
			return Response.status(Response.Status.GONE).build();
		}

		CacheControl cc = new CacheControl();
		cc.setNoCache(true);

		JiraDataImporter importer = controller.getImporter();

		importer.abort(authenticationContext.getLoggedInUser().getName());

		return Response.noContent().cacheControl(cc).build();
	}

}
