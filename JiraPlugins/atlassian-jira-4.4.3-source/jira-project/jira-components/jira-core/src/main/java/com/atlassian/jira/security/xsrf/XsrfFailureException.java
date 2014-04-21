package com.atlassian.jira.security.xsrf;

import com.google.common.collect.ImmutableSortedSet;
import webwork.action.Action;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is thrown when an Action request fails an XSRF check
 *
 * @since v4.1
 */
public class XsrfFailureException extends RuntimeException
{
    private final Action action;
    private final XsrfCheckResult checkResult;
    private final boolean sessionExpired;
    private final String requestURL;
    private final String requestMethod;
    private final Set<Map.Entry<String, List<String>>> parameters;

    public XsrfFailureException(final Action action, final HttpServletRequest request, final XsrfCheckResult checkResult, final boolean sessionExpired)
    {
        this.action = action;
        this.checkResult = checkResult;
        this.sessionExpired = sessionExpired;
        this.requestURL = request.getRequestURL().toString();
        this.requestMethod = request.getMethod();
        Map<String, List<String>> allParams = new HashMap<String, List<String>>();
        for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();)
        {
            String name = (String) enumeration.nextElement();
            String[] values = request.getParameterValues(name);
            allParams.put(name, Arrays.asList(values));
        }
        //
        // we sort the parameters into longest value first.  The idea is that is what the user is most likely to want to copy and paste
        // in the case where we display them on the screen
        //
        final Comparator<Map.Entry<String, List<String>>> biggestValueFirst = new Comparator<Map.Entry<String, List<String>>>()
        {
            public int compare(final Map.Entry<String, List<String>> entry1, final Map.Entry<String, List<String>> entry2)
            {
                int valLen1 = 0;
                int valLen2 = 0;
                if (entry1.getValue() != null)
                {
                    for (String s : entry1.getValue())
                    {
                        valLen1 += s != null ? s.length() : 0;
                    }
                }
                if (entry2.getValue() != null)
                {
                    for (String s : entry2.getValue())
                    {
                        valLen2 += s != null ? s.length() : 0;
                    }
                }
                if (valLen1 == valLen2)
                {
                    // respect the keys in this case
                    return entry1.getKey().compareTo(entry2.getKey());
                }
                // longest first
                return valLen2 - valLen1;
            }
        };
        this.parameters = ImmutableSortedSet.copyOf(biggestValueFirst, allParams.entrySet());
    }


    public Action getAction()
    {
        return action;
    }

    public String getRequestURL()
    {
        return requestURL;
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }

    public Set<Map.Entry<String, List<String>>> getRequestParameters()
    {
        return parameters;
    }

    public XsrfCheckResult getCheckResult()
    {
        return checkResult;
    }

    public boolean isSessionExpired()
    {
        return sessionExpired;
    }
}
