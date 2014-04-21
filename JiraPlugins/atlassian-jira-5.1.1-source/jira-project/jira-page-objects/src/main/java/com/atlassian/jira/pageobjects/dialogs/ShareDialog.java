package com.atlassian.jira.pageobjects.dialogs;

import com.atlassian.jira.pageobjects.components.MultiSelect;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * Represents the the sharing inline dialog on the view issue and issue navigator pages
 *
 * @since v5.0
 */
public class ShareDialog
{
    @ElementBy (cssSelector = "#jira-share-trigger")
    private PageElement trigger;

    @ElementBy(tagName = "body")
    protected PageElement body;

    private PageElement dialog;

    @Inject
    protected PageBinder binder;

    @Inject
    protected PageElementFinder locator;
    private MultiSelect shareMultiSeclect;


    public boolean isOpen()
    {
        return dialog != null && dialog.isVisible();
    }

    public List<String> getRecipients()
    {
        return getRecipients("data-username");
    }

    public List<String> getEmailRecipients()
    {
        return getRecipients("data-email");
    }

    public ShareDialog addRecipient(String recipient)
    {
        shareMultiSeclect.add(recipient);
        return this;
    }

    public ShareDialog removeRecipient(String username)
    {
        shareMultiSeclect.remove(username);
        return this;
    }

    public ShareDialog addNote(String text)
    {
        dialog.find(By.id("note")).type(text);
        return this;
    }

    public ShareDialog submit()
    {
        dialog.find(By.className("submit")).click();
        waitUntilFalse(dialog.timed().isVisible());
        return this;
    }

    public ShareDialog openViaKeyboardShortcut()
    {
        body.type("s");
        bindElements();
        return this;
    }

    public ShareDialog open()
    {
        trigger.click();
        bindElements();
        return this;
    }

    private void bindElements()
    {
        dialog = locator.find(By.id("inline-dialog-share-entity-popup"), TimeoutType.DIALOG_LOAD);
        shareMultiSeclect = binder.bind(MultiSelect.class, "sharenames", new Function<String, By>()
        {
            @Override
            public By apply(@Nullable String itemName)
            {
                //means find all items
                if(itemName == null)
                {
                    return By.cssSelector(".recipients li span img");
                }
                else
                {
                    return By.cssSelector(".recipients li[title=\"" + itemName + "\"]");
                }

            }
        });
    }

    public boolean isTriggerPresent()
    {
        return trigger.isPresent();
    }

    private List<String> getRecipients(String attributeName)
    {
        final List<String> ret = new ArrayList<String>();
        List<PageElement> elements = dialog.find(By.className("recipients")).findAll(By.tagName("li"));
        for (PageElement element : elements)
        {
            if (StringUtils.isNotBlank(element.getAttribute(attributeName)))
            {
                ret.add(element.getAttribute(attributeName));
            }
        }
        return ret;
    }
}
