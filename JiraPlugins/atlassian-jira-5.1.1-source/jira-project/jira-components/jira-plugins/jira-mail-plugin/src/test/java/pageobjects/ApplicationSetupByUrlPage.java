/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package pageobjects;

import com.atlassian.jira.pageobjects.pages.setup.ApplicationSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.MailSetupPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

public class ApplicationSetupByUrlPage extends ApplicationSetupPage
{
    @ElementBy (name = "next")
    private PageElement submitButton;

    @Override
    public String getUrl()
    {
        return "/secure/Setup!input.jspa";
    }

    public MailSetupPage submitToStep4()
    {
        submitButton.click();
        return pageBinder.bind(MailSetupPage.class);
    }

}
