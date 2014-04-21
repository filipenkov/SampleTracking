package com.atlassian.core.filters.legacy;

import com.atlassian.core.util.StringUtils;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

/**
 * Uses {@link StringUtils#escapeCP1252(String, String)} to replace high-bit punctuation characters
 * with ASCII equivalents in request parameter values.
 * <p/>
 * This is here for legacy functionality if required, but its use is <b>not</b> recommended. Rather,
 * applications should be configured with Unicode request and response encoding.
 *
 * @since 4.0
 */
public final class WordCurlyQuotesRequestWrapper extends HttpServletRequestWrapper
{
    private final String encoding;

    /**
     * Caches the escaped parameter values for a particular parameter, once {@link #getParameterValues(String)}
     * is called for that parameter. 
     */
    private final Map<String, String[]> parameterValueCache = new HashMap<String, String[]>();

    /**
     * Caches the escaped parameter map once {@link #getParameterMap()} is called.
     */
    private Map<String, String[]> parameterMap = null;

    public WordCurlyQuotesRequestWrapper(HttpServletRequest servletRequest, String encoding)
    {
        super(servletRequest);
        this.encoding = encoding;
    }

    public final String getParameter(String string)
    {
        return escapeString(super.getParameter(string));
    }

    private String escapeString(String string)
    {
        return StringUtils.escapeCP1252(string, encoding);
    }


    public final Map getParameterMap()
    {
        if (parameterMap == null)
        {
            //noinspection unchecked
            Map<String, Object> original = (Map<String, Object>) super.getParameterMap();

            parameterMap = new HashMap<String, String[]>();
            for (String key : original.keySet())
            {
                parameterMap.put(key, getParameterValues(key));
            }
        }
        return parameterMap;
    }


    public final String[] getParameterValues(String string)
    {
        String[] returnValue = parameterValueCache.get(string);

        //if we haven't yet cached this - look it up.
        if (returnValue == null)
        {
            String[] parameterValues = super.getParameterValues(string);

            //values could be null - don't bother converting them
            if (parameterValues == null)
                return null;

            for (int i = 0; i < parameterValues.length; i++)
            {
                String parameterValue = escapeString(parameterValues[i]);
                parameterValues[i] = parameterValue;
            }

            parameterValueCache.put(string, parameterValues);
            returnValue = parameterValues;

        }
        return returnValue;
    }
}
