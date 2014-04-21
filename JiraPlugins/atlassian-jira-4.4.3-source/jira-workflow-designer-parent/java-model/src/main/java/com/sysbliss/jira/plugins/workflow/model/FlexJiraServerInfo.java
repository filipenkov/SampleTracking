/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.io.Serializable;

/**
 * @author jdoklovic
 * 
 */
public interface FlexJiraServerInfo extends Serializable {

    boolean getIsEnterprise();

    void setIsEnterprise(boolean b);

    boolean getIsProfessional();

    void setIsProfessional(boolean b);

    boolean getIsStandard();

    void setIsStandard(boolean b);

    String getVersion();

    void setVersion(String v);

}
