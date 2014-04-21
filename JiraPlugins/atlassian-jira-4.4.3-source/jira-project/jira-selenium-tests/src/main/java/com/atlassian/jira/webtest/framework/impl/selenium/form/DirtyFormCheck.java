package com.atlassian.jira.webtest.framework.impl.selenium.form;


import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.selenium.SeleniumClient;
import junit.framework.Assert;

import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators.jQuery;
import static junit.framework.Assert.assertEquals;

/**
 * Class that makes dirty form testing easier.
 *
 * @since v4.3
 */
public class DirtyFormCheck extends SeleniumContextAware
{
    private String description;
    private Setup setup;
    private DirtyFormDescriptor dirtyFormDescriptor;
    private static final String DIRTY_FORM_DEFAULT_ERROR = "You have entered new data on this page. If you navigate away from this page without first saving your data, the changes will be lost.";
    private SeleniumClient client;
    SeleniumForm dirtyForm;
    SeleniumFormParent parent;
    private static final String CONFIRMATION_ERROR = "ERROR: There were no confirmations";

    //protected static final String VISIBLE_DIALOG_CONTENT_SELECTOR = JQUERY.create(".aui-dialog-open");
    //protected static final String DIALOG_CONTENT_READY_SELECTOR = JQUERY.create(".aui-dialog-content-ready");

    /**
     * @param context The Selenium test Context
     * @param description Description of testcase
     * @param setup The steps to perform before invoking the form, use the defult DirtyFormCheck.None for no setup
     * @param dirtyFormDescriptor The submission implementation - eg Link or Form based submission page
     */
    public DirtyFormCheck(SeleniumContext context, String description, Setup setup, DirtyFormDescriptor dirtyFormDescriptor)
    {
        super(context);

        this.description = description;
        this.setup = setup;
        this.dirtyFormDescriptor = dirtyFormDescriptor;
        this.client = context.client();
        this.parent = new SeleniumFormParent(context, dirtyFormDescriptor.getFormOpenerQuery(), dirtyFormDescriptor.getParentReadyQuery());
        this.dirtyForm = new SeleniumForm(context, parent, dirtyFormDescriptor);
    }

    public void run() throws Exception
    {
        run(DIRTY_FORM_DEFAULT_ERROR);
    }

    /**
     * Runs a set of standard dirty form checks
     * Needs expanding to include all the checks as described in
     * https://extranet.atlassian.com/display/QA/JIRA+Dev+Checklist+-+Forms
     *
     * @param dirtyFormError expected error message if the form is dirty
     */
    public void run(String dirtyFormError) throws Exception
    {
        setup.setup();
        if (!setup.isSetupDone())
        {
            throw new IllegalStateException("Setup has not run succesfully!");
        }
        openForm();
        _assertFormOpen();
        cancelForm();
        _assertCancelCausesNoErrors();
        openForm();
        _assertFormOpen();
        makeFormDirty();
        clickProvidedLink();
        Assert.assertEquals("Dirty form warning", dirtyFormDescriptor.getWarningMessage(), client.getConfirmation());
     }

    private void cancelForm()
    {
        dirtyForm.backLocator().element().click();
    }

    private void clickProvidedLink()
    {
        client.chooseCancelOnNextConfirmation();
        SeleniumLocator linkLocator = jQuery(dirtyFormDescriptor.getDirtyLinkQuery(), context);
        linkLocator.element().click();
    }


    private void openForm()
    {
        parent.formOpener().element().click();
    }

    private void _assertFormOpen()
    {
        assertThat(dirtyForm.isReady(), byDefaultTimeout());
    }

    private void _assertCancelCausesNoErrors()
    {
        assertThat(parent.isReady(), byDefaultTimeout());
        try
        {
            client.getConfirmation();
        }
        catch (Exception e)
        {
            assertEquals("There should be no alert as form was cancelled", CONFIRMATION_ERROR, e.getMessage());
        }
    }

    public final void _assertNotOpen()
    {
        //assertThat.elementNotPresentByTimeout(AbstractIssueDialog.VISIBLE_DIALOG_CONTENT_SELECTOR, context.timeouts().dialogLoad());
    }


    private void makeFormDirty()
    {
        String fieldName = dirtyFormDescriptor.getFieldName();
        if (dirtyFormDescriptor.isOption())
        {
            alterOption(fieldName);
        }
        else
        {
            alterField(fieldName);
        }
    }

    private void alterField(String fieldName)
    {
        String value = client.getValue(fieldName);
        client.typeInElementWithName(fieldName,value+"1");
    }

    private String getOption(String[] values, String oldValue)
    {
        for (String value : values)
        {
            if (!value.equals(oldValue))
            {
                return value;
            }
        }
        return "";
    }

    private void alterOption(String fieldName)
    {
        String[] options = client.getSelectOptions(fieldName);
        String oldValue = client.getSelectedLabel(fieldName);
        client.selectOption(fieldName, getOption(options, oldValue));
    }


    public static interface Setup
    {
        void setup();

        boolean isSetupDone();

        /**
         * Used when there are no setup steps
         */
        public static final class None implements Setup
        {
            boolean setupDone;

            @Override
            public void setup()
            {
                setupDone = true;
            }

            @Override
            public boolean isSetupDone()
            {
                return setupDone;
            }


        }
    }

    public static class DirtyFormDescriptor
    {
        private String formOpenerQuery;
        private String parentReadyQuery;
        private String cancelLinkQuery;
        private String warningMessage;
        private String submitLinkQuery;
        private String dirtyLinkQuery;
        private String fieldName;
        private String formReadyQuery;
        private boolean isOption;

        /**
         * Describes the form to be tested.
         *
         * @param formOpenerQuery query that matches the link used to open the form
         * @param parentReadyQuery jQuery used to check if parent page is ready (after cancel)
         * @param formReadyQuery the jQuery query string to use to check the form has displayed
         * @param submitLinkQuery the jQuery query string that matches the form submit button
         * @param cancelLinkQuery the jQuery query string that matches the form cancel button
         * @param dirtyLinkQuery  the jQuery query string that matches the link that will cause a dirty form warning
         * @param warningMessage the warning message that shpould appear, null to accept the default message
         * @param fieldName a field to be used to test the dirty form
         * @param isOption if the form field is an HTML select
         */
        public DirtyFormDescriptor(String formOpenerQuery, String parentReadyQuery, String formReadyQuery, String submitLinkQuery, String cancelLinkQuery, String dirtyLinkQuery, String warningMessage, String fieldName, boolean isOption)
        {
            this.formOpenerQuery = formOpenerQuery;
            this.cancelLinkQuery = cancelLinkQuery;
            this.warningMessage = warningMessage;
            this.submitLinkQuery = submitLinkQuery;
            this.fieldName = fieldName;
            this.isOption = isOption;
            this.formReadyQuery = formReadyQuery;
            this.parentReadyQuery = parentReadyQuery;
            this.dirtyLinkQuery = dirtyLinkQuery;
        }

        public String getSubmitLinkQuery()
        {
            return submitLinkQuery;
        }

        public String getCancelLinkQuery()
        {
            return cancelLinkQuery;
        }

        public String getDirtyLinkQuery()
        {
            return dirtyLinkQuery;
        }

        public String getWarningMessage()
        {
            return warningMessage;
        }

        public String getFormOpenerQuery()
        {
            return formOpenerQuery;
        }

        public String getParentReadyQuery()
        {
            return parentReadyQuery;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public String getFormReadyQuery()
        {
            return formReadyQuery;
        }

        public boolean isOption()
        {
            return isOption;
        }
    }


}
