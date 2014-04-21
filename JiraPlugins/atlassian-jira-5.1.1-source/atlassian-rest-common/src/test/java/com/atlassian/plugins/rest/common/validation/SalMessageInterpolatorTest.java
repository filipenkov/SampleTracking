package com.atlassian.plugins.rest.common.validation;

import com.atlassian.sal.api.message.I18nResolver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.validation.MessageInterpolator;
import javax.validation.metadata.ConstraintDescriptor;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SalMessageInterpolatorTest
{
    private I18nResolver resolver;
    private SalMessageInterpolator salMessageInterpolator;
    private MessageInterpolator.Context context;

    @Before
    public void setUp()
    {
        resolver = mock(I18nResolver.class);
        salMessageInterpolator = new SalMessageInterpolator(resolver);
        context = mock(MessageInterpolator.Context.class);
    }

    @Test
    public void testInterpolate()
    {
        when(resolver.getText("foo")).thenReturn("bar");
        assertEquals("bar", salMessageInterpolator.interpolate("foo", context));
    }

    @Test
    public void testInterpolateMissing()
    {
        when(resolver.getText("foo")).thenReturn(null);
        assertEquals("foo", salMessageInterpolator.interpolate("foo", context));
    }

    @Test
    @Ignore("Enable once SAL is moved to 2.1 and we can interpolate with a map of arguments")
    public void testInterpolateWithParams()
    {
        when(resolver.getText("foo")).thenReturn("bar");

        ConstraintDescriptor constraintDescriptor = mock(ConstraintDescriptor.class);
        when(constraintDescriptor.getAttributes()).thenReturn(Collections.singletonMap("foo", "bar"));
        when(context.getConstraintDescriptor()).thenReturn(constraintDescriptor);
        assertEquals("name: bar", salMessageInterpolator.interpolate("name: {foo}", context));
    }

}
