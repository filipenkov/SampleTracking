package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 * @since v4.4
 */
public class AdministrationSearchDialog
{
    private static final By ADMIN_QUICK_NAV_QUERYABLE_CONTAINER = By.cssSelector("#admin-quicknav-dialog #admin-quick-nav-queryable-container");
    private static final By ADMINISTRATION_SUGGESTIONS = By.cssSelector("#administration-suggestions");

    @ElementBy (cssSelector = "#admin-quicknav-dialog", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement dialog;

    @Inject
    private PageBinder binder;

    @WaitUntil
    public void dialogReady()
    {
        Poller.waitUntilTrue(isOpen());
    }

    public TimedCondition isOpen()
    {
        return dialog.timed().hasClass("aui-dialog-content-ready");
    }

    public AutoComplete getAdminQuickSearch()
    {
        return binder.bind(QueryableDropdownSelect.class, ADMIN_QUICK_NAV_QUERYABLE_CONTAINER, ADMINISTRATION_SUGGESTIONS);
    }
}
