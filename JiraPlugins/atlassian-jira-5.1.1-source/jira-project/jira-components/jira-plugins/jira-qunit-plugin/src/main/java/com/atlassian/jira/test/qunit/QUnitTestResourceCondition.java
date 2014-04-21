package com.atlassian.jira.test.qunit;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractJiraCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Determines if test resources should be loaded on the current page.
 *
 * Based on a "qunit-test" query parameter.
 */
public class QUnitTestResourceCondition extends AbstractJiraCondition
{
    @Override
    public boolean shouldDisplay(User user, JiraHelper jiraHelper)
    {
        HttpServletRequest request = ExecutingHttpRequest.get();
        if (request == null)
        {
            // Might be getting called on a servlet or other resource without a full Action context?
            return false;
        }
        
        String qunitTest = request.getParameter("qunit-test");
        return StringUtils.isNotEmpty(qunitTest);
    }
}
