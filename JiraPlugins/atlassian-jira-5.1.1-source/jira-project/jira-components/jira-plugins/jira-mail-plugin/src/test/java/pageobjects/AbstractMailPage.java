/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.SelectElement;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMailPage extends AbstractJiraAdminPage
{
    @Init
	@SuppressWarnings("unused")
	void disableNavigateAlert() {
		driver.executeScript("AJS.$(function() { AJS.$(\"form\").each(function(idx, el) { AJS.$(el).removeDirtyWarning(); });})");
	}

    public List<String> getFieldErrors() {
        return Lists.newArrayList(
                Collections2.transform(driver.findElements(By.cssSelector("div.field-group div.error")),
                        new Function<WebElement, String>()
                        {
                            @Override
                            public String apply(WebElement input)
                            {
                                return input.getText();
                            }
                        }));
    }

    @Override
    public String getUrl()
    {
        return null;
    }

    @Override
    public String linkId()
    {
        return null;
    }

    public void setRadioByValue(@Nonnull String name, @Nonnull String value) {
        driver.findElement(By.cssSelector(String.format("input[name=%s][value='%s']", name, value))).click();
    }


    public void setSelectByValue(@Nonnull String name, @Nonnull String value) {
		driver.findElement(By.name(name)).findElement(By.xpath("descendant::option[@value='" + value + "']")).click();
	}

    public List<Option> getSelectOptions(String name) {
        return elementFinder.find(By.name(name), SelectElement.class).getAllOptions();
	}

    public ImmutableList<String> getText(Collection<WebElement> elements) {
        return ImmutableList.copyOf(Collections2.transform(elements, new Function<WebElement, String>()
        {
            @Override
            public String apply(@Nullable WebElement from)
            {
                return from != null ? from.getText() : "";
            }
        }));
    }

    public ImmutableList<String> getTextFromOptions(Collection<Option> elements) {
        return ImmutableList.copyOf(Collections2.transform(elements, new Function<Option, String>()
        {
            @Override
            public String apply(@Nullable Option from)
            {
                return from != null ? from.text() : "";
            }
        }));
    }

	public void setSelect(@Nonnull String name, @Nonnull String value) {
		driver.findElement(By.name(name)).findElement(By.xpath("descendant::option[text()='" + value + "']")).click();
	}

    @Nullable
	public String getSelectedOptionText(@Nonnull String name) {
		final WebElement select = driver.findElement(By.name(name));
        final List<WebElement> option = select.findElements(By.xpath(
                String.format("descendant::option[@value='%s']", select.getAttribute("value"))));
		return option.isEmpty() ? null : option.get(0).getText();
	}

    void changeText(WebElement element, String text) {
        driver.executeScript(String.format("jQuery(arguments[0]).val('%s').change()", StringEscapeUtils.escapeJavaScript(text)), element);
    }

    void sendChangeEvent(WebElement webElement) {
        driver.executeScript("jQuery(arguments[0]).change()", webElement);
    }

    public boolean isOutgoingMailTabVisible() {
        return !driver.findElements(By.id("outgoing_mail_tab")).isEmpty();
    }
}
