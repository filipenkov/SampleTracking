package com.atlassian.renderer.attachments;

import java.sql.Timestamp;

/**
 * Represents an attachment object that can wrap the Jira & Confluence attatchment objects
 * in a way that will provide useful data to Renderer components.
 */
public class RendererAttachment
{
    private long id;
    private String fileName;
    private String contentType;
    private String author;
    private String comment;
    private String src;
    private String wrapPrefix;
    private String wrapSuffix;
    private Timestamp created;

    public RendererAttachment(long id, String fileName, String contentType, String author, String comment, String src, String wrapPrefix, String wrapSuffix, Timestamp created)
    {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.author = author;
        this.comment = comment;
        this.src = src;
        this.wrapPrefix = wrapPrefix;
        this.wrapSuffix = wrapSuffix;
        this.created = created;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getWrapPrefix()
    {
        return wrapPrefix;
    }

    public void setWrapPrefix(String wrapPrefix)
    {
        this.wrapPrefix = wrapPrefix;
    }

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getContentType()
    {
        return contentType;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public Timestamp getCreated()
    {
        return created;
    }

    public void setCreated(Timestamp created)
    {
        this.created = created;
    }

    public String wrapGeneratedElement(String generatedHtml)
    {
        if(wrapPrefix != null && wrapSuffix != null)
        {
            return wrapPrefix + generatedHtml + wrapSuffix;
        }
        else
        {
            return generatedHtml;
        }
    }
}
