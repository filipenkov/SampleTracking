package com.atlassian.jira.pageobjects.project.summary;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.jira.pageobjects.dialogs.AvatarDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.By;

/**
 * Edit project dialog as launched from the project configuration summary page
 *
 * @since v4.4
 */
public class EditProjectDialog extends FormDialog
{
    private PageElement projectName;
    private PageElement projectUrl;
    private PageElement projectDescription;
    private PageElement errorMessage;
    private PageElement avatarImage;
    private PageElement updateButton;

    public EditProjectDialog()
    {
        super("project-config-project-edit-dialog");
    }

    @Init
    public void initialize()
    {
        projectName = find(getProjectNameLocator());
        projectUrl = find(getProjectUrlLocator());
        projectDescription = find(getProjectDescriptionLocator());
        updateButton = find(getUpdateLocator());
        avatarImage = find(getProjectAvatarLocator());
        errorMessage = find(getErrorMessageLocator());
    }

    public EditProjectDialog setProjectName(final String newProjectName)
    {
        projectName.clear().type(newProjectName);
        return this;
    }

    public EditProjectDialog setUrl(final String url)
    {
        projectUrl.clear().type(url);
        return this;
    }

    public EditProjectDialog setAvatar(final String id)
    {
        avatarImage.click();
        binder.bind(AvatarDialog.class, "project-avatar-picker").setAvatar(id);
        Poller.waitUntilTrue(getDialogElement().timed().isVisible());
        return this;
    }

    public EditProjectDialog setDescription(final String description)
    {
        projectDescription.clear().type(description);
        return this;
    }

    public boolean submit()
    {
        return !submit(updateButton);
    }

    private static By getUpdateLocator()
    {
        return By.id("project-edit-submit");
    }

    private static By getProjectNameLocator()
    {
        return By.name("name");
    }

    private static By getProjectUrlLocator()
    {
        return By.name("url");
    }

    private static By getProjectDescriptionLocator()
    {
        return By.name("description");
    }

    private static By getProjectAvatarLocator()
    {
        return By.id("project_avatar_image");
    }

    private static By getErrorMessageLocator()
    {
        return By.cssSelector(".aui-message.error");
    }

    public String getErrorMessage()
    {
        return errorMessage.getText().trim();
    }
}
