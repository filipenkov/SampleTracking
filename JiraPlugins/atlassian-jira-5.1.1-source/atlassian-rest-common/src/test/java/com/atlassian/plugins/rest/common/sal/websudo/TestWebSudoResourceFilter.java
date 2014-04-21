package com.atlassian.plugins.rest.common.sal.websudo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.atlassian.plugins.rest.common.sal.websudo.nopackageprotection.ClassNoAnnotation;
import com.atlassian.plugins.rest.common.sal.websudo.nopackageprotection.ClassProtectedByClassAnnotation;
import com.atlassian.plugins.rest.common.sal.websudo.nopackageprotection.MethodProtectedByMethodAnnotation;
import com.atlassian.plugins.rest.common.sal.websudo.packageannotationnotrequired.MethodOverridesPackage;
import com.atlassian.plugins.rest.common.sal.websudo.packageannotationrequired.ClassProtectedByPackageAnnotation;
import com.atlassian.plugins.rest.common.sal.websudo.packageannotationrequired.ClassWebSudoNotRequiredAnnotation;
import com.atlassian.plugins.rest.common.sal.websudo.packageannotationrequired.MethodWebSudoNotRequiredAnnotation;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.PathValue;
import com.sun.jersey.spi.container.ContainerRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.annotation.Annotation;

/*
 * Test the WebSudoResourceFilter
 * <p>
 *
 * Annotations on more specific types override annotations on less specific element types.
 *
 *      Package > Class > Method
 *
 * E.g. A method with a @WebSudoNotRequired annotation vetoes the @WebSudoRequired annotation on a class
 */
public final class TestWebSudoResourceFilter
{
    private static final String METHOD_ONE = "aMethod";
    private static final String METHOD_TWO = "bMethod";

    private WebSudoResourceFilter webSudoResourceFilter;

    @Mock
    private WebSudoResourceContext webSudoResourceContext;

    @Mock
    private ContainerRequest containerRequest;

    @Before
    public void setUp()
    {
        initMocks(this);

        when(webSudoResourceContext.shouldEnforceWebSudoProtection()).thenReturn(true);
    }

    @After
    public void teardown()
    {
        webSudoResourceFilter = null;
        webSudoResourceContext = null;
        containerRequest = null;
    }

    @Test
    public void filterPassesWithWebSudoProtectionOn()
    {
        setupResourceFilter(ClassProtectedByPackageAnnotation.class, METHOD_ONE);
        when(webSudoResourceContext.shouldEnforceWebSudoProtection()).thenReturn(false);
        assertNotNull(webSudoResourceFilter.filter(containerRequest));
    }

    @Test
    public void filterPassesWithWebSudoProtectionOnNoAnnotations()
    {
        setupResourceFilter(ClassNoAnnotation.class, METHOD_ONE);
        when(webSudoResourceContext.shouldEnforceWebSudoProtection()).thenReturn(false);
        assertNotNull(webSudoResourceFilter.filter(containerRequest));
    }

    @Test
    public void filterPasses()
    {
        setupResourceFilter(MethodProtectedByMethodAnnotation.class, METHOD_ONE);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test
    public void filterPassesWithWebSudoNotRequiredClassAnnotation()
    {
        setupResourceFilter(ClassWebSudoNotRequiredAnnotation.class, METHOD_ONE);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test
    public void filterPassesWithWebSudoNotRequiredMethodAnnotation()
    {
        setupResourceFilter(MethodWebSudoNotRequiredAnnotation.class, METHOD_ONE);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test(expected = WebSudoRequiredException.class)
    public void filterRejectedWithPackageAnnotation()
    {
        setupResourceFilter(ClassProtectedByPackageAnnotation.class, METHOD_ONE);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test(expected = WebSudoRequiredException.class)
    public void filterRejectedWithClassAnnotation()
    {
        setupResourceFilter(ClassProtectedByClassAnnotation.class, METHOD_ONE);
        assertNotNull(webSudoResourceFilter.filter(containerRequest));
    }

    @Test(expected = WebSudoRequiredException.class)
    public void filterRejectedWithMethodAnnotation()
    {
        setupResourceFilter(MethodProtectedByMethodAnnotation.class, METHOD_TWO);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test(expected = WebSudoRequiredException.class)
    public void filterRejectedMethodAnnotationOverridesClassAnnotation()
    {
        setupResourceFilter(ClassWebSudoNotRequiredAnnotation.class, METHOD_TWO);
        webSudoResourceFilter.filter(containerRequest);
    }

    @Test(expected = WebSudoRequiredException.class)
    public void filterRejectedMethodAnnotationOverridesPackageAnnotation()
    {
        setupResourceFilter(MethodOverridesPackage.class, METHOD_ONE);
        webSudoResourceFilter.filter(containerRequest);
    }

    private void setupResourceFilter(final Class clazz, final String methodName, final Annotation... annotations)
    {
        try
        {
            AbstractMethod m = new AbstractResourceMethod(new AbstractResource(clazz, new PathValue("/")),
                    clazz.getMethod(methodName), clazz, clazz, "test", annotations);
            webSudoResourceFilter = new WebSudoResourceFilter(m, webSudoResourceContext);
        } catch (NoSuchMethodException nsme)
        {
            fail("Test setup failed due to " + nsme.getMessage());
        }
    }
}