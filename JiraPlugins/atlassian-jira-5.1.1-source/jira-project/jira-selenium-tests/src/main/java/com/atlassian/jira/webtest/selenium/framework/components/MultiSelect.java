package com.atlassian.jira.webtest.selenium.framework.components;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.Keys;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.core.PageObject;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import com.atlassian.webtest.ui.keys.TypeMode;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.webtest.ui.keys.Sequences.chars;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * Represents AJS.MultiSelect JavaScript control in the Selenium World&trade;. Occasionally MultiSelects may be referred
 * to as the 'pickers'.
 *
 * @param <T> target multi select component type
 * @since v4.2
 * @deprecated use extensions of {@link com.atlassian.jira.webtest.framework.component.fc.FrotherControl} instead
 */
@Deprecated
abstract class MultiSelect<T extends MultiSelect> extends AbstractSeleniumPageObject implements PageObject
{
    private final Class<T> targetType;
    protected final MultiSelectLocatorData locators;
    protected final Locator inputAreaLocator;

    private MultiSelectSuggestions<T> suggestions;

    /**
     * Create new MultiSelect with custom picker locator.
     *
     * @param targetType type of the inheriting component class
     * @param locators jquery locator collection of the represented MultiSelect control.
     * @param ctx Selenium context
     */
    protected MultiSelect(Class<T> targetType, MultiSelectLocatorData locators, SeleniumContext ctx)
    {
        super(ctx);
        this.targetType = notNull("targetType", targetType);
        this.locators = notNull("locators", locators);
        this.inputAreaLocator = SeleniumLocators.create(locators.textAreaLocator(), context);

    }

    /**
     * Factory method for the suggestions control.
     *
     * @return new instance of the suggestions drop-down part of this picker.
     */
    protected MultiSelectSuggestions<T> createSuggestions()
    {
        return new MultiSelectSuggestions<T>(this, locators, context);
    }


    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    public String locator()
    {
        return locators.mainLocator();
    }

    public String inputAreaLocator()
    {
        return locators.textAreaLocator();
    }

    public Locator inputLocatorObject()
    {
        return inputAreaLocator;
    }

    public String suggestionsLocator()
    {
        return locators.visibleSuggestionsLocator();
    }

    public String selectModelLocator()
    {
        return locators.selectModelLocator();
    }

    public String dropDownIconLocator()
    {
        return locators.dropDownIconLocator();
    }

    public String pickedElementLocator(String pickedElemValue)
    {
        return locator() + " ul.items li.item-row span.value-text:contains(" + pickedElemValue + ")";
    }

    public String anyPickedElementLocator()
    {
        return locator() + " ul.items li.item-row span.value-text";
    }

    public String anyFocusedPickedElementLocator()
    {
        return locator() + " ul.items li.item-row.focused";
    }

    public String focusedSuggestion()
    {
        return locators.visibleSuggestionsLocator() + " li.active";
    }

    /**
     * Returns jQuery locator in context of this MultiSelect control.
     *
     * @param locator raw jQuery locator
     * @return contextualized jQuery locator referring to <tt>locator</tt> withing context of this MultiSelect
     */
    public String locatorWithin(String locator)
    {
        return locators.inMultiSelect(locator);
    }

    private String bareMainLocator()
    {
        return Locators.removeLocatorPrefix(locator());
    }


    private String bareSuggestionsLocator()
    {
        return Locators.removeLocatorPrefix(suggestionsLocator());
    }

    private String bareFocusedSuggestionLocator()
    {
        return Locators.removeLocatorPrefix(focusedSuggestion());
    }

    /* ------------------------------------------------- QUERIES ------------------------------------------------- */

    // TODO implement
//    public List<String> getLozenges()
//    {
//        String result = new JqueryExecutor(client).execute("jQuery('" + bareMainLocator() + "ul.items li.item-row')");
//        return new ArrayList<String>(Arrays.asList(result.split(""));
//    }


    public int getLozengesCount()
    {
        return Integer.parseInt(client.getEval("window.jQuery('" + bareMainLocator() + " ul.items li.item-row').length"));
    }

    public int getFocusedLozengesCount()
    {
        return Integer.parseInt(client.getEval("window.jQuery('" + bareMainLocator() + " ul.items li.item-row.focused').length"));
    }

    public int getFocusedSuggestionsCount()
    {
        return Integer.parseInt(client.getEval("window.jQuery('" + bareSuggestionsLocator() + " li.active').length"));
    }

