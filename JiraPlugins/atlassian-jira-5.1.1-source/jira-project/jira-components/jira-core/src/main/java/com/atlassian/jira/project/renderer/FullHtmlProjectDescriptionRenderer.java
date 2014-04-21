package com.atlassian.jira.project.renderer;

import com.atlassian.jira.project.Project;

import javax.annotation.Nonnull;

public class FullHtmlProjectDescriptionRenderer implements ProjectDescriptionRenderer
{
    @Nonnull
    @Override
    public String getViewHtml(@Nonnull final Project project)
    {
        return project.getDescription();
    }

    @Nonnull
    @Override
    public String getEditHtml(@Nonnull final Project project)
    {
        return "<textarea class=\"textarea\" type=\"text\" rows=\"5\" name=\"description\" cols=\"\">" + project.getDescription() + "</textarea>";
    }

    @Nonnull
    @Override
    public String getDescriptionI18nKey()
    {
        return "admin.addproject.description.description";
    }
}
