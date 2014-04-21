package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class OutgoingMailServers
{

    @Inject
    private PageBinder binder;

    @ElementBy (id = "add-new-smtp-server")
    private PageElement configureSMTPServer;

    public NewSTMPMailServerConfig configureNewSTMP()
    {
        configureSMTPServer.click();
        return binder.bind(NewSTMPMailServerConfig.class) ;
    }

}
