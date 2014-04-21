/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

/**
 * @author jdoklovic
 * 
 */
public interface LayoutSerializer {
    public String serialize(JWDLayout layout) throws Exception;
}
