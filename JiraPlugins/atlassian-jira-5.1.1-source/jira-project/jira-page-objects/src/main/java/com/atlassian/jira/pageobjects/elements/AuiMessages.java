package com.atlassian.jira.pageobjects.elements;

import com.atlassian.jira.pageobjects.framework.util.JiraLocators;
import com.atlassian.pageobjects.elements.PageElement;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

/**
 * Utilities for finding and manipulating AUI messages on JIRA forms.
 *
 * @since 5.1
 */
public final class AuiMessages
{
    public static final String AUI_MESSAGE_CLASS = JiraLocators.CLASS_AUI_MESSAGE;

    public static final String AUI_MESSAGE_ERROR_SELECTOR = "." + JiraLocators.CLASS_AUI_MESSAGE + "." + AuiMessage.Type.ERROR.className();
    // TODO: other selectors as necessary

    private AuiMessages()
    {
        throw new AssertionError("Don't instantiate me");
    }

    /**
     * Predicate checking for AUI message of any type.
     *
     * @return prediacte for finding AUI message page elements
     */
    public static Predicate<PageElement> isAuiMessage()
    {
        return new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable PageElement input)
            {
                return input.hasClass(AUI_MESSAGE_CLASS);
            }
        };
    }

    public static Predicate<PageElement> isAuiMessageOfType(final AuiMessage.Type type)
    {
        if (!type.isClassifiable())
        {
            throw new IllegalArgumentException("Cannot look for unclassifiable type: <" + type + ">");
        }
        return Predicates.and(isAuiMessage(), new Predicate<PageElement>()
        {
            @Override
            public boolean apply(@Nullable PageElement input)
            {
                return input.hasClass(type.className());
            }
        });
    }
}
