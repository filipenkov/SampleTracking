package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.fc.FcInput;
import com.atlassian.jira.webtest.framework.component.fc.FcSuggestions;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControl;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControlComponent;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.component.AbstractSeleniumDropdown;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.fc.IssuePicker.Suggestions}.
 *
 * @since 4.3
 */
public abstract class AbstractFcSuggestions<F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>, I extends FcInput<I,F,S>> 
        extends AbstractSeleniumDropdown<F> implements FcSuggestions<F>, FrotherControlComponent<F,S,I>
{

    protected AbstractFcSuggestions(String fieldId, F parent, SeleniumContext ctx)
    {
        super(fieldId, parent, ctx);
    }

    @Override
    protected TimedCondition isOpenableByContext()
    {
        // TODO better?
        return parent().isReady();
    }


    @Override
    public final FcSuggestions<F> open()
    {
        if (isOpenable().byDefaultTimeout())
        {
            return parent().input().clickDropIcon();
        }
        else
        {
            throw new IllegalStateException("Already open or unable to open");
        }
    }


    @Override
    public final F fc()
    {
        return parent();
    }
}
