package com.atlassian.jira.webtest.framework.dialog;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.Openable;

/**
 * <p>
 * A dialog in JIRA. Dialogs are JS-heavy components that can be opened from given locations.
 *
 * @since v4.3
 */
public interface Dialog<T extends Dialog<T>> extends Openable<T>, Localizable
{
}

