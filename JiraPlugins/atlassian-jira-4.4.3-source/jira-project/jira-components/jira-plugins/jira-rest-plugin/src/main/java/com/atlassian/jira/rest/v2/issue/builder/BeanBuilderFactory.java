package com.atlassian.jira.rest.v2.issue.builder;

import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.rest.v2.issue.AttachmentBeanBuilder;

/**
 * Factory interface for getting instances of
 *
 * @since v4.2
 */
public interface BeanBuilderFactory
{
    /**
     * Returns a new instance of an AttachmentBeanBuilder.
     *
     * @return a AttachmentBeanBuilder
     * @param attachment
     */
    AttachmentBeanBuilder attachmentBean(Attachment attachment);
}
