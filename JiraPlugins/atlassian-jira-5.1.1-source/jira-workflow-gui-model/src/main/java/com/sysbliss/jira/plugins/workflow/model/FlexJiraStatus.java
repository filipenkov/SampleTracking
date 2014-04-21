/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraStatus {
    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    String getIconUrl();

    void setIconUrl(String iconURL);

    boolean getIsActive();

    void setIsActive(boolean active);
}
