package com.atlassian.jira.collector.plugin.rest;

import com.atlassian.core.util.FileSize;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.collector.plugin.components.Collector;
import com.atlassian.jira.collector.plugin.components.CollectorService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.util.AttachmentUtils;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.web.action.issue.TemporaryAttachmentsMonitor;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import webwork.config.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path ("tempattachment")
@Produces (MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class TemporaryAttachmentsResource
{
    private static final Logger log = Logger.getLogger(TemporaryAttachmentsResource.class);

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final long UNKNOWN_ISSUE_ID = -1L;

    private final JiraAuthenticationContext authContext;
    private final XsrfInvocationChecker xsrfChecker;
    private final XsrfTokenGenerator xsrfGenerator;
    private final CollectorService collectorService;
    private final JiraAuthenticationContext authenticationContext;
    private final TemplateRenderer templateRenderer;

    @Context
    private HttpServletRequest request;

    public TemporaryAttachmentsResource(JiraAuthenticationContext authContext, XsrfInvocationChecker xsrfChecker, XsrfTokenGenerator xsrfGenerator,
            final CollectorService collectorService, final JiraAuthenticationContext authenticationContext, final TemplateRenderer templateRenderer)
    {
        this.authContext = authContext;
        this.xsrfChecker = xsrfChecker;
        this.xsrfGenerator = xsrfGenerator;
        this.collectorService = collectorService;
        this.authenticationContext = authenticationContext;
        this.templateRenderer = templateRenderer;
    }

    @POST
    @Path ("{collectorId}")
    @Consumes (MediaType.WILDCARD)
    public Response addTemporaryAttachment(@QueryParam ("filename") String filename,
            @PathParam ("collectorId") String collectorId, @QueryParam ("size") Long size)
    {
        final XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(request);
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            return createTokenError(xsrfGenerator.generateToken(request));
        }

        final ServiceOutcome<Collector> result = collectorService.getCollector(collectorId);
        final Collector collector = result.getReturnedValue();
        if (StringUtils.isBlank(filename) || (collector == null || !collector.isEnabled()))
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
        }

        if (size == null || size < 0)
        {
            size = (long) request.getContentLength();
            if (size < 0)
            {
                String message = authContext.getI18nHelper().getText("attachfile.error.io.size", filename);
                return createError(Response.Status.BAD_REQUEST, message);
            }
        }

        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType))
        {
            contentType = DEFAULT_CONTENT_TYPE;
        }

        InputStream inputStream;
        try
        {
            inputStream = request.getInputStream();
        }
        catch (IOException e)
        {
            String message = authContext.getI18nHelper().getText("attachfile.error.io.error", filename, e.getMessage());
            return createError(Response.Status.INTERNAL_SERVER_ERROR, message);
        }

        final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = TemporaryAttachmentsMonitorLocator.getAttachmentsMonitor(request, collector.getId());
        if (temporaryAttachmentsMonitor == null)
        {
            return Response.serverError().cacheControl(CacheControl.NO_CACHE).build();
        }


        try
        {
            final TemporaryAttachment attach = createTemporaryAttachment(filename, contentType, inputStream);
            temporaryAttachmentsMonitor.add(attach);
            return Response.status(Response.Status.CREATED).entity(new GoodResult(attach.getId(), filename)).cacheControl(never()).build();
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @POST
    @Path ("multipart/{collectorId}")
    @Consumes ({ MediaType.MULTIPART_FORM_DATA })
    @Produces ({ MediaType.TEXT_HTML })
    public Response attachTemporaryFileViaForm(@PathParam ("collectorId") String collectorId, @MultipartFormParam ("screenshot") Collection<FilePart> fileParts)
    {
        //check if the collector exists!
        final ServiceOutcome<Collector> outcome = collectorService.getCollector(collectorId);
        if (outcome.getReturnedValue() == null || !outcome.getReturnedValue().isEnabled())
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(CacheControl.NO_CACHE).build();
        }

        final Collector collector = outcome.getReturnedValue();
        final I18nHelper i18n = authenticationContext.getI18nHelper();

        //check if we can get an attachments monitor
        final TemporaryAttachmentsMonitor temporaryAttachmentsMonitor = TemporaryAttachmentsMonitorLocator.getAttachmentsMonitor(request, collector.getId());
        if (temporaryAttachmentsMonitor == null)
        {
            return Response.serverError().cacheControl(CacheControl.NO_CACHE).build();
        }

        //do some basic validation
        final Map<String, Object> context = JiraVelocityUtils.createVelocityParams(authenticationContext);
        if (fileParts.size() != 1)
        {
            context.put("errorMsg", i18n.getText("collector.plugin.template.error.no.attachments"));
            return Response.status(Response.Status.BAD_REQUEST).entity(renderTemplate("templates/rest/tempfilejson.vm", context)).cacheControl(CacheControl.NO_CACHE).build();
        }


        //ensure the file is not larger than a certain size!
        final FilePart filePart = fileParts.iterator().next();
        try
        {
            int maxAttachmentSize = new Integer(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));
            if (filePart.getInputStream().available() > maxAttachmentSize)
            {
                context.put("errorMsg", i18n.getText("collector.plugin.template.error.attachment.large", FileSize.format(maxAttachmentSize)));
                return Response.status(Response.Status.BAD_REQUEST).entity(renderTemplate("templates/rest/tempfilejson.vm", context)).cacheControl(CacheControl.NO_CACHE).build();
            }

            //finally create the temporary attachment!
            final TemporaryAttachment temporaryAttachment = createTemporaryAttachment(filePart.getName(), filePart.getContentType(), filePart.getInputStream());
            temporaryAttachmentsMonitor.add(temporaryAttachment);
            context.put("temporaryAttachment", temporaryAttachment);
            return Response.ok(renderTemplate("templates/rest/tempfilejson.vm", context)).cacheControl(CacheControl.NO_CACHE).build();
        }
        catch (IOException e)
        {
            return Response.serverError().cacheControl(CacheControl.NO_CACHE).build();
        }
    }

    private TemporaryAttachment createTemporaryAttachment(final String fileName, final String contentType, final InputStream inputStream)
    {
        final File tmpDir = AttachmentUtils.getTemporaryAttachmentDirectory();
        long uniqueId;
        File tempAttachmentFile;
        do
        {
            //if the file already exists, choose a new UUID to avoid clashes!
            uniqueId = getUUID();
            tempAttachmentFile = new File(tmpDir, uniqueId + "_" + fileName);
        }
        while (tempAttachmentFile.exists());

        FileOutputStream output = null;
        try
        {
            output = new FileOutputStream(tempAttachmentFile);
            IOUtils.copy(inputStream, output);
            output.close();
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(output);
            log.error("Error creating temporary attachment", e);
            return null;
        }

        return new TemporaryAttachment(uniqueId, UNKNOWN_ISSUE_ID, tempAttachmentFile, fileName, contentType);
    }

    private long getUUID()
    {
        return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }


    private static Response createError(Response.Status status, com.atlassian.jira.util.ErrorCollection collection)
    {
        String message = getFirstElement(collection.getErrorMessages());
        if (message == null)
        {
            message = getFirstElement(collection.getErrors().values());
        }
        return createError(status, message);
    }

    private static Response createError(Response.Status status, String message)
    {
        return Response.status(status).cacheControl(never()).entity(new BadResult(message)).build();
    }

    private Response createTokenError(String newToken)
    {
        String message = authContext.getI18nHelper().getText("attachfile.xsrf.try.again");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .cacheControl(never()).entity(new BadResult(message, newToken)).build();
    }

    private <T> T throwFourOhFour(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new WebApplicationException(createError(Response.Status.NOT_FOUND, errorCollection));
    }

    private static <T> T getFirstElement(Collection<? extends T> values)
    {
        if (!values.isEmpty())
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    private String renderTemplate(String templatePath, Map<String, Object> context)
    {
        final StringWriter out = new StringWriter();
        try
        {
            templateRenderer.render(templatePath, context, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return out.toString();
    }


    @SuppressWarnings ({ "UnusedDeclaration" })
    @XmlRootElement
    public static class GoodResult
    {
        @XmlElement
        private String name;

        @XmlElement
        private String id;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private GoodResult() {}

        GoodResult(long id, String name)
        {
            this.id = String.valueOf(id);
            this.name = name;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            GoodResult that = (GoodResult) o;

            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @XmlRootElement
    public static class BadResult
    {
        @XmlElement
        private String errorMessage;

        @XmlElement
        private String token;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private BadResult() {}

        BadResult(String msg)
        {
            this(msg, null);
        }

        BadResult(String msg, String token)
        {
            this.errorMessage = msg;
            this.token = token;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            BadResult badResult = (BadResult) o;

            if (errorMessage != null ? !errorMessage.equals(badResult.errorMessage) : badResult.errorMessage != null)
            { return false; }
            if (token != null ? !token.equals(badResult.token) : badResult.token != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = errorMessage != null ? errorMessage.hashCode() : 0;
            result = 31 * result + (token != null ? token.hashCode() : 0);
            return result;
        }
    }
}