    public int getFocusedSuggestionIndex()
    {
        return Integer.parseInt(client.getEval("window.jQuery.inArray(window.jQuery('" + bareFocusedSuggestionLocator() + "')[0], "
                + "window.jQuery('" + bareSuggestionsLocator() +" li'))"));
    }

    public boolean hasAnyFocusedPickedElement()
    {
        return client.isElementPresent(anyFocusedPickedElementLocator());
    }

    /* ------------------------------------------------- COMPONENTS ------------------------------------------------- */

    public MultiSelectSuggestions<T> suggestions()
    {
        if (suggestions == null)
        {
            suggestions = createSuggestions();
        }
        return suggestions;
    }
    

    /* --------------------------------------------------- ACTIONS -------------------------------------------------- */

    public T focusOnInputArea()
    {
        client.focus(inputAreaLocator());
        return asTargetType();
    }

    public T insertQuery(String query)
    {

        context.ui().typeInLocator(inputAreaLocator(), chars(query));
        return asTargetType();
    }

    /**
     * Will invoke all picker events only for the last character in the <tt>query</tt>.
     **
     * @param query query to insert
     * @return this picker instance
     */
    public T insertQueryFast(String query)
    {
        context.ui().typeInLocator(inputAreaLocator(), asFastSequence(query));
        return asTargetType();
    }

    private KeySequence asFastSequence(String query)
    {
        return charsBuilder(query).typeMode(TypeMode.INSERT_WITH_EVENT).build();
    }

    public T deleteSelection()
    {
        focusOnInputArea();
        inputAreaLocator.element().type(Keys.BACKSPACE);
        return asTargetType();
    }

    public T selectAll()
    {
        focusOnInputArea();
        client.controlKeyDown();
        inputAreaLocator.element().type(chars("a"));
        client.controlKeyUp();
        return asTargetType();
    }

    public T prevSuggestion()
    {
        inputAreaLocator.element().type(SpecialKeys.ARROW_UP);
        return asTargetType();
    }

    public T nextSuggestion()
    {
        inputAreaLocator.element().type(SpecialKeys.ARROW_DOWN);
        return asTargetType();
    }

    /**
     * Clear input area from any previously inserted text.
     *
     * @return this picker instance
     */
    public T clearInputArea()
    {
        client.type(inputAreaLocator(), "");
        return insertQuery("");
    }

    /**
     * Removes focus from the input area of the picker, which will trigger processing of the
     * previously inserted queries and close the suggestions drop-down.
     *
     * @return this picker instance
     */
    public T awayFromInputArea()
    {
        client.fireEvent(inputAreaLocator(), "blur");
        // this will wait for the suggestions to close, which should be the last thing done by the 'onblur' event
        suggestions().assertClosed(700);
        return asTargetType();
    }

    /**
     * Press enter in the input area to confirm current option (the one selected in suggestions drop-down).
     *
     * @return this picker instance
     */
    public T confirmInput()
    {
        inputAreaLocator.element().type(SpecialKeys.ENTER);
        // this will wait for the suggestions to close, which should be the last thing done after pressing 'enter'
        suggestions().assertClosed(500);
        return asTargetType();
    }

    public MultiSelectSuggestions<T> triggerSuggestionsByArrowDown()
    {
        focusOnInputArea();
        inputAreaLocator.element().type(SpecialKeys.ARROW_DOWN);
        suggestions().assertOpen(500);
        return suggestions;
    }

    public MultiSelectSuggestions<T> triggerSuggestionsByClick()
    {
        client.mouseDown(dropDownIconLocator());
        client.click(dropDownIconLocator());
        suggestions().assertOpen(500);
        return suggestions();
    }

    T asTargetType()
    {
        return targetType.cast(this);
    }

    /* -------------------------------------------------- ASSERTIONS ------------------------------------------------ */

    /**
     * {@inheritDoc}
     *
     */
    public void assertReady(long timeout)
    {
        assertThat.elementPresentByTimeout(locator(), timeout);
    }

    public void assertNoElementPicked()
    {
        assertThat.elementNotPresentByTimeout(anyPickedElementLocator(), 2000);
    }

    public void assertElementPicked(String value)
    {
        assertThat.elementPresentByTimeout(pickedElementLocator(value), 5000);
    }

    public void assertElementNotPicked(String value)
    {
        assertThat.elementNotPresentByTimeout(pickedElementLocator(value), 2000);
    }


}
