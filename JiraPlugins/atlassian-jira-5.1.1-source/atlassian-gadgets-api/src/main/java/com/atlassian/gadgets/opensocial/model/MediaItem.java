package com.atlassian.gadgets.opensocial.model;

import java.net.URI;

import net.jcip.annotations.Immutable;

@Immutable
public final class MediaItem
{
    /**
     * An enumeration of potential media types.
     */
    public enum Type
    {
        AUDIO,
        IMAGE,
        VIDEO
    }
    
    private final String mimeType;
    private final Type type;
    private final URI url;

    public MediaItem(Builder builder)
    {
        if (builder.mimeType == null)
        {
            throw new NullPointerException("builder.mimeType must not be null");
        }
        if (builder.type == null)
        {
            throw new NullPointerException("builder.type must not be null");
        }
        if (builder.url == null)
        {
            throw new NullPointerException("builder.url must not be null");
        }
        this.mimeType = builder.mimeType;
        this.type = builder.type;
        this.url = builder.url;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public Type getType()
    {
        return type;
    }

    public URI getUrl()
    {
        return url;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        MediaItem mediaItem = (MediaItem) o;

        if (mimeType != null ? !mimeType.equals(mediaItem.mimeType) : mediaItem.mimeType != null)
        {
            return false;
        }
        if (url != null ? !url.equals(mediaItem.url) : mediaItem.url != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = mimeType != null ? mimeType.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    /**
     * A builder that facilitates construction of {@code MediaItem} objects. The final {@code MediaItem}
     * is created by calling the {@link MediaItem.Builder#build()} method
     */
    public static class Builder
    {
        private String mimeType;
        private Type type;
        private URI url;

        public Builder(URI url)
        {
            this.url = url;
        }

        /**
         * Set the mime type of the {@code MediaItem} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param mimeType the mime type to use for the {@code MediaItem}
         * @return this builder to allow for further construction
         */
        public Builder mimeType(String mimeType)
        {
            this.mimeType = mimeType;
            return this;
        }

        /**
         * Set the type of the {@code MediaItem} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param type the type to use for the {@code MediaItem}
         * @return this builder to allow for further construction
         */
        public Builder type(Type type)
        {
            this.type = type;
            return this;
        }

        /**
         * Set the url of the {@code MediaItem} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param url the mime type to use for the {@code MediaItem}
         * @return this builder to allow for further construction
         */
        public Builder url(URI url)
        {
            this.url = url;
            return this;
        }

        /**
         * Returns the final constructed {@code MediaItem}
         *
         * @return the {@code MediaItem}
         */
        public MediaItem build()
        {
            return new MediaItem(this);
        }

    }
}
