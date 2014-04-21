package com.atlassian.gadgets.dashboard.internal.rest.representations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.gadgets.dashboard.internal.UserPref;

import org.apache.commons.lang.builder.EqualsBuilder;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

/**
 * Provides a JAXB view of a UserPref, so that we can build new gadgets on the fly in Javascript.
 */
@XmlRootElement
public final class UserPrefRepresentation
{
    @XmlElement
    private final String name;
    @XmlElement
    private final String value;
    @XmlElement
    private final String type;
    @XmlElement
    private final String displayName;
    @XmlElement
    private final boolean required;
    @XmlElement
    private final List<EnumValueRepresentation> options;

    public UserPrefRepresentation(final UserPref userPref)
    {
        this.name = escapeHtml(userPref.getName());
        this.value = escapeHtml(userPref.getValue() == null ? userPref.getDefaultValue() : userPref.getValue());
        this.type = userPref.getDataType().name().toLowerCase(Locale.ENGLISH);
        this.displayName = escapeHtml(userPref.getDisplayName());
        this.required = userPref.isRequired();
        this.options = transformEnumValues(userPref.getEnumValues(), this.value);
    }

    private List<EnumValueRepresentation> transformEnumValues(final Map<String, String> enumValues, final String selectedValue)
    {
        final List<EnumValueRepresentation> result = new ArrayList<EnumValueRepresentation>();        
        for (Map.Entry<String, String> enumValueEntry : enumValues.entrySet())
        {
            final boolean isDefaultValue = new EqualsBuilder().append(selectedValue, enumValueEntry.getKey()).isEquals();
            result.add(new EnumValueRepresentation(enumValueEntry.getKey(), enumValueEntry.getValue(), isDefaultValue));
        }
        return result;
    }

    // Provided for JAXB.
    @SuppressWarnings ({ "UnusedDeclaration", "unused" })
    private UserPrefRepresentation()
    {
        this.type = null;
        this.displayName = null;
        this.name = null;
        this.value = null;
        this.required = false;
        this.options = new ArrayList<EnumValueRepresentation>();
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public String getType()
    {
        return type;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public boolean isRequired()
    {
        return required;
    }

    public List<EnumValueRepresentation> getOptions()
    {
        return new ArrayList<EnumValueRepresentation>(options);
    }   

    @XmlRootElement
    public static class EnumValueRepresentation
    {
        @XmlElement
        private final String value;
        @XmlElement
        private final String label;
        @XmlElement
        private final Boolean selected;

        public EnumValueRepresentation(final String value, final String label, final boolean selected)
        {
            this.value = escapeHtml(value);
            this.label = escapeHtml(label);
            this.selected = selected;
        }

        // Provided for JAXB.
        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private EnumValueRepresentation()
        {
            this.value = null;
            this.label = null;
            this.selected = null;
        }

        public String getValue()
        {
            return value;
        }

        public String getLabel()
        {
            return label;
        }

        public Boolean isSelected()
        {
            return selected;
        }
    }
}
