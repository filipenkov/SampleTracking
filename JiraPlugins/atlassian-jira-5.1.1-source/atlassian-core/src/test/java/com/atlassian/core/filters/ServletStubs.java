package com.atlassian.core.filters;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.MultiHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Collection;
import java.util.Arrays;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.test.util.JavaBeanMethodHandler;

public class ServletStubs
{
    public static Request getRequestInstance()
    {
        return (Request) DuckTypeProxy.getProxy(Request.class,
            Arrays.asList(new StubRequest()), new JavaBeanMethodHandler());
    }

    public static Response getResponseInstance()
    {
        return (Response) DuckTypeProxy.getProxy(Response.class,
            Arrays.asList(new StubResponse()), new JavaBeanMethodHandler());
    }

    public interface Request extends HttpServletRequest
    {
        /**
         * Sets the request URI which is returned by {@link #getRequestURI()}.
         */
        void setRequestURI(String requestUri);

        /**
         * Sets a parameter which is returned by {@link #getParameter(String)} and {@link #getParameterValues(String)}.
         */
        void setParameter(String name, String value);

        /**
         * Adds a parameter value which is returned by {@link #getParameter(String)} and {@link #getParameterValues(String)}.
         */
        void addParameter(String name, String value);

        /**
         * Sets the parameter map to be returned by {@link #getParameterMap()}.
         */
        void setParameterMap(HashMap<String, String[]> parameterMap);
    }

    /**
     * This adds methods to {@link HttpServletResponse} to get and set values you can't otherwise
     * change.
     */
    public interface Response extends HttpServletResponse
    {
        /**
         * @return the value set by {@link #setContentType(String)}, or null if it has not been set.
         */
        String getContentType();

        /**
         * Set the character encoding to be returned by {@link #getCharacterEncoding()}.
         */
        void setCharacterEncoding(String encoding);

        /**
         * Return the header value set by {@link #setHeader(String, String)}, or null if the header has not been set.
         * If {@link #addHeader(String, String)} has been called multiple times, only the first value is returned.
         */
        String getHeader(String headerName);

        /**
         * Return the header value set by {@link #setDateHeader(String, long)}, or -1 if the header has not been set.
         * If {@link #addDateHeader(String, long)} has been called multiple times, only the first value is returned.
         */
        long getDateHeader(String headerName);

        /**
         * Returns all the data written to the response's {@link #getWriter()} as a String.
         */
        String getOutput();
    }

    /**
     * Stores headers in a multi-map and writes output to a StringWriter.
     */
    private static class StubResponse
    {
        private StringWriter output = new StringWriter();
        private MultiMap headers = new MultiHashMap();

        public PrintWriter getWriter() throws IOException
        {
            return new PrintWriter(output);
        }

        public String getOutput()
        {
            return output.toString();
        }

        public void setDateHeader(String name, long date)
        {
            headers.remove(name);
            headers.put(name, date);
        }

        public void setHeader(String name, String value)
        {
            headers.remove(name);
            headers.put(name, value);
        }

        public void addHeader(String name, String value)
        {
            headers.put(name, value);
        }

        public void addDateHeader(String name, long date)
        {
            headers.put(name, date);
        }

        public String getHeader(String headerName)
        {
            Collection values = (Collection) headers.get(headerName);
            return values == null ? null : (String) values.iterator().next(); // return the first value
        }

        public long getDateHeader(String headerName)
        {
            Collection values = (Collection) headers.get(headerName);
            return values == null ? -1 : (Long) values.iterator().next(); // return the first value
        }
    }

    private static class StubRequest
    {
        private MultiMap parameters = new MultiHashMap();

        public void setParameter(String name, String value)
        {
            parameters.remove(name);
            parameters.put(name, value);
        }

        public void addParameter(String name, String value)
        {
            parameters.put(name, value);
        }

        public String getParameter(String name)
        {
            Collection values = (Collection) parameters.get(name);
            return values == null ? null : (String) values.iterator().next(); // return the first value
        }

        public String[] getParameterValues(String name)
        {
            //noinspection unchecked
            Collection<String> values = (Collection<String>) parameters.get(name);
            return values == null ? null : values.toArray(new String[values.size()]);
        }
    }
}
