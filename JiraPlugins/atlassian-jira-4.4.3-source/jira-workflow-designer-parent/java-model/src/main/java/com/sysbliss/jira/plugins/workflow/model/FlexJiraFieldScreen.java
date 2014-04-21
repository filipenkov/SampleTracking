/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraFieldScreen extends Serializable {

    public void setName(String s);

    public void setId(String id);

    public String getId();

    public String getName();
}
