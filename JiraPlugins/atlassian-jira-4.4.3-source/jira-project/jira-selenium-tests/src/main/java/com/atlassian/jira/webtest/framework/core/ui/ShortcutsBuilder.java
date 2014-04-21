package com.atlassian.jira.webtest.framework.core.ui;

import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.SpecialKey;
import com.atlassian.webtest.ui.keys.TypeMode;

import java.util.Collection;
import java.util.EnumSet;

import static com.atlassian.webtest.ui.keys.KeyEventType.KEYDOWN;
import static com.atlassian.webtest.ui.keys.KeyEventType.KEYPRESS;
import static com.atlassian.webtest.ui.keys.Sequences.charsBuilder;
import static com.atlassian.webtest.ui.keys.Sequences.keysBuilder;

/**
 * Utility to build shortcut key sequences.
 *
 * @see com.atlassian.webtest.ui.keys.KeySequence
 * @see com.atlassian.jira.webtest.framework.core.ui.Shortcuts
 * @since v4.3
 */
final class ShortcutsBuilder
{
    private ShortcutsBuilder()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static final Collection<KeyEventType> DOWN_AND_PRESS = EnumSet.of(KEYDOWN, KEYPRESS);
    private static final Collection<KeyEventType> DOWN = EnumSet.of(KEYDOWN);
    private static final Collection<KeyEventType> PRESS = EnumSet.of(KEYPRESS);

    static KeySequence charShortcut(String chars)
    {
        return charsBuilder(chars).keyEvents(DOWN_AND_PRESS).build();
    }

    static KeySequence charShortcutSequence(String chars)
    {
        return charsBuilder(chars).keyEvents(PRESS).build();
    }

    static KeySequence keyShortcut(SpecialKey... keys)
    {
        return keysBuilder(keys).keyEvents(DOWN).typeMode(TypeMode.TYPE).build();
    }
}
