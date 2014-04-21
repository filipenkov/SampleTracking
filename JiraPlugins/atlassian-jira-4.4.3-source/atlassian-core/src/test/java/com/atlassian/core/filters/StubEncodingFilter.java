package com.atlassian.core.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

public class StubEncodingFilter extends AbstractEncodingFilter
{
    private String encoding = "UTF-8";
    private String contentType = "text/plain";

    protected String getEncoding()
    {
        return encoding;
    }

    protected String getContentType()
    {
        return contentType;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * If you want to omit the filterChain, this class will substitute a no-op instance.
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException
    {
        super.doFilter(servletRequest, servletResponse, NoOpFilterChain.getInstance());
    }
}
