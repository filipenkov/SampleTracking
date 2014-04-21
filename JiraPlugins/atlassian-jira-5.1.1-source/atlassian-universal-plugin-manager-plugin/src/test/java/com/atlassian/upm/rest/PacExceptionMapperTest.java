package com.atlassian.upm.rest;

import com.atlassian.plugins.PacException;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PacExceptionMapperTest
{
    private PacExceptionMapper mapper;
    
    @Mock RepresentationFactory representationFactory;
    @Mock PacException exception;
    
    @Before
    public void setup()
    {
        mapper = new PacExceptionMapper(representationFactory);
    }
    
    @Test
    public void test404()
    {
        testStatusCode(404);
    }
    
    @Test
    public void test500()
    {
        testStatusCode(500);
    }
    
    @Test
    public void test502()
    {
        testStatusCode(502);
    }
    
    @Test
    public void testNonNumeric()
    {
        when(exception.getMessage()).thenReturn("not a number");
        assertThat(mapper.toResponse(exception).getStatus(), is(equalTo(502)));
    }
    
    private void testStatusCode(int statusCode)
    {
        when(exception.getMessage()).thenReturn(Integer.toString(statusCode));
        assertThat(mapper.toResponse(exception).getStatus(), is(equalTo(statusCode)));
    }
}
