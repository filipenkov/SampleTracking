package com.atlassian.jira.collector.plugin.components;

import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemplateStoreImpl implements TemplateStore
{
    //currently a static list. In future this should probably be pluggable!
    final static List<Template> templates = new ArrayList<Template>();

    static
    {
        templates.add(new Template("feedback", "collector.plugin.template.got.feedback",
                "templates/collector/feedback.vm"));
		templates.add(new Template("raise-bug", "collector.plugin.template.bug.raise.bug",
				"templates/collector/raise-bug.vm"));
        templates.add(new Template("custom", "collector.plugin.template.custom",
                "templates/collector/custom.vm"));
    }

    @Override
    public List<Template> getTemplates()
    {
        return Collections.unmodifiableList(new ArrayList<Template>(templates));
    }

    @Override
    public Template getTemplate(final String id)
    {
        Assertions.notNull("id", id);
        for (Template template : templates)
        {
            if (template.getId().equals(id))
            {
                return template;
            }
        }
        return null;
    }
}
