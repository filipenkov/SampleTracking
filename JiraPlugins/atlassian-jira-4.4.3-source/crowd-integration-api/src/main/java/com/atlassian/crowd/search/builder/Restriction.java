package com.atlassian.crowd.search.builder;

import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;

/**
 * Example usage:
 * <p/>
 * <code>Restriction.on(UserTermKeys.USERNAME).startsWith("rob");</code>
 * <p/>
 * It is a good idea to add a static import to UserTermKeys
 * in your class to make things even more convenient.
 */
public class Restriction
{
    /**
     * Returns an intermediate form of a property restriction.
     *
     * <code>
     * Restriction.on(UserTermKeys.USERNAME).startsWith("rob");
     * </code>
     *
     * @param property property to restrict on
     * @return intermediate form of a property restriction 
     */
    public static <T> RestrictionWithProperty<T> on(Property<T> property)
    {
        return new RestrictionWithProperty<T>(property);
    }

    public static class RestrictionWithProperty<T>
    {
        private final Property<T> property;

        public RestrictionWithProperty(final Property<T> property)
        {
            this.property = property;
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property exactly matches the given
         * value.
         *
         * @param value value to exactly match
         * @return property restriction that is only satisfied if the value of the property exactly matches the given
         *          value.
         */
        public PropertyRestriction<T> exactlyMatching(T value)
        {
            return new TermRestriction<T>(property, MatchMode.EXACTLY_MATCHES, value);
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property starts with the given
         * value.
         *
         * @param value prefix value
         * @return property restriction that is only satisfied if the value of the property exactly matches the given
         *          value.
         */
        public PropertyRestriction<T> startingWith(T value)
        {
            return new TermRestriction<T>(property, MatchMode.STARTS_WITH, value);
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property contains the given value.
         *
         * @param value value to contain
         * @return property restriction that is only satisfied if the value of the property contains the given value
         */
        public PropertyRestriction<T> containing(T value)
        {
            return new TermRestriction<T>(property, MatchMode.CONTAINS, value);
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property is less than the given
         * value.
         *
         * @param value value to be less than
         * @return property restriction that is only satisfied if the value of the property is less than the given value
         */
        public PropertyRestriction<T> lessThan(T value)
        {
            return new TermRestriction<T>(property, MatchMode.LESS_THAN, value);
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property is greater than the given
         * value.
         *
         * @param value value to be greater than
         * @return property restriction that is only satisfied if the value of the property is greater than the given
         *          value
         */
        public PropertyRestriction<T> greaterThan(T value)
        {
            return new TermRestriction<T>(property, MatchMode.GREATER_THAN, value);
        }

        /**
         * Returns a property restriction that is only satisfied if the value of the property is null.
         *
         * @return property restriction that is only satisfied if the value of the property is null
         */
        public PropertyRestriction<T> isNull()
        {
            return new TermRestriction<T>(property, MatchMode.NULL, null);
        }
    }
}
