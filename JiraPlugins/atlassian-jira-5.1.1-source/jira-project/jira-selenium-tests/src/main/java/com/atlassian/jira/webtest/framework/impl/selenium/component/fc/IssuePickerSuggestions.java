package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.fc.IssuePicker.Suggestions}.
 *
 * @since 4.3
 */
public class IssuePickerSuggestions extends AbstractFcSuggestions<IssuePicker,IssuePicker.Suggestions,IssuePicker.Input>
        implements IssuePicker.Suggestions
{

    public IssuePickerSuggestions(String fieldId, IssuePicker parent, SeleniumContext ctx)
    {
        super(fieldId, parent, ctx);
    }


    @Override
    public TimedCondition hasHistorySearch()
    {
        return hasSection(IssuePicker.Sections.HISTORY_SEARCH);
    }

    @Override
    public TimedCondition hasCurrentSearch()
    {
        return hasSection(IssuePicker.Sections.CURRENT_SEARCH);
    }

    @Override
    public TimedCondition hasUserInputOption()
    {
        return hasSection(IssuePicker.Sections.USER_INPUT);
    }

    @Override
    public TimedCondition hasSection(IssuePicker.Sections section)
    {
        return hasSection(section.id());
    }

    @Override
    public TimedQuery<IssuePicker.CountableSection> historySearch()
    {
        return Queries.transform(section(IssuePicker.Sections.HISTORY_SEARCH), new SectionTransformer());
    }

    @Override
    public TimedQuery<IssuePicker.CountableSection> currentSearch()
    {
        return Queries.transform(section(IssuePicker.Sections.CURRENT_SEARCH), new SectionTransformer());
    }

    @Override
    public TimedQuery<Section<IssuePicker>> userInput()
    {
        return section(IssuePicker.Sections.USER_INPUT);
    }

    @Override
    public TimedQuery<Section<IssuePicker>> section(IssuePicker.Sections section)
    {
        return section(section.id());
    }

    private class SectionTransformer implements Function<Section<IssuePicker>, IssuePicker.CountableSection>
    {
        @Override
        public IssuePicker.CountableSection get(Section<IssuePicker> input)
        {
            return input != null ? new SeleniumCountableSection(input, context()) : null;
        }
    }
}
