package com.atlassian.jira.rest.api.field;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB-compatible bean that represents a JIRA field.
 *
 * @since v4.2
 */
@XmlRootElement (name = "field")
public class FieldBean<T>
{
    /**
     * Returns a new FieldBean instance.
     *
     * @param <T> the type of the value object
     * @param name the field name
     * @param type the field type
     * @param value the field value
     * @return a FieldBean
     */
    public static <T> FieldBean<T> create(String name, String type, T value)
    {
        return new FieldBean<T>(name, type, value);
    }

    /**
     * Returns a new Builder instance, which can be used to build a FieldBean.
     *
     * @param <T> the type of the value object
     * @return a Builder
     * @see com.atlassian.jira.rest.api.field.FieldBean.Builder#build()
     */
    public static <T> Builder<T> builder()
    {
        return new Builder<T>();
    }

    /**
     * The field-s name.
     */
    @XmlElement (name = "name")
    private final String name;

    /**
     * The field's type.
     */
    @XmlElement (name = "type")
    private final String type;

    /**
     * The field's value. This must hold an instance of a class that JAXB knows how to marshall.
     */
    @XmlElement (name = "value")
    private final T value;

    /**
     * Creates a new CustomFieldBean.
     *
     * @param name the field name
     * @param type the field type
     * @param value the field value
     */
    public FieldBean(String name, String type, T value)
    {
        if (name == null) { throw new NullPointerException("name"); }
        if (type == null) { throw new NullPointerException("type"); }

        this.name = name;
        this.type = type;
        this.value = value;
    }

    /**
     * Returns the field name.
     *
     * @return a String containing the field name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the type.
     *
     * @return a String containing the field type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the value.
     *
     * @return an Object containing the field value
     */
    public T getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        FieldBean fieldBean = (FieldBean) o;
        return name.equals(fieldBean.name)
                && type.equals(fieldBean.type)
                && !(value != null ? !value.equals(fieldBean.value) : fieldBean.value != null);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "FieldBean{name='" + name + '\'' + ", type='" + type + '\'' + ", value=" + value + '}';
    }

    /**
     * Builder object for FieldBean instances.
     */
    public static class Builder<T>
    {
        private String name;
        private String type;
        private T value;

        Builder()
        {
            // empty
        }

        /**
         * Sets the name to use when building the FieldBean.
         *
         * @param name a String containing the name
         * @return this
         */
        public Builder<T> name(String name)
        {
            this.name = name;
            return this;
        }

        /**
         * Sets the type to use when building the FieldBean.
         *
         * @param type a String containing the type
         * @return this
         */
        public Builder<T> type(String type)
        {
            this.type = type;
            return this;
        }

        /**
         * Sets the value to use when building the FieldBean.
         *
         * @param value the field value
         * @return this
         */
        public Builder<T> value(T value)
        {
            this.value = value;
            return this;
        }

        /**
         * Builds a new FieldBean instance with the requested name, type, and value.
         *
         * @return a FieldBean
         */
        public FieldBean<T> build()
        {
            return create(name, type, value);
        }
    }
}