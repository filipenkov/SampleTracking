package com.atlassian.crowd.integration.rest.util;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.integration.rest.entity.*;
import com.atlassian.crowd.search.builder.*;
import com.atlassian.crowd.search.query.entity.restriction.*;

import java.text.*;
import java.util.*;

/**
 * Utility class to convert from a SearchRestriction interface to one of SearchRestrictionEntity classes.
 */
public class SearchRestrictionEntityTranslator
{
    /**
     * Represents a supported value type.
     */
    public enum SupportedType
    {
        BOOLEAN(Boolean.class),
        DATE(Date.class),
        STRING(String.class);

        private final Class type;

        SupportedType(final Class type)
        {
            this.type = type;
        }

        public Class getType()
        {
            return type;
        }

        /**
         * Returns the SupportedType from the specified name. The matching is case-insensitive.
         *
         * @param supportedType Name of the enum constant.
         * @return SupportedType enum constant.
         */
        public static SupportedType of(final String supportedType)
        {
            return SupportedType.valueOf(supportedType.toUpperCase());
        }

        /**
         * Returns the SupportedType from the specified Class type.
         *
         * @param type Class of the supported type
         * @return SupportedType enum constant.
         */
        public static SupportedType of(final Class type)
        {
            for (SupportedType supportedType : SupportedType.values())
            {
                if (supportedType.getType().equals(type))
                {
                    return supportedType;
                }
            }
            throw new IllegalArgumentException(type.getCanonicalName() + " is an unsupported type.");
        }
    }

    /**
     * The format used for times in the REST plugin. Conforms to ISO 8601. Format is also used in JIRA.
     */
    public static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Converts from a BooleanRestriction to a BooleanRestrictionEntity.
     *
     * @param booleanRestriction BooleanRestriction
     * @return BooleanRestrictionEntity
     */
    public static BooleanRestrictionEntity toBooleanRestrictionEntity(final BooleanRestriction booleanRestriction)
    {
        Collection<SearchRestrictionEntity> restrictionEntities = new ArrayList<SearchRestrictionEntity>();
        for (SearchRestriction sr : booleanRestriction.getRestrictions())
        {
            restrictionEntities.add(toSearchRestrictionEntity(sr));
        }
        return new BooleanRestrictionEntity(booleanRestriction.getBooleanLogic().name(), restrictionEntities);
    }

    /**
     * Converts from a BooleanRestrictionEntity to a BooleanRestriction.
     *
     * @param booleanRestrictionEntity boolean restriction entity to convert from
     * @return BooleanRestriction
     * @throws IllegalArgumentException if no BooleanLogic enum constant could be found for {@link com.atlassian.crowd.integration.rest.entity.BooleanRestrictionEntity#getBooleanLogic()}.
     */
    public static BooleanRestriction toBooleanRestriction(final BooleanRestrictionEntity booleanRestrictionEntity)
    {
        final BooleanRestriction.BooleanLogic booleanLogic = BooleanRestriction.BooleanLogic.valueOf(booleanRestrictionEntity.getBooleanLogic().toUpperCase());
        final Collection<SearchRestriction> restrictions = new ArrayList<SearchRestriction>();
        for (SearchRestrictionEntity searchRestrictionEntity : booleanRestrictionEntity.getRestrictions())
        {
            restrictions.add(toSearchRestriction(searchRestrictionEntity));
        }

        switch (booleanLogic)
        {
            case AND:
                return Combine.allOf(restrictions);
            case OR:
                return Combine.anyOf(restrictions);
            default:
                throw new AssertionError("Unknown BooleanLogic type: " + booleanLogic);
        }
    }

    /**
     * Converts from a PropertyRestriction to a PropertyRestrictionEntity.
     *
     * @param propertyRestriction PropertyRestriction
     * @return PropertyRestrictionEntity
     */
    public static PropertyRestrictionEntity toPropertyRestrictionEntity(final PropertyRestriction propertyRestriction)
    {
        PropertyEntity propertyEntity = toPropertyEntity(propertyRestriction.getProperty());

        String valueString;

        MatchMode mm = propertyRestriction.getMatchMode();
        if (mm == MatchMode.NULL)
        {
            valueString = null;
        }
        else
        {
            valueString = valueToString(propertyRestriction.getValue());
        }

        return new PropertyRestrictionEntity(propertyEntity, mm.name(), valueString);
    }

    /**
     * Converts from a PropertyRestrictionEntity to a PropertyRestriction.
     *
     * @param propertyRestrictionEntity property restriction entity to convert from
     * @return PropertyRestriction
     */
    public static PropertyRestriction toPropertyRestriction(final PropertyRestrictionEntity propertyRestrictionEntity)
    {
        final Property property = toProperty(propertyRestrictionEntity.getProperty());
        final MatchMode matchMode = MatchMode.valueOf(propertyRestrictionEntity.getMatchMode().toUpperCase());
        final SupportedType supportedType = SupportedType.of(property.getPropertyType());
        return new TermRestriction(property, matchMode, valueFromString(supportedType, propertyRestrictionEntity.getValue()));
    }

