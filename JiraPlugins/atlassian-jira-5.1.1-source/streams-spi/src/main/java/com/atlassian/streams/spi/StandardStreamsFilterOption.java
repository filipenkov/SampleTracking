package com.atlassian.streams.spi;

import java.util.Map;

import com.atlassian.streams.api.StreamsFilterType;

import static com.atlassian.streams.api.StreamsFilterType.DATE;
import static com.atlassian.streams.api.StreamsFilterType.SELECT;
import static com.atlassian.streams.api.StreamsFilterType.LIST;

/**
 * Default streams filter options
 */
public enum StandardStreamsFilterOption implements StreamsFilterOption
{
    UPDATE_DATE("update-date", DATE, "streams.filter.option.update.date", "Update Date", true),
    USER("user", StreamsFilterType.USER, "streams.filter.option.author", "Username", true, "streams.filter.option.help.author"),
    ISSUE_KEY("issue-key", LIST, "streams.filter.option.issueKey", "JIRA Issue Key", true, "streams.filter.option.help.issueKey");

    public static final String STANDARD_FILTERS_PROVIDER_KEY = "streams";

    public static final String ACTIVITY_KEY = "activity";
    public static final String ACTIVITY_OBJECT_VERB_SEPARATOR = ":";
    public static final String PROJECT_KEY = "key";
    public static final StreamsFilterType PROJECT_TYPE = SELECT;

    private final StreamsFilterOption streamsFilterOption;

    StandardStreamsFilterOption(String key, StreamsFilterType type, String i18nKey, String displayName, boolean unique)
    {
        this.streamsFilterOption = new Builder(key, type).displayName(displayName).i18nKey(i18nKey).unique(unique).build();
    }

    StandardStreamsFilterOption(String key, StreamsFilterType type, String i18nKey, String displayName, boolean unique, String helpTextI18nKey)
    {
        this.streamsFilterOption = new Builder(key, type).helpTextI18nKey(helpTextI18nKey).displayName(displayName).i18nKey(i18nKey).unique(unique).build();
    }

    public static StreamsFilterOption projectKeys(Map<String, String> values, String product)
    {
        return new StreamsFilterOption.Builder(PROJECT_KEY, PROJECT_TYPE).
            displayName("Project").
            helpTextI18nKey("streams.filter.option.help.project." + product.toLowerCase()).
            i18nKey("streams.filter.option.project." + product.toLowerCase()).
            unique(true).
            values(values).
            build();
    }

    /**
     * The filter option key
     *
     * @return The filter option key
     */
    public String getKey()
    {
        return streamsFilterOption.getKey();
    }

    /**
     * The filter option display name
     *
     * @return The filter option display name
     */
    public String getDisplayName()
    {
        return streamsFilterOption.getDisplayName();
    }

    /**
     * The filter option help text i18n key
     *
     * @return The filter option help text i18n key
     */
    public String getHelpTextI18nKey()
    {
        return streamsFilterOption.getHelpTextI18nKey();
    }

    /**
     * The filter option i18n key
     *
     * @return The filter option i18n key
     */
    public String getI18nKey()
    {
        return streamsFilterOption.getI18nKey();
    }

    /**
     * The filter option type
     *
     * @return The filter option type
     */
    public StreamsFilterType getFilterType()
    {
        return streamsFilterOption.getFilterType();
    }

    /**
     * Whether the filter option accepts multiple values or not
     *
     * @return Whether the filter option accepts multiple values or not
     */
    public boolean isUnique()
    {
        return streamsFilterOption.isUnique();
    }
    
    /**
     * If true, the values of this filter option should be shown as separate pseudo-providers
     * in the filter UI.
     * 
     * @return True if the values of the filter option should be shown as provider aliases
     */
    public boolean isProviderAlias()
    {
        return streamsFilterOption.isProviderAlias();
    }

    /**
     * The default values for the streams filter option
     *
     * @return The default values for the streams filter option
     */
    public Map<String, String> getValues()
    {
        return streamsFilterOption.getValues();
    }
}
