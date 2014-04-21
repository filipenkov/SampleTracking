package com.atlassian.jira.webtest.framework.impl.selenium.util;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.AbstractTimedQuery;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.AbstractSeleniumTimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.query.ScriptExecutionQuery;
import com.atlassian.selenium.SeleniumClient;

import java.util.regex.Pattern;

/**
 * Executes jQuery code in Selenium (given the current page has loaded a JQuery library). 
 *
 * @since v4.2
 */
public final class JqueryExecutor extends SeleniumContextAware
{
    static final String JQUERY_CHECK_IS_DEFINED = "if (selenium.browserbot.getUserWindow().jQuery) true; else false;";
    static final String JQUERY_CHECK_IS_FUNCTION = "if (typeof(selenium.browserbot.getUserWindow().jQuery) == \"function\") true; else false;";
    private static final String CHECK_JQUERY = "selenium.browserbot.getUserWindow().jQuery";
    private static final String PURE_JQUERY = "jquery\\(([^\\)]*)\\)";
    private static final String PURE_JQUERY_NAMESPACE = "jquery\\.";
    private static final String JQUERY_IN_SELENIUM = "selenium.browserbot.getUserWindow().jQuery($1)";
    private static final String JQUERY_NAMESPACE_IN_SELENIUM = "selenium.browserbot.getUserWindow().jQuery\\.";

    public static enum JqueryState {
        UNDEFINED(false, "jQuery undefined"),
        NOT_FUNCTION(false, "jQuery not a function"),
        DEFINED(true, "OK");

        private final boolean canExecute;
        private final String description;

        private JqueryState(final boolean canExecute, final String description)
        {
            this.canExecute = canExecute;
            this.description = description;
        }

        public static JqueryState checkState(SeleniumClient selenium)
        {
            if (!execBooleanTest(selenium, JQUERY_CHECK_IS_DEFINED))
            {
                return UNDEFINED;
            }
            if (!execBooleanTest(selenium, JQUERY_CHECK_IS_FUNCTION))
            {
                return NOT_FUNCTION;
            }
            return DEFINED;
        }
        private static boolean execBooleanTest(SeleniumClient selenium, String test)
        {
            return Boolean.parseBoolean(selenium.getEval(test));
        }

        public boolean canExecute()
        {
            return canExecute;
        }

        @Override
        public String toString()
        {
            return description;
        }
    }

    private final Pattern jQueryFunctionPattern;
    private final Pattern jQueryNamespacePattern;

    public JqueryExecutor(SeleniumContext ctx)
    {
        super(ctx);
        this.jQueryFunctionPattern = Pattern.compile(PURE_JQUERY, Pattern.CASE_INSENSITIVE);
        this.jQueryNamespacePattern = Pattern.compile(PURE_JQUERY_NAMESPACE, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Check, if jQuery code can be executed on the current web page. This boils down to making
     * sure that the 'jQuery' function is present,
     *
     * @return <code>true</code>, if jQuery may be executed at the current page, <code>false</code> otherwise. If this
     * method returns <code>false</code>, calling {@link #execute(String)} may result in errors
     */
    public TimedCondition canExecute()
    {
        return new CanExecuteCondition(context);
    }

    /**
     * Check JQuery state in the current page. 
     *
     * @return current jQuery state
     * @see JqueryExecutor.JqueryState
     */
    public JqueryState jqueryState()
    {
        return JqueryState.checkState(client);
    }

    /**
     * Evaluate Jquery. Use for debugging weird Selenium cases, where Jquery is actually
     * not a function.
     *
     * @return evaluation of jQuery variable in the current page. 
     */
    public String checkJquery()
    {
        return client.getEval(CHECK_JQUERY);
    }

    /**
     * Execute given jQuery <tt>script</tt> in Selenium, first waiting for the condition that jQuery is defined on
     * the tested page.
     *
     * @param script pure jQuery script
     * @return result of script evaluation
     * @throws IllegalStateException if this script cannot be executed (jQuery not defined on the page before timeout)
     */
    public TimedQuery<String> execute(String script)
    {
        return Queries.conditionalQuery(scriptQuery(script), canExecute())
                .expirationHandler(ExpirationHandler.THROW_ILLEGAL_STATE).build();
    }

    /**
     * Execute given jQuery <tt>script</tt> in Selenium, first waiting for the condition that jQuery is defined on
     * the tested page. If this condition is not met before timeout, the resulting query will return <code>null</code>.
     *
     * @param script pure jQuery script
     * @return timed query representing result of script evaluation
     */
    public TimedQuery<String> executeSafe(String script)
    {
        return Queries.conditionalQuery(scriptQuery(script), canExecute())
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();
    }

    /**
     * Execute <tt>script</tt> now without verifying if jQuery is defined in the current test context.
     *
     * @param script script to execute
     * @return evaluation result
     */
    public String executeNow(String script)
    {
        return client.getEval(toSeleniumScript(script));
    }

    private AbstractTimedQuery<String> scriptQuery(String script, ExpirationHandler handler)
    {
        return new ScriptExecutionQuery.Builder(context).expirationHandler(handler).script(toSeleniumScript(script)).build();
    }

    private AbstractTimedQuery<String> scriptQuery(String script)
    {
        return new ScriptExecutionQuery.Builder(context).script(toSeleniumScript(script)).build();
    }


    private String toSeleniumScript(final String script)
    {
        String functionsReplaced = jQueryFunctionPattern.matcher(script).replaceAll(JQUERY_IN_SELENIUM);
        return jQueryNamespacePattern.matcher(functionsReplaced).replaceAll(JQUERY_NAMESPACE_IN_SELENIUM);
    }

    private static class CanExecuteCondition extends AbstractSeleniumTimedCondition
    {
        protected CanExecuteCondition(SeleniumContext context)
        {
            super(context, Timeouts.UI_ACTION);
        }

        @Override
        public boolean now()
        {
            return JqueryState.checkState(client).canExecute();
        }
    }
}
