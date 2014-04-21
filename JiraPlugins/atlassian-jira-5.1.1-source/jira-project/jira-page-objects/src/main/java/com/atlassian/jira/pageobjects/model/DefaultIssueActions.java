package com.atlassian.jira.pageobjects.model;

/**
 * Enumeration of default issue actions
 *
 * @since v4.3
 */
public enum DefaultIssueActions implements IssueOperation
{
    // TODO add all actions
    EDIT("edit-issue", "Edit", "edit-issue", "e"),
    COMMENT("comment-issue", "Comment", "m"),
    LOG_WORK("log-work", "Log Work"),
    ATTACH_FILES("attach-file", "Attach Files"),
    CREATE_SUBTASK("create-subtask", "Create Sub-Task"),
    CONVERT_TO_SUBTASK("issue-to-subtask", "Convert to Sub-Task"),
    MOVE("move-issue", "Move"),
    LINK_ISSUE("link-issue", "Link"),
    DELETE_ISSUE("delete-issue", "Delete"),
    EDIT_LABELS("edit-labels", "Labels", "l");

    private static final String CSS_CLASS_PREFIX = "issueaction-%s";

    private final String id;
    private final String name;
    private final String cssClass;
    private final CharSequence shortcut;

    private DefaultIssueActions(String id, String name)
    {
        this(id, name, null);
    }

    private DefaultIssueActions(String id, String name, CharSequence shortcut)
    {
        this.id = id;
        this.name = name;
        this.cssClass = String.format(CSS_CLASS_PREFIX, id);
        this.shortcut = shortcut;
    }

    private DefaultIssueActions(String id, String name, String cssClassSuffix, CharSequence shortcut)
    {
        this.id = id;
        this.name = name;
        this.cssClass = String.format(CSS_CLASS_PREFIX, cssClassSuffix);
        this.shortcut = shortcut;
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
    public CharSequence shortcut()
    {
        if (!hasShortcut())
        {
            throw new IllegalStateException("No shortcut defined for " + this);
        }
        return shortcut;
    }

}
