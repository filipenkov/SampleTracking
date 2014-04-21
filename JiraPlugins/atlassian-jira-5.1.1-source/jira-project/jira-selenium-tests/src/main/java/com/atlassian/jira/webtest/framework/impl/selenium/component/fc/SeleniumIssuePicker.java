package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.fc.IssuePicker;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.fc.IssuePicker}.
 *
 * @author Dariusz Kordonski
 */
public class SeleniumIssuePicker extends AbstractSeleniumFrotherControl<IssuePicker, IssuePicker.Suggestions, IssuePicker.Input>
        implements IssuePicker
{

    private final IssuePickerInput input;

    public SeleniumIssuePicker(String fieldId, SeleniumContext context)
    {
        super(fieldId, context);
        this.input = new IssuePickerInput(fieldId, context);
    }

    @Override
    public IssuePicker.Input input()
    {
        return input;
    }

    @Override
    public IssuePicker.Suggestions suggestions()
    {
        return null;
    }

    private class IssuePickerInput extends AbstractSeleniumFcInput<IssuePicker.Input, IssuePicker, IssuePicker.Suggestions>
            implements Input
    {
            protected IssuePickerInput(String fieldId, SeleniumContext context)
            {
                super(fieldId, SeleniumIssuePicker.this, context);
            }

        @Override
        public Suggestions suggestions()
        {
            return parent().suggestions();
        }

        @Override
        protected Input asTargetType()
        {
            return this;
        }
    }
}
