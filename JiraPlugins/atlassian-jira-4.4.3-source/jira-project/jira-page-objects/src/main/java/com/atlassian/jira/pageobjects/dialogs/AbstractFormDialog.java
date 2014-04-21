package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.TimedElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Represents a JIRA from dialog. These type of Dialogs basically slurp HTML from the server and display it on the
 * client side. Input is submitted to a back end action. This action will either indicate that the action was a
 * success or will return HTML with errors in it for display to the user.
 *
 * @since v4.4
 */
public abstract class AbstractFormDialog
{
    @Inject
    protected PageElementFinder locator;

    @Inject
    protected PageBinder binder;

    private PageElement loading;
    private PageElement form;
    private PageElement dialogElement;

    private final String id;

    protected AbstractFormDialog(String id)
    {
        this.id = id;
    }

    /**
     * Called when the dialog is first displayed. Waits util the dialog is ready to work with before exiting.
     */
    //Don't overwrite as you will probably end up calling the same method twice.
    @WaitUntil
    final public void ready()
    {
         waitUntilTrue(getDialogElement().timed().hasClass("aui-dialog-content-ready"));
    }

    //Don't overwrite as you will probably end up calling the same method twice.
    @Init
    final public void initAbstractDialog()
    {
        PageElement element = getDialogElement();
        form = element.find(By.tagName("form"));
        loading = element.find(By.cssSelector("form.submitting"), TimeoutType.DIALOG_LOAD);
    }

    public boolean isOpen()
    {
        PageElement element = getDialogElement();
        return element.isPresent() && element.isVisible();
    }

    /**
     * Do a submit on the passed element. The method will then wait for the response from the server before returning.
     *
     * @param pageElement the page element to submit.
     * @return true if the dialog is still open.
     */
    protected boolean submit(final PageElement pageElement)
    {
        assertDialogOpen();
        pageElement.click();
        waitWhileSubmitting();
        return isOpen();
    }

    /**
     * Do a submit on the passed element found using the passed locator. The method will then wait for the response
     * from the server before returning.
     *
     * @param locator for the element to select.
     * @return true if the dialog is still open.
     */
    protected boolean submit(By locator)
    {
        return submit(find(locator));
    }

    /**
     * Do a submit on the passed the form element with the passed name. The method will then wait for the response
     * from the server before returning.
     *
     * @param name the name of the element to submit.
     * @return true if the dialog is still open.
     */
    protected boolean submit(String name)
    {
        return submit(By.name(name));
    }

    /**
     * Return true iff the dialog has error messages contained in its associated form.
     *
     * @return true iff the dialog has error messages contained in its associated form.
     */
    public boolean hasFormErrors()
    {
        return !getFormErrorList().isEmpty();
    }

    /**
     * Return a list of the form errors currently on the dialog.
     *
     * @return a list of form errors currently on the dialog.
     */
    public List<String> getFormErrorList()
    {
        assertDialogOpen();

        List<PageElement> all = form.findAll(By.cssSelector("div.error"));
        List<String> errors = Lists.newArrayListWithExpectedSize(all.size());
        for (PageElement element : all)
        {
            errors.add(StringUtils.stripToNull(element.getText()));
        }
        return errors;
    }

    /**
     * Return a mapping of the errors currently on the form. The mapping if from parameterName -> error.
     *
     * @return a mapping from parameterName -> error of all the errors currently on the form.
     */
    public Map<String, String> getFormErrors()
    {
        assertDialogOpen();

        Map<String, String> errors = Maps.newLinkedHashMap();
        List<PageElement> errorNodes = form.findAll(By.cssSelector("div.error"));

        for (PageElement errorNode : errorNodes)
        {
            errors.put(errorNode.getAttribute("data-field"), errorNode.getText().trim());
        }

        return errors;
    }

    protected void assertDialogOpen()
    {
        assertTrue("Dialog is not open.", isOpen());
    }

    protected void assertDialogClosed()
    {
        assertFalse("Dialog is not closed.", isOpen());
    }

    protected void waitWhileSubmitting()
    {
        waitUntilFalse(loading.timed().isPresent());
    }

    protected void waitUntilClosed()
    {
        TimedElement timed = getDialogElement().timed();
        waitUntilFalse(not(and(timed.isPresent(), timed.isVisible())));
    }

    protected static void setElement(final PageElement element, final String value)
    {
        if (value != null)
        {
            element.clear();
            if (StringUtils.isNotBlank(value))
            {
                element.type(value);
            }
        }
    }

    protected PageElement getDialogElement()
    {
        if (dialogElement == null)
        {
            dialogElement = locator.find(By.id(id), TimeoutType.DIALOG_LOAD);
        }
        return dialogElement;
    }

    protected PageElement find(By locator)
    {
        return getDialogElement().find(locator);
    }
}
