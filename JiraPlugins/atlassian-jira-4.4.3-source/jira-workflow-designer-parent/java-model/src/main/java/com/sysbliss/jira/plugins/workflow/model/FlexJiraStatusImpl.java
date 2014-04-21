/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraStatusImpl implements FlexJiraStatus, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2123529633353642308L;
    private String name;
    private String description;
    private String iconUrl;
    private String id;
    private boolean isActive;

    /** {@inheritDoc} */
    public String getDescription() {
	return description;
    }

    /** {@inheritDoc} */
    public String getIconUrl() {
	return iconUrl;
    }

    /** {@inheritDoc} */
    public String getId() {
	return id;
    }

    /** {@inheritDoc} */
    public String getName() {
	return name;
    }

    /** {@inheritDoc} */
    public void setDescription(final String description) {
	this.description = description;

    }

    /** {@inheritDoc} */
    public void setIconUrl(final String iconURL) {
	this.iconUrl = iconURL;

    }

    /** {@inheritDoc} */
    public void setName(final String name) {
	this.name = name;

    }

    /** {@inheritDoc} */
    public void setId(final String id) {
	this.id = id;

    }

    /** {@inheritDoc} */
    public boolean getIsActive() {
	return isActive;
    }

    /** {@inheritDoc} */
    public void setIsActive(final boolean active) {
	this.isActive = active;

    }

}
