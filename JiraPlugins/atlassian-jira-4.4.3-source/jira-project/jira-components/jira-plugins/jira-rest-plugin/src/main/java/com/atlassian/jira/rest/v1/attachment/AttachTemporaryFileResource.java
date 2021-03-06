package com.atlassian.jira.rest.v1.attachment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path ("AttachTemporaryFile")
@Produces (MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class AttachTemporaryFileResource
{
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final JiraAuthenticationContext authContext;
    private final WebAttachmentManager webAttachmentManager;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final XsrfInvocationChecker xsrfChecker;
    private final XsrfTokenGenerator xsrfGenerator;

    public AttachTemporaryFileResource(JiraAuthenticationContext authContext,
            WebAttachmentManager webAttachmentManager, IssueService issueService, ProjectService projectService,
            XsrfInvocationChecker xsrfChecker, XsrfTokenGenerator xsrfGenerator)
    {
        this.authContext = authContext;
        this.webAttachmentManager = webAttachmentManager;
        this.issueService = issueService;
        this.projectService = projectService;
        this.xsrfChecker = xsrfChecker;
        this.xsrfGenerator = xsrfGenerator;
    }

    @POST
    @Consumes (MediaType.WILDCARD)
    public Response addTemporaryAttachment(@QueryParam ("filename") String filename,
            @QueryParam ("projectId") Long projectId, @QueryParam ("issueId") Long issueId,
            @QueryParam("size") Long size, @Context HttpServletRequest request)
    {
        XsrfCheckResult xsrfCheckResult = xsrfChecker.checkWebRequestInvocation(request);
        if (xsrfCheckResult.isRequired() && !xsrfCheckResult.isValid())
        {
            return createTokenError(xsrfGenerator.generateToken(request));
        }

        if (StringUtils.isBlank(filename) || (issueId == null && projectId == null))
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
        }

        if (size == null || size < 0)
        {
            size = (long)request.getContentLength();
            if (size < 0)
            {
                String message = authContext.getI18nHelper().getText("attachfile.error.io.size", filename);
                return createError(Response.Status.BAD_REQUEST, message);
            }
        }

        Project project = null;
        Issue issue = null;

        final User user = authContext.getLoggedInUser();
        if (issueId != null)
        {
            issue = getIssue(user, issueId);
        }
        else
        {
            project = getProject(user, projectId);
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

        try
        {
            final TemporaryAttachment attach = webAttachmentManager.createTemporaryAttachment(inputStream, filename,
                    contentType, size, issue, project);
            return Response.status(Response.Status.CREATED)
                    .entity(new GoodResult(attach.getId(), filename)).cacheControl(never()).build();
        }
        catch (AttachmentException e)
        {
            return createError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private Issue getIssue(User user, Long id)
    {
        IssueService.IssueResult result = issueService.getIssue(user, id);
        if (result.isValid())
        {
            return result.getIssue();
        }
        else
        {
            return throwFourOhFour(result.getErrorCollection());
        }
    }

    private Project getProject(User user, Long id)
    {
        ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, id);
        if (projectResult.isValid())
        {
            return projectResult.getProject();
        }
        else
        {
            return throwFourOhFour(projectResult.getErrorCollection());
        }
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

    @SuppressWarnings ( { "UnusedDeclaration" })
    @XmlRootElement
    public static class GoodResult
    {
        @XmlElement
        private String name;

        @XmlElement
        private String id;

        @SuppressWarnings ( { "UnusedDeclaration", "unused" })
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

    @SuppressWarnings ( { "UnusedDeclaration" })
    @XmlRootElement
    public static class BadResult
    {
        @XmlElement
        private String errorMessage;

        @XmlElement
        private String token;

        @SuppressWarnings ( { "UnusedDeclaration", "unused" })
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
