package com.atlassian.gadgets.util;

import com.atlassian.sal.api.ApplicationProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class HttpTimeoutsProviderTest
{
    public static final String INVALID_TIMEOUT_STRING = "lala";
    public static final int VALID_TIMEOUT_VALUE = 3000;
    public static final int OTHER_VALID_TIMEOUT_VALUE = 7000;

    @Mock ApplicationProperties applicationProperties;

    private HttpTimeoutsProvider httpTimeoutsProvider;

    @Before
    public void setUp()
    {
        this.httpTimeoutsProvider = new HttpTimeoutsProvider(applicationProperties);
    }

    @After
    public void tearDown()
    {
        System.clearProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY);
        System.clearProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY);
    }

    @Test
    public void testGetDefaultConnectionTimeout()
    {
        assertEquals(HttpTimeoutsProvider.DEFAULT_CONNECT_TIMEOUT_MS, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetSystemPropertyConnectionTimeout()
    {
        System.setProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY, String.valueOf(VALID_TIMEOUT_VALUE));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetApplicationPropertyConnectionTimeout()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetConnectionTimeoutWhenBothSystemAndApplicationPropertiesSet()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        System.setProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY, String.valueOf(OTHER_VALID_TIMEOUT_VALUE));
        assertEquals(OTHER_VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetConnectionTimeoutWhenSystemPropertyIsInvalid()
    {
        System.setProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_CONNECT_TIMEOUT_MS, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetConnectionTimeoutWhenApplicationPropertyIsInvalid()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_CONNECT_TIMEOUT_MS, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetConnectionTimeoutWhenSystemPropertyIsInvalidButNotApplicationProperty()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        System.setProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetConnectionTimeoutWhenSystemPropertyAndApplicationPropertyIsInvalid()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(INVALID_TIMEOUT_STRING));
        System.setProperty(HttpTimeoutsProvider.CONNECTION_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_CONNECT_TIMEOUT_MS, httpTimeoutsProvider.getConnectionTimeout());
    }

    @Test
    public void testGetDefaultSocketTimeout()
    {
        assertEquals(HttpTimeoutsProvider.DEFAULT_SOCKET_TIMEOUT_MS, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSystemPropertySocketTimeout()
    {
        System.setProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY, String.valueOf(VALID_TIMEOUT_VALUE));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetApplicationPropertySocketTimeout()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSocketTimeoutWhenBothSystemAndApplicationPropertiesSet()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        System.setProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY, String.valueOf(OTHER_VALID_TIMEOUT_VALUE));
        assertEquals(OTHER_VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSocketTimeoutWhenSystemPropertyIsInvalid()
    {
        System.setProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_SOCKET_TIMEOUT_MS, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSocketTimeoutWhenApplicationPropertyIsInvalid()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_SOCKET_TIMEOUT_MS, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSocketTimeoutWhenSystemPropertyIsInvalidButNotApplicationProperty()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(VALID_TIMEOUT_VALUE));
        System.setProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(VALID_TIMEOUT_VALUE, httpTimeoutsProvider.getSocketTimeout());
    }

    @Test
    public void testGetSocketTimeoutWhenSystemPropertyAndApplicationPropertyIsInvalid()
    {
        when(applicationProperties.getPropertyValue(eq(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY)))
                .thenReturn(String.valueOf(INVALID_TIMEOUT_STRING));
        System.setProperty(HttpTimeoutsProvider.SOCKET_TIMEOUT_PROPERTY_KEY, String.valueOf(INVALID_TIMEOUT_STRING));
        assertEquals(HttpTimeoutsProvider.DEFAULT_SOCKET_TIMEOUT_MS, httpTimeoutsProvider.getSocketTimeout());
    }

}
