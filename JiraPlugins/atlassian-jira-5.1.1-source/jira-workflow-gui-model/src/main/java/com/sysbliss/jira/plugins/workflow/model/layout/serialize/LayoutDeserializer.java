/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;

/**
 * @author jdoklovic
 * 
 */
public interface LayoutDeserializer {
    public JWDLayout deserialize(String packet) throws Exception;
}
