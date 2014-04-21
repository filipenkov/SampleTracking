/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.seraph.auth.AuthenticationContextImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the ViewAttachmentServlet class.
 */
public class TestViewAttachmentServlet extends LegacyJiraMockTestCase
{
    @Mock
    PermissionManager permissionsManager;

    @Mock
    IssueManager issueManagerMock;

    @Mock
    HttpServletResponse servletResponse;

    @Mock
    HttpServletRequest servletRequest;

    @Mock
    RequestDispatcher requestDispatcher;

    Attachment attachment;

    public TestViewAttachmentServlet(final String s)
    {
        super(s);
    }

    public void testHasPermissions() throws Exception
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();
        final Long issueId = (long) 1;

        //the issue that will be returned from the attachment
        final MutableIssue issue = new MockIssue();
        issue.setIssueTypeId(issueId.toString());

        //our trusty test user edwin
        final User edwin = createMockUser("edwin");

        //setup a permissions Manager, and check that it is called with the correct arguments
        when(permissionsManager.hasPermission(Permissions.BROWSE, issue, edwin)).thenReturn(Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, permissionsManager);

        when(issueManagerMock.getIssueObject(issueId)).thenReturn(issue);

        attachment = new Attachment(issueManagerMock, UtilsForTests.getTestEntity("FileAttachment", EasyMap.build("issue",
            issueId)), null);

        //as we return 'true' from the permissions manager, we should also get 'true' here
        final boolean hasPermission = viewAttachmentServlet.hasPermissionToViewAttachment(edwin.getName(), attachment);
        assertTrue(hasPermission);
    }

    public void testRedirectIfNotHasPermissions() throws Exception
    {
        //set it up so the user has no permissions
        final ViewAttachmentServlet viewAttachmentServlet = new ViewAttachmentServlet()
        {
            @Override
            protected boolean hasPermissionToViewAttachment(final String userString, final Attachment attachment)
            {
                return false;
            }

            @Override
            protected Attachment getAttachment(final String query)
            {
                return null;
            }
        };

        final String contextPath = "someContext";
        when(servletRequest.getContextPath()).thenReturn(contextPath);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getPathInfo()).thenReturn("/attachments/something");
        when(servletRequest.getRemoteUser()).thenReturn("");
        when(servletRequest.getRequestDispatcher("/secure/views/securitybreach.jsp")).thenReturn(requestDispatcher);

        new AuthenticationContextImpl().setUser(new MockUser("edwin"));
        viewAttachmentServlet.service(servletRequest, servletResponse);
        verify(requestDispatcher).forward(servletRequest, servletResponse);
    }

    public void testResponseHeaders() throws Exception
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();

        attachment = new Attachment(null, UtilsForTests.getTestEntity("FileAttachment", EasyMap.build("mimetype", "sampleMimeType", "filesize",
            new Long(11), "filename", "sampleFilename")), null);

        when(servletRequest.getPathInfo()).thenReturn("/attachments/something");
        when(servletRequest.getRemoteUser()).thenReturn("");

        viewAttachmentServlet.setResponseHeaders(servletRequest, servletResponse);
        verify(servletResponse).setContentType("sampleMimeType");
        verify(servletResponse).setContentLength(11);
        verify(servletResponse).setHeader("Content-Disposition", "inline; filename*=UTF-8''sampleFilename;");
    }

    public void testCacheControlHeadersShouldEncourageLongTermCaching() throws Exception
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();

        attachment = new Attachment(null, UtilsForTests.getTestEntity("FileAttachment", EasyMap.build("mimetype", "sampleMimeType", "filesize",
            new Long(11), "filename", "sampleFilename")), null);

        when(servletRequest.getPathInfo()).thenReturn("/attachments/something");
        when(servletRequest.getRemoteUser()).thenReturn("");

        viewAttachmentServlet.setResponseHeaders(servletRequest, servletResponse);
        verify(servletResponse).setHeader("Cache-control", "private, max-age=31536000");
        verify(servletResponse).setDateHeader("Expires", -1L); // forbid HTTP1.0 shared proxies from caching attachments
    }

    public void testInvalidAttachmentPath() throws Exception
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();

        when(servletRequest.getPathInfo()).thenReturn("abc");
        when(servletRequest.getRemoteUser()).thenReturn("");

        try
        {
            viewAttachmentServlet.setResponseHeaders(servletRequest, servletResponse);
            fail("InvalidAttachmentPathException should have been thrown.");
        }
        catch(InvalidAttachmentPathException e)
        {
            assertEquals("Invalid attachment path", e.getMessage());
        }
    }

    public void testAttachmentNotFoundWithStringId() throws Exception
    {
        ViewAttachmentServlet viewAttachmentServlet = new ViewAttachmentServlet();
        try
        {
            viewAttachmentServlet.getAttachment("/abc/");
            fail("AttachmentNotFoundException should have been thrown.");
        }
        catch(AttachmentNotFoundException e)
        {
            assertTrue(e.getMessage().contains("abc"));
        }
    }

    class PublicViewAttachmentServlet extends ViewAttachmentServlet
    {
        @Override
        public Attachment getAttachment(final String query)
        {
            return attachment;
        }

        // stop it getting too far down and dirty.  Its the content disposition we want not the bytes
        // at least in this test anyways!
        @Override
        MimeSniffingKit getMimeSniffingKit()
        {
            return new MimeSniffingKit(getApplicationProperties())
            {
                @Override
                File getFileForAttachment(final Attachment attachment)
                {
                    return null;
                }
            };
        }
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }
}
