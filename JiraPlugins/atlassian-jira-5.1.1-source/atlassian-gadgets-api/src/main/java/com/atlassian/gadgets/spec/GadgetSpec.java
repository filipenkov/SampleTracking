package com.atlassian.gadgets.spec;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.gadgets.view.ViewType;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.jcip.annotations.Immutable;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Represents a gadget specification
 */
@Immutable
public final class GadgetSpec
{
    private final URI specUri;
    private final Iterable<UserPrefSpec> userPrefs;
    private final boolean scrolling;
    private final int height;
    private final int width;
    private final String title;
    private final URI titleUrl;
    private final URI thumbnail;
    private final String author;
    private final String authorEmail;
    private final String description;
    private final String directoryTitle;
    private final Map<String,Feature> features;
    private final Iterable<String> unsupportedFeatureNames;
    private final Set<String> viewsNames;

    private GadgetSpec(Builder builder)
    {
        this.specUri = builder.specUri;

        List<UserPrefSpec> userPrefsCopy = new LinkedList<UserPrefSpec>();
        for (UserPrefSpec userPrefSpec : builder.userPrefs)
        {
            userPrefsCopy.add(userPrefSpec);
        }
        this.userPrefs = Collections.unmodifiableList(userPrefsCopy);

        this.scrolling = builder.scrolling;
        this.height = builder.height;
        this.width = builder.width;
        this.title = builder.title;
        this.titleUrl = builder.titleUrl;
        this.thumbnail = builder.thumbnail;
        this.author = builder.author;
        this.authorEmail = builder.authorEmail;
        this.description = builder.description;
        this.directoryTitle = builder.directoryTitle;
        this.features = Collections.unmodifiableMap(new HashMap<String, Feature>(builder.features));

        List<String> unsupportedFeatureNamesCopy = new LinkedList<String>();
        for (String unsupportedFeatureName : builder.unsupportedFeatureNames)
        {
            unsupportedFeatureNamesCopy.add(unsupportedFeatureName);
        }
        this.unsupportedFeatureNames = Collections.unmodifiableList(unsupportedFeatureNamesCopy);

        this.viewsNames = Collections.unmodifiableSet(new HashSet<String>(builder.viewsNames));
    }

    /**
     * Get the uri for this gadget spec
     * @return the uri for this gadget spec
     */
    public URI getUrl()
    {
        return this.specUri;
    }

    /**
     * Get the user prefs in this spec
     * @return an iterable of the user prefs in this spec
     */
    public Iterable<UserPrefSpec> getUserPrefs()
    {
        return this.userPrefs;
    }

