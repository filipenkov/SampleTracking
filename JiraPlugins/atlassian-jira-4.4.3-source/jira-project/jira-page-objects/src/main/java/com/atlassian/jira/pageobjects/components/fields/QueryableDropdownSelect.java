package com.atlassian.jira.pageobjects.components.fields;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.AbstractTimedQuery;
import com.atlassian.pageobjects.elements.query.ExpirationHandler;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import javax.inject.Inject;
import java.util.List;

import static com.google.common.collect.Lists.transform;

/**
 * @since v4.4
 */
public class QueryableDropdownSelect implements AutoComplete
{
    @Inject
    private PageElementFinder elementFinder;

    private final By containerSelector;
    private final By suggestionsSelector;

    protected PageElement container;
    protected PageElement field;
    protected PageElement suggestions;
    protected PageElement icon;
    protected PageElement label;

    public QueryableDropdownSelect(final By containerSelector, final By suggestionsSelector)
    {
        this.containerSelector = containerSelector;
        this.suggestionsSelector = suggestionsSelector;
    }

    @Init
    public void getElements()
    {
        container = elementFinder.find(containerSelector, TimeoutType.AJAX_ACTION);
        field = container.find(By.tagName("input"));
        icon = container.find(By.className("icon"));
        label = container.find(By.tagName("label"));
    }

    public boolean isPresent()
    {
        return container.isPresent();
    }

    public AutoComplete query(final String query)
    {
        this.field.type(query);
        Poller.waitUntilTrue(container.timed().hasAttribute("data-query", query));
        return this;
    }

    public AutoComplete down(final int steps) {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.DOWN);
        }
        return this;
    }

    public AutoComplete up(final int steps) {
        for (int i =0; i < steps; i++) {
            this.field.type(Keys.UP);
        }
        return this;
    }

    @Override
    public AutoComplete acceptUsingMouse(final Suggestion suggestion)
    {
        suggestion.click();
        return this;
    }

    @Override
    public AutoComplete acceptUsingKeyboard(Suggestion suggestion)
    {
        field.type(Keys.RETURN);
        return this;
    }

    public PageElement getLabel()
    {
        return label;
    }

    @Override
    public Suggestion getActiveSuggestion()
    {
        PageElement suggestionElement = getSuggestionsContainerElement().find(By.className("active"));
        return new Suggestion(suggestionElement);
    }

    @Override
    public TimedQuery<Suggestion> getTimedActiveSuggestion()
    {
        return new AbstractTimedQuery<Suggestion>(container.timed().isPresent(), ExpirationHandler.RETURN_CURRENT)
        {
            @Override
            protected boolean shouldReturn(Suggestion currentEval)
            {
                return true;
            }

            @Override
            protected Suggestion currentValue()
            {
                return getActiveSuggestion();
            }
        };
    }

    private PageElement getSuggestionsContainerElement()
    {
        return elementFinder.find(suggestionsSelector);
    }

    private List<PageElement> getSuggestionsElements()
    {
        return getSuggestionsContainerElement().findAll(By.tagName("li"));
    }

    
    public List<Suggestion> getSuggestions()
    {
        return transform(getSuggestionsElements(), Suggestion.BUILDER);
    }

    @Override
    public TimedQuery<List<Suggestion>> getTimedSuggestions()
    {
        return new AbstractTimedQuery<List<Suggestion>>(container.timed().isPresent(), ExpirationHandler.RETURN_CURRENT)
        {
            @Override
            protected boolean shouldReturn(List<Suggestion> currentEval)
            {
                return true;
            }

            @Override
            protected List<Suggestion> currentValue()
            {
                return getSuggestions();
            }
        };
    }
}
