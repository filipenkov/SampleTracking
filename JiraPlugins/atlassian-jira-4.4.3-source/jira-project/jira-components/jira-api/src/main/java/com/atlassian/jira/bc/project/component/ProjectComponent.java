package com.atlassian.jira.bc.project.component;

import org.ofbiz.core.entity.GenericValue;

/**
 * A key domain object representing a "working part" of a Project such that an
 * Issue can be raised against or be relevant only to some parts. Typical usage
 * in projects to develop a technology product have a ProjectComponent for each
 * subsystem or module, e.g. GUI, Database, Indexing, Importing.
 *
 * Components can have a lead, or user responsible for the issues raised against
 * that component.
 *
 * The AssigneeType value ({@link com.atlassian.jira.project.AssigneeTypes})
 * refers to the default assignee for issues raised on that component.
 */
public interface ProjectComponent
{
    /**
     * Returns the component ID.
     *
     * @return component ID
     */
    Long getId();

    /**
     * Returns the component description.
     *
     * @return component description
     */
    String getDescription();

    /**
     * Returns the name of the lead for this project component.
     *
     * @return name of the lead for this project component
     */
    String getLead();

    /**
     * Returns the name of this project component.
     *
     * @return name of this project component
     */
    String getName();

    /**
     * Returns the id of the project of this component.
     * @return the project's id.
     */
    Long getProjectId();


    /**
     * Returns the assignee type.
     * @return the assignee type.
     */
    long getAssigneeType();

    /**
     * @deprecated use this object instead of the stinky GenericValue!
     * @return the underlying GenericValue
     */
    GenericValue getGenericValue();

}
