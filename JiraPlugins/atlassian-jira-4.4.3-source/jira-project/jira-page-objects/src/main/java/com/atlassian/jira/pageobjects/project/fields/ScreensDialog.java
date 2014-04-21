package com.atlassian.jira.pageobjects.project.fields;

import com.atlassian.jira.pageobjects.components.InlineDialog;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Represents the "x screens" inline dialog for a specific Field Layout Item on the Project Configuration
 * Fields tab. Each field item is associated with its own Field Configuration.
 *
 * @since v4.4
 */
public class ScreensDialog extends InlineDialog
{
    public ScreensDialog(final PageElement trigger, final String contentsId)
    {
        super(trigger, contentsId);
    }

    @Override
    public ScreensDialog open()
    {
        super.open();
        return this;
    }

    @Override
    public ScreensDialog close()
    {
        super.close();
        return this;
    }

    public List<String> getScreens()
    {
        final List<String> screens = Lists.newArrayList();

        final PageElement dialogContents = getDialogContents();
        final List<PageElement> screenElements = dialogContents.findAll(By.className("project-config-list-label"));
        for (final PageElement screenElement : screenElements)
        {
            screens.add(screenElement.getText());
        }
        return screens;
    }

}
