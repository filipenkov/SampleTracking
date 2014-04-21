package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.component.SeleniumAutoCompleteInput;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * Implementation of {@link com.atlassian.jira.webtest.framework.component.AutoCompleteInput} for the
 * {@link SeleniumDotDialog}.
 *
 * @since v4.3
 */
public class SeleniumDDInput extends SeleniumAutoCompleteInput<SeleniumDDInput,DotDialog> implements DotDialog.DDInput
{
    private final SeleniumLocator inputLocator;
    private final SeleniumLocator iconLocator;

    public SeleniumDDInput(SeleniumDotDialog parent, SeleniumContext ctx)
    {
        super(parent, ctx);
        this.inputLocator = parent.queryableContainerLocator().combine(css(".text:input"));
        this.iconLocator = parent.queryableContainerLocator().combine(css("span.icon.drop-menu"));
    }

    @Override
    protected SeleniumDDInput asTargetType()
    {
        return this;
    }

    /* ------------------------------------------------ LOCATORS ---------------------------------------------------- */

    @Override
    protected SeleniumLocator inputLocator()
    {
        return inputLocator;
    }

    @Override
    protected SeleniumLocator iconLocator()
    {
        return iconLocator;
    }

    /* ----------------------------------------------- COMPONENTS --------------------------------------------------- */

    @Override
    public DotDialog.DDDropDown dropDown()
    {
        return parent().dropDown();
    }

}
