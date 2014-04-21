package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import org.openqa.selenium.By;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v4.4
 */
public class Suggestion
{
    public static final Function<PageElement, Suggestion> BUILDER = new Function<PageElement, Suggestion>()
    {
        @Override
        public Suggestion apply(@Nullable PageElement from)
        {
            return new Suggestion(from);
        }
    };

    private final PageElement container;

    public Suggestion(PageElement container)
    {
        this.container = checkNotNull(container);
    }

    public void click()
    {
        findLink().click();
    }

    public String getText()
    {
        return container.getText();
    }

    public String getMainLabel()
    {
        final String all =  findLink().getText();
        // TODO we can do better by spanning main label in List.js. But not on RC day!
        return all.substring(0, all.length() - getAliasLabel().length()).trim();
    }

    private PageElement findLink()
    {
        return container.find(By.tagName("a"));
    }

    public String getAliasLabel()
    {
        PageElement aliasSpan = container.find(By.className("aui-item-suffix"));
        if (aliasSpan.isPresent())
        {
            return aliasSpan.getText();
        }
        else
        {
            return "";
        }
    }

    @Override
    public String toString()
    {
        return asString("Suggestion[mainLabel=", getMainLabel(), ",aliasLabel=", getAliasLabel(), "]");
    }

    @Override
    public int hashCode()
    {
        return getMainLabel().hashCode() * 37 + getAliasLabel().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!Suggestion.class.isInstance(obj))
        {
            return false;
        }
        final Suggestion that = (Suggestion) obj;
        return this.getMainLabel().equals(that.getMainLabel()) && this.getAliasLabel().equals(that.getAliasLabel());
    }
}
