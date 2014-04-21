package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;

/**
 * <p>
 * Abstract popup dialog implementation. Popups are special kind of dialogs that are opdned in separate browser windows.
 *
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumPopup<D extends Dialog<D>> extends AbstractSeleniumDialog<D> implements Dialog<D>
{

    private final SeleniumLocator mainLocator;
    private final SeleniumLocator openLinkLocator;
    private final String popupWindowId;

    protected AbstractSeleniumPopup(SeleniumContext context, String dialogId, String windowName, SeleniumLocator openLinkLocator)
    {
        super(context);
        this.mainLocator = id(notNull("dialogId", dialogId));
        this.popupWindowId = notNull("dialogWindowName", windowName);
        this.openLinkLocator = notNull("openLinkLocator", openLinkLocator);
    }

    /* -------------------------------------------------- LOCATORS -------------------------------------------------- */

    @Override
    protected SeleniumLocator openDialogLocator()
    {
        return mainLocator;
    }

    /* -------------------------------------------------- QUERIES --------------------------------------------------- */

    @Override
    public TimedCondition isOpen()
    {
        return and(isInPopupWindow(), super.isOpen());
    }

    protected final TimedCondition isInPopupWindow()
    {
        return conditions().inWindow(popupWindowId);
    }


    /* -------------------------------------------------- ACTIONS --------------------------------------------------- */

    @Override
    public D open()
    {
        if (!isOpenable().now())
        {
            throw new IllegalStateException("Not openable - isOpenable failed: " + isOpenable());
        }
        openLinkLocator.element().click();
        waitFor().popup(popupWindowId);
        client.selectPopUp(popupWindowId);
        return asTargetType();
    }

    protected final D asTargetType()
    {
        return dialogType().cast(this);
    }


    protected abstract Class<D> dialogType();
}
