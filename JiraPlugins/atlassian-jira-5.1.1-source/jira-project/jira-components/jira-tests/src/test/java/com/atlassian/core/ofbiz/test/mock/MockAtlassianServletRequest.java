package com.atlassian.core.ofbiz.test.mock;

import com.mockobjects.servlet.MockHttpServletRequest;

import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An extension of the MockHttpServletRequest which doesn't error when cookies are queried, and maintains a map
 * of request attributes (unlike MockHttpServletRequest which requires you to set them up in advance).
 */
public class MockAtlassianServletRequest extends MockHttpServletRequest {
    Map requestAttributes = new HashMap();
    private String scheme;
    private String serverName;
    private int serverPort;
    private String contextPath;

    public Object getAttribute(String s) {
        return requestAttributes.get(s);
    }

    public void setAttribute(String s, Object o) {
        requestAttributes.put(s, o);
    }

    public Locale getLocale() {
        return null;
    }

    @Override
    public int getRemotePort()
    {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getLocalName()
    {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getLocalAddr()
    {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public int getLocalPort()
    {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
