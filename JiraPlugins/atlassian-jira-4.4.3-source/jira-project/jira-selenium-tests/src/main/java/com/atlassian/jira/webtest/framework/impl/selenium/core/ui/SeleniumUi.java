package com.atlassian.jira.webtest.framework.impl.selenium.core.ui;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.WebTestUi;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.StringLocators;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.TypeMode;

import java.util.Map;
import java.util.WeakHashMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.ConditionAssertions.byDefaultTimeout;
import static com.atlassian.jira.webtest.framework.core.TimedAssertions.assertThat;
import static com.atlassian.webtest.ui.keys.Sequences.chars;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.core.ui.WebTestUi}.
 *
 * @since v4.3
 */
public class SeleniumUi extends SeleniumContextAware implements WebTestUi
{
    private final Locator body;
    private final Map<String,Locator> locatorCache = new WeakHashMap<String, Locator>();
    private final TargetSelector target = new TargetSelectorImpl();
    private Locator currentFrame = null;
    private String currentWindow = null;

    public SeleniumUi(SeleniumContext context)
    {
        super(context);
        this.body = SeleniumLocators.css("body", context()).withDefaultTimeout(Timeouts.COMPONENT_LOAD);
    }

    @Override
    public Locator body()
    {
        return body;
    }

    @Override
    public SeleniumUi pressInBody(KeySequence sequence)
    {
        assertThat(body.element().isPresent(), byDefaultTimeout());
        body.element().type(sequence);
        return this;
    }

    /**
     * Selenium-specific method to type given {@link com.atlassian.webtest.ui.keys.KeySequence} in a given locator.
     * Relieves tests from using {@link com.atlassian.selenium.keyboard.SeleniumTypeWriter} directly. 
     *
     * @param locator Selenium-style locator (in the 'type=value' form) to type into
     * @param sequence sequence to type
     * @return this UI instance
     */
    public SeleniumUi typeInLocator(String locator, KeySequence sequence)
    {
        getLocator(locator).element().type(sequence);
        return this;
    }

    public SeleniumUi clear(String locator)
    {
        getLocator(locator).element().clear();
        return this;
    }

    /**
     * Selenium-specific method to type sequence of characters in a given locator.
     * Relieves tests from using {@link com.atlassian.selenium.keyboard.SeleniumTypeWriter} directly.
     *
     * @param locator Selenium-style locator (in the 'type=value' form) to type into
     * @param chars character sequence to type
     * @return this UI instance
     */
    public SeleniumUi typeChars(String locator, String chars)
    {
        getLocator(locator).element().type(chars(chars));
        return this;
    }

    /**
     * <p>
     * Selenium-specific method to type sequence of characters in a given locator.
     * Relieves tests from using {@link com.atlassian.selenium.keyboard.SeleniumTypeWriter} directly.
     *
     * <p>
     * Uses {@link com.atlassian.webtest.ui.keys.TypeMode#INSERT_WITH_EVENT} to enter characters faster.
     *
     * @param locator Selenium-style locator (in the 'type=value' form) to type into
     * @param chars character sequence to type
     * @return this UI instance
     * @see com.atlassian.webtest.ui.keys.TypeMode#INSERT_WITH_EVENT
     */
    public SeleniumUi typeCharsFast(String locator, String chars)
    {
        getLocator(locator).element().type(charsBuilder(chars).typeMode(TypeMode.INSERT_WITH_EVENT).build());
        return this;
    }

    @Override
    public TargetSelector switchTo()
    {
        return target;
    }

    private Locator getLocator(String locatorString)
    {
        Locator answer = locatorCache.get(locatorString);
        if (answer == null)
        {
            answer = SeleniumLocators.create(locatorString, context());
            locatorCache.put(locatorString, answer);
            return locatorCache.get(locatorString);
        }
        return answer;
    }


    private class TargetSelectorImpl implements TargetSelector
    {

        @Override
        public WebTestUi mainWindow()
        {
            if (currentWindow != null)
            {
                currentWindow = null;
                client.selectWindow(null);
            }
            return SeleniumUi.this;
        }

        @Override
        public WebTestUi window(String name)
        {
            if (!isInWindow(notNull("name", name)))
            {
                currentWindow = name;
                client.selectWindow(name);
            }
            return SeleniumUi.this;
        }

        @Override
        public WebTestUi frame(Locator locator)
        {
            if (!isInFrame(notNull("locator", locator)))
            {
                currentFrame = locator;
                client.selectFrame(StringLocators.create(locator));
            }
            return SeleniumUi.this;
        }

        @Override
        public WebTestUi topFrame()
        {
            if (currentFrame != null)
            {
                currentFrame = null;
                client.selectFrame("relative=top");
            }
            return null;
        }
    }

    private boolean isInWindow(String windowId)
    {
        return currentWindow != null && currentWindow.equals(windowId);
    }

    private boolean isInFrame(Locator frameLocator)
    {
        return currentFrame != null && currentFrame.equals(frameLocator);
    }

}
