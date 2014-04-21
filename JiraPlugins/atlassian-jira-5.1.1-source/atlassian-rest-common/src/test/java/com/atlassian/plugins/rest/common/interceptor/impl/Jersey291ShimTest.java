package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static com.atlassian.plugins.rest.common.interceptor.impl.DispatchProviderHelper.invokeMethodWithInterceptors;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the compatibility shim for JRADEV-11989 / REST-206 / JERSEY-291.
 * To be removed after JIRA 6.0 ships.
 */
public class Jersey291ShimTest
{
    private static final String SYSTEM_PROPERTY = "com.atlassian.plugins.rest.shim.JERSEY-291";

    private static String originalSystemProperty;
    private static final Object[] ARRAY_WITH_NULL = { null };

    @BeforeClass
    public static void saveOriginalSystemProperty()
    {
        originalSystemProperty = System.getProperty(SYSTEM_PROPERTY);
    }

    @AfterClass
    public static void restoreOriginalSystemProperty()
    {
        initSystemProperty(originalSystemProperty);
    }

    private static void initSystemProperty(final String value)
    {
        if (value != null)
        {
            System.setProperty(SYSTEM_PROPERTY, value);
        }
        else
        {
            System.getProperties().remove(SYSTEM_PROPERTY);
        }
        DispatchProviderHelper.JERSEY_291_SHIM.set(null);
    }

    private AbstractResourceMethod abstractResourceMethod = mock(AbstractResourceMethod.class);

    //================================================================


    @Test
    public void testShimOffByDefault1() throws Exception
    {
        initSystemProperty(null);
        invokeAndCheckParameters("queryParam", emptyList());
    }

    @Test
    public void testShimOffByDefault2() throws Exception
    {
        initSystemProperty("hello");
        invokeAndCheckParameters("queryParam", emptyList());
    }

    @Test
    public void testEmptyListMapsToNull() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters(new Validator(ARRAY_WITH_NULL), "queryParam", emptyList());
    }

    @Test
    public void testEmptySetMapsToNull() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters(new Validator(ARRAY_WITH_NULL), "queryParam", emptySet());
    }

    @Test
    public void testEmptyTreeSetMapsToNull() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters(new Validator(ARRAY_WITH_NULL), "queryParam", new TreeSet<String>());
    }

    @Test
    public void testFormParamUnchanged() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters("formParam", emptyList());
    }

    @Test
    public void testUnannotatedUnchanged() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters("unannotated", emptyList());
    }

    @Test
    public void testNonEmptyCollectionUnchanged() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters("queryParam", singletonList("hello"));
    }

    @Test
    public void testNonCollectionUnchanged() throws Exception
    {
        initSystemProperty("true");
        invokeAndCheckParameters("queryParam", "");
    }


    //================================================================

    private void invokeAndCheckParameters(String annotationDonatingMethod, Object... inputParameters) throws Exception
    {
        invokeAndCheckParameters(new Validator(inputParameters), annotationDonatingMethod, inputParameters);

    }
    private void invokeAndCheckParameters(ResourceInterceptor validator, String annotationDonatingMethod, Object... inputParameters) throws Exception
    {
        when(abstractResourceMethod.getParameters()).thenReturn(parameters(annotationDonatingMethod));
        invokeMethodWithInterceptors(Arrays.asList(validator), abstractResourceMethod, null, null, inputParameters, null);
    }

    private List<Parameter> parameters(String exampleMethod) throws Exception
    {
        final Method m = ContainerClass.class.getMethod(exampleMethod, String.class);
        final Annotation[][] allParameterAnnotations = m.getParameterAnnotations();
        final List<Parameter> parameters = new ArrayList<Parameter>(allParameterAnnotations.length);
        for (Annotation[] thisParameterAnnotations : allParameterAnnotations)
        {
            parameters.add(new Parameter(thisParameterAnnotations, null, null, null, null, null));
        }
        return parameters;
    }

    // Annotation supply
    @SuppressWarnings("unused")
    public static class ContainerClass
    {
        public void unannotated(String x) {}
        public void queryParam(@QueryParam("foo") String x) {}
        public void formParam(@FormParam("baz") String x) {}
    }

    static class Validator implements ResourceInterceptor
    {
        private final Object[] expected;

        private Validator(final Object... expected)
        {
            this.expected = expected.clone();
        }

        public void intercept(final MethodInvocation invocation)
        {
            final Object[] got = invocation.getParameters();
            assertArrayEquals(expected, got);
        }
    }
}

