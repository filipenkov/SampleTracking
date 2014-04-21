package com.atlassian.jira.webtest.framework.page;

import com.atlassian.jira.webtest.framework.core.PageObject;

/**
 * @since 4.3
 */
public interface WebSudoLoginPage extends Page
{
   void setPassword(String password);
   void submit();
}
