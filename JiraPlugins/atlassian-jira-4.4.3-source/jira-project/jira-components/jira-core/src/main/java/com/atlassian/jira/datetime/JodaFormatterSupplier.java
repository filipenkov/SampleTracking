package com.atlassian.jira.datetime;

import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Interface for suppliers of JodaTime DateTimeFormatter instances.
 *
 * @since v4.4
 */
interface JodaFormatterSupplier
{
    org.joda.time.format.DateTimeFormatter get(Key key);

    /**
     * Key class JODA formatters.
     */
    static class Key
    {
        final Locale locale;
        final String pattern;

        Key(String pattern, Locale locale)
        {
            this.locale = checkNotNull(locale);
            this.pattern = checkNotNull(pattern);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            Key key = (Key) o;

            if (!locale.equals(key.locale)) { return false; }
            if (!pattern.equals(key.pattern)) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = locale.hashCode();
            result = 31 * result + pattern.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "Key{locale=" + locale + ", pattern='" + pattern + '\'' + '}';
        }
    }
}
