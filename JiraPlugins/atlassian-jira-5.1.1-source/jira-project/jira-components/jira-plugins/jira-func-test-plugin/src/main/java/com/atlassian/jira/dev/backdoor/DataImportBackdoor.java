package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Use this backdoor to import data. It's even faster than the UI!
 * <p/>
 *
 * @since v5.0
 */
@Path ("dataImport")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class DataImportBackdoor
{
    private ApplicationProperties applicationProperties;
    private DataImportService dataImportService;
    private JiraHome jiraHome;
    private JiraAuthenticationContext authContext;

    public DataImportBackdoor(UserUtil userUtil, JiraHome jiraHome, ApplicationProperties applicationProperties, JiraAuthenticationContext authContext)
    {
        this.applicationProperties = applicationProperties;
        this.jiraHome = jiraHome;
        this.authContext = authContext;
    }

    @POST
    public Response importData(DataImportBean importBean)
    {
        String licenseString = importBean.licenseString;
        boolean useDefaultPaths = importBean.useDefaultPaths;
        boolean quickImport = importBean.quickImport;
        DataImportParams params = new DataImportParams.Builder(importBean.filePath)
                .setLicenseString(licenseString)
                .setUseDefaultPaths(useDefaultPaths)
                .setQuickImport(quickImport)
                .build();

        DataImportService.ImportValidationResult result = getDataImportService().validateImport(authContext.getLoggedInUser(), params);
        DataImportService.ImportResult importResult = getDataImportService().doImport(authContext.getLoggedInUser(), result, TaskProgressSink.NULL_SINK);
        if (!importResult.isValid())
        {
            // Something went wrong. Die!
            throw new IllegalStateException("Restore failed!");
        }

        if (StringUtils.isNotBlank(importBean.baseUrl))
        {
            applicationProperties.setString(APKeys.JIRA_BASEURL, importBean.baseUrl);
        }

        return Response.ok(null).build();
    }

    @GET
    @Path("jiraHomePath")
    public Response getJiraHomePath()
    {
        String jiraHomePath = jiraHome.getHomePath();
        return Response.ok(jiraHomePath).build();
    }

    public DataImportService getDataImportService()
    {
        if (dataImportService == null)
        {
            dataImportService = ComponentAccessor.getComponent(DataImportService.class);
        }
        return dataImportService;
    }
}
