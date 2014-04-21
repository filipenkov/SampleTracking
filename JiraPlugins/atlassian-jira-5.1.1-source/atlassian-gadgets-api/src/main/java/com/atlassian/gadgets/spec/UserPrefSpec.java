package com.atlassian.gadgets.spec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.jcip.annotations.Immutable;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Represents a user pref containing the details from the gadget spec XML
 */
@Immutable
public final class UserPrefSpec
{
    private final String name;
    private final String displayName;
    private final boolean required;
    private final DataType dataType;
    private final Map<String, String> enumValues;
    private final String defaultValue;

    private UserPrefSpec(Builder builder)
    {
        this.name = builder.name;
        this.displayName = builder.displayName;
        this.required = builder.required;
        this.dataType = builder.dataType;

        // Using a LinkedHashMap to preserve the order the values were defined in when iterating.
        Map<String, String> enumValuesCopy = new LinkedHashMap<String, String>();
        for (String key : builder.enumValues.keySet())
        {
            enumValuesCopy.put(key, builder.enumValues.get(key));
        }
        this.enumValues = Collections.unmodifiableMap(enumValuesCopy);

        this.defaultValue = builder.defaultValue;
    }

   /**
     * Returns the pref name.
     * @return the pref name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the display name of this parameter.
     * @return the name to show the user for this pref
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * Returns true if the pref is required.
     * @return true if the pref is required
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * Returns the {@link DataType} of this pref.
     * @return this pref's datatype
     */
    public DataType getDataType()
    {
        return dataType;
    }

    /**
     * Returns the possible values for an enumerated pref.
     * @return possible values for the pref
     */
    public Map<String, String> getEnumValues()
    {
        return enumValues;
    }

    /**
     * Returns the pref's default value.
     * @return the default value of the pref
     */
    public String getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Factory method to create a new builder which can be used to create {@code UserPrefSpec} objects.  It returns
     * a {@code Builder} which allows you to set the user pref spec values.
     *
     * @param name the pref name
     * @return a {@code Builder} which allows you to set the user pref spec values
     */
    public static Builder userPrefSpec(String name)
    {
        return new Builder(name);
    }

    /**
     * Factory method which allows you to create a new {@code UserPrefSpec} object based on an existing
     * {@code UserPrefSpec}.
     *
     * @param userPrefSpec the {@code UserPrefSpec} to start with when building the new {@code UserPrefSpec}
     * @return a {@code Builder} which allows you to set the gadget spec values
     */
    public static Builder userPrefSpec(UserPrefSpec userPrefSpec)
    {
        return new Builder(userPrefSpec);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("name", getName())
            .append("dataType", getDataType())
            .toString();
    }

    /**
     * A builder that facilitates construction of {@code UserPrefSpec} objects. The final {@code UserPrefSpec}
     * is created by calling the {@link UserPrefSpec.Builder#build()} method
     */
    public static class Builder
    {
        private final String name;
        private String displayName;
        private boolean required;
        private DataType dataType;
        private Map<String, String> enumValues = Collections.emptyMap();
        private String defaultValue;

        /**
         * Constructor
         *
         * @param name the pref name
         */
        private Builder(String name)
        {
            this.name = notNull("name", name);
        }

        /**
         * Constructor
         *
         * @param spec the {@code UserPrefSpec} to start with when building the new {@code UserPrefSpec}
         */
        private Builder(UserPrefSpec spec)
        {
            notNull("spec", spec);
            this.name = spec.name;
            this.displayName = spec.displayName;
            this.required = spec.required;
            this.dataType = spec.dataType;
            this.enumValues = spec.enumValues;
            this.defaultValue = spec.defaultValue;
        }

        /**
         * Set the display name of the {@code UserPrefSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param displayName name to show the user for this {@code UserPrefSpec}
         * @return this builder to allow for further construction
         */
        public Builder displayName(String displayName)
        {
            notNull("displayName", displayName);
            this.displayName = displayName;
            return this;
        }

        /**
         * Set the setting of the {@code UserPrefSpec} under construction whether it is required or not and
         * return this {@code Builder} to allow further construction to be done.
         *
         * @param required the setting of this {@code UserPrefSpec} whether it is required or not
         * @return this builder to allow for further construction
         */
        public Builder required(boolean required)
        {
            this.required = required;
            return this;
        }

        /**
         * Set the {@link DataType} of the {@code UserPrefSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param dataType the {@link DataType} of this pref
         * @return this builder to allow for further construction
         */
        public Builder dataType(DataType dataType)
        {
            notNull("dataType", dataType);
            this.dataType = dataType;
            return this;
        }

        /**
         * Set the {@code Map} of the possible values for an enumerated {@code UserPrefSpec} under
         * construction and return this {@code Builder} to allow further construction to be done.
         *
         * @param enumValues the possible values for an enumerated pref
         * @return this builder to allow for further construction
         */
        public Builder enumValues(Map<String, String> enumValues)
        {
            this.enumValues = notNull("enumValues", enumValues);
            return this;
        }

        /**
         * Set the default value of the {@code UserPrefSpec} under construction and return this {@code Builder}
         * to allow further construction to be done.
         *
         * @param defaultValue the pref's default value
         * @return this builder to allow for further construction
         */
        public Builder defaultValue(String defaultValue)
        {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Returns the final constructed {@code UserPrefSpec}
         *
         * @return the {@code UserPrefSpec}
         */
        public UserPrefSpec build()
        {
            return new UserPrefSpec(this);
        }
    }
}
