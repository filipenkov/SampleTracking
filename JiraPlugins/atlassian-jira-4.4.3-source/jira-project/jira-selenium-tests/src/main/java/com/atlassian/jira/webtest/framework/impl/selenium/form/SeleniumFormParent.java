package com.atlassian.jira.webtest.framework.impl.selenium.form;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;


/**
 * Simple parent of a form, the only thing you nend ot provide is a jQuery
 * selector to use to detect page loaded.
 *
 * @since v4.3
 */
public class SeleniumFormParent extends AbstractSeleniumPage
{
    SeleniumLocator detector;
    SeleniumLocator formLocator;

    public SeleniumFormParent(SeleniumContext ctx, String formOpenQuery,  String pageLoadedQuery)
    {
        super(ctx);
        detector = jQuery(pageLoadedQuery);
        formLocator = jQuery(formOpenQuery);
    }

    @Override
    public SeleniumLocator detector()
    {
        return detector;
    }

    public SeleniumLocator formOpener()
    {
        return formLocator;
    }

}
