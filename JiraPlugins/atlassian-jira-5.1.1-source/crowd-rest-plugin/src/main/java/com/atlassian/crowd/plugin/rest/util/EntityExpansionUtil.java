package com.atlassian.crowd.plugin.rest.util;

import com.atlassian.plugins.rest.common.expand.*;
import com.atlassian.plugins.rest.common.expand.parameter.*;
import org.apache.commons.lang.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.http.*;
import javax.xml.bind.annotation.*;

/**
 * Utility class for entity expansion.
 *
 * @since v2.1
 */
public class EntityExpansionUtil
{
    /**
     * Name of the expand query parameter.
     */
    public final static String EXPAND_PARAM = "expand";

    private EntityExpansionUtil()
    {
        // prevent instantiation
    }

    /**
     * Returns whether a field of the specified entity class should be expanded.
     * <p/>
     * E.g. a client may perform a request to <tt>/user?expand=attributes</tt> which instructs the server to expand the
     * attributes field of the user entity.
     *
     * @param clazz the class of the field to check for expansion.
     * @param fieldName name of the field to check for expansion.
     * @param request HttpServletRequest to extract the expand query param from.
     * @return true if the field should be expanded, otherwise false.
     */
    public static boolean shouldExpandField(Class clazz, final String fieldName, final HttpServletRequest request)
    {
        Validate.notNull(clazz);
        Validate.notNull(fieldName);
        Validate.notNull(request);

        final ExpandParameter expandParameter = getExpandParameter(request);
        return shouldExpandField(clazz, fieldName, expandParameter);
    }

    /**
     * Returns whether a field of the specified entity class should be expanded.
     *
     * @see {@link #shouldExpandField(Class, String, javax.servlet.http.HttpServletRequest)} to extract the expand query
     * parameter from a <tt>HttpServletRequest</tt>.
     * @param clazz the class of the field to check for expansion.
     * @param fieldName name of the field to check for expansion.
     * @param expandParameter ExpandParameter to check for expansion.
     * @return true if the field should be expanded, otherwise false.
     * @throws IllegalArgumentException if the field name could not be found in the class.
     */
    public static boolean shouldExpandField(Class clazz, final String fieldName, final ExpandParameter expandParameter)
    {
        Validate.notNull(clazz);
        Validate.notNull(fieldName);
        Validate.notNull(expandParameter);

        try
        {
            final Field attrField = clazz.getDeclaredField(fieldName);
            Expandable attrExpandable = getExpandable(attrField);
            return expandParameter.shouldExpand(attrExpandable);
        }
        catch (NoSuchFieldException e)
        {
            throw new IllegalArgumentException(String.format("Could not find field %s in class %s", fieldName, clazz.getCanonicalName()), e);
        }
    }

    /**
     * Returns a ExpandParameter representing the <tt>expand</tt> query parameter from an HttpServletRequest.
     *
     * @param request HttpServletRequest.
     * @return ExpandParameter representing the <tt>expand</tt> query parameter.
     */
    public static ExpandParameter getExpandParameter(final HttpServletRequest request)
    {
        Validate.notNull(request);

        String[] expandValues = request.getParameterValues(EXPAND_PARAM);
        return new DefaultExpandParameter(expandValues != null ? Arrays.asList(expandValues) : Collections.<String>emptyList());
    }

    /**
     * Returns the expandable annotation with the properly set value. The value is defined as the first valid point in the following list:
     * <ol>
     * <li>the value of the {@link Expandable} annotation if it is set</li>
     * <li>the name of an {@link javax.xml.bind.annotation.XmlElement} if the annotation is present on the field and its name is not {@code ##default}</li>
     * <li>the name of the field</li>
     * <ol>
     * @param field the field to look up the Expandable for
     * @return {@code null} if the field is null, {@code null} if the field doesn't have an expandable annotation,
     *         an expandable annotation with a properly set value.
     */
    private static Expandable getExpandable(final Field field)
    {
        if (field == null)
        {
            return null;
        }

        final Expandable expandable = field.getAnnotation(Expandable.class);
        if (expandable == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(expandable.value()))
        {
            return expandable;
        }

        final XmlElement xmlElement = field.getAnnotation(XmlElement.class);
        if (xmlElement != null && StringUtils.isNotEmpty(xmlElement.name()) && !StringUtils.equals("##default", xmlElement.name()))
        {
            return new ExpandableWithValue(xmlElement.name());
        }

        return new ExpandableWithValue(field.getName());
    }

    private static class ExpandableWithValue implements Expandable
    {
        private final String value;

        public ExpandableWithValue(String value)
        {
            this.value = value;
        }

        public String value()
        {
            return value;
        }

        public Class<? extends Annotation> annotationType()
        {
            return Expandable.class;
        }
    }

}
