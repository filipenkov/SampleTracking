package com.atlassian.trackback;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * A simple bean to represent a trackback ping (either being sent or received)
 */
public class Trackback implements Serializable
{
    Long id;
    String url;
    String blogName;
    String excerpt;
    String title;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    /**
     * @return The URL of the ping sender
     */
    public String getUrl()
    {
        return url;
    }

    /** Set the URL of the sender. */
    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getBlogName()
    {
        return blogName;
    }

    public void setBlogName(String blogName)
    {
        this.blogName = blogName;
    }

    public String getExcerpt()
    {
        return excerpt;
    }

    public void setExcerpt(String uncleanedExcerpt)
    {
        this.excerpt = uncleanedExcerpt;

        if (excerpt != null && excerpt.length() > 255)
        {
            excerpt = excerpt.substring(0, 251);
            excerpt += "...";
        }
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Test if the URL and title of the trackbacks are the same
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Trackback)) return false;

        final Trackback trackback = (Trackback) o;

        if (title != null ? !title.equals(trackback.title) : trackback.title != null) return false;
        if (url != null ? !url.equals(trackback.url) : trackback.url != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (url != null ? url.hashCode() : 0);
        result = 29 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        return new ToStringBuilder(this).toString();
    }
}
