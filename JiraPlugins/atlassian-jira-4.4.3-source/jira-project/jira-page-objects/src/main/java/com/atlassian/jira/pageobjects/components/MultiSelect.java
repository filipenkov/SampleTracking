package com.atlassian.jira.pageobjects.components;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

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
    private String id;

    @Init
    public void initialize()
    {
        this.selectDiv =  elementFinder.find(By.id(id + "-multi-select"));
        this.textArea = selectDiv.find(By.id(id + "-textarea"));
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
    }

    /**
     * Adds an item by typing it in and picking the first suggestion. Assumes that the item passed in
     * will be used as the lozenge label
     *
     * @param item the item to add
     */
    public void add(final String item)
    {
        textArea.type(item);
        textArea.type(Keys.DOWN);
        waitUntilTrue("Expected suggestions to be present, but was not", isSuggestionsPresent());
        getFirstSuggestion().click();
        waitUntilTrue("Expected item " + item + "to be added, but was not", hasItem(item));
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
        final PageElement element = selectDiv.find(getItemLocator(name));
        return pageBinder.bind(Item.class, element);
    }

    public List<Item> getItems()
    {
        final List<Item> items = Lists.newArrayList();
        final List<PageElement> itemElements = selectDiv.findAll(By.cssSelector(".representation li"));
        for (final PageElement itemElement : itemElements)
        {
            items.add(pageBinder.bind(Item.class, itemElement));
        }
        return items;
    }


    public TimedQuery<Boolean> hasItems()
    {
        return selectDiv.find(By.cssSelector(".representation li")).timed().isPresent();
    }

    public TimedQuery<Boolean> hasItem(final String name)
    {
        return selectDiv.find(getItemLocator(name)).timed().isPresent();
    }

    private TimedQuery<Boolean> isSuggestionsPresent()
    {
        return getFirstSuggestion().timed().isPresent();
    }

    private PageElement getFirstSuggestion()
    {
        return elementFinder.find(By.cssSelector("#" + id + "-suggestions .aui-list-item"));
    }

    private static By getItemLocator(final String name)
    {
        return By.cssSelector(".representation li[title=\"" + name + "\"]");
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

}
