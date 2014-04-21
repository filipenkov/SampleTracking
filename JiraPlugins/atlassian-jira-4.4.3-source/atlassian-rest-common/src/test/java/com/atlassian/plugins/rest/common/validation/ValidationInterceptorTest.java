package com.atlassian.plugins.rest.common.validation;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.sal.api.message.I18nResolver;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.MessageInterpolator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationInterceptorTest
{
    private I18nResolver resolver;
    private ValidationInterceptor interceptor;
    private MethodInvocation methodInvocation;
    private AbstractResourceMethod method;
    private ArgumentCaptor<Response> responseCaptor;
    private HttpResponseContext response;

    @Before
    public void setUp()
    {
        resolver = mock(I18nResolver.class);
        interceptor = new ValidationInterceptor(resolver);
        methodInvocation = mock(MethodInvocation.class);
        HttpContext httpContext = mock(HttpContext.class);
        response = mock(HttpResponseContext.class);
        method = mock(AbstractResourceMethod.class);


        when(httpContext.getResponse()).thenReturn(response);
        when(methodInvocation.getHttpContext()).thenReturn(httpContext);
        when(methodInvocation.getMethod()).thenReturn(method);

        responseCaptor = ArgumentCaptor.forClass(Response.class);
    }

    @Test
    public void testInterceptPass() throws IllegalAccessException, InvocationTargetException
    {
        ValidatableObject obj = new ValidatableObject("jim");

        when(methodInvocation.getParameters()).thenReturn(new Object[] {obj});
        Parameter parameter = mock(Parameter.class);
        when(parameter.getSource()).thenReturn(Parameter.Source.ENTITY);
        when(method.getParameters()).thenReturn(Collections.<Parameter>singletonList(parameter));

        interceptor.intercept(methodInvocation);
        verify(methodInvocation, times(1)).invoke();

        verify(response, never()).getEntity();
    }

    @Test
    public void testInterceptPassNoAnnotations() throws IllegalAccessException, InvocationTargetException
    {
        when(methodInvocation.getParameters()).thenReturn(new Object[] {new Object()});
        Parameter parameter = mock(Parameter.class);
        when(parameter.getSource()).thenReturn(Parameter.Source.ENTITY);
        when(method.getParameters()).thenReturn(Collections.<Parameter>singletonList(parameter));

        interceptor.intercept(methodInvocation);

        verify(response, never()).getEntity();
    }

    @Test
    public void testInterceptPassParametersButNoEntity() throws IllegalAccessException, InvocationTargetException
    {
        when(methodInvocation.getParameters()).thenReturn(new Object[] {new Object()});
        Parameter parameter = mock(Parameter.class);
        when(parameter.getSource()).thenReturn(Parameter.Source.PATH);
        when(method.getParameters()).thenReturn(Collections.<Parameter>singletonList(parameter));

        interceptor.intercept(methodInvocation);

        verify(response, never()).getEntity();
    }

    @Test
    public void testInterceptPassNoEntityParameter() throws IllegalAccessException, InvocationTargetException
    {
        when(methodInvocation.getParameters()).thenReturn(new Object[0]);
        when(method.getParameters()).thenReturn(Collections.<Parameter>emptyList());

        interceptor.intercept(methodInvocation);

        verify(response, never()).getEntity();
    }

    @Test
    public void testInterceptFail() throws IllegalAccessException, InvocationTargetException
    {
        when(resolver.getText("notnull")).thenReturn("Not Null");
        ValidatableObject obj = new ValidatableObject(null);

        when(methodInvocation.getParameters()).thenReturn(new Object[] {obj});
        Parameter parameter = mock(Parameter.class);
        when(parameter.getSource()).thenReturn(Parameter.Source.ENTITY);
        when(method.getParameters()).thenReturn(Collections.<Parameter>singletonList(parameter));

        interceptor.intercept(methodInvocation);
        verify(methodInvocation, never()).invoke();
        verify(response).setResponse(responseCaptor.capture());

        assertNotNull(responseCaptor.getValue());
        assertEquals(400, responseCaptor.getValue().getStatus());

        Object entity = responseCaptor.getValue().getEntity();
        assertTrue(entity instanceof ValidationErrors);
        ValidationErrors errors = (ValidationErrors) entity;
        assertEquals(1, errors.getErrors().size());
        assertEquals("Not Null", errors.getErrors().get(0).getMessage());
    }

    @Test
    public void testInterceptFailWithCustomMessageInterpolator() throws IllegalAccessException, InvocationTargetException
    {
        MessageInterpolator messageInterpolator = mock(MessageInterpolator.class);
        when(messageInterpolator.interpolate(eq("notnull"), (MessageInterpolator.Context) anyObject())).thenReturn("Bar");
        interceptor = new ValidationInterceptor(messageInterpolator);

        ValidatableObject obj = new ValidatableObject(null);

        when(methodInvocation.getParameters()).thenReturn(new Object[] {obj});
        Parameter parameter = mock(Parameter.class);
        when(parameter.getSource()).thenReturn(Parameter.Source.ENTITY);
        when(method.getParameters()).thenReturn(Collections.<Parameter>singletonList(parameter));

        interceptor.intercept(methodInvocation);
        verify(methodInvocation, never()).invoke();
        verify(response).setResponse(responseCaptor.capture());

        assertNotNull(responseCaptor.getValue());
        assertEquals(400, responseCaptor.getValue().getStatus());

        Object entity = responseCaptor.getValue().getEntity();
        assertTrue(entity instanceof ValidationErrors);
        ValidationErrors errors = (ValidationErrors) entity;
        assertEquals(1, errors.getErrors().size());
        assertEquals("Bar", errors.getErrors().get(0).getMessage());
    }

    static class ValidatableObject
    {
        @NotNull(message = "notnull")
        @Size(min = 2, max = 10, message = "size")
        private String name;

        public ValidatableObject(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }
}
