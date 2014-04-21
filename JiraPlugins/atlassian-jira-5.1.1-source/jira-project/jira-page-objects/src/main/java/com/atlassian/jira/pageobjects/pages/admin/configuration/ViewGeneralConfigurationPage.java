package com.atlassian.jira.pageobjects.pages.admin.configuration;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByDataAttribute;
import org.openqa.selenium.By;

/**
 * View General configuration properties
 *
 * @since v5.1
 */
public class ViewGeneralConfigurationPage extends AbstractJiraPage
{
    private final static String URI = "/secure/admin/ViewApplicationProperties.jspa";

    private static final String STATUS_ON = "status-active";
    private static final String STATUS_OFF = "status-inactive";

    static final String ROW_TAG = "tr";
    static final String PROPERTY_ID = "property-id";

    static final String PROPERTY_JIRA_MODE = "jira-mode";
    static final String PROPERTY_CONTACT_ADMIN_FORM = "contact-admin-form";
    static final String PROPERTY_DISABLE_INLINE_EDIT = "disableInlineEdit";

    @ElementBy(id = "edit-app-properties")
    private PageElement editButton;

    @Override
    public TimedCondition isAt()
    {
        return elementFinder.find(By.id("options_table")).timed().isPresent();
    }

    public String getUrl()
    {
        return URI;
    }

    public EditGeneralConfigurationPage edit()
    {
        editButton.click();
        return pageBinder.bind(EditGeneralConfigurationPage.class);
    }

    public boolean isContactAdminFormOn()
    {
        return isBooleanPropertyEnabled(PROPERTY_CONTACT_ADMIN_FORM);
    }

    public boolean isBooleanPropertyEnabled(String propertyId)
    {
        return findPropertyCell(propertyId).hasClass(STATUS_ON);
    }

    public boolean isInlineEditPresent()
    {
        return findPropertyCell(PROPERTY_DISABLE_INLINE_EDIT).isPresent();
    }

    public PageElement findPropertyCell(String propertyId)
    {
        return elementFinder.find(ByDataAttribute.byTagAndData(ROW_TAG, PROPERTY_ID, propertyId));
    }
}
