/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model;

import java.util.List;
import java.util.Map;

/**
 * @author jdoklovic
 * 
 */
public class FlexJiraActionImpl extends AbstractFlexWorkflowObject implements FlexJiraAction {

    private static final long serialVersionUID = 2539118376825978970L;
    private FlexJiraResult unconditionalResult;
    private FlexJiraConditions conditions;
    private Map metaAttributes;
    private String view;
    private List validators;
    private String fieldScreenId;
    private boolean isCommon;
    private String label;

    /** {@inheritDoc} */
    public void setUnconditionalResult(final FlexJiraResult result) {
	this.unconditionalResult = result;

    }

    /** {@inheritDoc} */
    public FlexJiraResult getUnconditionalResult() {
	return this.unconditionalResult;
    }

    public boolean getIsCommon()
    {
        return isCommon;
    }

    public void setIsCommon(boolean common)
    {
        this.isCommon = common;
    }

    /** {@inheritDoc} */
    public FlexJiraConditions getConditions() {
	return this.conditions;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    /** {@inheritDoc} */
    public Map getMetaAttributes() {
	return this.metaAttributes;
    }

    /** {@inheritDoc} */
    public List getValidators() {
	return this.validators;
    }

    /** {@inheritDoc} */
    public String getView() {
	return this.view;
    }

    /** {@inheritDoc} */
    public void setConditions(final FlexJiraConditions conditions) {
	this.conditions = conditions;

    }

    /** {@inheritDoc} */
    public void setMetaAttributes(final Map metaAttributes) {
	this.metaAttributes = metaAttributes;

    }

    /** {@inheritDoc} */
    public void setValidators(final List validators) {
	this.validators = validators;

    }

    /** {@inheritDoc} */
    public void setView(final String view) {
	this.view = view;

    }

    /** {@inheritDoc} */
    public String getFieldScreenId() {
	return fieldScreenId;
    }

    /** {@inheritDoc} */
    public void setFieldScreenId(final String id) {
	this.fieldScreenId = id;

    }

}
