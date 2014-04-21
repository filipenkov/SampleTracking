package com.atlassian.jira.webtest.framework.core.component;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * {@link Option} objects factory.
 *
 * @since v4.3
 */
public class Options
{
    public static final String DEFAULT_OPTION_VALUE = asString("-1");

    static abstract class AbstractOption implements Option
    {
        private final String id, label, value;

        public AbstractOption(String id, String value, String label)
        {
            if (value == null && label == null && id == null)
            {
                throw new IllegalArgumentException("One of the option identifiers must be non-null");
            }
            this.value = value;
            this.label = label;
            this.id = id;
        }

        @Override
        public String id()
        {
            return id;
        }

        @Override
        public String value()
        {
            return value;
        }

        @Override
        public String label()
        {
            return label;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj.getClass() != getClass())
            {
                return false;
            }
            AbstractOption other = (AbstractOption) obj;
            if (id != null && !id.equals(other.id))
            {
                return false;
            }
            if (value != null && !value.equals(other.value))
            {
                return false;
            }
            if (label != null && !label.equals(other.label))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (label != null ? label.hashCode() : 0);
            return result;
        }
    }


    public static class IdOption extends AbstractOption
    {
        IdOption(String id) { super(id, null, null); }

    }

    public static class ValueOption extends AbstractOption
    {
        ValueOption(String value) { super(null, value, null); }
    }

    public static class LabelOption extends AbstractOption
    {
        LabelOption(String label) { super(null, null, label); }
    }

    public static class FullOption extends AbstractOption
    {
        FullOption(String id, String value, String label) { super(id, value, label); }
    }

    /**
     * New option distinguishable by its HTML ID.
     *
     * @param id HTML id of the option
     * @return new option
     */
    public static IdOption id(String id)
    {
        return new IdOption(id);
    }

    /**
     * New option distinguishable by its HTML value attribute.
     *
     * @param value HTML value attribute of the option
     * @return new option
     */
    public static ValueOption value(String value)
    {
        return new ValueOption(value);
    }

    /**
     * New option distinguishable by its UI label.
     *
     * @param label label of the option visible in the UI
     * @return new option
     */
    public static LabelOption label(String label)
    {
        return new LabelOption(label);
    }


    /**
     * New option distinguishable all identifiers. Some of the identifiers may be <code>null</code>, but at least
     * one has to be non-<code>null</code>
     *
     * @param id HTML id of the option
     * @param value HTML value attribute of the option
     * @param label label of the option visible in the UI
     * @return new option
     */
    public static Option full(String id, String value, String label)
    {
        return new FullOption(id, value, label);
    }

    /**
     * New option with default option value.
     *
     * @return new default option
     * @see #DEFAULT_OPTION_VALUE
     */
    public static Option defaultValue()
    {
        return value(DEFAULT_OPTION_VALUE);
    }
}
