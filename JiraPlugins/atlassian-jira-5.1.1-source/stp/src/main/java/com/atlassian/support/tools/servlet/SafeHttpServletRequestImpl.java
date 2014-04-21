package com.atlassian.support.tools.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

public class SafeHttpServletRequestImpl implements SafeHttpServletRequest {
	private final HttpServletRequest request;
	
	public SafeHttpServletRequestImpl(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getAuthType() {
		return request.getAuthType();
	}

	@Override
	public Cookie[] getCookies() {
		return request.getCookies();
	}

	@Override
	public long getDateHeader(String name) {
		return request.getDateHeader(name);
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public Enumeration getHeaders(String name) {
		return request.getHeaders(name);
	}

	@Override
	public Enumeration getHeaderNames() {
		return request.getHeaderNames();
	}

	@Override
	public int getIntHeader(String name) {
		return request.getIntHeader(name);
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public String getPathInfo() {
		return request.getPathInfo();
	}

	@Override
	public String getPathTranslated() {
		return request.getPathTranslated();
	}

	@Override
	public String getContextPath() {
		return request.getContextPath();
	}


	public Object getAttribute(String name) {
		return request.getAttribute(name);
	}

	public Enumeration getAttributeNames() {
		return request.getAttributeNames();
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		request.setCharacterEncoding(env);
	}

	public int getContentLength() {
		return request.getContentLength();
	}

	public String getContentType() {
		return request.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	public Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	public String getProtocol() {
		return request.getProtocol();
	}

	public String getScheme() {
		return request.getScheme();
	}

	public String getServerName() {
		return request.getServerName();
	}

	public String getQueryString() {
		return request.getQueryString();
	}

	public int getServerPort() {
		return request.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return request.getReader();
	}

	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	public boolean isUserInRole(String role) {
		return request.isUserInRole(role);
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return request.getRequestedSessionId();
	}

	public void setAttribute(String name, Object o) {
		request.setAttribute(name, o);
	}

	public String getRequestURI() {
		return request.getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return request.getRequestURL();
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}

	public HttpSession getSession() {
		return request.getSession();
	}

	public boolean isRequestedSessionIdValid() {
		return request.isRequestedSessionIdValid();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return request.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return request.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return request.isRequestedSessionIdFromUrl();
	}

	public void removeAttribute(String name) {
		request.removeAttribute(name);
	}

	@Override
	public Locale getLocale() {
		return request.getLocale();
	}

	@Override
	public Enumeration getLocales() {
		return request.getLocales();
	}

	@Override
	public boolean isSecure() {
		return request.isSecure();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return request.getRequestDispatcher(path);
	}

	@Override
	public String getRealPath(String path) {
		return request.getRealPath(path);
	}

	@Override
	public int getRemotePort() {
		return request.getRemotePort();
	}

	@Override
	public String getLocalName() {
		return request.getLocalName();
	}

	@Override
	public String getLocalAddr() {
		return request.getLocalAddr();
	}

	@Override
	public int getLocalPort() {
		return request.getLocalPort();
	}

	@Override
	public String getParameter(String name) {
		return StringEscapeUtils.escapeHtml(request.getParameter(name));
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] rawValues = request.getParameterValues(name);
		if (rawValues != null) {
			String[] cleanValues = new String[rawValues.length];
			for (int a = 0; a < rawValues.length; a++) {
				cleanValues[a] = StringEscapeUtils.escapeHtml(rawValues[a]);
			}
			
			return cleanValues;
		}
		
		return null;
	}

	@Override
	public Map getParameterMap() {
		Map rawMap = request.getParameterMap();
		
		if (rawMap != null) {
			Map cleanMap = new HashMap();
			for (Object key : rawMap.keySet()) {
				Object value = rawMap.get(key);
				cleanMap.put(key, value instanceof String ? StringEscapeUtils.escapeHtml((String) value) : value);
			}
			
			return cleanMap;
		}

		return null;
	}
}
