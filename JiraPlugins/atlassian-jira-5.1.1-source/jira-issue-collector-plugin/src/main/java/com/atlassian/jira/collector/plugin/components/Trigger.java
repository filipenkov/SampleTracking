package com.atlassian.jira.collector.plugin.components;

/**
 * Represents a trigger that the user clicks on to bring up the form template used for capturing the user's input.
 * <p/>
 * Triggers can have a position or custom function to provide a trigger.
 */
public final class Trigger
{
    public enum Position
    {
        TOP, CUSTOM, SUBTLE, RIGHT, INVALID;

        public String getI18nKey()
        {
            return "collector.plugin.trigger.position." + this.toString();
        }
    }

    private final String text;
    private final Position position;
    private final String customFunction;

    public Trigger(final String text, final Position position, final String customFunction)
    {
        this.text = text;
        this.position = position;
        this.customFunction = customFunction;
    }

    public String getText()
    {
        return this.text;
    }

    public Position getPosition()
    {
        return position;
    }

    public String getCustomFunction()
    {
        return customFunction;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Trigger trigger = (Trigger) o;

        if (customFunction != null ? !customFunction.equals(trigger.customFunction) : trigger.customFunction != null)
        { return false; }
        if (position != trigger.position) { return false; }
        if (!text.equals(trigger.text)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = text.hashCode();
        result = 31 * result + position.hashCode();
        result = 31 * result + (customFunction != null ? customFunction.hashCode() : 0);
        return result;
    }
}
