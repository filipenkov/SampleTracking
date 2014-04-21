package com.atlassian.jira.pageobjects.project.fields;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Mock implementation for easy comparisons in tests.
 *
 * @since v4.4
 */
public class MockField implements Field
{
    private final String name;
    private final String description;
    private final boolean required;
    private final String renderer;
    private final String screens;

    public MockField(final String name, final String description, final boolean isRequired, final String renderer, final String screens)
    {
        this.name = name;
        this.description = description;
        this.required = isRequired;
        this.renderer = renderer;
        this.screens = screens;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isRequired()
    {
        return required;
    }

    @Override
    public String getRenderer()
    {
        return renderer;
    }

    @Override
    public String getScreens()
    {
        return screens;
    }

    @Override
    public ScreensDialog openScreensDialog()
    {
        throw new UnsupportedOperationException("Not supported in mocks");
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof Field))
        {
            return false;
        }
        final Field rhs = (Field)o;
        return new EqualsBuilder()
                .append(name, rhs.getName())
                .append(description, rhs.getDescription())
                .append(required, rhs.isRequired())
                .append(renderer, rhs.getRenderer())
                .append(screens, rhs.getScreens())
                .isEquals();

    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(name)
                .append(description)
                .append(required)
                .append(renderer)
                .append(screens)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("name", name).
                append("description", description).
                append("required", required).
                append("renderer", renderer).
                append("screens", screens).
                toString();
    }
}
