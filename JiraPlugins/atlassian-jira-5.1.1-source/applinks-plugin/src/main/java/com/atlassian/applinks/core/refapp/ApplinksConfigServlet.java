package com.atlassian.applinks.core.refapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang.StringUtils;

/**
 * This servlet is available in the RefApp and allows the user to configure
 * RefApp-specific AppLinks properties (such as the application's instance
 * name).
 *
 * @since   3.0
 */
public class ApplinksConfigServlet extends HttpServlet
{
    private final TemplateRenderer templateRenderer;
    private final InternalHostApplication internalHostApplication;
    private final PluginSettings pluginSettings;
    private final WebSudoManager webSudoManager;

    public ApplinksConfigServlet(final TemplateRenderer templateRenderer, final InternalHostApplication internalHostApplication,
                                 final PluginSettingsFactory pluginSettingsFactory, final WebSudoManager webSudoManager)
    {
        this.templateRenderer = templateRenderer;
        this.internalHostApplication = internalHostApplication;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.webSudoManager = webSudoManager;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("serverName", internalHostApplication.getName());

        resp.setContentType("text/html");
        templateRenderer.render("templates/host/refapp/config.vm", params, resp.getWriter());
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
    {
        final String serverName = req.getParameter("serverName");
        if (!StringUtils.isEmpty(serverName))
        {
            pluginSettings.put(RefAppInternalHostApplication.INSTANCE_NAME_KEY, serverName);
        }
        resp.sendRedirect("./applinksconfig");
    }
}
