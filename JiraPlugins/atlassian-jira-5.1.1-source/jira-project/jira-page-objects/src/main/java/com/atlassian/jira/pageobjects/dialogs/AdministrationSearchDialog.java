package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.fields.AutoComplete;
import com.atlassian.jira.pageobjects.components.fields.QueryableDropdownSelect;
import com.atlassian.pageobjects.PageBinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 * @since v4.4
 */
public class AdministrationSearchDialog extends FormDialog
{
    private static final By ADMIN_QUICK_NAV_QUERYABLE_CONTAINER = By.cssSelector("#admin-quicknav-dialog #admin-quick-nav-queryable-container");
    private static final By ADMINISTRATION_SUGGESTIONS = By.cssSelector("#administration-suggestions");

    @Inject
    private PageBinder binder;

    public AdministrationSearchDialog()
    {
        super("admin-quicknav-dialog");
    }

    public AutoComplete getAdminQuickSearch()
    {
        return binder.bind(QueryableDropdownSelect.class, ADMIN_QUICK_NAV_QUERYABLE_CONTAINER, ADMINISTRATION_SUGGESTIONS);
    }
}
