package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * @since v4.4
 */
public class AvatarDialog
{
    @ElementBy (id = "avatar-dialog", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement dialog;

    @WaitUntil
    public void dialogReady()
    {
        Poller.waitUntilTrue(dialog.timed().hasClass("aui-dialog-open"));
    }

    public void setAvatar (final String id)
    {
        dialog.find(By.id(id)).click();
        Poller.waitUntilFalse(dialog.timed().isVisible());
    }
}
