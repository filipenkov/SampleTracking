package mock.servlet;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.Cookie;

/**
 * Extension of the mockobjects MockHttpServletRequest with support for cookies & other
 * stuff.
 *
 * @since v4.2
 */
public class ExtendedMockHttpServletRequest extends com.mockobjects.servlet.MockHttpServletRequest
{
    private final List<Cookie> cookies = new ArrayList<Cookie>();

    private int remotePort;
    private int localPort;

    private String localName;
    private String localAddr;

    public ExtendedMockHttpServletRequest addCookie(String name, String value)
    {
        cookies.add(new Cookie(name, value));
        return this;
    }

    public ExtendedMockHttpServletRequest setUpRemotePort(int port)
    {
        this.remotePort = port;
        return this;
    }

    public ExtendedMockHttpServletRequest setUpLocalPort(int port)
    {
        this.localPort = port;
        return this;
    }

    public ExtendedMockHttpServletRequest setUpLocalName(String name)
    {
        this.localName = name;
        return this;
    }

    public ExtendedMockHttpServletRequest setUpLocalAddr(String addr)
    {
        this.localAddr = addr;
        return this;
    }

    @Override
    public Cookie[] getCookies()
    {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public String getLocalName()
    {
        return localName;
    }

    public String getLocalAddr()
    {
        return localAddr;
    }

    public int getLocalPort()
    {
        return localPort;
    }
}
