package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.pageobjects.elements.CheckboxElement;
import com.google.common.base.Function;

import javax.annotation.Nullable;

/**
 * Utilities related to {@link com.atlassian.pageobjects.elements.PageElement}.
 *
*/
public final class PageElementUtils
{

    private PageElementUtils()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static final Function<CheckboxElement, CheckboxElement> CHECK = new Function<CheckboxElement, CheckboxElement>()
    {
        public CheckboxElement apply(@Nullable CheckboxElement from)
        {
            return from.check();
        }
    };

    public static final Function<CheckboxElement, CheckboxElement> UNCHECK = new Function<CheckboxElement, CheckboxElement>()
    {
        public CheckboxElement apply(@Nullable CheckboxElement from)
        {
            return from.uncheck();
        }
    };
}
