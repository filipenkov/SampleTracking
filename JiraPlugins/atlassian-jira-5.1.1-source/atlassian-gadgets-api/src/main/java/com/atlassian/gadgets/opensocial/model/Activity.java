package com.atlassian.gadgets.opensocial.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Represents an activity, such as the creation of a wiki page
 *
 *  @since 2.0
 */
@Immutable
public final class Activity
{
    private final AppId appId;
    private final String body;
    private final String externalId;
    private final ActivityId id;
    private final Date updated;
    private final List<MediaItem> mediaItems;
    private final Long postedTime;
    private final Float priority;
    private final String streamFaviconUrl;
    private final String streamSourceUrl;
    private final String streamTitle;
    private final String streamUrl;
    private final String title;
    private final String url;
    private final PersonId userId;

    private Activity(Activity.Builder builder)
    {
        this.appId = builder.appId;
        this.body = builder.body;
        this.externalId = builder.externalId;
        this.id = builder.id;
        this.updated = builder.updated;
        if(builder.mediaItems != null)
        {
            this.mediaItems = builder.mediaItems;
        }
        else
        {
            this.mediaItems = Collections.emptyList();
        }
        this.postedTime = builder.postedTime;
        this.priority = builder.priority;
        this.streamFaviconUrl = builder.streamFaviconUrl;
        this.streamSourceUrl = builder.streamSourceUrl;
        this.streamTitle = builder.streamTitle;
        this.streamUrl = builder.streamUrl;
        this.title = builder.title;
        this.url = builder.url;
        this.userId = builder.userId;
    }

    /**
     * Get the app id this activity is associated with.
     * @return the app id
     */
    public AppId getAppId()
    {
        return appId;
    }

    /**
     * Get a body string specifying an optional expanded version of an activity.
     * @return the body
     */
    public String getBody()
    {
        return body;
    }

    /**
     * Get an optional string ID generated by the posting application.
     * @return the external id
     */
    public String getExternalId()
    {
        return externalId;
    }

    /**
     * Get the activity id that is permanently associated with this activity.
     * @return the activity id
     */
    public ActivityId getId()
    {
        return id;
    }

    /**
     * Get the last updated date of the activity.
     * @return the updated date
     */
    public Date getUpdated()
    {
        if (updated != null)
        {
            return new Date(updated.getTime());
        }
        return null;
    }

    /**
     * Get the media items associated with the activity.
     * @return an unmodifiable list of the media items associated with the activity
     */
    public List<MediaItem> getMediaItems()
    {
        return Collections.unmodifiableList(mediaItems);
    }

    /**
     * Get the time at which this activity took place in milliseconds since the epoch.
     * @return the posted time
     */
    public Long getPostedTime()
    {
        return postedTime;
    }

    /**
     * Get the priority, a number between 0 and 1 representing the relative priority of this activity in relation to
     * other activities from the same source.
     * @return the priority
     */
    public Float getPriority()
    {
        return priority;
    }

    /**
     * Get a string specifying the URL for the stream's favicon.
     * @return the stream favicon URL
     */
    public String getStreamFaviconUrl()
    {
        return streamFaviconUrl;
    }

    /**
     * Get a string specifying the stream's source URL.
     * @return the stream source URL
     */
    public String getStreamSourceUrl()
    {
        return streamSourceUrl;
    }

    /**
     * Get a string specifing the title of the stream.
     * @return the stream title
     */
    public String getStreamTitle()
    {
        return streamTitle;
    }

    /**
     * Get a string specifying the stream's URL.
     * @return the stream URL
     */
    public String getStreamUrl()
    {
        return streamUrl;
    }

