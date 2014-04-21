package com.atlassian.jira.webtest.framework.core.component;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Represents an HTML input.
 *
 * @since v4.3
 */
public interface Input extends ValueHolder, Localizable
{

    /**
     * Enter given key sequence into this input.
     *
     * @param keys key sequence to enter
     * @return this input
     */
    Input type(KeySequence keys);
}
