package com.atlassian.jira.webtest.framework.component.tab;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.Openable;

/**
 * Represents a tab in the web UI.
 *
 * @since v4.3
 */
public interface Tab<T extends Tab> extends Localizable, Openable<T>
{
}
