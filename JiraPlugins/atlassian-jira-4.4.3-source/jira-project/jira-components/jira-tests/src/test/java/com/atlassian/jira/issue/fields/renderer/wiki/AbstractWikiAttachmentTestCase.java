package com.atlassian.jira.issue.fields.renderer.wiki;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.renderer.RenderContext;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericValue;
import org.picocontainer.ComponentAdapter;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * Abstract test base that provides a mocked-out Attachment and attachementManager.
 */
public abstract class AbstractWikiAttachmentTestCase extends AbstractWikiTestCase
{
    public static final String TEST_FILE = "testFile.jpg";
    public static final String TEST_FILE_2 = "testFile2.jpg";
    public static final String TEST_NO_FILE = "testThereIsNoFile.jpg";
    public static final String TEST_KEY = "TST-1";
    public static final Long TEST_ATTACHMENT_ID = new Long(1);
    public static final String ATTACHMENT_LINK = "<p><span class=\"nobr\"><a href=\"http://localhost:8080/secure/attachment/" + TEST_ATTACHMENT_ID.toString() + "/" + TEST_ATTACHMENT_ID.toString() + "_" + TEST_FILE + "\" title=\"" + TEST_FILE + " attached to " + TEST_KEY + "\">" + TEST_FILE + "<sup><img class=\"rendericon\" src=\"http://localhost:8080/images/icons/link_attachment_7.gif\" height=\"7\" width=\"7\" align=\"absmiddle\" alt=\"\" border=\"0\"/></sup></a></span></p>";
    public static final String ATTACHMENT_LINK_ERROR = "<p><span class=\"error\">&#91;^" + TEST_NO_FILE + "&#93;</span></p>";

    private RenderContext context;
    private ComponentAdapter oldAttachmentManager;

    @Override
    protected void setUp() throws Exception
    {
        if (is14OrGreater())
        {
            super.setUp();
        }
    }

    @Override
    protected void registerManagers()
    {
        final MockControl ctrlMockAttachment = MockClassControl.createControl(MockAttachment.class);
        final MockAttachment mockAttachment = (MockAttachment) ctrlMockAttachment.getMock();
        mockAttachment.getId();
        ctrlMockAttachment.setDefaultReturnValue(TEST_ATTACHMENT_ID);
        mockAttachment.getFilename();
        ctrlMockAttachment.setDefaultReturnValue(TEST_FILE);
        mockAttachment.getMimetype();
        ctrlMockAttachment.setDefaultReturnValue("jpg");
        mockAttachment.getAuthor();
        ctrlMockAttachment.setDefaultReturnValue("user");
        mockAttachment.getCreated();
        ctrlMockAttachment.setDefaultReturnValue(new Timestamp(System.currentTimeMillis()));
        ctrlMockAttachment.replay();

        final MockControl ctrlMockAttachment2 = MockClassControl.createControl(MockAttachment.class);
        final MockAttachment mockAttachment2 = (MockAttachment) ctrlMockAttachment2.getMock();
        mockAttachment2.getId();
        ctrlMockAttachment2.setDefaultReturnValue(TEST_ATTACHMENT_ID);
        mockAttachment2.getFilename();
        ctrlMockAttachment2.setDefaultReturnValue(TEST_FILE_2);
        mockAttachment2.getMimetype();
        ctrlMockAttachment2.setDefaultReturnValue("jpg");
        mockAttachment2.getAuthor();
        ctrlMockAttachment2.setDefaultReturnValue("user");
        mockAttachment2.getCreated();
        ctrlMockAttachment2.setDefaultReturnValue(new Timestamp(System.currentTimeMillis()));
        ctrlMockAttachment2.replay();

        final ArrayList attachmentList = new ArrayList();
        attachmentList.add(mockAttachment);
        attachmentList.add(mockAttachment2);

        final GenericValue issueOpen = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "TST-1", "summary", "summary",
            "security", new Long(1)));
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getGenericValue", issueOpen);
        mockIssue.expectAndReturn("getKey", TEST_KEY);
        mockIssue.expectAndReturn("getAttachments", attachmentList);

        context = getRenderContext();
        context.addParam(AtlassianWikiRenderer.ISSUE_CONTEXT_KEY, mockIssue.proxy());

        final Mock mockAttachmentManager = new Mock(AttachmentManager.class);
        //        mockAttachmentManager.expectAndReturn("getAttachments", P.ANY_ARGS, attachmentList);
        mockAttachmentManager.expectAndReturn("getAttachment", P.ANY_ARGS, mockAttachment);
        oldAttachmentManager = ManagerFactory.addService(AttachmentManager.class, (AttachmentManager) mockAttachmentManager.proxy());
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (is14OrGreater())
        {
            super.tearDown();
            ManagerFactory.addService(AttachmentManager.class, (AttachmentManager) oldAttachmentManager.getComponentInstance());
        }
    }

    public RenderContext getRenderContextWithIssue()
    {
        return context;
    }

    // This is stupid, but needed. For some reason, seemingly related to
    // the Attachment class taking a GenericValue in its constructor, a mock
    // of the Attachment class seems to fail, only under windows. This class
    // allows the mock to use a no-arg constructor and to bs the genericValue.
    private class MockAttachment extends Attachment
    {
        public MockAttachment()
        {
            super(null, new MockGenericValue("AttachmentMock"));
        }
    }
    /*
            MockControl ctrlMockAttachment = MockClassControl.createControl(RendererAttachment.class);
            RendererAttachment mockAttachment = (RendererAttachment) ctrlMockAttachment.getMock();
            mockAttachment.getId();
            ctrlMockAttachment.setDefaultReturnValue(TEST_ATTACHMENT_ID.longValue());
            mockAttachment.getFileName();
            ctrlMockAttachment.setDefaultReturnValue(TEST_FILE);
            ctrlMockAttachment.replay();

    //        MockControl ctrlMockAttachment2 = MockClassControl.createControl(RendererAttachment.class);
    //        RendererAttachment mockAttachment2 = (RendererAttachment) ctrlMockAttachment2.getMock();
    //        mockAttachment2.getId();
    //        ctrlMockAttachment2.setDefaultReturnValue(TEST_ATTACHMENT_ID.longValue());
    //        mockAttachment2.getFileName();
    //        ctrlMockAttachment2.setDefaultReturnValue(TEST_FILE_2);
    //        ctrlMockAttachment2.replay();

    //        ArrayList attachmentList = new ArrayList();
    //        attachmentList.add(mockAttachment);
    //        attachmentList.add(mockAttachment2);

            Mock mockAttachmentManager = new Mock(RendererAttachmentManager.class);
            mockAttachmentManager.expectAndReturn("getAttachment", P.ANY_ARGS, mockAttachment);
            ManagerFactory.addService(RendererAttachmentManager.class, mockAttachmentManager.proxy());
            */
}
