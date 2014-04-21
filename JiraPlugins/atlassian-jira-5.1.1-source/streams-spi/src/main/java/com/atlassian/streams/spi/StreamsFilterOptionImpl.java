package com.atlassian.streams.spi;

import java.util.Map;

import com.atlassian.streams.api.StreamsFilterType;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Default implementation of the {@code StreamsFilterOption} class
 */
final class StreamsFilterOptionImpl implements StreamsFilterOption
{
    private final String key;
    private final String displayName;
    private final String helpTextI18nKey;
    private final String i18nKey;
    private final StreamsFilterType type;
    private final boolean unique;
    private final boolean providerAlias;
    private final Map<String, String> values;

    StreamsFilterOptionImpl(StreamsFilterOption.Builder builder)
    {
        this.key = builder.getKey();
        this.displayName = builder.getDisplayName();
        this.helpTextI18nKey = builder.getHelpTextI18nKey();
        this.i18nKey = builder.getI18nKey();
        this.type = builder.getType();
        this.unique = builder.isUnique();
        this.providerAlias = builder.isProviderAlias();
        this.values = builder.getValues();
    }

    public static StreamsFilterOption.Builder builder(String key, StreamsFilterType type)
    {
        return new StreamsFilterOption.Builder(key, type);
    }

    public String getKey()
    {
        return key;
    }

    public String getDisplayName()
    {
        return isNotBlank(displayName) ? displayName : isNotBlank(i18nKey) ? i18nKey : key;
    }

    public String getHelpTextI18nKey()
    {
        return helpTextI18nKey;
    }

    public String getI18nKey()
    {
        return i18nKey;
    }

    public StreamsFilterType getFilterType()
    {
        return type;
    }

    public boolean isUnique()
    {
        return unique;
    }

    public boolean isProviderAlias()
    {
        return providerAlias;
    }
    
    public Map<String, String> getValues()
    {
        return values;
    }
}
