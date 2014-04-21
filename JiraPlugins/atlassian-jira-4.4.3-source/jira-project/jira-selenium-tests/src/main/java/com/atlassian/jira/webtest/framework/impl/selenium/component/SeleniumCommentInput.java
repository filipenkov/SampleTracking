package com.atlassian.jira.webtest.framework.impl.selenium.component;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.component.CommentInput;
import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.AbstractLocatorBasedPageObject;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumInput;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.webtest.ui.keys.KeySequence;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.and;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.component.CommentInput}.
 *
 * @since v4.3
 */
public class SeleniumCommentInput extends AbstractLocatorBasedPageObject implements CommentInput
{
    private static final String COMMENT_TEXTAREA_ID = "comment";
    private static final String SECURITY_LEVEL_DROPDOWN_ID = "commentLevel-suggestions";

    private final SeleniumLocator main;
    private final SeleniumLocator visibilityDropDownIcon;
    private final SeleniumLocator wikiToolsLocator;
    private final SeleniumLocator wikiPreviewLinkLocator;
    private final Input commentInput;
    private final SeleniumVisibilityDropdown dropDown;

    public SeleniumCommentInput(SeleniumLocator locator, SeleniumContext ctx)
    {
        super(ctx);
        this.main = notNull("mainLocator", locator);
        this.commentInput = new SeleniumInput(main.combine(id(COMMENT_TEXTAREA_ID)), context());
        this.dropDown = new SeleniumVisibilityDropdown(SECURITY_LEVEL_DROPDOWN_ID, context());
        this.visibilityDropDownIcon = main.combine(css("span.icon.drop-menu"));
        this.wikiToolsLocator = main.combine(css("div.field-tools"));
        this.wikiPreviewLinkLocator = wikiToolsLocator.combine(id("comment-preview_link"));
    }

    @Override
    public Locator locator()
    {
        return main;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return main;
    }

    @Override
    public CommentVisibilityDropdown visibilityDropdown()
    {
        return dropDown;
    }

    @Override
    public TimedCondition isEditMode()
    {
        return not(isPreviewMode());
    }

    @Override
    public TimedCondition isPreviewMode()
    {
        return and(hasWikiRendering(),conditions().hasClass(wikiPreviewLinkLocator,"selected"));
    }

    @Override
    public TimedCondition hasWikiRendering()
    {
        return wikiToolsLocator.element().isPresent();
    }

    @Override
    public CommentInput toggleMode()
    {
        wikiPreviewLinkLocator.element().click();
        return this;
    }

    @Override
    public Input type(KeySequence keys)
    {
        commentInput.type(keys);
        return this;
    }

    @Override
    public TimedQuery<String> value()
    {
        return commentInput.value();
    }

    private class SeleniumVisibilityDropdown extends AbstractSeleniumDropdown<CommentInput> implements CommentVisibilityDropdown
    {
        protected SeleniumVisibilityDropdown(String id, SeleniumContext ctx)
        {
            super(id, SeleniumCommentInput.this, ctx);
        }

        @Override
        protected TimedCondition isOpenableByContext()
        {
            return parent().isReady();
        }

        @Override
        public AjsDropdown<CommentInput> open()
        {
            if (isOpenable().byDefaultTimeout())
            {
                visibilityDropDownIcon.element().click();
                return this;
            }
            return null;
        }

        @Override
        public TimedQuery<Section<CommentInput>> groupsSection()
        {
            return section("groups");
        }

        @Override
        public TimedQuery<Section<CommentInput>> rolesSection()
        {
            return section("roles");
        }

        @Override
        public TimedQuery<Item<CommentInput>> allUsersItem()
        {
            return item("All Users");
        }
    }
}
