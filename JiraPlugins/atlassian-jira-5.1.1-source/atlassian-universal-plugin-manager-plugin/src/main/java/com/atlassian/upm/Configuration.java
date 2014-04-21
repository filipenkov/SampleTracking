package com.atlassian.upm;

import java.util.Collection;
import java.util.Date;

import com.google.common.collect.ImmutableList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Domain model representing the configuration.
 * <p/>
 * {@code Configuration} objects should be created using the {@code Configuration.Builder} class.
 */
public final class Configuration
{
    @JsonProperty private final String title;
    @JsonProperty private final String comment;
    @JsonProperty private final Date saveDate;
    @JsonProperty private final Collection<PluginConfiguration> plugins;

    @JsonCreator
    public Configuration(@JsonProperty("title") String title,
        @JsonProperty("comment") String comment,
        @JsonProperty("saveDate") Date saveDate,
        @JsonProperty("plugins") Collection<PluginConfiguration> plugins)
    {
        this.title = title;
        this.comment = comment;
        this.saveDate = saveDate;
        this.plugins = ImmutableList.copyOf(plugins);
    }

    private Configuration(Builder builder)
    {
        this.title = builder.title;
        this.comment = builder.comment;
        this.saveDate = builder.saveDate;
        this.plugins = ImmutableList.copyOf(builder.plugins);
    }

    /**
     * Returns the title of the configuration.
     *
     * @return the title of the configuration
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Returns the optional comment entered by the system administrator when the configuration was saved.
     *
     * @return the optional comment entered by the system administrator when the configuration was saved.
     */
    public String getComment()
    {
        return comment;
    }

    /**
     * Returns the {@code Date} when the configuration was saved.
     *
     * @return the {@code Date} when the configuration was saved
     */
    public Date getSaveDate()
    {
        return saveDate;
    }

    /**
     * Get the list of {@code PluginConfiguration} plugins
     *
     * @return the list of {@code PluginConfiguration} plugins
     */
    public Iterable<PluginConfiguration> getPlugins()
    {
        return plugins;
    }

    /**
     * A builder that facilitates construction of {@code Configuration} objects. The final {@code Configuration} is
     * created by calling the {@link Configuration.Builder#build()} method
     */
    public static final class Builder
    {
        private String title;
        private String comment;
        private Date saveDate = new Date();
        private final Collection<PluginConfiguration> plugins;

        /**
         * Constructor
         *
         * @param plugins the list of {@code PluginConfiguration} plugins
         */
        public Builder(final Iterable<PluginConfiguration> plugins)
        {
            this.plugins = ImmutableList.copyOf(plugins);
        }

        /**
         * Sets the title of the configuration.
         *
         * @param title the title of the configuration
         * @return this builder to allow for further construction
         */
        public Builder title(String title)
        {
            this.title = title;
            return this;
        }

        /**
         * Sets the optional comment entered by the system administrator when the configuration was saved.
         *
         * @param comment the optional comment entered by the system administrator when the configuration was saved.
         * @return this builder to allow for further construction
         */
        public Builder comment(String comment)
        {
            this.comment = comment;
            return this;
        }

        /**
         * Returns the final constructed {@code Configuration}.
         *
         * @return the {@code Configuration}
         */
        public Configuration build()
        {
            return new Configuration(this);
        }
    }
}
