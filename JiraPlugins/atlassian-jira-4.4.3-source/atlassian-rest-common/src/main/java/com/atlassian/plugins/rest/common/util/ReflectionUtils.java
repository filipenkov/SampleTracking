package com.atlassian.plugins.rest.common.util;

import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Arrays;

/**
 * A class to simplify some reflection calls.
 */
public class ReflectionUtils
{
    /**
     * Gets the value of the {@link Field field} for the given object. It will change the accessibility of the field if necessary.
     * Setting it back to its original value at the end of the method call.
     * @param field the field to read from
     * @param object the object to read the field from
     * @return the value of the field.
     */
    public static Object getFieldValue(Field field, Object object)
    {
        final boolean accessible = field.isAccessible();
        try
        {
            if (!accessible)
            {
                field.setAccessible(true);
            }
            return field.get(object);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Could not access '" + field + "' from '" + object + "'", e);
        }
        finally
        {
            if (!accessible)
            {
                field.setAccessible(false);
            }
        }
    }

    /**
     * Sets the value of the {@link Field field} for the given object. It will change the accessibility of the field if necessary.
     * Setting it back to its original value at the end of the method call.
     * @param field the field to set the value of
     * @param object the object to for which to set the field value.
     * @param value the new value to be set to the field of object.
     */
    public static void setFieldValue(Field field, Object object, Object value)
    {
        final boolean accessible = field.isAccessible();
        try
        {
            if (!accessible)
            {
                field.setAccessible(true);
            }
            field.set(object, value);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Could not access '" + field + "' from '" + object + "'", e);
        }
        finally
        {
            if (!accessible)
            {
                field.setAccessible(false);
            }
        }
    }

    /**
     * Returns the result of running {@link Class#getDeclaredFields()} on the
     * supplied class, as well as all its super types. Fields are ordered in
     * ascending hierarchy order (subclasses first).
     *
     * @since v1.0.4
     * @param clazz
     * @return  all of the class's fields (including inherited fields).
     */
    public static List<Field> getDeclaredFields(Class clazz)
    {
        if (clazz == null)
        {
            return Lists.newArrayList();
        }
        else
        {
            final List<Field> superFields = getDeclaredFields(clazz.getSuperclass());
            superFields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));
            return superFields;
        }
    }
}
