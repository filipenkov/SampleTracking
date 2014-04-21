package com.atlassian.jira.issue.fields.renderer.wiki.links;

import com.atlassian.jira.issue.fields.renderer.wiki.AbstractWikiAttachmentTestCase;

/**
 * Tests a link generated for an attachment in jira. This also test the portion of the JiraLinkResolver that delegates
 * to attachmentLinks.
 */
public class TestWikiAttachmentLink extends AbstractWikiAttachmentTestCase
{

    public void testIssueAttachmentLink()
    {
        if(is14OrGreater())
        {
            assertEquals(ATTACHMENT_LINK, getRenderer().convertWikiToXHtml(getRenderContextWithIssue(), "[^" + TEST_FILE + "]"));
        }
    }

    public void testIssueAttachmentLinkWithNoAttachment()
    {
        if(is14OrGreater())
        {
            assertEquals(ATTACHMENT_LINK_ERROR, getRenderer().convertWikiToXHtml(getRenderContextWithIssue(), "[^" + TEST_NO_FILE + "]"));
        }
    }

    public void testNoGenericValueError()
    {
        if(is14OrGreater())
        {
            // Do not use the context with an issue in it, we want to generate an exception within the
            // JiraAttachmentLink
            assertEquals(ATTACHMENT_LINK_ERROR, getRenderer().convertWikiToXHtml(getRenderContext(), "[^" + TEST_NO_FILE + "]"));
        }
    }
}
