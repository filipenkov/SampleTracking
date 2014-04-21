package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class NewSTMPMailServerConfig
{
    @ElementBy (name = "name")
    private PageElement nameField;

    @ElementBy (name = "from")
    private PageElement fromAddressField;

    @ElementBy (name = "prefix")
    private PageElement emailPrefixField;

    @ElementBy (name = "serverName")
    private PageElement hostNameField;

    @ElementBy (id = "add_submit")
    private PageElement submitField;

    @Inject
    private PageBinder binder;


    public NewSTMPMailServerConfig fill(String name, String fromAddress, String emailPrefix, String hostName)
    {
        nameField.type(name);
        fromAddressField.type(fromAddress);
        emailPrefixField.type(emailPrefix);
        hostNameField.type(hostName);

        return this;
    }

    public <T extends Page> T submit(final Class<T> nextPage, String... arguments)
    {
        submitField.click();
        return binder.navigateToAndBind(nextPage, arguments);
    }

}