    /**
     * Get a string specifying the primary text of an activity.
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Get a string specifying the URL that represents this activity.
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Get the id of the user who this activity is for.
     * @return the user id
     */
    public PersonId getUserId()
    {
        return userId;
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

        Activity activity = (Activity) o;

        if (appId != null ? !appId.equals(activity.appId) : activity.appId != null)
        {
            return false;
        }
        if (body != null ? !body.equals(activity.body) : activity.body != null)
        {
            return false;
        }
        if (externalId != null ? !externalId.equals(activity.externalId) : activity.externalId != null)
        {
            return false;
        }
        if (id != null ? !id.equals(activity.id) : activity.id != null)
        {
            return false;
        }
        if (!mediaItems.equals(activity.mediaItems))
        {
            return false;
        }
        if (postedTime != null ? !postedTime.equals(activity.postedTime) : activity.postedTime != null)
        {
            return false;
        }
        if (priority != null ? !priority.equals(activity.priority) : activity.priority != null)
        {
            return false;
        }
        if (streamFaviconUrl != null ? !streamFaviconUrl.equals(activity.streamFaviconUrl) : activity.streamFaviconUrl != null)
        {
            return false;
        }
        if (streamSourceUrl != null ? !streamSourceUrl.equals(activity.streamSourceUrl) : activity.streamSourceUrl != null)
        {
            return false;
        }
        if (streamTitle != null ? !streamTitle.equals(activity.streamTitle) : activity.streamTitle != null)
        {
            return false;
        }
        if (streamUrl != null ? !streamUrl.equals(activity.streamUrl) : activity.streamUrl != null)
        {
            return false;
        }
        if (title != null ? !title.equals(activity.title) : activity.title != null)
        {
            return false;
        }
        if (updated != null ? !updated.equals(activity.updated) : activity.updated != null)
        {
            return false;
        }
        if (url != null ? !url.equals(activity.url) : activity.url != null)
        {
            return false;
        }
        if (userId != null ? !userId.equals(activity.userId) : activity.userId != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = appId != null ? appId.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (updated != null ? updated.hashCode() : 0);
        result = 31 * result + mediaItems.hashCode();
        result = 31 * result + (postedTime != null ? postedTime.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (streamFaviconUrl != null ? streamFaviconUrl.hashCode() : 0);
        result = 31 * result + (streamSourceUrl != null ? streamSourceUrl.hashCode() : 0);
        result = 31 * result + (streamTitle != null ? streamTitle.hashCode() : 0);
        result = 31 * result + (streamUrl != null ? streamUrl.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Activity{" +
                "appId=" + appId +
                ", body='" + body + '\'' +
                ", externalId='" + externalId + '\'' +
                ", id=" + id +
                ", updated=" + updated +
                ", mediaItems=" + mediaItems +
                ", postedTime=" + postedTime +
                ", priority=" + priority +
                ", streamFaviconUrl='" + streamFaviconUrl + '\'' +
                ", streamSourceUrl='" + streamSourceUrl + '\'' +
                ", streamTitle='" + streamTitle + '\'' +
                ", streamUrl='" + streamUrl + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", userId=" + userId +
                '}';
    }

    /**
     * A builder that facilitates construction of {@code Activity} objects. The final {@code Activity}
     * is created by calling the {@link Activity.Builder#build()} method
     */
    public static class Builder
    {
        private AppId appId;
        private String body;
        private String externalId;
        private ActivityId id;
        private Date updated;
        private List<MediaItem> mediaItems;
        private Long postedTime;
        private Float priority;
        private String streamFaviconUrl;
        private String streamSourceUrl;
        private String streamTitle;
        private String streamUrl;
        private String title;
        private String url;
        private PersonId userId;

        /**
         * Create a new {@code Activity.Builder} with the same field values as the passed in {@code Activity}
         * @param activity the activity whose fields should be copied
         */
        public Builder(Activity activity)
        {
            this.appId(activity.appId);
            this.body(activity.body);
            this.externalId(externalId);
            this.id(activity.id);
            this.updated(activity.updated);
            this.mediaItems(activity.mediaItems);
            this.postedTime(activity.postedTime);
            this.priority(activity.priority);
            this.streamFaviconUrl(activity.streamFaviconUrl);
            this.streamSourceUrl(activity.streamSourceUrl);
            this.streamTitle(activity.streamTitle);
            this.streamUrl(activity.streamUrl);
            this.title(activity.title);
            this.url(activity.url);
            this.userId(activity.userId);
        }

        /**
         * Create a new {@code Activity.Builder} that can be used to create an {@code Activity}
         * @param title the title of the {@code Activity} under construction
         */
        public Builder(String title)
        {
            if (title == null)
            {
                throw new NullPointerException("title parameter must not be null when creating a new Activity.Builder");
            }
            this.title = title;
        }

        /**
         * Set the application (gadget type) id of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param appId the application id to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder appId(AppId appId)
        {
            this.appId = appId;
            return this;
        }

        /**
         * Set the body of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param body the body to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder body(String body)
        {
            this.body = body;
            return this;
        }

        /**
         * Set the externalId of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param externalId the externalId to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder externalId(String externalId)
        {
            this.externalId = externalId;
            return this;
        }

        /**
         * Set the id of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param id the id to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder id(ActivityId id)
        {
            this.id = id;
            return this;
        }

        /**
         * Set the updated date of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param updated the updated date to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder updated(Date updated)
        {
            this.updated = updated;
            return this;
        }

        /**
         * Set the mediaItems associated with the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param mediaItems the media items to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder mediaItems(List<MediaItem> mediaItems)
        {
            this.mediaItems = mediaItems;
            return this;
        }

        /**
         * Set the postedTime of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param postedTime the postedTime to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder postedTime(Long postedTime)
        {
            this.postedTime = postedTime;
            return this;
        }

        /**
         * Set the priority of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param priority the priority to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder priority(Float priority)
        {
            this.priority = priority;
            return this;
        }

        /**
         * Set the streamFaviconUrl of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param streamFaviconUrl the streamFaviconUrl to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder streamFaviconUrl(String streamFaviconUrl)
        {
            this.streamFaviconUrl = streamFaviconUrl;
            return this;
        }

        /**
         * Set the streamSourceUrl of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param streamSourceUrl the streamSourceUrl to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder streamSourceUrl(String streamSourceUrl)
        {
            this.streamSourceUrl = streamSourceUrl;
            return this;
        }

        /**
         * Set the streamTitle of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param streamTitle the streamTitle to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder streamTitle(String streamTitle)
        {
            this.streamTitle = streamTitle;
            return this;
        }

        /**
         * Set the streamUrl of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param streamUrl the streamUrl to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder streamUrl(String streamUrl)
        {
            this.streamUrl = streamUrl;
            return this;
        }

        /**
         * Set the title of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param title the title to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        private Builder title(String title)
        {
            this.title = title;
            return this;
        }

        /**
         * Set the url of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param url the url to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder url(String url)
        {
            this.url = url;
            return this;
        }

        /**
         * Set the user id of the {@code Activity} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param userId the user id to use for the {@code Activity}
         * @return this builder to allow for further construction
         */
        public Builder userId(PersonId userId)
        {
            this.userId = userId;
            return this;
        }

        /**
         * Returns the final constructed {@code Activity}
         *
         * @return the {@code Activity}
         */
        public Activity build()
        {
            return new Activity(this);
        }
    }

    /**
     * Activity fields that can be loaded when retrieving an activity
     */
    public static enum Field
    {
        APP_ID("appId"),
        BODY("body"),
        BODY_ID("bodyId"),
        EXTERNAL_ID("externalId"),
        ID("id"),
        LAST_UPDATED("updated"),
        MEDIA_ITEMS("mediaItems"),
        POSTED_TIME("postedTime"),
        PRIORITY("priority"),
        STREAM_FAVICON_URL("streamFaviconUrl"),
        STREAM_SOURCE_URL("streamSourceUrl"),
        STREAM_TITLE("streamTitle"),
        STREAM_URL("streamUrl"),
        TEMPLATE_PARAMS("templateParams"),
        TITLE("title"),
        TITLE_ID("titleId"),
        URL("url"),
        USER_ID("userId");

        /**
         * The json field that the instance represents.
         */
        private final String jsonString;

        /**
         * create a field based on the json element.
         *
         * @param jsonString the name of the element
         */
        private Field(String jsonString)
        {
            this.jsonString = jsonString;
        }

        /**
         * emit the field as a json element.
         *
         * @return the field name
         */
        @Override
        public String toString()
        {
            return jsonString;
        }
    }
}