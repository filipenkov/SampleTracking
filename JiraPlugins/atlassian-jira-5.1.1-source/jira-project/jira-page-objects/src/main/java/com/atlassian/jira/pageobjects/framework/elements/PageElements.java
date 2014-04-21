package com.atlassian.jira.pageobjects.framework.elements;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Predicates and functions for page elements.
 *
 * @since v5.0
 */
public final class PageElements
{
    public static final String BODY = "body";
    public static final String TR = "tr";
    public static final String TD = "td";

    private PageElements()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static Predicate<PageElement> hasClass(final String className)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.hasClass(className);
            }
        };
    }

    public static Predicate<PageElement> hasDataAttribute(final String attribute)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.getAttribute("data-" + attribute) != null;
            }
        };
    }

    public static Predicate<PageElement> hasDataAttribute(final String attribute, final String value)
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return input.hasAttribute("data-" + attribute, value);
            }
        };
    }

    public static Predicate<PageElement> hasValue(@Nonnull final String value)
    {
        checkNotNull(value, "value");
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(PageElement input)
            {
                return value.equals(input.getValue());
            }
        };
    }

    /**
     * Binds 'simple' page objects that take one constructor parameter (page elements), e.g. table rows etc.
     *
     * @param binder page binder
     * @param pageObjectClass target page object class
     * @param <P> page object type
     * @return page binding function
     */
    public static <P> Function<PageElement,P> bind(final PageBinder binder, final Class<P> pageObjectClass)
    {
        return new Function<PageElement, P>()
        {
            @Override
            public P apply(PageElement input)
            {
                return binder.bind(pageObjectClass, input);
            }
        };
    }


}
