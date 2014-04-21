package com.atlassian.streams.spi;

import java.util.Map;

import com.atlassian.streams.api.StreamsFilterType;

import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a filter option for streams
 */
public interface StreamsFilterOption
{
    /**
     * The filter option key
     *
     * @return The filter option key
     */
    String getKey();

    /**
     * The filter option display name
     *
     * @return The filter option display name
     */
    String getDisplayName();

    /**
     * The filter option help text
     *
     * @return The filter option help text
     */
    String getHelpTextI18nKey();

    /**
     * The filter option i18n key
     *
     * @return The filter option i18n key
     */
    String getI18nKey();

    /**
     * The filter option type
     *
     * @return The filter option type
     */
    StreamsFilterType getFilterType();

    /**
     * Whether the filter option accepts multiple values or not
     *
     * @return Whether the filter option accepts multiple values or not
     */
    boolean isUnique();

    /**
     * If true, the values of this filter option should be shown as separate pseudo-providers
     * in the filter UI.
     * 
     * @return True if the values of the filter option should be shown as provider aliases
     */
    boolean isProviderAlias();
    
    /**
     * The default values for the streams filter option
     *
     * @return The default values for the streams filter option
     */
    Map<String, String> getValues();

    /**
     * Streams filter option builder
     */
    public static class Builder
    {
        private String key;
        private String displayName;
        private String helpTextI18nKey;
        private String i18nKey;
        private StreamsFilterType type;
        private boolean unique = true;
        private boolean providerAlias = false;
        private Map<String, String> values;

        public Builder(String key, StreamsFilterType type)
        {
            this.key = checkNotNull(key, "key");
            this.type = checkNotNull(type, "type");
            this.values = ImmutableMap.of();
        }

        public Builder helpTextI18nKey(String helpTextI18nKey)
        {
            this.helpTextI18nKey = helpTextI18nKey;
            return this;
        }

        public Builder displayName(String displayName)
        {
            this.displayName = displayName;
            return this;
        }

        public Builder i18nKey(String i18nKey)
        {
            this.i18nKey = i18nKey;
            return this;
        }

        public Builder unique(boolean unique)
        {
            this.unique = unique;
            return this;
        }

        public Builder providerAlias(boolean providerAlias)
        {
            this.providerAlias = providerAlias;
            return this;
        }

        public Builder values(Map<String, String> values)
        {
            //STRM - 2013, Adding extra information if we have it to the error message
            this.values = ImmutableMap.copyOf(checkNotNull(values, "Values are null for key: " + key + " and name: " + displayName));
            return this;
        }

        public StreamsFilterOption build()
        {
            return new StreamsFilterOptionImpl(this);
        }

        String getKey()
        {
            return key;
        }

        String getHelpTextI18nKey()
        {
            return helpTextI18nKey;
        }

        String getDisplayName()
        {
            return displayName;
        }

        String getI18nKey()
        {
            return i18nKey;
        }

        StreamsFilterType getType()
        {
            return type;
        }

        boolean isUnique()
        {
            return unique;
        }

        boolean isProviderAlias()
        {
            return providerAlias;
        }
        
        Map<String, String> getValues()
        {
            return values;
        }
    }
}
