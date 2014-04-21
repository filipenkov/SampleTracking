/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraMetadataContainer {

    /**
     * @param metaAttributes
     */
    void setMetaAttributes(Map metaAttributes);

    Map getMetaAttributes();
}
