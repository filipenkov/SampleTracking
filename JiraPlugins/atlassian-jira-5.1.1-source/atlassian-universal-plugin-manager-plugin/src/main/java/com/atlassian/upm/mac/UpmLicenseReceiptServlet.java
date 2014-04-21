package com.atlassian.upm.mac;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.api.util.Pair;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.license.internal.mac.LicenseReceiptHandler;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.rest.UpmUriBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Servlet that receives a form post from my.atlassian.com containing a newly created plugin license.
 * This servlet should be used in environments where a licensing-aware UPM *is* present.
 *
 * If the request is acceptable, the license will be stored for the plugin and the browser will be
 * redirected back to the main UPM tab with that plugin's details expanded.
 *
 * The request is acceptable if:
 * <ul>
 *   <li> It is authenticated for an admin user.
 *   <li> Its referrer header is from the MAC domain.
 *   <li> The new license passes our usual validation rules.
 *   <li> If the plugin already had a license, the new license's properties are not a downgrade
 *     (see LicenseReceiptValidator for details).
 * </ul>
 * 
 * In most cases, if the request is not acceptable, we will redirect to the main UPM page, rather
 * than returning an HTTP error code.  This is because the problem might be due to something wrong
 * with the UPM-MAC integration (or, in the case of the referrer header, the browser configuration)
 * and we don't want the user to see an unfriendly error page.  We only return an HTTP error if the
 * request is grossly malformed enough that it could not have come from MAC.
 */
public class UpmLicenseReceiptServlet extends HttpServlet
{
    private final UpmUriBuilder uriBuilder;
    private final AuditLogService auditLogService;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PluginLicenseRepository licenseRepository;
    private final LicenseReceiptHandler handler;
    
    public UpmLicenseReceiptServlet(
        final PluginAccessorAndController pluginAccessorAndController,
        final PluginLicenseRepository licenseRepository,
        final LicenseReceiptHandler handler,
        final UpmUriBuilder uriBuilder,
        final AuditLogService auditLogService)
    {
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
        this.handler = checkNotNull(handler, "handler");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.auditLogService = checkNotNull(auditLogService, "auditLogService");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        final String pluginKey = request.getPathInfo().substring(request.getPathInfo().lastIndexOf('/') + 1);

        boolean licensePreviouslyDefined = licenseRepository.getPluginLicense(pluginKey).isDefined();

        Pair<Boolean, Option<String>> result = handler.handle(request, response);
        boolean success = result.first();
        Option<String> possibleMessage = result.second();

        //log a message only for successes
        if (success)
        {
            String logMessage = licensePreviouslyDefined ? "upm.auditLog.plugin.license.update" : "upm.auditLog.plugin.license.add";
            String name = pluginAccessorAndController.getPlugin(pluginKey).getName();
            auditLogService.logI18nMessage(logMessage, name, pluginKey);
        }

        //only redirect if a message is to be displayed
        for (String message : possibleMessage)
        {
            redirectToUpmPluginDetails(pluginKey, message, response);
        }
    }

    private void redirectToUpmPluginDetails(String pluginKey, String messageCode, HttpServletResponse response) throws IOException
    {
        response.sendRedirect(uriBuilder.buildUpmTabPluginUri("manage", pluginKey, messageCode).toASCIIString());
    }
}
