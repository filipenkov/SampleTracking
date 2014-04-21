/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraServerInfoImpl implements FlexJiraServerInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 6317416933719487308L;
    private boolean isEnterprise;
    private boolean isProfessional;
    private boolean isStandard;
    private String version;

    /** {@inheritDoc} */
    public boolean getIsEnterprise() {
	return isEnterprise;
    }

    /** {@inheritDoc} */
    public void setIsEnterprise(final boolean b) {
	this.isEnterprise = b;

    }

    /** {@inheritDoc} */
    public boolean getIsProfessional() {
	return isProfessional;
    }

    /** {@inheritDoc} */
    public boolean getIsStandard() {
	return isStandard;
    }

    /** {@inheritDoc} */
    public String getVersion() {
	return version;
    }

    /** {@inheritDoc} */
    public void setIsProfessional(final boolean b) {
	this.isProfessional = b;

    }

    /** {@inheritDoc} */
    public void setIsStandard(final boolean b) {
	this.isStandard = b;

    }

    /** {@inheritDoc} */
    public void setVersion(final String v) {
	this.version = v;

    }

}
