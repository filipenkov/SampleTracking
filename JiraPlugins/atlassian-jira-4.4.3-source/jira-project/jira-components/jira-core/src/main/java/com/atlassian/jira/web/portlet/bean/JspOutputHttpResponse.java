package com.atlassian.jira.web.portlet.bean;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class JspOutputHttpResponse implements HttpServletResponse
{
    private StringWriter out = new StringWriter();

    private PrintWriter writer = new PrintWriter(out);

    private HttpServletResponse realResponse;
    private String contentType;

    public JspOutputHttpResponse(HttpServletResponse realResponse)
    {
        this.realResponse = realResponse;
    }


    public String getCharacterEncoding()
    {
        return "utf-8";
    }

    public String getContentType()
    {
        return this.contentType;
    }

    public PrintWriter getWriter() throws IOException
    {
        return writer;
    }

    public void setCharacterEncoding(final String s)
    {

    }

    public StringWriter getStringWriter()
    {
        return out;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        return null;
    }

    public void setContentLength(int i)
    {

    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void setBufferSize(int i)
    {

    }

    public int getBufferSize()
    {
        return 0;
    }

    public void flushBuffer() throws IOException
    {

    }

    public void resetBuffer()
    {

    }

    public boolean isCommitted()
    {
        return false;
    }

    public void reset()
    {

    }

    public void setLocale(Locale locale)
    {

    }

    public Locale getLocale()
    {
        return null;
    }

    public void addCookie(Cookie cookie)
    {

    }

    public boolean containsHeader(String string)
    {
        return false;
    }


    public String encodeURL(String string)
    {
        return realResponse.encodeURL(string);
    }

    public String encodeRedirectURL(String string)
    {
        return realResponse.encodeRedirectURL(string);
    }

    public String encodeUrl(String string)
    {
        return null;
    }

    public String encodeRedirectUrl(String string)
    {
        return null;
    }

    public void sendError(int i, String string) throws IOException
    {

    }

    public void sendError(int i) throws IOException
    {

    }

    public void sendRedirect(String string) throws IOException
    {

    }

    public void setDateHeader(String string, long l)
    {

    }

    public void addDateHeader(String string, long l)
    {

    }

    public void setHeader(String string, String string1)
    {

    }

    public void addHeader(String string, String string1)
    {

    }

    public void setIntHeader(String string, int i)
    {

    }

    public void addIntHeader(String string, int i)
    {

    }

    public void setStatus(int i)
    {

    }

    public void setStatus(int i, String string)
    {

    }

}
