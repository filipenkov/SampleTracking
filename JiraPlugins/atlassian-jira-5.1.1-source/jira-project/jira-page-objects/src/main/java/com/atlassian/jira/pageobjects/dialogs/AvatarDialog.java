package com.atlassian.jira.pageobjects.dialogs;

import org.openqa.selenium.By;

/**
 * @since v4.4
 */
public class AvatarDialog extends FormDialog
{
    public AvatarDialog(String id)
    {
        super(id);
    }

    public void setAvatar (final String id)
    {
        getDialogElement().find(By.id("avatar-" + id)).click();
        waitUntilHidden();
    }
}
