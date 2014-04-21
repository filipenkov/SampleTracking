package com.atlassian.jira.collector.plugin.transformer;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.gzipfilter.GzipFilter;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Replaces window.top with window to avoid x-domain javascript errors.
 */
public class WebResourceFixererUpper extends AbstractHttpFilter
{
    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException
    {
        final GenericResponseWrapper wrapper = new GenericResponseWrapper(response);

        //this is rather dodgy but basically collector resources can't be gziped since the search and replace wont
        //work for window.top.  Theoretically this filter could be inserted in the filterchain after the gzip filter
        //so that the content isn't gzipped yet, however this also doesn't work since then the URL match can't
        //be restricted to collector-resources since the web resource URL rewrite filter will muck up the URLs. ARGH!
        request.setAttribute(GzipFilter.class.getName() + "_already_filtered", Boolean.TRUE);
        
        filterChain.doFilter(request, wrapper);

        final String encoding = response.getCharacterEncoding();
        final String resource = new String(wrapper.getData(), encoding);
        final ServletOutputStream out = response.getOutputStream();
        byte[] output = resource.replaceAll("window\\.top", "window").getBytes(encoding);

        out.write(output);
        out.flush();
    }

    static class GenericResponseWrapper extends HttpServletResponseWrapper
    {
        private ByteArrayOutputStream output;

        public GenericResponseWrapper(HttpServletResponse response)
        {
            super(response);
            output = new ByteArrayOutputStream();
        }

        public byte[] getData()
        {
            return output.toByteArray();
        }

        @Override
        public ServletOutputStream getOutputStream()
        {
            return new FilterServletOutputStream(output);
        }

        @Override
        public void setContentLength(int length) {
            //noop
        }

        @Override
        public void setHeader(final String name, final String value)
        {
            if(StringUtils.equalsIgnoreCase("content-length", name)) {
                return;
            }
            super.setHeader(name, value);
        }

        @Override
        public PrintWriter getWriter() throws UnsupportedEncodingException
        {
            OutputStreamWriter out = new OutputStreamWriter(getOutputStream(), getCharacterEncoding());
            return new PrintWriter(out, true);
        }
    }

    static class FilterServletOutputStream extends ServletOutputStream
    {
        private OutputStream stream;

        public FilterServletOutputStream(OutputStream output)
        {
            stream = output;
        }

        @Override
        public void flush() throws IOException
        {
            stream.flush();
        }

        @Override
        public void close() throws IOException
        {
            stream.close();
        }

        @Override
        public void write(int b) throws IOException
        {
            stream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException
        {
            stream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException
        {
            stream.write(b, off, len);
        }

    }

}
