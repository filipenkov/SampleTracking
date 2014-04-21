package com.atlassian.security.auth.trustedapps;

import org.apache.commons.httpclient.ProxyHost;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;

/**
 * Chooses a proxy host to use for a given URI. Note the use of 'Host' in the name of this class; {@link ProxyHost} is
 * a type in the {@link org.apache.commons.httpclient} package; it is not the same thing as {@link Proxy}, which is
 * part of the {@link java.net} package.
 * <p>
 * This class was created in order that trusted application links can be established even when proxy servers sit in
 * between the applications. The new HttpClient implementation in the Apache HttpComponents library
 * (aka HttpClient 4.x) does this 'automatically'; however, we don't want to upgrade to that library yet
 * because other Atlassian projects currently depend on HttpClient3.x.
 * <p>
 * This class is a temporary measure, and should be removed when the Platform is upgraded to use HttpClient 4.x.
 * <p>
 * For a summary of how proxy selector works, 
 * see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html">Java Networking and Proxies</a>.
 *
 * @since v2.5.2
 */
final class ProxyHostSelector
{
    /**
     * The proxy selector that provides this proxy host selector with a list of candidate proxies.
     * Never null.
     */
    private final ProxySelector proxySelector;

    /**
     * Returns a new proxy host selector that gets its candidate proxies from the default proxy selector.
     */
    static ProxyHostSelector withDefaultProxySelector()
    {
        return new ProxyHostSelector(ProxySelector.getDefault());
    }

    /**
     * Returns a new proxy host selector that gets its candidate proxies from the given proxy selector.
     */
    static ProxyHostSelector withProxySelector(ProxySelector p)
    {
        Null.not("Proxy selector", p);
        return new ProxyHostSelector(p);
    }

    /**
     * Constructs a proxy host selector with the given proxy selector.
     * <p>
     * This constructor is deliberately private to ensure that all client code uses the 
     * static factory methods to obtain new instances.
     * 
     * @param p the proxy selector that the new instance will use; must not be null.
     */
    private ProxyHostSelector(ProxySelector p)
    {
        Null.not("Proxy selector", p);
        this.proxySelector = p;
    }

    /**
     * Returns the proxy selector that supplies this proxy host selector
     * with a list of candidate proxies.
     * <p>
     * Client code must be careful not to mess around with the state of the returned
     * proxy selector; it's not a clone of the original, it <b>is</b> the original.
     *
     * @return a reference to the proxy selector
     */
    ProxySelector getProxySelector()
    {
        return proxySelector;
    }

    /**
     * This implementation chooses one proxy from the candidate proxies supplied by the proxy selector.
     */
    ProxyHost select(URI uri)
    {
        Null.not("URI", uri);
        Proxy p = chooseProxy(proxySelector.select(uri));
        return p.type() == Proxy.Type.HTTP ? proxyToProxyHost(p) : null;
    }

    /**
     * Converts the given proxy to a proxy host.
     *
     * @param p the proxy to convert, which must not be null.
     *          The address of the proxy must be an instance of {@link InetSocketAddress}.
     * @return a new proxy host
     * @throws RuntimeException if the address of the proxy is not an instance of {@code InetSocketAddress}
     */
    private static ProxyHost proxyToProxyHost(Proxy p)
    {
        // At this moment (20 March 2012), it is not possible to construct a proxy that is of type HTTP, and
        // which has an address that is *not* and instance of InetSocketAddress.
        InetSocketAddress isa = (InetSocketAddress) p.address();

        // assume default scheme (http)
        return new ProxyHost(getHost(isa), isa.getPort());
    }

    /**
     * Chooses the first non-SOCKS proxy from a list of candidate proxies. 
     * <p>
     * If there are only SOCKS proxies, {@link Proxy#NO_PROXY Proxy.NO_PROXY} is returned.
     *
     * @param candidates the list of proxies to choose from, never <code>null</code> or empty
     *
     * @return the chosen proxy, which will never be null.
     */
    private static Proxy chooseProxy(List<Proxy> candidates)
    {
        for (Proxy proxy: candidates)
        {
            switch (proxy.type())
            {
                case DIRECT:
                case HTTP:   return proxy;
                case SOCKS:  break;
            }
        }
        return Proxy.NO_PROXY;
    }

    /**
     * Obtains a host from an {@link InetSocketAddress}.
     *
     * @param isa the socket address
     *
     * @return a host string, either as a symbolic name or as a literal IP address string
     */
    private static String getHost(InetSocketAddress isa)
    {
        return isa.isUnresolved() ? isa.getHostName() : isa.getAddress().getHostAddress();
    }
}
