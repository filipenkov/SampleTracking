package com.atlassian.jira.issue.managers;

import com.atlassian.core.util.WebRequestUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.MockAttachmentPathManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestDefaultAttachmentManager extends ListeningTestCase
{
    final GenericValue attachment1 = new MockGenericValue("FileAttachment", EasyMap.build("id", new Long(100), "issue", new Long(1), "filename", "C:\temp"));
    final GenericValue attachment2 = new MockGenericValue("FileAttachment", EasyMap.build("id", new Long(101), "issue", new Long(1), "filename", "C:\temp"));
    private GenericValue issue = new MockGenericValue("Issue", EasyMap.build("id", new Long(1)))
    {
        public List getRelatedOrderBy(String relationName, List orderBy)
        {
            return EasyList.build(attachment1, attachment2);
        }
    };

    @Test
    public void testIsAttachmentsEnabledAndPathSetHappyPath()
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(new IsEqual(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)), Boolean.TRUE);

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, (ApplicationProperties) mockApplicationProperties.proxy(), new MockAttachmentPathManager(), null, null);
        assertTrue(attachmentManager.attachmentsEnabled());
        mockApplicationProperties.verify();
    }

    @Test
    public void testIsAttachmentsEnabledAndPathSetAttachmentsDisabled()
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(new IsEqual(APKeys.JIRA_OPTION_ALLOWATTACHMENTS)), Boolean.FALSE);

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, (ApplicationProperties) mockApplicationProperties.proxy(), new MockAttachmentPathManager(), null, null);
        assertFalse(attachmentManager.attachmentsEnabled());
        mockApplicationProperties.verify();
    }

    @Test
    public void testIsScreenshotAppletEnabledPropertyTrue()
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(new IsEqual(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED)), Boolean.TRUE);

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, (ApplicationProperties) mockApplicationProperties.proxy(), null, null, null);

        assertTrue(attachmentManager.isScreenshotAppletEnabled());
        mockApplicationProperties.verify();
    }

    @Test
    public void testIsScreenshotAppletEnabledPropertyFalse()
    {
        Mock mockApplicationProperties = new Mock(ApplicationProperties.class);
        mockApplicationProperties.expectAndReturn("getOption", P.args(new IsEqual(APKeys.JIRA_SCREENSHOTAPPLET_ENABLED)), Boolean.FALSE);

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, (ApplicationProperties) mockApplicationProperties.proxy(), null, null, null);

        assertFalse(attachmentManager.isScreenshotAppletEnabled());
        mockApplicationProperties.verify();
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSOSX()
    {
        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.MACOSX;
            }

            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return false;
            }
        };

        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }
    @Test
    public void testIsScreenshotAppleSupportedByOSLinuxDisabled()
    {
        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.LINUX;
            }
            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return false;
            }
        };

        assertFalse(attachmentManager.isScreenshotAppletSupportedByOS());
    }
    @Test
    public void testIsScreenshotAppleSupportedByOSLinuxEnabled()
    {
        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.LINUX;
            }
            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return true;
            }
        };

        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppleSupportedByOSWindows()
    {
        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.WINDOWS;
            }
            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return false;
            }
        };

        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testIsScreenshotAppletSupportedByOSUnsupportedOSLinuxDisabled()
    {

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.OTHER;
            }
            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return false;
            }
        };

        assertFalse(attachmentManager.isScreenshotAppletSupportedByOS());
    }
    @Test
    public void testIsScreenshotAppletSupportedByOSUnsupportedOSLinuxEnabled()
    {

        DefaultAttachmentManager attachmentManager = new DefaultAttachmentManager(null, null, null, null, null, null, null)
        {
            int getUsersOS()
            {
                return WebRequestUtils.OTHER;
            }
            @Override
            protected boolean isScreenshotAppletEnabledForLinux()
            {
                return true;
            }
        };

        assertTrue(attachmentManager.isScreenshotAppletSupportedByOS());
    }

    @Test
    public void testGetAttachment() throws GenericEntityException
    {
        Mock delegatorMock = new Mock(OfBizDelegator.class);
        Map findByPrimKeyArgs = EasyMap.build("id", new Long(101));
        delegatorMock.expectAndReturn("findByPrimaryKey", P.args(P.eq("FileAttachment"), P.eq(findByPrimKeyArgs)), attachment2);
        OfBizDelegator delegator = (OfBizDelegator) delegatorMock.proxy();

        DefaultAttachmentManager dam = new DefaultAttachmentManager(null, delegator, null, null, null, null, null);
        Attachment attachment = dam.getAttachment(new Long(101));
        assertNotNull(attachment);
        assertEquals(new Long(101), attachment.getId());
        delegatorMock.verify();
    }

    @Test
    public void testGetAttachments() throws GenericEntityException
    {
        DefaultAttachmentManager dam = new DefaultAttachmentManager(null, null, null, null, null, null, null);
        List attachments = dam.getAttachments(issue);
        assertNotNull(attachments);
        assertFalse(attachments.isEmpty());
        assertEquals(2, attachments.size());
        assertTrue(attachments.contains(new Attachment(null, attachment1, null)));
        assertTrue(attachments.contains(new Attachment(null, attachment2, null)));
    }

}
