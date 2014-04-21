package com.atlassian.jira.webtest.framework.impl.selenium.form;

import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumSubmittableChildPage;


/**
 * TODO: Document this class / interface here represents a Form, mainly useful for Dirty Form Checking
 *
 * @since v4.3
 */
public class SeleniumForm extends AbstractSeleniumSubmittableChildPage
{
    SeleniumLocator submitLocator;
    SeleniumLocator backLocator;
    SeleniumLocator detector;

    public SeleniumForm(SeleniumContext ctx, AbstractSeleniumPage parent, DirtyFormCheck.DirtyFormDescriptor descriptor)
    {
        super(parent, ctx);
        populateLocators(descriptor);
    }

    private void populateLocators(DirtyFormCheck.DirtyFormDescriptor dirtyFormDescriptor)
    {
        backLocator = jQuery(dirtyFormDescriptor.getCancelLinkQuery());
        submitLocator = jQuery(dirtyFormDescriptor.getSubmitLinkQuery());
        detector = jQuery(dirtyFormDescriptor.getFormReadyQuery());
    }


    public SeleniumLocator submitLocator()
    {
        return submitLocator;
    }


    public SeleniumLocator backLocator()
    {
        return backLocator;
    }

    @Override
    public SeleniumLocator detector()
    {
        return detector;
    }
}
