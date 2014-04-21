package com.atlassian.jira.web.action.admin.vcs;

import com.atlassian.jira.vcs.viewcvs.ViewCvsBrowser;

import java.util.Map;
import java.net.MalformedURLException;

/**
 * This class was extracted from TestAddRepository. Originally both separate classes were defined in the same file.
 */
public class MockViewCvsBrowser extends ViewCvsBrowser
{
    public MockViewCvsBrowser(String baseURL, Map params) throws MalformedURLException
    {
        super(baseURL, params);
    }
}
