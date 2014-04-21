package com.atlassian.jira.webtest.selenium.framework.dialogs;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.condition.IsPresentCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.selenium.framework.core.AbstractSeleniumPageObject;
import com.atlassian.jira.webtest.selenium.framework.model.Locators;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.webtest.framework.core.condition.Conditions.not;
import static com.atlassian.jira.webtest.selenium.framework.model.Locators.removeLocatorPrefix;

/**
 * Abstract implementation of {@link Dialog} in terms of a locator marking this dialog's
 * visible contents. The locator is provided by concrete implementations of dialogs.
 *
 * @since v4.2
 */
public abstract class AbstractDialog<T extends AbstractDialog<T>> extends AbstractSeleniumPageObject
        implements Dialog
{

    private final Class<T> targetType;

    protected AbstractDialog(SeleniumContext ctx, Class<T> targetType)
    {
        super(ctx);
        this.targetType = notNull("targetType", targetType);
    }


    /**
     * jQuery locator of the main dialog component (when visible).   
     *
     * @return jQuery locator
     */
    protected abstract String visibleDialogContentsLocator();

    /**
     * <p>
     * Subclasses may override this method to provide dialog-specific contents
     * locator, whose presence will indicate that the dialog contents have been
     * loaded and the dialog is generally ready to use.
     *
     * <p>
     * If the locator has not prefix, it must be relative to the dialog contents
     * locator, as it will be appended to the main dialog locator. Locators containing
     * prefix will be used as the stand.
     *
     * @return dialog-specific locator of a characteristic element of dialog contents.
     */
    protected String dialogContentsReadyLocator()
    {
        return "";
    }

    public final String locator()
    {
        return visibleDialogContentsLocator();
    }

    public final void assertReady(final long timeout)
    {
        assertThat.elementPresentByTimeout(fullContentsReadyLocator(), timeout);
    }

    private String fullContentsReadyLocator()
    {
        final String contentsReadyLoc = dialogContentsReadyLocator();
        if (StringUtils.isNotEmpty(contentsReadyLoc))
        {
            if (Locators.hasLocatorPrefix(contentsReadyLoc))
            {
                return contentsReadyLoc;
            }
            else
            {
                return inDialog(contentsReadyLoc);
            }
        }
        else
        {
            return visibleDialogContentsLocator();
        }
    }

    public final String inDialog(final String jqueryLocator)
    {
        return visibleDialogContentsLocator() + " " + removeLocatorPrefix(jqueryLocator);
    }

    public final boolean isOpen()
    {
        return isOpenCondition().by(1000);
    }

    private TimedCondition isOpenCondition()
    {
        return new IsPresentCondition(context, visibleDialogContentsLocator());
    }

    public final boolean isClosed()
    {
        return not(isOpenCondition()).by(1000);
    }

    protected final T asTargetType()
    {
        return targetType.cast(this);
    }
}
