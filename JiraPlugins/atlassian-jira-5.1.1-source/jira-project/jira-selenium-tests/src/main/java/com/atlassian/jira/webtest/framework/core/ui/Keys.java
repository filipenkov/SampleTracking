package com.atlassian.jira.webtest.framework.core.ui;

import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKeys;

/**
 * Enumeration of common JIRA keys.
 *
 * @since v4.3
 */
public final class Keys
{
    private Keys()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static final KeySequence ESCAPE = SpecialKeys.ESC.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);
    public static final KeySequence ENTER = SpecialKeys.ENTER.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);
    public static final KeySequence UP = SpecialKeys.ARROW_UP.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);
    public static final KeySequence DOWN = SpecialKeys.ARROW_DOWN.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);
    public static final KeySequence BACKSPACE = SpecialKeys.BACKSPACE.withEvents(KeyEventType.KEYDOWN, KeyEventType.KEYPRESS);
}
