/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import javax.annotation.Nonnull;

public class OutgoingServersPage extends AbstractMailPage
{
    @ElementBy (id = "add-new-smtp-server")
    private PageElement addServerButton;

    @ElementBy (id = "smtp-mail-servers-panel")
    private PageElement smtpMailServerPanel;

    @ElementBy (xpath = "//td/ul[@class='operations-list']//a[text()='Edit']")
    private PageElement editServer;

    @Override
    public TimedCondition isAt()
    {
        return smtpMailServerPanel.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/OutgoingMailServers.jspa";
    }

    public AddSmtpMailServerPage addServer() {
        addServerButton.click();
        return pageBinder.bind(AddSmtpMailServerPage.class);
    }

    @Nonnull
    public UpdateSmtpMailServerPage editServer()
    {
        editServer.click();
        return pageBinder.bind(UpdateSmtpMailServerPage.class);
    }

}
