package com.atlassian.jira.dev.backdoor;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.ImportTaskManager;
import com.atlassian.jira.task.ImportTaskManagerImpl;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.index.Contexts;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ServletContextKeys;
import com.atlassian.jira.web.action.setup.DataImportAsyncCommand;
import com.atlassian.jira.web.action.setup.Setup2;
import com.atlassian.jira.web.action.setup.SetupOldUserHelper;
import com.atlassian.jira.web.servletcontext.ServletContextReference;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.lifecycle.LifecycleManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import static com.atlassian.jira.ComponentManager.getComponentInstanceOfType;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Resource for setting up and restoring data in func tests.
 *
 * @since 4.4
 */
@Path ("setup")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class JiraSetupResource
{
    private static final Logger log = Logger.getLogger(JiraSetupResource.class);

    private static ServletContextReference<ImportTaskManager> taskManagerReference =
            new ServletContextReference<ImportTaskManager>(ServletContextKeys.DATA_IMPORT_TASK_MANAGER);

    private final ApplicationProperties applicationProperties;

    private final IndexPathManager indexPathManager;
    private final IssueIndexManager issueIndexManager;
    private final AttachmentPathManager attachmentPathManager;
    private final JiraLicenseUpdaterService licenseService;
    private final JiraSystemRestarter restarter;
    private final UserService userService;
    private final DataImportService dataImportService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final CrowdService crowdService;
    private final LifecycleManager lifecycleManager;

    public JiraSetupResource(ApplicationProperties applicationProperties, IndexPathManager indexPathManager,
                             IssueIndexManager issueIndexManager, AttachmentPathManager attachmentPathManager, UserService userService,
                             VelocityRequestContextFactory velocityRequestContextFactory, CrowdService crowdService,
                             LifecycleManager lifecycleManager)
    {
        this.lifecycleManager = lifecycleManager;
        this.applicationProperties = checkNotNull(applicationProperties);
        this.indexPathManager = indexPathManager;
        this.issueIndexManager = issueIndexManager;
        this.attachmentPathManager = attachmentPathManager;
        this.licenseService = getComponentInstanceOfType(JiraLicenseUpdaterService.class);
        this.restarter = getComponentInstanceOfType(JiraSystemRestarter.class);
        this.userService = userService;
        this.dataImportService = getComponentInstanceOfType(DataImportService.class);
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.crowdService = crowdService;
    }

    @POST
    @Path("initialSetup")
    @AnonymousAllowed
    public Response initialSetup(String requestBody)
    {
        if (isSetup())
        {
            return Response.ok("alreadySetup").build();
        }
        try
        {
            return doSetup(requestBody);
        }
        catch (JSONException e)
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("Only JSON accepted").build();
        }
    }

    @GET
    @AnonymousAllowed
    @Path("resetSetup")
    public Response resetSetup(@Context HttpServletRequest request)
    {
        if (isSetup())
        {
            return doReset(request);
        }
        return Response.ok("not set up :P").build();
    }

    private Response doReset(HttpServletRequest request) {
        File xml = null;
        try
        {
            xml = prepareImportFile("/blank.xml");
            doImport(xml.getAbsolutePath(), true, request);
            applicationProperties.setString(APKeys.JIRA_SETUP, null);
            return Response.ok("ok").build();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally
        {
            FileUtils.deleteQuietly(xml);
        }
    }

    @GET
    @AnonymousAllowed
    @Path("import")
    public Response doImport(@QueryParam("filePath") String filePath, @QueryParam("quickImport") boolean quickImport,
            @Context HttpServletRequest request)
    {
        if (taskManagerReference.get() != null && taskManagerReference.get().getTask() != null
                && !taskManagerReference.get().getTask().isFinished())
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("Import already in progress").build();
        }
        taskManagerReference.set(new ImportTaskManagerImpl());

        final DataImportAsyncCommand importCallable = new DataImportAsyncCommand(johnsonContainer(request.getSession(true).getServletContext()),
                dataImportService, getAdminUser(), validResult(filePath, quickImport), newJohnsonEvent(),
                velocityRequestContextFactory.getJiraVelocityRequestContext(), null);
        submitInSystemContext(importCallable);
        return Response.ok().build();
    }

    @GET
    @AnonymousAllowed
    @Path("importResult")
    public Response importResult()
    {
        if (taskManagerReference.get() != null && taskManagerReference.get().getTask() != null)
        {
            TaskDescriptor<DataImportService.ImportResult> task = taskManagerReference.get().getTask();
            if (task.isFinished())
            {
                taskManagerReference.set(null);
                DataImportService.ImportResult result = getResult(task);
                if (result.isValid())
                {
                    return Response.ok(RestImportResult.ok()).build();
                }
                else
                {
                    return Response.ok(RestImportResult.error(result.getImportError().toString(), result.getErrorCollection())).build();
                }
            }
            else
            {
                return Response.ok(RestImportResult.running()).build();
            }
        }
        else
        {
            return Response.ok(RestImportResult.ok()).build();
        }
    }

    private void submitInSystemContext(DataImportAsyncCommand importCallable) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader(ComponentManager.getInstance().getClass().getClassLoader());
            taskManagerReference.get().submitTask(importCallable, "Backdoor restore ;P");
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(current);
        }
    }


    private File prepareImportFile(String resourcePath) throws IOException {
        final InputStream resourceStream = getImportFileStream(resourcePath);
        final File target = File.createTempFile(Integer.toString(resourcePath.hashCode()), ".xml");
        OutputStream targetStream = null;
        try
        {
            targetStream = new FileOutputStream(target);
            IOUtils.copy(resourceStream, targetStream);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException("Error while trying to restore JIRA data from resource: " + resourcePath, ioe);
        }
        finally
        {
            IOUtils.closeQuietly(resourceStream);
            IOUtils.closeQuietly(targetStream);
        }
        return target;
    }

    private InputStream getImportFileStream(String resourcePath)
    {
        return checkNotNull(getClass().getClassLoader().getResourceAsStream(resourcePath),
                "Import resource with path \"" + resourcePath + "\" not found");
    }

    private DataImportService.ImportResult getResult(TaskDescriptor<DataImportService.ImportResult> task)
    {
        try
        {
            return task.getResult();
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }

    private JohnsonEventContainer johnsonContainer(ServletContext context)
    {
        return JohnsonEventContainer.get(context);
    }

    private Event newJohnsonEvent()
    {
        return new Event(EventType.get("import"), "JIRA is currently being restored from backup",
                EventLevel.get(EventLevel.WARNING));
    }

    private DataImportService.ImportValidationResult validResult(String filePath, boolean quickImport)
    {
        // we fake setup import so that we can import from any file ha(ck)!
        return new DataImportService.ImportValidationResult(new SimpleErrorCollection(),
                new DataImportParams.Builder(filePath).setQuickImport(quickImport).setupImport().build());
    }

    private User getAdminUser()
    {
        return crowdService.getUser("admin");
    }

    private Response doSetup(String requestBody) throws JSONException
    {
        JSONObject json = new JSONObject(requestBody);
        if (!json.has("license"))
        {
            return Response.status(Response.Status.BAD_REQUEST).entity("License must be provided").build();
        }
        applicationProperties.setString(APKeys.JIRA_TITLE, safeGet(json, "instanceName", "Test JIRA"));
        applicationProperties.setString(APKeys.JIRA_BASEURL, safeGet(json, "baseUrl", "http://localhost:8090/jira"));
        applicationProperties.setString(APKeys.JIRA_MODE, safeGet(json, "mode", "public"));
        indexing();
        attachments();
        license(json.getString("license"));
        restore();
        usersAndGroups(json);
        // TODO stuff from SetupComplete
        applicationProperties.setString(APKeys.JIRA_SETUP, "true");
        return Response.ok("success").build();
    }

    private String safeGet(JSONObject json, String key, String defaultVal) throws JSONException
    {
        if (json.has(key))
        {
            return json.getString(key);
        }
        else
        {
            return defaultVal;
        }
    }

    private void indexing()
    {
        if (issueIndexManager.isIndexingEnabled())
        {
            try
            {
                issueIndexManager.deactivate();
            }
            catch (final Exception ignored)
            {
            }
        }
        indexPathManager.setUseDefaultDirectory();
        try
        {
            issueIndexManager.activate(Contexts.percentageLogger(issueIndexManager, log));
        }
        catch (final Exception ignored)
        {
        }
    }

    private void attachments()
    {
        attachmentPathManager.setUseDefaultDirectory();
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
    }


    private void license(String license)
    {
        licenseService.setLicense(licenseService.validate(null, license));
    }

    private void restore()
    {
        restarter.ariseSirJIRA();
    }

    private void usersAndGroups(JSONObject json) throws JSONException
    {
        try
        {
            UserService.CreateUserValidationResult result = userService.validateCreateUserForSetup(null,
                    safeGet(json, "username", "admin"),
                    safeGet(json, "password", "admin"),
                    safeGet(json, "password", "admin"),
                    safeGet(json, "email", "admin@stuff.com"),
                    safeGet(json, "fullName", "Administrator"));
            SetupOldUserHelper.addUser(result);
            SetupOldUserHelper.addGroup(Setup2.DEFAULT_GROUP_ADMINS);
            SetupOldUserHelper.addGroup(Setup2.DEFAULT_GROUP_DEVELOPERS);
            SetupOldUserHelper.addGroup(Setup2.DEFAULT_GROUP_USERS);
            SetupOldUserHelper.addToGroup(Setup2.DEFAULT_GROUP_ADMINS, result.getUsername());
            SetupOldUserHelper.addToGroup(Setup2.DEFAULT_GROUP_DEVELOPERS, result.getUsername());
            SetupOldUserHelper.addToGroup(Setup2.DEFAULT_GROUP_USERS, result.getUsername());
            ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, Setup2.DEFAULT_GROUP_ADMINS);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
        catch (CreateException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isSetup()
    {
        return lifecycleManager.isApplicationSetUp();
    }


    public static class RestImportResult
    {
        static RestImportResult ok()
        {
            return new RestImportResult("OK");
        }

        static RestImportResult running()
        {
            return new RestImportResult("IN PROGRESS");
        }

        static RestImportResult error(String errorType, String... errors)
        {
            return new RestImportResult("FAIL", errorType, errors);
        }

        static RestImportResult error(String errorType, ErrorCollection errors)
        {
            Collection<String> errorMsgs = errors.getErrorMessages();
            return new RestImportResult("FAIL", errorType, errorMsgs.toArray(new String[errorMsgs.size()]));
        }

        @XmlElement
        private String result;

        @XmlElement
        private String errorType;

        @XmlElement
        private String[] errors;

        public RestImportResult(String result)
        {
            this(result, null, null);
        }

        public RestImportResult(String result, String errorType, String[] errors)
        {
            this.result = result;
            this.errorType = errorType;
            this.errors = errors;
        }

    }
}
