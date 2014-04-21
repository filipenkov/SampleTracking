/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.exception;

public class AttachmentNotFoundException extends NotFoundException
{
    private String id;

    public AttachmentNotFoundException(Object attachmentId)
    {
        super(String.valueOf(attachmentId));
        this.id = String.valueOf(attachmentId);
    }

    public String getAttachmentId()
    {
        return this.id;
    }
}
