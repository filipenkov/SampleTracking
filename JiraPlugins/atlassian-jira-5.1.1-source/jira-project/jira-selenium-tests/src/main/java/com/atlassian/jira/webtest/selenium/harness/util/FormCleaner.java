package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.util.JqueryExecutor;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class that cleans up forms opened by Selenium after tests, in order to avoid the 'isDirty' pop-ups
 * that block execution of subsequent tests.
 *
 * @since v4.2
 */
public final class FormCleaner extends SeleniumContextAware
{
    private static final String ALL_TEXT_ELEMENTS_LOCATOR = ":text, textarea";
    private static final String GET_ALL_DIRTY_ELEMENTS_IDS = "var dirtyElemIds = []; jQuery('%s').each(function(idx, elem) {"
            + " if (jQuery(elem).val() !== '') dirtyElemIds.push(jQuery(elem).attr('id')); }); dirtyElemIds.join(',');";

    private final JqueryExecutor scriptExecutor;

    public FormCleaner(SeleniumContext ctx)
    {
        super(ctx);
        this.scriptExecutor = new JqueryExecutor(context);
    }

    private static String fullScript()
    {
        return String.format(GET_ALL_DIRTY_ELEMENTS_IDS, ALL_TEXT_ELEMENTS_LOCATOR);
    }

    /**
     * Clean up all text inputs on the page.
     *
     */
    public void cleanUpPage()
    {
        if (!scriptExecutor.canExecute().byDefaultTimeout())
        {
            FuncTestOut.log("Cannot execute page cleanup, reason: " + scriptExecutor.jqueryState() + ", jQuery check: "
                    + scriptExecutor.checkJquery());
            return;
        }
        String dirtyIds = scriptExecutor.execute(fullScript()).byDefaultTimeout();
        if (StringUtils.isNotBlank(dirtyIds))
        {
            cleanUpFields(dirtyIds.split(","));
        }
    }


    /**
     * Clean up selected fields (if applicable).
     *
     * @param locators Selenium locators of the fields to clean
     */
    public void cleanUpFields(String... locators)
    {
        for(String id : locators)
        {
            if (StringUtils.isNotBlank(id) && client.isElementPresent(id))
            {
                client.type(id.trim(), "");
            }
            else if (!client.isElementPresent(id))
            {
                FuncTestOut.log("Id <" + id + "> returned as dirty but not found on the page");
            }

        }
    }
}
