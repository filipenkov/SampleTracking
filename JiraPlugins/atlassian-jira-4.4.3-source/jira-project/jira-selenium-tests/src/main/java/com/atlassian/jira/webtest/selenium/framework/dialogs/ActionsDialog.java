package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocators;
import com.atlassian.jira.webtest.framework.util.Timeout;
import com.atlassian.jira.webtest.selenium.framework.model.CancelType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import com.atlassian.webtest.ui.keys.TypeMode;
import junit.framework.AssertionFailedError;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.Common.BODY;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;

/**
 * Test utility representing the Dot Dialog.
 *
 * @since v4.2
 */
public class ActionsDialog extends AbstractDialog<ActionsDialog>
{
    // TODO clean up mess

    private final String ISSUEACTIONS_SUGGESTIONS_LOCATOR = "jquery=#issueactions-suggestions";
    private final String VISIBLE_ISSUEACTIONS_SUGGESTIONS_LOCATOR = ISSUEACTIONS_SUGGESTIONS_LOCATOR + ":visible";
    private final String JQUERY_FIELD = "jquery=#issue-actions-dialog .text";

    private static final int WAIT_TIME = 10000;
    private static final int YOU_HAD_BETTER_NOT_HAVE_MORE_ISSUES_IN_YOUR_ISSUENAV = 10;

    private Locator inputLocatorObject;

    public ActionsDialog(SeleniumContext ctx)
    {
        super(ctx, ActionsDialog.class);
        this.inputLocatorObject = SeleniumLocators.create(inputAreaLocator(), context);
    }


    public ActionsDialog openFromViewIssue()
    {
        open();
        return this;
    }

    public ActionsDialog openFromIssueNav(int issueId)
    {
        // TODO this is not tested yet, so probably won't work ;)
        assertThat.elementPresentByTimeout(issueRowSelector(issueId), WAIT_TIME);
        if (!runThroughIssues(issueId, "j") || !runThroughIssues(issueId, "k"))
        {
            throw new AssertionFailedError("Could not find issue with ID=" + issueId + " on the page");   
        }
        open();
        return this;
    }

    private boolean runThroughIssues(int issueId, String runKey)
    {
        for (int i=0; i<YOU_HAD_BETTER_NOT_HAVE_MORE_ISSUES_IN_YOUR_ISSUENAV && !rowFocused(issueId); i++)
        {
            client.keyPress(BODY, runKey);
        }
        return rowFocused(issueId);
    }

    private boolean rowFocused(final int issueId)
    {
        Timeout.waitFor(50).milliseconds();
        return client.isElementPresent(issueRowSelector(issueId) + ".focused");
    }

    public ActionsDialog open()
    {
        assertThat.elementPresentByTimeout(BODY, WAIT_TIME);
        client.focus(BODY);
        context.ui().pressInBody(Shortcuts.DOT_DIALOG);
        assertThat.elementPresentByTimeout(JQUERY_FIELD, WAIT_TIME);
        return this;
    }

    private String issueRowSelector(final int issueId)
    {
        return "jquery=#issuerow" + issueId;
    }


    public void close()
    {
        while (client.isVisible("jquery=#issue-actions-dialog")) {
            client.focus("jquery=#issue-actions-dialog");
            client.click("jquery=#issue-actions-dialog");
            CancelType.BY_ESCAPE.execute(client, null);
            Timeout.waitFor(200).milliseconds();
        }
        assertNotOpen();
    }

    /**
     * Assert that this dialog is currently not open on the page.
     *
     */
    public final void assertNotOpen() {
        assertThat.elementNotPresentByTimeout(AbstractIssueDialog.VISIBLE_DIALOG_CONTENT_SELECTOR, context.timeouts().dialogLoad());
    }

    public final void closeDropDown()
    {
        if (client.isElementPresent(dropDownLocator()))
        {
            context.ui().pressInBody(SpecialKeys.ESC);
        }
    }

    protected String dropDownLocator()
    {
        return ISSUEACTIONS_SUGGESTIONS_LOCATOR;
    }

    public String inputAreaLocator()
    {
        return JQUERY_FIELD;    
    }

    public Locator inputLocatorObject()
    {
        return inputLocatorObject;
    }

    public void assertNoSuggestions()
    {
        assertThat.elementPresentByTimeout(VISIBLE_ISSUEACTIONS_SUGGESTIONS_LOCATOR + " .no-suggestions", context.timeoutFor(Timeouts.COMPONENT_LOAD));
    }

    public void assertStringIsBolded (String str)
    {
        assertThat.elementPresentByTimeout(ISSUEACTIONS_SUGGESTIONS_LOCATOR + " ul li:not(.hidden) em:contains(" + str + ")");
    }

    public ActionsDialog queryActions(String query)
    {
        return queryActions(query, true);
    }

    public ActionsDialog queryActions(String query, boolean reset)
    {
        if (reset)
        {
            inputLocatorObject.element().clear();
        }
        context.ui().typeInLocator(JQUERY_FIELD, fastSequence(query));
        return this;
    }

    private KeySequence fastSequence(String quuery)
    {
        return charsBuilder(quuery).typeMode(TypeMode.INSERT_WITH_EVENT).build();
    }

    private void assertSingleSuggestionPresent(final String suggestion)
    {
        assertThat.elementPresentByTimeout(ISSUEACTIONS_SUGGESTIONS_LOCATOR + " li:not(.hidden):contains(" + suggestion + ")");

    }

    public void assertSuggestionPresent(final String suggestion, final String... moreSuggestions)
    {
        assertSingleSuggestionPresent(suggestion);
        for (String more : moreSuggestions)
        {
            assertSingleSuggestionPresent(more);
        }
    }

    private void assertSingleSuggestionNotPresent(final String suggestion)
    {
        assertThat.elementNotPresentByTimeout(ISSUEACTIONS_SUGGESTIONS_LOCATOR + " li:not(.hidden):contains(" + suggestion + ")");
    }

    public void assertSuggestionNotPresent(final String suggestion, final String... moreSuggestions)
    {
        assertSingleSuggestionNotPresent(suggestion);
        for (String more : moreSuggestions)
        {
            assertSingleSuggestionNotPresent(more);
        }
    }

    public void selectSuggestionUsingClick()
    {
        client.click("css=#issueactions-suggestions" + " li.active a");
    }

    public void selectActionUsingEnter()
    {
        Timeout.waitFor(300).milliseconds();
        client.focus(JQUERY_FIELD);
        context.ui().pressInBody(SpecialKeys.ENTER);
    }

    public void assertSuggestionPresent(final String suggestion, final boolean focused)
    {
        if (focused)
        {
            assertThat.elementPresentByTimeout(ISSUEACTIONS_SUGGESTIONS_LOCATOR + " li.active:not(.hidden):contains(" + suggestion + ")");
        }
        else
        {
            assertSuggestionPresent(suggestion);
        }
    }

    @Override
    protected String visibleDialogContentsLocator()
    {
        return AbstractIssueDialog.DIALOG_CONTENT_READY_SELECTOR;
    }

    @Override
    protected String dialogContentsReadyLocator()
    {
        return JQUERY_FIELD;
    }

    public boolean isOpenable()
    {
        throw new UnsupportedOperationException("not implemented");
    }

    public boolean isSuggestionsOpen()
    {
        return client.isElementPresent("jquery=body > div.ajs-layer.active #issueactions-suggestions");
    }
}
