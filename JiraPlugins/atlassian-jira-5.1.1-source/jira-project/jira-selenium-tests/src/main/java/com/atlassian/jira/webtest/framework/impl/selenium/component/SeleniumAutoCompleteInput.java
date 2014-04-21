package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.component.AutoCompleteInput;
import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.AbstractSeleniumComponent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.Sequences;
import com.atlassian.webtest.ui.keys.SpecialKeys;

/**
 * Abstract Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.AutoCompleteInput}.
 *
 * @since v4.3
 */
public abstract class SeleniumAutoCompleteInput<T extends  AutoCompleteInput<P>, P extends Localizable>
        extends AbstractSeleniumComponent<P> implements AutoCompleteInput<P>
{

    protected SeleniumAutoCompleteInput(P parent, SeleniumContext ctx)
    {
        super(parent, ctx);
    }

    protected abstract T asTargetType();

    /* ------------------------------------------------ LOCATORS ---------------------------------------------------- */

    @Override
    protected final SeleniumLocator detector()
    {
        return inputLocator();
    }

    /**
     * Locator of the input text element.
     *
     * @return main input locator
     */
    protected abstract SeleniumLocator inputLocator();

    /**
     * Locator of the drop-down icon element.
     *
     * @return icon locator
     */
    protected abstract SeleniumLocator iconLocator();

    /* ------------------------------------------------- QUERIES ---------------------------------------------------- */

    @Override
    public final TimedQuery<String> value()
    {
        return inputLocator().element().value();
    }

    /* ------------------------------------------------- ACTIONS ---------------------------------------------------- */

    @Override
    public T type(KeySequence sequence)
    {
        inputLocator().element().type(sequence);
        return asTargetType();
    }

    @Override
    public T clear()
    {
        type(Sequences.empty());
        return asTargetType();
    }

    @Override
    public AjsDropdown<P> clickDropIcon()
    {
        iconLocator().element().click();
        return dropDown();
    }

    @Override
    public AjsDropdown<P> arrowDown()
    {
        inputLocator().element().type(SpecialKeys.ARROW_DOWN);
        return dropDown();
    }

    @Override
    public final P focusAway()
    {
        client.fireEvent(inputLocator().fullLocator(), "blur");
        return parent();
    }
}