    /**
     * Converts from a Property to a PropertyEntity.
     *
     * @param property Property to convert from
     * @return PropertyEntity
     */
    public static PropertyEntity toPropertyEntity(final Property property)
    {
        final SupportedType supportedType = SupportedType.of(property.getPropertyType());
        return new PropertyEntity(property.getPropertyName(), supportedType.name());
    }

    /**
     * Converts from a PropertyEntity to a Property.
     *
     * @param propertyEntity PropertyEntity to convert from.
     * @return Property
     * @throws IllegalArgumentException if the property value type is unknown
     */
    public static Property toProperty(final PropertyEntity propertyEntity)
    {
        final String typeString = propertyEntity.getType();
        SupportedType supportedType = SupportedType.of(typeString);

        return new PropertyImpl(propertyEntity.getName(), supportedType.getType());
    }

    /**
     * Converts from a SearchRestriction a SearchRestrictionEntity.
     *
     * @param searchRestriction search restriction to convert
     * @return SearchRestrictionEntity
     */
    public static SearchRestrictionEntity toSearchRestrictionEntity(final SearchRestriction searchRestriction)
    {
        if (searchRestriction instanceof BooleanRestriction)
        {
            return toBooleanRestrictionEntity((BooleanRestriction) searchRestriction);
        }
        else if (searchRestriction instanceof PropertyRestriction)
        {
            return toPropertyRestrictionEntity((PropertyRestriction) searchRestriction);
        }
        else if (searchRestriction instanceof NullRestriction)
        {
            return NullRestrictionEntity.INSTANCE;
        }
        else
        {
            throw new IllegalArgumentException("Unknown search restriction type");
        }
    }

    /**
     * Converts from a SearchRestrictionEntity to a SearchRestriction.
     *
     * @param searchRestrictionEntity search restriction entity to convert from
     * @return SearchRestriction
     */
    public static SearchRestriction toSearchRestriction(final SearchRestrictionEntity searchRestrictionEntity)
    {
        if (searchRestrictionEntity instanceof BooleanRestrictionEntity)
        {
            return toBooleanRestriction((BooleanRestrictionEntity) searchRestrictionEntity);
        }
        else if (searchRestrictionEntity instanceof PropertyRestrictionEntity)
        {
            return toPropertyRestriction((PropertyRestrictionEntity) searchRestrictionEntity);
        }
        else if (searchRestrictionEntity instanceof NullRestrictionEntity)
        {
            return NullRestrictionImpl.INSTANCE;
        }
        else
        {
            throw new IllegalArgumentException("Unknown search restriction entity type");
        }
    }

    /**
     * Converts the value to a String.
     *
     * @param value value
     * @return String format of the value
     */
    public static String valueToString(final Object value)
    {
        if (value instanceof Enum)
        {
            return ((Enum) value).name();
        }
        else if (value instanceof Date)
        {
            return asTimeString((Date) value);
        }
        else
        {
            return value.toString();
        }
    }

    /**
     * Converts from a String to a value type.
     *
     * @param supportedType the supported type of the value
     * @param value value
     * @return value type
     */
    public static Object valueFromString(final SupportedType supportedType, final String value)
    {
        switch (supportedType)
        {
            case BOOLEAN:
                return Boolean.valueOf(value);
            case DATE:
                return fromTimeString(value);
            case STRING:
                return value;
            default:
                throw new AssertionError("Unknown supported type: " + supportedType);
        }
    }

    /**
     * Converts the given Date object to a String using. The string is in the format <pre>{@value #TIME_FORMAT}</pre>.
     *
     * @param date a Date
     * @return a String representation of the date and time
     * @see java.text.SimpleDateFormat
     */
    public static String asTimeString(final Date date)
    {
        return new SimpleDateFormat(TIME_FORMAT).format(date);
    }

    /**
     * Converts the given date and time String to a Date object. The time parameter is expected to be in the format
     * <pre>{@value #TIME_FORMAT}</pre>.
     *
     * @param time a String representation of a date and time
     * @return a Date
     * @throws RuntimeException if there is an error parsing the date
     * @throws IllegalArgumentException if the input string is not in the expected format
     * @see java.text.SimpleDateFormat
     */
    public static Date fromTimeString(final String time) throws IllegalArgumentException
    {
        try
        {
            return new SimpleDateFormat(TIME_FORMAT).parse(time);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Error parsing time: " + time, e);
        }
    }
}
