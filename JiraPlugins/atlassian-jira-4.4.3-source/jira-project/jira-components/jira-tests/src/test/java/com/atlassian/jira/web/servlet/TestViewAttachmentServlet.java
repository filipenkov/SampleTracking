/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.servlet;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.IOException;
import java.io.File;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tests the ViewAttachmentServlet class.
 */
public class TestViewAttachmentServlet extends LegacyJiraMockTestCase
{

    Attachment attachment;

    public TestViewAttachmentServlet(final String s)
    {
        super(s);
    }

    public void testHasPermissions() throws GenericEntityException, EntityNotFoundException
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();
        final Long issueId = new Long(1);

        //the issue that will be returned from the attachment
        final MutableIssue issue = new MockIssue();
        issue.setIssueTypeId(issueId.toString());

        //our trusty test user edwin
        final User edwin = UtilsForTests.getTestUser("edwin");

        //setup a permissions Manager, and check that it is called with the correct arguments
        final Mock permissionsManager = new Mock(PermissionManager.class);
        permissionsManager.expectAndReturn("hasPermission", P.args(P.eq(Permissions.BROWSE), P.eq(issue), P.eq(edwin)), Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) permissionsManager.proxy());

        final Mock issueManagerMock = new Mock(IssueManager.class);
        issueManagerMock.expectAndReturn("getIssueObject", P.args(P.eq(issueId)), issue);

        attachment = new Attachment((IssueManager) issueManagerMock.proxy(), UtilsForTests.getTestEntity("FileAttachment", EasyMap.build("issue",
            issueId)), null);

        //as we return 'true' from the permissions manager, we should also get 'true' here
        final boolean hasPermission = viewAttachmentServlet.hasPermissionToViewAttachment(edwin.getName(), attachment);
        assertTrue(hasPermission);

        //check that the permissions manager was actually called & correct args
        permissionsManager.verify();
    }

    public void testRedirectIfNotHasPermissions() throws IOException, ServletException
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

        final Mock servletResponse = new Mock(HttpServletResponse.class);
        final Mock servletRequest = new Mock(HttpServletRequest.class);
        final Mock requestDispatcher = new Mock(RequestDispatcher.class);
        servletRequest.setStrict(true);
        final String contextPath = "someContext";
        servletRequest.setupResult("getContextPath", contextPath);
        servletRequest.setupResult("getMethod", "GET");
        servletRequest.expectAndReturn("getPathInfo", "/attachments/something");
        servletRequest.expectAndReturn("getRemoteUser", "");
        servletRequest.expectAndReturn("getRequestDispatcher", "/secure/views/securitybreach.jsp", requestDispatcher.proxy());
        requestDispatcher.expectVoid("forward", P.args(P.eq(servletRequest.proxy()), P.eq(servletResponse.proxy())));

        viewAttachmentServlet.service((HttpServletRequest) servletRequest.proxy(), (HttpServletResponse) servletResponse.proxy());
        servletResponse.verify();
    }

    public void testResponseHeaders() throws IOException
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();
        final Mock servletRequest = new Mock(HttpServletRequest.class);

        final Mock servletResponse = new Mock(HttpServletResponse.class);
        attachment = new Attachment(null, UtilsForTests.getTestEntity("FileAttachment", EasyMap.build("mimetype", "sampleMimeType", "filesize",
            new Long(11), "filename", "sampleFilename")), null);

        servletRequest.expectAndReturn("getPathInfo", "/attachments/something");
        servletRequest.expectAndReturn("getRemoteUser", "");

        servletResponse.expectVoid("setContentType", "sampleMimeType");
        servletResponse.expectVoid("setContentLength", new Integer(11));
        servletResponse.expectVoid("setHeader", P.args(P.eq("Content-Disposition"), P.eq("inline; filename*=UTF-8''sampleFilename;")));

        viewAttachmentServlet.setResponseHeaders((HttpServletRequest) servletRequest.proxy(), (HttpServletResponse) servletResponse.proxy());

        servletResponse.verify();
    }

    public void testInvalidAttachmentPath() throws IOException
    {
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet();
        final Mock servletRequest = new Mock(HttpServletRequest.class);

        final Mock servletResponse = new Mock(HttpServletResponse.class);

        servletRequest.setStrict(true);
        servletRequest.expectAndReturn("getPathInfo", "abc");
        servletRequest.expectAndReturn("getRemoteUser", "");

        try
        {
            viewAttachmentServlet.setResponseHeaders((HttpServletRequest) servletRequest.proxy(), (HttpServletResponse) servletResponse.proxy());
            fail("InvalidAttachmentPathException should have been thrown.");
        }
        catch(InvalidAttachmentPathException e)
        {
            assertEquals("Invalid attachment path", e.getMessage());
        }

        servletResponse.verify();
    }

    public void testAttachmentNotFoundWithStringId() throws IOException
    {
        ViewAttachmentServlet viewAttachmentServlet = new ViewAttachmentServlet();
        try
        {
            viewAttachmentServlet.getAttachment("/abc/");
            fail("AttachmentNotFoundException should have been thrown.");
        }
        catch(AttachmentNotFoundException e)
        {
            assertTrue(e.getMessage().indexOf("abc") >= 0);
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
}