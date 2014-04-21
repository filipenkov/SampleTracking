/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugins.importer.imports.importer.ImportStats;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.impl.AbstractConfigBean;
import com.atlassian.jira.plugins.importer.web.ImportProcessBean;
import com.atlassian.jira.plugins.importer.web.ImporterControllerFactory;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Path("importer")
public class ImporterResource {
	public static final Logger logger = Logger.getLogger(ImporterResource.class);

	private final JiraAuthenticationContext authenticationContext;
	private final GlobalPermissionManager globalPermissionManager;
	private final ImporterControllerFactory importerControllerFactory;

	public ImporterResource(JiraAuthenticationContext authenticationContext, GlobalPermissionManager globalPermissionManager,
                            ImporterControllerFactory importerControllerFactory) {
		this.authenticationContext = authenticationContext;
		this.globalPermissionManager = globalPermissionManager;
		this.importerControllerFactory = importerControllerFactory;
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
		if (session == null) {
			return Response.status(Response.Status.GONE).build();
		}

        CacheControl cc = new CacheControl();
        cc.setNoCache(true);

        JiraDataImporter importer = importerControllerFactory.getController(externalSystem).getImporter();

        return Response.ok(importer.getLog().getImportLog()).cacheControl(cc).build();
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

		final ImportProcessBean bean = importerControllerFactory.getController(importer).getImportProcessBean(session);
		final String prefix = importerControllerFactory.getController(importer).getId();
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
		if (session == null) {
			return Response.status(Response.Status.GONE).build();
		}

		final JiraDataImporter importer = importerControllerFactory.getController(externalSystem).getImporter();

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
		if (session == null) {
			return Response.status(Response.Status.GONE).build();
		}

		CacheControl cc = new CacheControl();
		cc.setNoCache(true);

		JiraDataImporter importer = importerControllerFactory.getController(externalSystem).getImporter();

		importer.abort(authenticationContext.getLoggedInUser().getName());

		return Response.noContent().cacheControl(cc).build();
	}

}
