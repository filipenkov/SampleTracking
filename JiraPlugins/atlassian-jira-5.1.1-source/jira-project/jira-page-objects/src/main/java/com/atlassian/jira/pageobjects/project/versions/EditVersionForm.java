package com.atlassian.jira.pageobjects.project.versions;

import com.atlassian.jira.pageobjects.components.restfultable.AbstractEditRow;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * @since v4.4
 */
public class EditVersionForm extends AbstractEditRow
{

    private static final String VERSION_NAME = ".project-config-version-name";
    private static final String VERSION_DESC = ".project-config-version-description";
    private static final String VERSION_RELEASE_DATE = ".project-config-version-release-date";

    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder finder;

    public EditVersionForm(final By rowSelector)
    {
        super(rowSelector);
    }

    public EditVersionForm fill(String name, String description, String date)
    {
        getNameField().clear().type(name);
        getDescriptionField().clear().type(description);

        if (date != null)
        {
            getReleaseDateField().clear().type(date);
        }
        return this;
    }

    public EditVersionForm submit()
    {
        getAddButton().click();
        waitUntilFalse(row.timed().hasClass("loading"));
        return this;
    }

    public EditVersionForm cancel()
    {
        getCancelLink().click();
        waitUntilFalse(row.timed().hasClass("loading"));
        waitUntilFalse(getCancelLink().timed().isPresent());
        return this;
    }

    public Field getNameField()
    {
        return pageBinder.bind(Field.class, findInRow(VERSION_NAME));
    }


    public Field getDescriptionField()
    {
        return pageBinder.bind(Field.class, findInRow(VERSION_DESC));
    }

    public Field getReleaseDateField()
    {
        return pageBinder.bind(Field.class, findInRow(VERSION_RELEASE_DATE));
    }
}
