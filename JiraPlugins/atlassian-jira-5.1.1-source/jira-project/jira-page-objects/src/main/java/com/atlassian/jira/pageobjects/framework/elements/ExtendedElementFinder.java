package com.atlassian.jira.pageobjects.framework.elements;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.hamcrest.Matcher;
import org.openqa.selenium.By;

import java.util.NoSuchElementException;

import static com.atlassian.jira.util.lang.GuavaPredicates.forMatcher;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Finds with filtering
 *
 * @since v5.0
 */
public final class ExtendedElementFinder
{

    public static ExtendedElementFinder forFinder(PageElementFinder finder)
    {
        return new ExtendedElementFinder(finder);
    }

    private final PageElementFinder elementFinder;

    private ExtendedElementFinder(PageElementFinder elementFinder)
    {
        this.elementFinder = checkNotNull(elementFinder);
    }

    public PageElement find(By by, Predicate<PageElement> filter)
    {
        // TODO retarded. Need to upgrade Guava in JIRA to use the .find() with default value param!
        try
        {
            return Iterables.find(elementFinder.findAll(by), filter);
        }
        catch(NoSuchElementException e)
        {
            return null;
        }
    }

    public PageElement find(By by, Matcher<PageElement> filter)
    {
        return find(by, forMatcher(filter));
    }

    public Iterable<PageElement> findAll(By by, Predicate<PageElement> filter)
    {
        return ImmutableList.copyOf(Iterables.filter(elementFinder.findAll(by), filter));
    }

    public Iterable<PageElement> findAll(By by, Matcher<PageElement> matcher)
    {
        return findAll(by, forMatcher(matcher));
    }
}


