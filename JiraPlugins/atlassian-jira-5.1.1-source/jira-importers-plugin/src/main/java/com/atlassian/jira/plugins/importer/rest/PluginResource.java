/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.rest;

import com.atlassian.jira.plugins.importer.XmlUtil;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginInformation;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@Path("plugin")
@Produces({MediaType.APPLICATION_JSON})
public class PluginResource {

	private static final Logger log = Logger.getLogger(PluginResource.class);

    private static final long HOUR = 3600 * 1000;

	public static final long DAY = 24 * HOUR;

	private static final String PAC_FIND_COMPATIBLE_PATH = "https://plugins.atlassian.com/server/1.0/pluginversion/find/compatiblekey/jira/%s/%s";
	public static final String PLUGIN_KEY = "com.atlassian.jira.plugins.jira-importers-plugin";

	private final PluginAccessor pluginAccessor;
	private final BuildUtilsInfo buildUtilsInfo;

	private final AtomicBoolean upgradeAvailable = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Date lastCheck;

	public PluginResource(PluginAccessor pluginAccessor, BuildUtilsInfo buildUtilsInfo) {
		this.pluginAccessor = pluginAccessor;
		this.buildUtilsInfo = buildUtilsInfo;
	}

	@GET
	@Path("/upgradeAvailable")
	public Response isUpgradeAvailable() {
        if (running.compareAndSet(false, true)) {
            try {
                final Date now = new Date();
                if (lastCheck == null || now.getTime() > (lastCheck.getTime() + DAY)) {
                    lastCheck = now;
                    
                    Plugin plugin = pluginAccessor.getPlugin(PLUGIN_KEY);
                    if (plugin != null) {
                        PluginInformation pi = plugin.getPluginInformation();
                        if (pi != null) {
                            String version = pi.getVersion();
                            try {
								String pacVersion = getVersionFromPac();
								if (StringUtils.isNotEmpty(pacVersion)) {
                                	upgradeAvailable.set(!version.equals(pacVersion));
								}
                            } catch (Exception e) {
                                log.warn("Unabled to check current release on PAC", e);
                            }
                        }
                    }
                }
            } finally {
                running.set(false);
            }
        }
		return Response.ok(upgradeAvailable.get()).build();
	}

	@Nullable
	public String getVersionFromPac() throws IOException, SAXException, JDOMException, JaxenException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(
				String.format(PAC_FIND_COMPATIBLE_PATH, buildUtilsInfo.getCurrentBuildNumber(), PLUGIN_KEY));

		client.executeMethod(method);

		return getVersionFromXml(method.getResponseBodyAsStream());
	}

	@Nullable
	public String getVersionFromXml(InputStream input)
			throws IOException, SAXException, JDOMException, JaxenException {
		final Element root = XmlUtil.getSAXBuilder().build(input).getRootElement();
		final Object version = new JDOMXPath("/items/item/version").selectSingleNode(root);
		if (version instanceof Element) {
			return ((Element) version).getText();
		}
		return null;
	}
}
