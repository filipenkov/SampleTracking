package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines a keyboard shortcut. A shortcut consists of the keys required for the shortcut (could be multiples for the
 * same shortcut such as gh or gd), a context (some shortcuts only apply on the view issue page), the operation and a
 * parameter for the operation (generally a jQuery selector, but can be anything really.).
 *
 * @since v4.1
 */
public final class KeyboardShortcut implements Comparable<KeyboardShortcut>
{
    private final Set<List<String>> shortcuts;
    private final KeyboardShortcutManager.Context context;
    private final KeyboardShortcutManager.Operation operation;
    private final String parameter;
    private final String descriptionI18nKey;
    private final int order;

    private static final String START_TAG = "<kbd>";
    private static final String END_TAG = "</kbd>";
    private final boolean hidden;

    public KeyboardShortcut(final KeyboardShortcutManager.Context context, final KeyboardShortcutManager.Operation operation, final String parameter, final int order, final Set<List<String>> shortcuts, final String descriptionI18nKey, final boolean hidden)
    {
        this.hidden = hidden;
        this.context = notNull("context", context); 
        this.shortcuts = new LinkedHashSet<List<String>>(notNull("shortcuts", shortcuts));
        this.operation = notNull("operation", operation);
        this.parameter = notNull("parameter", parameter);
        this.order = order;
        this.descriptionI18nKey = descriptionI18nKey;
    }

    public KeyboardShortcutManager.Context getContext()
    {
        return context;
    }

    public Set<List<String>> getShortcuts()
    {
        return shortcuts;
    }

    public int getOrder()
    {
        return order;
    }

    public KeyboardShortcutManager.Operation getOperation()
    {
        return operation;
    }

    public String getParameter()
    {
        return parameter;
    }

    public String getDescriptionI18nKey()
    {
        return descriptionI18nKey;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * Given a set of shortcuts (such as ["gh", "gd"]), this method will produce a pretty 
     * internationalized HTML string such as
     * <p>
     * <code>&lt;kbd&gt;g&lt;/kbd&gt;</code> then <code>&lt;kbd&gt;h&lt;/kbd&gt;</code>
     * </p> <p>
     * or
     * </p> <p>
     * <code>&lt;kbd&gt;g&lt;/kbd&gt;</code> then <code>&lt;kbd&gt;d&lt;/kbd&gt;</code>
     * </p>
     * 
     * @param i18nHelper the i18nHelper to user for translations.
     * @return pretty printed shortcut.
     */
    public String getPrettyShortcut(final I18nHelper i18nHelper)
    {
        final StringBuilder ret = new StringBuilder();
        int count = 0;
        for (final List<String> shortcut : shortcuts)
        {
            if (count > 0)
            {
                ret.append(" ").append(i18nHelper.getText("common.words.or")).append(" ");
            }
            if (shortcut.size() == 2)
            {
                ret.append(i18nHelper.getText("keyboard.shortcuts.two.keys", START_TAG + shortcut.get(0) + END_TAG,
                    START_TAG + shortcut.get(1) + END_TAG));
            }
            else if (shortcut.size() == 3)
            {
                ret.append(i18nHelper.getText("keyboard.shortcuts.three.keys", START_TAG + shortcut.get(0) + END_TAG,
                    START_TAG + shortcut.get(1) + END_TAG, START_TAG + shortcut.get(2) + END_TAG));
            }
            else
            {
                //if there's 1 char or more than 3, simply put the whole shortcut string in the emphasis
                ret.append(START_TAG).append(StringUtils.join(shortcut, " ")).append(END_TAG);
            }
            count++;
        }
        return ret.toString();
    }

    public int compareTo(final KeyboardShortcut shortcut)
    {
        final int order2 = shortcut.getOrder();

        if (order == order2)
        {
            return 0;
        }
        else if (order < order2)
        {
            return -1;
        }
        return 1;
    }

    @Override
    public String toString()
    {
        return "KeyboardShortcut{" + "context=" + context + ", shortcuts=" + shortcuts + ", operation=" + operation + ", parameter='" + parameter + '\'' + ", descriptionI18nKey='" + descriptionI18nKey + '\'' + ", order=" + order + ", hidden=" + hidden +'}';
    }
}
