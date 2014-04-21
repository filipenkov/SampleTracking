/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public abstract class AbstractFlexWorkflowObject implements FlexWorkflowObject {

    private int id;
    private int entityId;
    private String description;
    private String name;

    public void setId(final int id) {
	this.id = id;

    }

    public int getId() {
	return this.id;
    }

    public void setEntityId(final int id) {
	this.entityId = id;

    }

    public int getEntityId() {
	return this.entityId;
    }

    public void setDescription(final String desc) {
	this.description = desc;

    }

    public String getDescription() {
	return this.description;
    }

    public void setName(final String name) {
	this.name = name;

    }

    public String getName() {
	return this.name;
    }
}
