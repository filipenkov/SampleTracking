package com.atlassian.jira.issue.fields.renderer.wiki.embedded;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.fields.renderer.wiki.AbstractWikiAttachmentTestCase;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.picocontainer.ComponentAdapter;

import java.util.ArrayList;

/**
 * Tests the renderer that handles embedding attached images within the markup.
 */
public class TestWikiEmbeddedResourceRenderer extends AbstractWikiAttachmentTestCase
{
    private static final String EMBEDDED_IMAGE_LINK = "<p><img src=\"http://localhost:8080/secure/attachment/" + TEST_ATTACHMENT_ID.toString() + "/" + TEST_ATTACHMENT_ID.toString() + "_" + TEST_FILE + "\" align=\"absmiddle\" border=\"0\" /></p>";
    private static final String EMBEDDED_THUMBNAIL_ANCHOR_PREFIX = "<p><a id=\"1_thumb\" href=\"http://localhost:8080/secure/attachment/" + TEST_ATTACHMENT_ID.toString() + "/" + TEST_ATTACHMENT_ID.toString() + "_" + TEST_FILE + "\" title=\"" + TEST_FILE + "\">";
    private static final String EMBEDDED_THUMBNAIL_ANCHOR_SUFFIX = "</a></p>";
    private static final String EMBEDDED_IMAGE_THUMBNAIL_LINK = EMBEDDED_THUMBNAIL_ANCHOR_PREFIX + "<img src=\"http://localhost:8080/secure/thumbnail/" + TEST_ATTACHMENT_ID.toString() + "/" + TEST_ATTACHMENT_ID.toString() + "_" + TEST_FILE + "\" align=\"absmiddle\" border=\"0\" />" + EMBEDDED_THUMBNAIL_ANCHOR_SUFFIX;
    private static final String EMBEDDED_EXTERNAL_IMAGE_LINK = "<p><img src=\"http://www.google.com.au/intl/en_au/images/logo.gif\" align=\"absmiddle\" border=\"0\" /></p>";
    private static final String EMBEDDED_LINK_FAILURE = "<p><span class=\"error\">No usable issue stored in the context, unable to resolve filename &#39;" + TEST_FILE + "&#39;</span></p>";
    private static final String EMBEDDED_LINK_FAILURE_NO_FILE = "<p><span class=\"error\">Unable to render embedded object: File (" + TEST_NO_FILE + ") not found.</span></p>";

    private ComponentAdapter oldThumbnailManager;

    @Override
    protected void tearDown() throws Exception
    {
        if (is14OrGreater())
        {
            ManagerFactory.addService(ThumbnailManager.class, (ThumbnailManager) oldThumbnailManager.getComponentInstance());
            super.tearDown();
        }
    }

    @Override
    protected void registerManagers()
    {
        super.registerManagers();
        final MockControl ctrlMockThumbnail = MockClassControl.createControl(Thumbnail.class);
        final Thumbnail mockThumbnail = (Thumbnail) ctrlMockThumbnail.getMock();
        mockThumbnail.getAttachmentId();
        ctrlMockThumbnail.setDefaultReturnValue(TEST_ATTACHMENT_ID.longValue());
        mockThumbnail.getFilename();
        ctrlMockThumbnail.setDefaultReturnValue(TEST_FILE);
        ctrlMockThumbnail.replay();

        final ArrayList thumbnailList = new ArrayList();
        thumbnailList.add(mockThumbnail);

        final Mock mockThumbnailManager = new Mock(ThumbnailManager.class);
        mockThumbnailManager.expectAndReturn("getThumbnails", P.ANY_ARGS, thumbnailList);
        mockThumbnailManager.expectAndReturn("isThumbnailable", P.ANY_ARGS, Boolean.TRUE);
        oldThumbnailManager = ManagerFactory.addService(ThumbnailManager.class, (ThumbnailManager) mockThumbnailManager.proxy());
    }

    public void testEmbeddedImageAttachment()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_IMAGE_LINK, getRenderer().convertWikiToXHtml(getRenderContextWithIssue(), "!" + TEST_FILE + "!"));
        }
    }

    public void testEmbeddedImageExternal()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_EXTERNAL_IMAGE_LINK, getRenderer().convertWikiToXHtml(getRenderContextWithIssue(),
                "!http://www.google.com.au/intl/en_au/images/logo.gif!"));
        }
    }

    public void testExternalLinkSuccessWithNoIssue()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_EXTERNAL_IMAGE_LINK, getRenderer().convertWikiToXHtml(getRenderContext(),
                "!http://www.google.com.au/intl/en_au/images/logo.gif!"));
        }
    }

    public void testInternalLinkFailureWithNoIssue()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_LINK_FAILURE, getRenderer().convertWikiToXHtml(getRenderContext(), "!" + TEST_FILE + "!"));
        }
    }

    public void testInternalLinkFailureWithNoAttachment()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_LINK_FAILURE_NO_FILE, getRenderer().convertWikiToXHtml(getRenderContextWithIssue(), "!" + TEST_NO_FILE + "!"));
        }
    }

    public void testEmbeddedImageThumbnailAttachment()
    {
        if (is14OrGreater())
        {
            assertEquals(EMBEDDED_IMAGE_THUMBNAIL_LINK,
                getRenderer().convertWikiToXHtml(getRenderContextWithIssue(), "!" + TEST_FILE + "|thumbnail!"));
        }
    }
}
