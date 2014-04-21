package com.atlassian.plugins.rest.common.expand;

import static org.junit.Assert.*;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;

/**
 * Testing {@link DefaultExpandParameter}
 */
public class DefaultExpandParameterTest
{
    @Test
    public void testExpandParameterWithNull()
    {
        final ExpandParameter parameter = new DefaultExpandParameter(null);
        assertTrue(parameter.isEmpty());
    }

    @Test
    public void testExpandParameterWithEmptyString()
    {
        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton(""));
        assertTrue(parameter.isEmpty());
    }

    @Test
    public void testExpandParameterWithValidString1()
    {
        final String parameterValue = "value";
        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton(parameterValue));

        assertFalse(parameter.isEmpty());
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(parameterValue)).isEmpty());
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(parameterValue)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation("shouldnot")));
    }

    @Test
    public void testExpandParameterWithValidString2()
    {
        final String value1 = "value1";
        final String value2 = "value2";

        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton(value1 + "," + value2));

        assertFalse(parameter.isEmpty());
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value1)).isEmpty());
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value2)).isEmpty());
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value1)));
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value2)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation("shouldnot")));
    }

    @Test
    public void testExpandParameterWithValidString3()
    {
        final String value1 = "value1";
        final String value2 = "value2";

        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton(value1 + "." + value2));

        assertFalse(parameter.isEmpty());
        assertFalse(parameter.getExpandParameter(getExpandableAnnotation(value1)).isEmpty());
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value2)).isEmpty());
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value1)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation(value2)));
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value1)).shouldExpand(getExpandableAnnotation(value2)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation("shouldnot")));
    }

    @Test
    public void testExpandParameterWithWilcard1()
    {
        final String value1 = "value1";
        final String value2 = "value2";

        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton("*" + "." + value2));

        assertFalse(parameter.isEmpty());
        assertFalse(parameter.getExpandParameter(getExpandableAnnotation(value1)).isEmpty());
        assertFalse(parameter.getExpandParameter(getExpandableAnnotation(value2)).isEmpty());
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value1)));
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value2)));
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value1)).shouldExpand(getExpandableAnnotation(value2)));
        assertTrue(parameter.shouldExpand(getExpandableAnnotation("should")));
    }

    @Test
    public void testExpandParameterWithWilcard2()
    {
        final String value1 = "value1";
        final String value2 = "value2";

        final ExpandParameter parameter = new DefaultExpandParameter(Collections.singleton(value1 + "." + "*"));

        assertFalse(parameter.isEmpty());
        assertFalse(parameter.getExpandParameter(getExpandableAnnotation(value1)).isEmpty());
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value2)).isEmpty());
        assertTrue(parameter.shouldExpand(getExpandableAnnotation(value1)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation(value2)));
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value1)).shouldExpand(getExpandableAnnotation(value2)));
        assertFalse(parameter.shouldExpand(getExpandableAnnotation("shouldnot")));
        assertTrue(parameter.getExpandParameter(getExpandableAnnotation(value1)).shouldExpand(getExpandableAnnotation("should")));
    }

    private Expandable getExpandableAnnotation(final String parameterValue)
    {
        return new Expandable()
        {
            public String value()
            {
                return parameterValue;
            }

            public Class<? extends Annotation> annotationType()
            {
                return Expandable.class;
            }
        };
    }
}
