package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class SingleSelect
{

    @ElementBy(cssSelector = ".ajs-layer.active")
    private PageElement activeLayer;

    private PageElement parent;
    private PageElement container;
    private PageElement field;


    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private AtlassianWebDriver webDriver;

    public SingleSelect(PageElement parent)
    {
        this.parent = parent;
        this.container = parent.find(By.className("aui-ss"), TimeoutType.AJAX_ACTION);
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
                // Say no to suggestions
                if (container.timed().isPresent().byDefaultTimeout())
                {
                    field.type(Keys.BACK_SPACE);
                }
            }
            else
            {
                field.clear().type(value);
                waitUntilTrue("Expected query " + value, hasQuery(value));
                PageElement activeSuggestion = activeLayer.find(By.cssSelector("li.active"));
                activeSuggestion.click();
            }
        }

        return this;
    }

    private TimedCondition hasQuery(String query)
    {
        return and(container.timed().isPresent(), container.timed().hasAttribute("data-query", query));
    }

    public String getValue()
    {
        return field.getValue();
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

    /**
     * Type into this single select without any additional validation
     *
     * @param text text to type
     * @return this single select instance
     */
    public SingleSelect type(CharSequence text)
    {
        field.clear().type(text);
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
