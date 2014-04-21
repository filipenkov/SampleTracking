package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Minimal implementation of a FrotherControl. Constructor takes in the id the frother control is bound to.
 *
 * @since v4.4
 */
public class MultiSelect
{
    @Inject
    private PageElementFinder elementFinder;

    @Inject
    private PageBinder pageBinder;

    private PageElement textArea;
    private PageElement selectDiv;
    private PageElement errorDiv;
    
    private String id;
    private final Function<String, By> itemLocator;

    @Init
    public void initialize()
    {
        this.selectDiv =  elementFinder.find(By.id(id + "-multi-select"));
        this.textArea = selectDiv.find(By.id(id + "-textarea"));
        this.errorDiv = elementFinder.find(By.id(id + "-error"));
        waitUntilTrue(isPresent());
    }

    private TimedQuery<Boolean> isPresent()
    {
        return Conditions.and(
                selectDiv.timed().isPresent(),
                textArea.timed().isPresent()
        );
    }

    /**
     * Constructs the minimal implementation based on a given select[multiple] id.
     *
     * @param id the id of the select[multiple] element
     */
    public MultiSelect(final String id)
    {
        this.id = id;
        this.itemLocator = new Function<String, By>()
        {
            @Override
            public By apply(@Nullable String itemName)
            {
                //means find all items
                if(itemName == null)
                {
                    return By.cssSelector(".representation li");
                }
                else
                {
                    return By.cssSelector(".representation li[title=\"" + itemName + "\"]");
                }

            }
        };
    }

    /**
     * Constructs the minimal implementation based on a given select[multiple] id.
     *
     * @param id the id of the select[multiple] element
     * @param itemLocator a function that given a string will create a locator to locate the item for this multiselect given the name or all items if no name is provided
     */
    public MultiSelect(final String id, Function<String, By> itemLocator)
    {
        this.id = id;
        this.itemLocator = itemLocator;
    }

    /**
     * Adds an item by typing it in and picking the first suggestion. Assumes that the item passed in
     * will be used as the lozenge label
     *
     * @param item the item to add
     */
    public void add(final String item)
    {
        addNotWait(item);
        waitUntilTrue("Expected item " + item + "to be added, but was not", hasItem(item));
    }

    public void addNotWait(final String item)
    {
        textArea.type(item);
        waitUntilTrue("Expected suggestions to be present, but was not", isSuggestionsPresent());
        getFirstSuggestion().click();
    }

    /**
     * Removes a given item by clicking on the (x) next to the lozenge.
     *
     * @param item the item to remove
     */
    public void remove(final String item)
    {
        final Item itemByName = getItemByName(item);
        itemByName.remove();
        waitUntilFalse("Expected item " + item + "to be removed, but was not", hasItem(item));
    }

    public void clear()
    {
        final List<Item> items = getItems();
        for (final Item item : items)
        {
            final String name = item.getName();
            item.remove();
            waitUntilFalse("Expected item " + item + "to be removed, but was not", hasItem(name));
        }
        waitUntilFalse(hasItems());
    }

    public Item getItemByName(final String name)
    {
        final PageElement element = selectDiv.find(itemLocator.apply(name));
        return pageBinder.bind(Item.class, element);
    }

    public List<Item> getItems()
    {
        final List<Item> items = Lists.newArrayList();
        final List<PageElement> itemElements = selectDiv.findAll(itemLocator.apply(null));
        for (final PageElement itemElement : itemElements)
        {
            items.add(pageBinder.bind(Item.class, itemElement));
        }
        return items;
    }

    public TimedQuery<Boolean> hasItems()
    {
        return selectDiv.find(itemLocator.apply(null)).timed().isPresent();
    }

    public TimedQuery<Boolean> hasItem(final String name)
    {
        return selectDiv.find(itemLocator.apply(name)).timed().isPresent();
    }

    public String getError()
    {
        return errorDiv.isPresent() ? StringUtils.trimToNull(errorDiv.getText()) : null;
    }
    
    public String waitUntilError()
    {
        Poller.waitUntil(errorDiv.timed().isPresent(), Matchers.is(true));
        return StringUtils.trimToNull(errorDiv.getText());
    }

    private TimedQuery<Boolean> isSuggestionsPresent()
    {
        return getFirstSuggestion().timed().isVisible();
    }

    private PageElement getFirstSuggestion()
    {
        return elementFinder.find(By.cssSelector("#" + id + "-suggestions .aui-list-item"));
    }

    public static class Item
    {
        private final PageElement item;

        public Item(final PageElement item)
        {
            this.item = item;
        }

        public String getName()
        {
            return item.getAttribute("title");
        }

        public void remove()
        {
            item.find(By.className("item-delete")).click();
        }
    }

    protected PageElement getTextArea()
    {
        return textArea;
    }
}
