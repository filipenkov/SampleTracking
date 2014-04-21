package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.fc.FcInput;
import com.atlassian.jira.webtest.framework.component.fc.FcSuggestions;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControl;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * Abstract implementation of {@link com.atlassian.jira.webtest.framework.component.fc.FrotherControl}
 *
 * @author Dariusz Kordonski
 */
public abstract class AbstractSeleniumFrotherControl<F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>,
        I extends FcInput<I,F,S>> extends AbstractLocatorBasedPageObject implements FrotherControl<F,S,I>
{
    private final SeleniumLocator mainLocator;
    private final SeleniumLocator selectModelLocator;

    protected AbstractSeleniumFrotherControl(String fieldId, SeleniumContext context)
    {
        super(context);
        this.mainLocator = id(fieldId + "-multi-select");
        this.selectModelLocator = mainLocator.combine(css("select#" + fieldId + ":hidden"));
    }


    @Override
    public Locator locator()
    {
        return mainLocator;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return mainLocator;
    }

    @Override
    public Locator inputAreaLocator()
    {
        return input().locator();
    }

    @Override
    public Locator suggestionsLocator()
    {
        return suggestions().locator();
    }

    @Override
    public Locator selectModelLocator()
    {
        return selectModelLocator;
    }

}
