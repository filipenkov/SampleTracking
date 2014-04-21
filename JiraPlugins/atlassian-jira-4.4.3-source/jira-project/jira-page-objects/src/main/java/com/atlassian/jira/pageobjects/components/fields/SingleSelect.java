package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.by.ByJquery;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.by;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class SingleSelect
{
    private PageElement parent;
    private PageElement field;

    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private AtlassianWebDriver webDriver;

    public SingleSelect(PageElement parent)
    {
        this.parent = parent;
        this.field = parent.find(By.tagName("input"));
    }

    public SingleSelect select(String value)
    {
        webDriver.executeScript("window.focus()");

        assertTrue("Unable to find container of the Single-Select", field.isPresent());

        if (isAutocompleteDisabled())
        {
            field.clear().type(value);
        }
        else
        {
            if (StringUtils.isEmpty(value))
            {
                field.type(Keys.BACK_SPACE).clear();
            }
            else
            {
                field.type(value);
                Poller.waitUntil(parent.find(ByJquery.$("[data-query=" + value + "]")).timed().isVisible(), is(true), by(8000));
            }

            PageElement activeSuggestion = elementFinder.find(By.cssSelector(".ajs-layer.active .active"));
            if (activeSuggestion.isPresent())
            {
                activeSuggestion.click();
            }
        }

        return this;
    }

    public boolean isAutocompleteDisabled()
    {
        return field.hasClass("aui-ss-disabled");
    }

    public SingleSelect clear()
    {
        field.clear();
        return this;
    }

    public String getError()
    {
        final PageElement error = parent.find(By.className("error"));

        if (error.isPresent())
        {
            return error.getText();
        }
        else
        {
            return null;
        }
    }
}