    /**
     * Check if a particular view is supported in this spec
     * @param viewType to check if supported
     * @return true if the view is supported, according to the spec
     */
    public boolean supportsViewType(ViewType viewType)
    {
        if (viewsNames.isEmpty())
        {
            return false;
        }
        else if (viewsNames.contains(viewType.getCanonicalName()))
        {
            return true;
        }

        Iterator<String> it = viewType.getAliases().iterator();
        while (it.hasNext())
        {
            if (viewsNames.contains(it.next()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the scrolling setting of the gadget in this spec
     * @return true if the gadget should be displayed scrollably if applicable
     */
    public boolean isScrolling()
    {
        return this.scrolling;
    }

    /**
     * Get the height of the gadget in this spec
     * @return the height of the gadget in this spec
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Get the width of the gadget in this spec
     * @return the width of the gadget in this spec
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Get the title of the gadget in this spec
     * @return the title of the gadget in this spec
     */
    public String getTitle()
    {
        return this.title;
    }

    /**
     * Get the url that the title of the gadget in this spec should link to
     * @return the title url of the gadget in this spec
     */
    public URI getTitleUrl()
    {
        return this.titleUrl;
    }

    /**
     * Get the URI for the thumbnail image representing the gadget
     * @return the URI for the thumbnail image representing the gadget
     */
    public URI getThumbnail()
    {
        return this.thumbnail;
    }

    /**
     * Get the author of the gadget in this spec
     * @return the author of the gadget in this spec
     */
    public String getAuthor()
    {
        return this.author;
    }

    /**
     * Get the email for the author of the gadget in this spec
     * @return the email of the author of the gadget in this spec
     */
    public String getAuthorEmail()
    {
        return this.authorEmail;
    }

    /**
     * Get the description of the gadget in this spec
     * @return the description of the gadget in this spec
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Get the title for the directory of the gadget in this spec
     * @return the title for the directory of the gadget in this spec
     */
    public String getDirectoryTitle()
    {
        return this.directoryTitle;
    }

    /**
     * Get the features in this spec
     * @return a map of feature name Strings to Feature objects
     */
    public Map<String,Feature> getFeatures()
    {
        return this.features;
    }

    /**
     * Get the features that are needed (required, not optional) but aren't available
     * @return a group of feature names for features that are listed as required in this spec but aren't supported by the container
     */
    public Iterable<String> getUnsupportedFeatureNames()
    {
        return this.unsupportedFeatureNames;
    }

    /**
     * Factory method to create a new builder which can be used to create {@code GadgetSpec} objects.  It returns
     * a {@code Builder} which allows you to set the gadget spec values.
     *
     * @param specUri {@code URI} of the gadget
     * @return a {@code Builder} which allows you to set the gadget spec values
     */
    public static Builder gadgetSpec(URI specUri)
    {
        return new Builder(specUri);
    }

    /**
     * Factory method which allows you to create a new {@code GadgetSpec} object based on an existing
     * {@code GadgetSpec}.
     *
     * @param gadgetSpec the {@code GadgetSpec} to start with when building the new {@code GadgetSpec}
     * @return a {@code Builder} which allows you to set the gadget spec values
     */
    public static Builder gadgetSpec(GadgetSpec gadgetSpec)
    {
        return new Builder(gadgetSpec);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("title", getTitle())
            .append("url", getUrl())
            .toString();
    }

    /**
     * A builder that facilitates construction of {@code GadgetSpec} objects. The final {@code GadgetSpec}
     * is created by calling the {@link GadgetSpec.Builder#build()} method
     */
    public static class Builder
    {
        private final URI specUri;
        private Iterable<UserPrefSpec> userPrefs = Collections.emptySet();
        private boolean scrolling;
        private int height;
        private int width;
        private String title;
        private URI titleUrl;
        private URI thumbnail;
        private String author;
        private String authorEmail;
        private String description;
        private String directoryTitle;
        private Map<String,Feature> features = Collections.emptyMap();
        private Iterable<String> unsupportedFeatureNames = Collections.emptySet();
        private Set<String> viewsNames = Collections.emptySet();

        /**
         * Constructor
         *
         * @param specUri {@code URI} of the gadget
         */
        private Builder(URI specUri)
        {
            this.specUri = notNull("specUri", specUri);
        }

        /**
         * Constructor
         *
         * @param spec the {@code GadgetSpec} to start with when building the new {@code GadgetSpec}
         */
        private Builder(GadgetSpec spec)
        {
            notNull("spec", spec);
            this.specUri = spec.specUri;
            this.userPrefs = spec.userPrefs;
            this.scrolling = spec.scrolling;
            this.height = spec.height;
            this.width = spec.width;
            this.title = spec.title;
            this.titleUrl = spec.titleUrl;
            this.thumbnail = spec.thumbnail;
            this.author = spec.author;
            this.authorEmail = spec.authorEmail;
            this.description = spec.description;
            this.directoryTitle = spec.directoryTitle;
            this.features = spec.features;
            this.unsupportedFeatureNames = spec.unsupportedFeatureNames;
            this.viewsNames = spec.viewsNames;
        }

        /**
         * Set the list of {@code UserPrefSpec} objects for the {@code GadgetSpec} under construction and return
         * this {@code Builder} to allow further construction to be done.
         *
         * @param userPrefs the list of {@code UserPrefSpec} to use as the user preference values for the {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder userPrefs(Iterable<UserPrefSpec> userPrefs)
        {
            this.userPrefs = notNull("userPrefs", userPrefs);
            return this;
        }

        /**
         * Set the scrolling setting of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param scrolling the scrolling setting of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder scrolling(boolean scrolling)
        {
            this.scrolling = scrolling;
            return this;
        }

        /**
         * Set the height of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param height the height of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder height(int height)
        {
            this.height = height;
            return this;
        }

        /**
         * Set the width of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param width the width of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder width(int width)
        {
            this.width = width;
            return this;
        }

        /**
         * Set the title of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param title the title of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder title(String title)
        {
            this.title = title;
            return this;
        }

        /**
         * Set the title {@code URI} of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param titleUrl the title {@code URI} of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder titleUrl(URI titleUrl)
        {
            this.titleUrl = titleUrl;
            return this;
        }

        /**
         * Set the thumbnail {@code URI} of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param thumbnail the thumbnail {@code URI} of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder thumbnail(URI thumbnail)
        {
            this.thumbnail = thumbnail;
            return this;
        }

        /**
         * Set the author of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param author the author of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder author(String author)
        {
            this.author = author;
            return this;
        }

        /**
         * Set the author email of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param authorEmail the author email of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder authorEmail(String authorEmail)
        {
            this.authorEmail = authorEmail;
            return this;
        }

        /**
         * Set the description of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param description the description of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder description(String description)
        {
            this.description = description;
            return this;
        }

        /**
         * Set the directory title of the {@code GadgetSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param directoryTitle the directory title of this {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder directoryTitle(String directoryTitle)
        {
            this.directoryTitle = directoryTitle;
            return this;
        }

        /**
         * Set the {@code Map} of {@code Feature} for the {@code GadgetSpec} under construction and return
         * this {@code Builder} to allow further construction to be done.
         *
         * @param features the {@code Map} of {@code Feature} for the {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder features(Map<String, Feature> features)
        {
            this.features = notNull("features", features);
            return this;
        }

        /**
         * Set the list of unsupported features for the {@code GadgetSpec} under construction and return
         * this {@code Builder} to allow further construction to be done.
         *
         * @param unsupportedFeatureNames the list of unsupported features for the {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder unsupportedFeatureNames(Iterable<String> unsupportedFeatureNames)
        {
            this.unsupportedFeatureNames = notNull("unsupportedFeatureNames", unsupportedFeatureNames);
            return this;
        }

        /**
         * Set the list of view names for the {@code GadgetSpec} under construction and return
         * this {@code Builder} to allow further construction to be done.
         *
         * @param viewsNames the list of view names for the {@code GadgetSpec}
         * @return this builder to allow for further construction
         */
        public Builder viewsNames(Set<String> viewsNames)
        {
            this.viewsNames = notNull("viewsNames", viewsNames);
            return this;
        }

        /**
         * Returns the final constructed {@code GadgetSpec}
         *
         * @return the {@code GadgetSpec}
         */
        public GadgetSpec build()
        {
            return new GadgetSpec(this);
        }
    }
}
