package com.atlassian.jira.service.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.JiraKeyUtils;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class ServiceUtils
{
    private static final String INVALID_ISSUEKEY_CHARS = "\n\r\t \"''`~,.:;<>()[]{}!@#$%^&*+=|\\/?";
    private static final Logger log = Logger.getLogger(ServiceUtils.class);

    private ServiceUtils()
    {
    //cannot create instance of class
    }

    /**
     * @deprecated no longer used
     */
    @Deprecated
    public static GenericValue getProject(final String string)
    {
        final StringTokenizer tokenizer = new StringTokenizer(string, INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validProjectKey(token))
            {
                return ComponentAccessor.getProjectManager().getProjectByKey(token);
            }
        }
        return null;
    }

    /**
     * Given an actual key - return the issue that matches that key, or null if no
     * issues match that key.
     */
    public static GenericValue getIssue(final String key)
    {
        GenericValue issue = null;
        try
        {
            issue = ComponentAccessor.getIssueManager().getIssue(key);
            if (issue == null)
            {
                // JRA-16111 - we want to look for moved issues if an email references the old issue key
                final Issue movedIssue = ComponentAccessor.getChangeHistoryManager().findMovedIssue(key);
                if (movedIssue != null)
                {
                    issue = movedIssue.getGenericValue();
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.warn("Error getting issue from key [" + key + "]: " + e.getMessage(), e);
        }
        return issue;
    }

    /**
     * Loops through the string and returns the issue that is found within, or null
     * if there is no issue matching.
     *
     * It finds any string matching XXX-### and then looks it up to see if it is a valid
     * issue.  It will return the first valid issue that exists.
     *
     * @param   searchString    the string to search through for issues
     * @return                  the issue that has been found, or null of one is not found.
     */
    public static GenericValue findIssueInString(final String searchString)
    {
        final StringTokenizer tokenizer = new StringTokenizer(TextUtils.noNull(searchString).toUpperCase(), INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validIssueKey(token))
            {
                final GenericValue issue = getIssue(token);
                if (issue != null)
                {
                    return issue;
                }
            }
        }
        return null;
    }

    public static GenericValue[] findIssuesInString(final String searchString)
    {
        if (searchString == null)
        {
            return null;
        }

        final List<GenericValue> al = new ArrayList<GenericValue>();
        final StringTokenizer tokenizer = new StringTokenizer(searchString, INVALID_ISSUEKEY_CHARS, false);
        String token;
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            if (JiraKeyUtils.validIssueKey(token))
            {
                final GenericValue issue = ServiceUtils.getIssue(token);
                if (issue != null)
                {
                    al.add(issue);
                }
            }
        }
        return al.toArray(new GenericValue[al.size()]);
    }

    /**
     * This method creates a map of parameters from a string.
     *
     * The format of the string is key=value, key=value, key=value.
     *
     * At the moment this is really only used for Handler parameters, but that whole area
     * needs to be rewritten for JIRA 2.0.
     */
    public static Map<String, String> getParameterMap(final String parameterString)
    {
        final Map<String, String> params = new HashMap<String, String>();

        if (parameterString != null)
        {
            final StringTokenizer st = new StringTokenizer(parameterString, ",");

            while (st.hasMoreTokens())
            {
                final String token = st.nextToken().trim();
                final int equalIdx = token.indexOf('=');

                if (equalIdx >= 0)
                {
                    final String paramName = token.substring(0, equalIdx);
                    String paramValue = null;

                    if (equalIdx + 1 < token.length())
                    {
                        paramValue = token.substring(equalIdx + 1);
                    }
                    params.put(paramName, paramValue);
                }
            }
        }
        return params;
    }
}
