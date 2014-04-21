package com.atlassian.jira.pageobjects.gadgets;

import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.util.List;

/**
 * GadgetView is an implentation of the internal view for a gadget and just extends a
 * WebElement so all the normal methods that WebDriver exposes on web elements is available.
 */
public class GadgetView implements WebElement
{
    @Inject
    AtlassianWebDriver driver;

    private final WebElement view;

    public GadgetView(WebElement view)
    {
        this.view = view;
    }

    /**
     * Closes the Gadget view and returns the WebDriver context back to the default content
     * Which is usually the Dashboard content.
     */
    public void close()
    {
        driver.switchTo().defaultContent();
    }

    public void click()
    {
        view.click();
    }

    public void submit()
    {
        view.submit();
    }

    public String getValue()
    {
        return view.getValue();
    }

    public void sendKeys(final CharSequence... charSequences)
    {
        view.sendKeys();
    }

    public void clear()
    {
        view.clear();
    }

    public String getTagName()
    {
        return view.getTagName();
    }

    public String getAttribute(final String s)
    {
        return view.getAttribute(s);
    }

    public boolean toggle()
    {
        return view.toggle();
    }

    public boolean isSelected()
    {
        return view.isSelected();
    }

    public void setSelected()
    {
        view.setSelected();
    }

    public boolean isEnabled()
    {
        return view.isEnabled();
    }

    public String getText()
    {
        return view.getText();
    }

    public List<WebElement> findElements(final By by)
    {
        return view.findElements(by);
    }

    public WebElement findElement(final By by)
    {
        return view.findElement(by);
    }
}