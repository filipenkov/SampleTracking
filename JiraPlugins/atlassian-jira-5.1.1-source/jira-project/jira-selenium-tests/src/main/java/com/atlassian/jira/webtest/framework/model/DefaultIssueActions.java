package com.atlassian.jira.webtest.framework.model;

import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.Sequences;


/**
 * Enumeration of default issue actions
 *
 * @since v4.3
 */
public enum DefaultIssueActions implements IssueOperation
{
    // TODO all actions
    EDIT("edit-issue", "Edit", "edit-issue", "e"),
    COMMENT("comment-issue", "Comment", "m"),
    LOG_WORK("log-work", "Log Work"),
    ATTACH_FILES("attach-file", "Attach Files"),
    CREATE_SUBTASK("create-subtask", "Create Sub-Task"),
    CONVERT_TO_SUBTASK("issue-to-subtask", "Convert to Sub-Task"),
    MOVE("move-issue", "Move"),
    LINK_ISSUE("link-issue", "Link"),
    EDIT_LABELS("edit-labels", "Labels", "l");


    private static final String CSS_CLASS_PREFIX = "issueaction-%s";

    private final String id;
    private final String name;
    private final String cssClass;
    private final KeySequence shortcut;

    private DefaultIssueActions(String id, String name)
    {
        this(id, name, null);
    }

    private DefaultIssueActions(String id, String name, String shortcut)
    {
        this.id = id;
        this.name = name;
        this.cssClass = String.format(CSS_CLASS_PREFIX, id);
        this.shortcut = shortcutSequence(shortcut);
    }

    private DefaultIssueActions(String id, String name, String cssClassSuffix, String shortcut)
    {
        this.id = id;
        this.name = name;
        this.cssClass = String.format(CSS_CLASS_PREFIX, cssClassSuffix);
        this.shortcut = shortcutSequence(shortcut);
    }

    private KeySequence shortcutSequence(String shortcut)
    {
        return shortcut != null ? Sequences.chars(shortcut) : null;
    }

    @Override
    public String id()
    {
        return id;
    }

    @Override
    public String uiName()
    {
        return name;
    }

    @Override
    public String cssClass()
    {
        return cssClass;
    }

    @Override
    public boolean hasShortcut()
    {
        return shortcut != null;
    }

    @Override
    public KeySequence shortcut()
    {
        if (!hasShortcut())
        {
            throw new IllegalStateException("No shortcut defined for " + this);
        }
        return shortcut;
    }

}
