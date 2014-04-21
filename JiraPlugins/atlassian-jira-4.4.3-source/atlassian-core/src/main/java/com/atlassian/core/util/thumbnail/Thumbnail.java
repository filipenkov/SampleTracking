/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.core.util.thumbnail;

// Thumbnail image of image attachments.
public class Thumbnail
{
    private int height;
    private int width;

    private String filename;
    private long attachmentId;

    private MimeType mimeType;

    public enum MimeType
    {
        JPG("image/jpeg"),
        PNG("image/png");

        private final String name;

        private MimeType(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    public Thumbnail(int height, int width, String fileName, long attachmentId)
    {
        this(height, width, fileName, attachmentId, MimeType.JPG);
    }

    public Thumbnail(final int height, final int width, final String filename, final long attachmentId, final MimeType mimeType)
    {
        this.height = height;
        this.width = width;
        this.mimeType = mimeType;
        this.filename = filename;
        this.attachmentId = attachmentId;
    }

    public int getHeight()
    {
        return height;
    }

    public int getWidth()
    {
        return width;
    }

    /**
     * Get the filename of the file this thumbnail represents.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * the id of the attachment for which this is a thumbnail of
     * @return
     */
    public long getAttachmentId()
    {
        return attachmentId;
    }

    public MimeType getMimeType()
    {
        return mimeType;
    }

    public String toString()
    {
        return "Thumbnail " + mimeType + " " + filename + " width:" + width + " height:" + height;
    }
}
