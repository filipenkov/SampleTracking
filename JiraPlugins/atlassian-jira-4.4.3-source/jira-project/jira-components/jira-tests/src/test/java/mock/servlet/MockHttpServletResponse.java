package mock.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock implementation of HttpServletResponse
 *
 */
public class MockHttpServletResponse implements HttpServletResponse
{
    private ServletOutputStream servletOutputStream;
    private PrintWriter writer;

    private String contentType;

    public MockHttpServletResponse()
    {
    }

    public MockHttpServletResponse(final ServletOutputStream servletOutputStream)
    {
        this.servletOutputStream = servletOutputStream;
    }

    public MockHttpServletResponse(final PrintWriter writer)
    {
        this.writer = writer;
    }

    public void addCookie(final Cookie cookie)
    {
    }

    public boolean containsHeader(final String s)
    {
        return false;
    }

    public String encodeURL(final String s)
    {
        return null;
    }

    public String encodeRedirectURL(final String s)
    {
        return null;
    }

    public String encodeUrl(final String s)
    {
        return null;
    }

    public String encodeRedirectUrl(final String s)
    {
        return null;
    }

    public void sendError(final int i, final String s) throws IOException
    {
    }

    public void sendError(final int i) throws IOException
    {
    }

    public void sendRedirect(final String s) throws IOException
    {
    }

    public void setDateHeader(final String s, final long l)
    {
    }

    public void addDateHeader(final String s, final long l)
    {
    }

    public void setHeader(final String s, final String s1)
    {
    }

    public void addHeader(final String s, final String s1)
    {
    }

    public void setIntHeader(final String s, final int i)
    {
    }

    public void addIntHeader(final String s, final int i)
    {
    }

    public void setStatus(final int i)
    {
    }

    public void setStatus(final int i, final String s)
    {
    }

    public String getCharacterEncoding()
    {
        return null;
    }

    public String getContentType()
    {
        return contentType;
    }

    public ServletOutputStream getOutputStream() throws IOException
    {
        return servletOutputStream;
    }

    public PrintWriter getWriter() throws IOException
    {
        return writer;
    }

    public void setCharacterEncoding(final String s)
    {
    }

    public void setContentLength(final int i)
    {
    }

    public void setContentType(final String ct)
    {
        this.contentType = ct;
    }

    public void setBufferSize(final int i)
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

    public void setLocale(final Locale locale)
    {
    }

    public Locale getLocale()
    {
        return null;
    }
}
