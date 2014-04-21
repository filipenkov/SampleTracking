package com.atlassian.jira.hints;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Holds hint data. Currently only the text of a hint, might be extended
 * in the future.
 *
 * @since v4.2
 */
public final class Hint
{
    private final String tooltip;

    private final String text;

    public Hint(final String text, final String tooltip)
    {
        notNull("text", text);
        notNull("tooltip", tooltip);

        this.text = text;
        this.tooltip = tooltip;
    }

    public String getText()
    {
        return text;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final Hint hint = (Hint) o;

        if (!text.equals(hint.text))
        {
            return false;
        }
        if (!tooltip.equals(hint.tooltip))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = tooltip.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }
}
