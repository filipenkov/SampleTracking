package com.atlassian.jira.plugin.viewissue;

import javax.annotation.Nullable;

/**
 * Date field holder used for rendering in Velocity templates.
 *
 * @since v4.4
 */
public class DateBlockField
{
    private final String id;
    private final String label;
    private final String styleClass;
    private final String displayHtml;
    private final String iso8601;
    private final String title;
    private final String fieldType;

    public DateBlockField(String id, String label, @Nullable String styleClass, String displayHtml, @Nullable String iso8601, @Nullable String title, @Nullable String fieldType)
    {
        this.id = id;
        this.styleClass = styleClass;
        this.label = label;
        this.displayHtml = displayHtml;
        this.iso8601 = iso8601;
        this.title = title;
        this.fieldType = fieldType;
    }

    public String getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    @Nullable
    public String getStyleClass()
    {
        return styleClass;
    }

    public String getDisplayHtml()
    {
        return displayHtml;
    }

    @Nullable
    public String getIso8601Value()
    {
        return iso8601;
    }

    @Nullable
    public String getTitle()
    {
        return title;
    }

    @Nullable
    public String getFieldType()
    {
        return fieldType;
    }
}
