package com.atlassian.jira.webtest.framework.component.fc;


import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * Issue picker - Frother Control used to find and select JIRA issues. 
 *
 * @author Dariusz Kordonski
 */
public interface IssuePicker extends FrotherControl<IssuePicker, IssuePicker.Suggestions, IssuePicker.Input>
{
    /**
     * Represents section that may show up in this picker's suggestions.
     *
     */
    static enum Sections
    {
        HISTORY_SEARCH("history-search"),
        CURRENT_SEARCH("current-search"),
        USER_INPUT("user-inputted-option");


        private final String id;

        Sections(String id)
        {
            this.id = id;
        }

        public String id()
        {
            return id;
        }
    }

    static interface CountableSection extends AjsDropdown.Section<IssuePicker>
    {
        int currentCount();

        int totalCount();
    }

    static interface Suggestions extends FcSuggestions<IssuePicker>, FrotherControlComponent<IssuePicker, Suggestions, Input>
    {
        TimedCondition hasHistorySearch();

        TimedCondition hasCurrentSearch();

        TimedCondition hasUserInputOption();

        TimedCondition hasSection(Sections section);

        TimedQuery<CountableSection> historySearch();

        TimedQuery<CountableSection> currentSearch();

        TimedQuery<Section<IssuePicker>> userInput();

        TimedQuery<Section<IssuePicker>> section(Sections section);
    }

    static interface Input extends FcInput<Input,IssuePicker,Suggestions>,
            FrotherControlComponent<IssuePicker, Suggestions, Input>
    {
    }


    // TODO add 'legacy' issue popup triggering - it is a part of every issue picker
}
