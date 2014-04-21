package com.atlassian.jira.collector.plugin.components;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents a form template used to render the input form users will fill in to create an issue in JIRA.
 * <p/>
 * A template consists of a unique ID, a name key used for rendering an i18nized name of the template, as well as a path
 * to a velocity template for rendering the actual form contents.
 */
public final class Template
{
    private final String id;
    private final String nameKey;
    private final String templatePath;

    public Template(final String id, final String nameKey, final String templatePath)
    {
        this.id = notNull("id", id);
        this.nameKey = notNull("nameKey", nameKey);
        this.templatePath = notNull("templatePath", templatePath);
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        return nameKey;
    }

    public String getTemplatePath()
    {
        return templatePath;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Template template = (Template) o;

        if (!id.equals(template.id)) { return false; }
        if (!nameKey.equals(template.nameKey)) { return false; }
        if (!templatePath.equals(template.templatePath)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id.hashCode();
        result = 31 * result + nameKey.hashCode();
        result = 31 * result + templatePath.hashCode();
        return result;
    }
}
