package com.atlassian.jira.rest.v1.attachment;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import webwork.action.Action;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertStatus;
import static javax.ws.rs.core.Response.Status;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestAttachTemporaryFileResource
{
    private IMocksControl control;
    private MockUser user;
    private JiraAuthenticationContext authCtx;
    private WebAttachmentManager webAttachmentManager;
    private IssueService issueService;
    private ProjectService projectService;
    private HttpServletRequest request;
    private ServletInputStream stream;
    private XsrfInvocationChecker xsrfChecker;
    private XsrfTokenGenerator xsrfGenerator;

    @Before
    public void createDeps()
    {
        control = createControl();
        user = new MockUser("Brenden");
        authCtx = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        webAttachmentManager = control.createMock(WebAttachmentManager.class);
        issueService = control.createMock(IssueService.class);
        projectService = control.createMock(ProjectService.class);
        request = control.createMock(HttpServletRequest.class);
        stream = control.createMock(ServletInputStream.class);
        xsrfChecker = new OkChecker();
        xsrfGenerator = control.createMock(XsrfTokenGenerator.class);
    }

    @Test
    public void testBadXsrf()
    {
        XsrfInvocationChecker checker = control.createMock(XsrfInvocationChecker.class);

        expect(checker.checkWebRequestInvocation(request)).andReturn(new Result(true, false));
        String token = "Toke3423424243n";
        expect(xsrfGenerator.generateToken(request)).andReturn(token);

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, checker, xsrfGenerator);

        control.replay();

        Response actualResponse = resource.addTemporaryAttachment(null, null, 178L, null, request);
        assertResponseCacheNever(actualResponse);
        final AttachTemporaryFileResource.BadResult expectedResponse =
                new AttachTemporaryFileResource.BadResult(NoopI18nHelper.makeTranslation("attachfile.xsrf.try.again"), token);
        assertResponseBody(expectedResponse, actualResponse);
        assertStatus(Status.INTERNAL_SERVER_ERROR, actualResponse);

        control.verify();
    }

    @Test
    public void testBadArguments()
    {
        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        control.replay();

        assertBadRequest(resource.addTemporaryAttachment(null, null, 178L, null, null));
        assertBadRequest(resource.addTemporaryAttachment("    ", null, 178L, null, null));
        assertBadRequest(resource.addTemporaryAttachment("filename", null, null, null, null));

        control.verify();
    }

    @Test
    public void testBadArgumentsNoSize() throws IOException, AttachmentException
    {
        assertBaseSize(-1L);
        assertBaseSize(null);
    }

    private void assertBaseSize(final Long size)
    {
        control.reset();

        String filename = "fileNameTestBadNoSize";
        expect(request.getContentLength()).andReturn(-1);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        Response response = resource.addTemporaryAttachment(filename, 17L, null, size, request);

        String expectedMessage = NoopI18nHelper.makeTranslation("attachfile.error.io.size", filename);
        assertBadRequest(Status.BAD_REQUEST, expectedMessage, response);

        control.verify();
    }

    @Test
    public void testGoodCreatingIssue() throws IOException, AttachmentException
    {
        MockProject project = new MockProject(178L);
        String contentType = "application/octet-stream";
        long size = 1L;
        TemporaryAttachment ta = new TemporaryAttachment(273738L, 3838383L, new File("something"), "name", contentType);

        XsrfInvocationChecker checker = control.createMock(XsrfInvocationChecker.class);
        expect(checker.checkWebRequestInvocation(request)).andReturn(new Result(false, false));

        expect(projectService.getProjectById(user, project.getId()))
                .andReturn(new ProjectService.GetProjectResult(new SimpleErrorCollection(), project));

        expect(request.getInputStream()).andReturn(stream);
        expect(request.getContentType()).andReturn(null);

        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                size, null, project))
                .andReturn(ta);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, checker, xsrfGenerator);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), null, size, request);
        assertResponseBody(new AttachTemporaryFileResource.GoodResult(ta.getId(), ta.getFilename()), response);
        assertResponseCacheNever(response);
        assertStatus(Status.CREATED, response);

        control.verify();
    }

    @Test
    public void testBadCreatingIssueNoProject() throws IOException, AttachmentException
    {
        long pid = 178L;

        String errorMsg = "badddd";
        expect(projectService.getProjectById(user, pid))
                .andReturn(new ProjectService.GetProjectResult(errors(errorMsg)));


        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        try
        {
            resource.addTemporaryAttachment("ignored", pid, null, 1L, request);
            fail("Expected error");
        }
        catch (WebApplicationException e)
        {
            assertResponseBody(new AttachTemporaryFileResource.BadResult(errorMsg), e.getResponse());
            assertResponseCacheNever(e.getResponse());
            assertStatus(Status.NOT_FOUND, e.getResponse());
        }

        control.verify();
    }

    @Test
    public void testGoodpdatingIssueGood() throws IOException, AttachmentException
    {
        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        int contentLength = 272728;
        String contentType = "text/plain";
        TemporaryAttachment ta = new TemporaryAttachment(273738L, 3838383L, new File("something"), "name", contentType);

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));

        expect(request.getInputStream()).andReturn(stream);
        expect(request.getContentLength()).andReturn(contentLength);
        expect(request.getContentType()).andReturn(contentType);

        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                contentLength, issue, null))
                .andReturn(ta);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), issue.getId(), null, request);
        assertResponseBody(new AttachTemporaryFileResource.GoodResult(ta.getId(), ta.getFilename()), response);
        assertResponseCacheNever(response);
        assertStatus(Status.CREATED, response);

        control.verify();
    }

    @Test
    public void testBadUpdatingIssueNoIssue() throws IOException, AttachmentException
    {
        long pid = 178L;
        long id = 575;

        String errorMsg = "badddd";
        expect(issueService.getIssue(user, id))
                .andReturn(new IssueService.IssueResult(null, errors(errorMsg)));

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        try
        {
            resource.addTemporaryAttachment("ignored", pid, id, 1L, request);
            fail("Expected error");
        }
        catch (WebApplicationException e)
        {
            assertResponseBody(new AttachTemporaryFileResource.BadResult(errorMsg), e.getResponse());
            assertResponseCacheNever(e.getResponse());
            assertStatus(Status.NOT_FOUND, e.getResponse());
        }

        control.verify();
    }

    @Test
    public void testErrorWhileGettingStream() throws IOException, AttachmentException
    {
        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        String contentType = "text/plain";
        String errorMessage = "message";
        String fileName = "name";

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));

        expect(request.getContentType()).andReturn(contentType);
        expect(request.getInputStream()).andThrow(new IOException(errorMessage));

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        Response response = resource.addTemporaryAttachment(fileName, project.getId(), issue.getId(), 1L, request);
        String expectedMessage = NoopI18nHelper.makeTranslation("attachfile.error.io.error", fileName, errorMessage);
        assertResponseBody(new AttachTemporaryFileResource.BadResult(expectedMessage), response);
        assertResponseCacheNever(response);
        assertStatus(Status.INTERNAL_SERVER_ERROR, response);

        control.verify();
    }

    @Test
    public void testAttachmentError() throws IOException, AttachmentException
    {
        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        long contentLength = 272728;
        String contentType = "text/plain";
        String message = "errorMessae";
        TemporaryAttachment ta = new TemporaryAttachment(273738L, 3838383L, new File("something"), "name", contentType);

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));

        expect(request.getInputStream()).andReturn(stream);
        expect(request.getContentType()).andReturn(contentType);

        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                contentLength, issue, null))
                .andThrow(new AttachmentException(message));

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfChecker, xsrfGenerator);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), issue.getId(), contentLength, request);
        assertBadRequest(Status.INTERNAL_SERVER_ERROR, message, response);

        control.verify();
    }

    private void assertBadRequest(Status expectedStatus, String expectedError, Response response)
    {
        assertResponseCacheNever(response);
        assertResponseBody(new AttachTemporaryFileResource.BadResult(expectedError), response);
        assertStatus(expectedStatus, response);
    }

    private void assertBadRequest(Response response)
    {
        assertResponseCacheNever(response);
        assertResponseBody(null, response);
        assertStatus(Status.BAD_REQUEST, response);
    }

    public SimpleErrorCollection errors(String... errors)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        for (String error : errors)
        {
            collection.addErrorMessage(error);
        }
        return collection;
    }

    public SimpleErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }

    private static class Result implements XsrfCheckResult
    {
        private final boolean required;
        private final boolean valid;

        private Result(boolean required, boolean valid)
        {
            this.required = required;
            this.valid = valid;
        }

        public boolean isRequired()
        {
            return required;
        }

        public boolean isValid()
        {
            return valid;
        }

        public boolean isGeneratedForAuthenticatedUser()
        {
            return false;
        }
    }

    public static class OkChecker implements XsrfInvocationChecker
    {
        public XsrfCheckResult checkActionInvocation(Action action, Map<String, ?> parameters)
        {
            return new Result(true, true);
        }

        public XsrfCheckResult checkWebRequestInvocation(HttpServletRequest httpServletRequest)
        {
            return new Result(true, true);
        }
    }
}
