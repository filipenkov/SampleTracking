package com.atlassian.jira.collector.plugin.components;

import java.util.List;

/**
 * Store that returns all form templates used for user input in the system.
 */
public interface TemplateStore
{
    /**
     * Returns all form templates available in the system.
     *
     * @return all form templates available in the system
     */
    List<Template> getTemplates();

    /**
     * Given an id this method returns the template with this id.
     *
     * @param id the id of the template to return
     * @return A templat with the matching id or null if none was found.
     */
    Template getTemplate(final String id);

}
