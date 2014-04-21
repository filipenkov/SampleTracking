package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.jira.issue.Issue;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a context object used with the renderers.
 */
public class IssueRenderContext
{
    private Issue issue;
    private Map params;

    public IssueRenderContext(Issue issue)
    {
        this.issue = issue;
        params = new HashMap();
    }

    public Issue getIssue()
    {
        return issue;
    }

    public void setIssue(Issue issue)
    {
        this.issue = issue;
    }

    public Map getParams()
    {
        return params;
    }

    public void addParam(Object key, Object value)
    {
        params.put(key, value);
    }

    public Object getParam(Object key)
    {
        return params.get(key);
    }
}
