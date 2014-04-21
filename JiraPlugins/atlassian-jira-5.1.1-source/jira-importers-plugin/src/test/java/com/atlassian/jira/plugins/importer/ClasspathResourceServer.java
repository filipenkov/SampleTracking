/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer;

import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.StatisticsHandler;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ClasspathResourceServer extends ExternalResource {
	public final StatisticsHandler statistics = new StatisticsHandler();

	private final Server jettyServer;

	public ClasspathResourceServer(final Map<String, String> resourceMapping) {
		jettyServer = new Server();
		jettyServer.addConnector(new SocketConnector());
		jettyServer.addHandler(statistics);
		jettyServer.addHandler(new AbstractHandler() {
			@Override
			public void handle(String s, HttpServletRequest
					httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException {
				final String resource = resourceMapping.get(httpServletRequest.getPathInfo());
				final InputStream is = resource != null ? getClass().getResourceAsStream(resource) : null;
				if (is == null) {
					httpServletResponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
					return;
				}
				try {
					ServletOutputStream os = httpServletResponse.getOutputStream();
					IOUtils.copy(is, os);
					httpServletResponse.flushBuffer();
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		});


	}

	public URI getBaseUri() {
		try {
			return new URI("http://localhost:" + jettyServer.getConnectors()[0].getLocalPort());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void before() throws Throwable {
		jettyServer.start();
	}

	@Override
	protected void after() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
