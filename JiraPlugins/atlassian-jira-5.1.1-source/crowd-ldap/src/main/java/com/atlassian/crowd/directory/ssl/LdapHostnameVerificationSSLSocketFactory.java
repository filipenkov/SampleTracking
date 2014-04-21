package com.atlassian.crowd.directory.ssl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>This SocketFactory needs to call
 * {@link sun.security.ssl.SSLSocketFactoryImpl#trySetHostnameVerification} so it goes out of its way to check for the
 * <code>sun.security.ssl</code> implementation of the security classes.</p>
 * <p>The Mac OS X packaging uses <code>com.sun.net.ssl.internal.ssl</code>, rather than <code>sun.security</code>,
 * so reflection is used to work with both.</p>
 */
public class LdapHostnameVerificationSSLSocketFactory extends SocketFactory
{
    private static final Logger log = LoggerFactory.getLogger(LdapHostnameVerificationSSLSocketFactory.class);

    private static final Class<?>[] METHOD_ARG_TYPES = {String.class};
    private static final String UNABLE_TO_SET_MESSAGE = "Unable to set hostname verification on SSLSocket";

    private final SSLSocketFactory sf;

    private LdapHostnameVerificationSSLSocketFactory() throws NoSuchAlgorithmException
    {
        this.sf = SSLContext.getDefault().getSocketFactory();
        if (!isInSunSslImplementationPackage(sf.getClass(), "SSLSocketFactoryImpl"))
        {
            throw new RuntimeException("Unexpected SSLSocketFactory implementation: " + sf.getClass().getName());
        }
    }

    public static synchronized SocketFactory getDefault()
    {
        log.debug("Name checking SSLSocketFactory created");
        try
        {
            return new LdapHostnameVerificationSSLSocketFactory();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * The Mac OS X packaging uses <code>com.sun.net.ssl.internal.ssl</code>, rather than <code>sun.security.ssl</code>,
     * so this method checks both.
     */
    public static boolean isInSunSslImplementationPackage(Class<?> c, String expectedName)
    {
        String name = c.getName();
        return (name.equals("sun.security.ssl." + expectedName) || name.equals("com.sun.net.ssl.internal.ssl." + expectedName));
    }

    /**
     * Accept a <code>sun.security.ssl.SSLSocketImpl</code> or a <code>com.sun.net.ssl.internal.ssl.SSLSocketImpl</code>
     * and invoke <code>trySetHostnameVerification("ldap")</code> through reflection.
     */
    static void makeUseLdapVerification(Socket s)
    {
        Class<? extends Socket> c = s.getClass();
        if (!isInSunSslImplementationPackage(s.getClass(), "SSLSocketImpl"))
        {
            throw new IllegalArgumentException("Unexpected SSLSocket implementation: " + c.getName());
        }

        try
        {
            Method m = c.getMethod("trySetHostnameVerification", METHOD_ARG_TYPES);
            m.invoke(s, "ldap");
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException(UNABLE_TO_SET_MESSAGE, e);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(UNABLE_TO_SET_MESSAGE, e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(UNABLE_TO_SET_MESSAGE, e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(UNABLE_TO_SET_MESSAGE, e);
        }
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException
    {
        log.warn("Creating socket to " + address);
        Socket s = sf.createSocket(address, port, localAddress, localPort);
        makeUseLdapVerification(s);
        return s;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException
    {
        log.debug("Creating socket to " + host);
        Socket s = sf.createSocket(host, port);
        makeUseLdapVerification(s);
        return s;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException
    {
        log.debug("Creating socket to " + host);
        Socket s = sf.createSocket(host, port);
        makeUseLdapVerification(s);
        return s;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
            UnknownHostException
    {
        log.debug("Creating socket to " + host);
        Socket s = sf.createSocket(host, port, localHost, localPort);
        makeUseLdapVerification(s);
        return s;
    }

    @Override
    public Socket createSocket() throws IOException
    {
        log.debug("Creating disconnected socket");
        Socket s = sf.createSocket();
        makeUseLdapVerification(s);
        return s;
    }
}
