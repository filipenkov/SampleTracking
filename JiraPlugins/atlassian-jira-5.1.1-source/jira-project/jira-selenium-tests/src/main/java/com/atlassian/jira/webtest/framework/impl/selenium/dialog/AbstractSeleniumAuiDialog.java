package com.atlassian.jira.webtest.framework.impl.selenium.dialog;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.dialog.Dialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * <p/>
 * Abstract Selenium {@link com.atlassian.jira.webtest.framework.dialog.Dialog} implementation representing an AUI
 * dialog.
 * <p/>
 * <p/>
 * AUI dialogs are opened as a layer on top of the current page. Each AUI dialog contains an unique ID and when open and
 * its contents loaded, it adds a special CSS marker class to its DIV container.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumAuiDialog<T extends Dialog<T>> extends AbstractSeleniumDialog<T>
        implements Dialog<T>
{
    private static final String OPEN_DIALOG_DETECTOR_TEMPLATE = "div#%s.%s";

    private final SeleniumLocator openDialogLocator;
    private final SeleniumLocator cancelLocator;

    /**
     * Subclasses must provide unique ID for given dialog type. This id is used as an identifier of the div element
     * serving as a container of the dialog.
     *
     * @param context current selenium test context
     * @param dialogId unique HTML id of the dialog container div
     */
    protected AbstractSeleniumAuiDialog(SeleniumContext context, String dialogId)
    {
        super(context);
        this.openDialogLocator = createOpenLocator(dialogId);
        this.cancelLocator = locatorFor(locator().combine(forClass("cancel")));
    }

    private SeleniumLocator createOpenLocator(String dialogId)
    {
        return css(String.format(OPEN_DIALOG_DETECTOR_TEMPLATE, dialogId, getOpenDialogClass())).withDefaultTimeout(Timeouts.DIALOG_LOAD);
    }

    protected String getOpenDialogClass()
    {
        return "aui-dialog-content-ready";
    }

    @Override
    protected SeleniumLocator openDialogLocator()
    {
        return openDialogLocator;
    }

    protected final SeleniumLocator cancelLinkLocator()
    {
        return cancelLocator;
    }

}
