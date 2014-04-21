package com.atlassian.crowd.directory.ssl;

import java.lang.reflect.Method;
import java.net.Socket;

import javax.net.SocketFactory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LdapHostnameVerificationSSLSocketFactoryTest
{
    @Test
    public void constructsInstanceOfFactoryThroughStaticMethodReflection() throws Exception
    {
        String factory = LdapHostnameVerificationSSLSocketFactory.class.getName();

        Class<?> c = getClass().getClassLoader().loadClass(factory);
        Method m = c.getMethod("getDefault");

        SocketFactory fac = (SocketFactory) m.invoke(null);
        assertNotNull(fac);
    }

    @Test
    public void disconnectedSocketsCreated() throws Exception
    {
        Socket s = LdapHostnameVerificationSSLSocketFactory.getDefault().createSocket();
        assertTrue(LdapHostnameVerificationSSLSocketFactory.isInSunSslImplementationPackage(s.getClass(), "SSLSocketImpl"));

        /* Invoke with reflection to allow for regular and Mac OS packages */
        Method m = s.getClass().getMethod("getHostnameVerification");
        assertEquals("ldap", m.invoke(s));
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsWhenSocketIsNotDefaultSslImpl() throws Exception
    {
        Socket socket = mock(Socket.class);
        LdapHostnameVerificationSSLSocketFactory.makeUseLdapVerification(socket);
    }

    @Test
    public void testForSunSslImplementationPackageFailsForNonSslClass()
    {
        assertFalse(LdapHostnameVerificationSSLSocketFactory.isInSunSslImplementationPackage(
                Object.class, "Object"));
    }
}
