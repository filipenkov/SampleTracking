/*
 * Copyright (C) 2012 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.google.common.collect.Iterables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class TestAbstractMessageHandlerAttachments extends AbstractTestMessageHandler
{
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }

    @Test
    public void testAttachmentsPresentAndChangeLogged() throws Exception
    {
        final AbstractMessageHandler handler = createMessageHandlerForAttachmentTests(true, true);
        Message message = HandlerTestUtil.createMessageFromFile("GmailHtmlBinaryattachment.msg");
        Issue issue = Mockito.mock(Issue.class);
        final Collection<ChangeItemBean> cibs = handler.createAttachmentsForMessage(message, issue, context);

        Assert.assertEquals(1, cibs.size());
        Assert.assertEquals("binary.bin", Iterables.getOnlyElement(cibs).getToString());

        Assert.assertEquals(1, attachmentManager.attachmentLog.size());
        Assert.assertEquals("binary.bin", attachmentManager.attachmentLog.get(0).filename);
        Assert.assertEquals("application/octet-stream", attachmentManager.attachmentLog.get(0).contentType);

        Assert.assertEquals(Collections.emptyList(), commentsAdded);
    }

    @Test
    public void AttachmentWithoutAttachmentContentDisposition() throws Exception {
        final AbstractMessageHandler handler = createMessageHandlerForAttachmentTests(true, true);
        Message message = HandlerTestUtil.createMessageFromFile("MessageWithAttachment.msg");
        Issue issue = Mockito.mock(Issue.class);
        final Collection<ChangeItemBean> cibs = handler.createAttachmentsForMessage(message, issue, context);

        Assert.assertEquals(2, cibs.size());
        Assert.assertEquals("email_banner_650.gif", Iterables.get(cibs, 0).getToString());
        Assert.assertEquals("message.wav", Iterables.get(cibs, 1).getToString());

        Assert.assertEquals(2, attachmentManager.attachmentLog.size());
        Assert.assertEquals("email_banner_650.gif", attachmentManager.attachmentLog.get(0).filename);
        Assert.assertEquals("image/gif", attachmentManager.attachmentLog.get(0).contentType);

        Assert.assertEquals("message.wav", attachmentManager.attachmentLog.get(1).filename);
        Assert.assertEquals("audio/x-wav", attachmentManager.attachmentLog.get(1).contentType);

        Assert.assertEquals(Collections.emptyList(), commentsAdded);
    }

    @Test
    public void testAttachmentsSkippedOnGlobalDisable() throws Exception
    {
        final AbstractMessageHandler handler = createMessageHandlerForAttachmentTests(false, true);
        Message message = HandlerTestUtil.createMessageFromFile("GmailHtmlBinaryattachment.msg");
        Issue issue = Mockito.mock(Issue.class, Mockito.RETURNS_DEEP_STUBS);
        final Collection<ChangeItemBean> cibs = handler.createAttachmentsForMessage(message, issue, context);

        Assert.assertEquals(Collections.emptyList(), cibs);
        Assert.assertEquals(Collections.emptyList(), attachmentManager.attachmentLog);

        Assert.assertEquals(1, commentsAdded.size());
        final String comment = Iterables.getOnlyElement(commentsAdded);
        Assert.assertTrue(comment.contains("JIRA Attachments disabled"));
        Assert.assertTrue(comment.contains("binary.bin"));
        Assert.assertFalse(comment.contains("<unknown file name>"));
    }

    @Test
    public void testAttachmentsSkippedOnUserNotAllowed() throws Exception
    {
        final AbstractMessageHandler handler = createMessageHandlerForAttachmentTests(true, false);
        Message message = HandlerTestUtil.createMessageFromFile("GmailHtmlBinaryattachment.msg");
        Issue issue = Mockito.mock(Issue.class, Mockito.RETURNS_DEEP_STUBS);
        final Collection<ChangeItemBean> cibs = handler.createAttachmentsForMessage(message, issue, context);

        Assert.assertEquals(Collections.emptyList(), cibs);
        Assert.assertEquals(Collections.emptyList(), attachmentManager.attachmentLog);

        Assert.assertEquals(1, commentsAdded.size());
        final String comment = Iterables.getOnlyElement(commentsAdded);
        Assert.assertTrue(comment.contains("does not have permission to create"));
        Assert.assertTrue(comment.contains("binary.bin"));
        Assert.assertFalse(comment.contains("<unknown file name>"));
    }

    private AbstractMessageHandler createMessageHandlerForAttachmentTests(boolean globalEnabled, boolean userAllowed)
    {
        final ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        final PermissionManager permissionManager = Mockito.mock(PermissionManager.class);

        Mockito.when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)).thenReturn(globalEnabled);
        Mockito.when(permissionManager.hasPermission(Mockito.eq(Permissions.CREATE_ATTACHMENT), Mockito.<Issue>any(), Mockito.<User>any()))
                .thenReturn(userAllowed);

        final AbstractMessageHandler messageHandler = new AbstractMessageHandler(userManager, applicationProperties, jiraApplicationContext, mailLoggingManager,
                messageUserProcessor, permissionManager)
        {
            @Override
            public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException
            {
                return false;
            }

            @Override
            protected boolean attachPlainTextParts(Part part) throws MessagingException, IOException
            {
                return true;
            }

            @Override
            protected boolean attachHtmlParts(Part part) throws MessagingException, IOException
            {
                return true;
            }
        };

        messageHandler.reporteruserName = TESTUSER_USERNAME;

        return messageHandler;
    }


}
