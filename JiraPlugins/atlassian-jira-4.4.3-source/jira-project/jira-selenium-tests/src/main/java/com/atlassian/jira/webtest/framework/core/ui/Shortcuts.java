package com.atlassian.jira.webtest.framework.core.ui;

import com.atlassian.webtest.ui.keys.Key;
import com.atlassian.webtest.ui.keys.KeyEventAware;
import com.atlassian.webtest.ui.keys.KeyEventType;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.ModifierKey;
import com.atlassian.webtest.ui.keys.TypeMode;
import com.atlassian.webtest.ui.keys.TypeModeAware;

import java.util.List;
import java.util.Set;

import static com.atlassian.jira.webtest.framework.core.ui.ShortcutsBuilder.charShortcut;
import static com.atlassian.jira.webtest.framework.core.ui.ShortcutsBuilder.charShortcutSequence;

/**
 * Enumeration of common JIRA shortcuts.
 *
 * @since v4.3
 */
public enum Shortcuts implements KeySequence, TypeModeAware, KeyEventAware
{
    ASSIGN(charShortcut("a")),
    CREATE(charShortcut("c")),
    COMMENT(charShortcut("m")),
    LABEL(charShortcut("l")),
    EDIT(charShortcut("e")),
    J_NEXT(charShortcut("j")),
    K_PREVIOUS(charShortcut("k")),
    UP(charShortcut("u")),
    DOT_DIALOG(charShortcut(".")),
    SHORTCUTS_HELP(charShortcut("?")),
    GO_TO_DASHBOARD(charShortcutSequence("gd")),
    GO_TO_NAVIGATOR(charShortcutSequence("gi")),
    GO_TO_PROJECT(charShortcutSequence("gp")),

    // Reference Plugin shortcuts (defined in atlassian-plugin.xml of the jira-reference-plugin)
    GLOBAL_CONTEXT_SHORTCUT(charShortcutSequence("tv")),
    ISSUE_NAVIGATION_SHORTCUT(charShortcutSequence("yz")),
    ISSUE_ACTION_SHORTCUT(charShortcutSequence("wx"));


    private final KeySequence wrapped;

    Shortcuts(KeySequence wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public List<Key> keys()
    {
        return wrapped.keys();
    }

    @Override
    public Set<ModifierKey> withPressed()
    {
        return wrapped.withPressed();
    }


    @Override
    public Set<KeyEventType> keyEvents()
    {
        return ((KeyEventAware)wrapped).keyEvents();
    }

    @Override
    public TypeMode typeMode()
    {
        return TypeMode.TYPE;
    }
}
