/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public interface FlexWorkflowObject extends Serializable {

    void setId(int id);

    int getId();

    void setEntityId(int id);

    int getEntityId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String desc);
}
