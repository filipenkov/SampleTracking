/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.sysbliss.jira.plugins.workflow.model.layout.AbstractLayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRectImpl;

/**
 * @author jdoklovic
 * 
 */
public class SerializableNodeImpl extends AbstractLayoutObject implements SerializableNode {

    private Integer stepId;
    private Boolean isInitialAction;
    private LayoutRect rect;
    private List<String> inLinkIds;
    private List<String> outLinkIds;

    /** {@inheritDoc} */
    public List<String> getInLinkIds() {
	return inLinkIds;
    }

    /** {@inheritDoc} */
    public List<String> getOutLinkIds() {
	return outLinkIds;
    }

    @JsonDeserialize(as = LayoutRectImpl.class)
    public LayoutRect getRect() {
	return rect;
    }

    /** {@inheritDoc} */
    public void setInLinkIds(final List<String> ids) {
	this.inLinkIds = ids;
    }

    /** {@inheritDoc} */
    public void setOutLinkIds(final List<String> ids) {
	this.outLinkIds = ids;
    }

    @JsonDeserialize(as = LayoutRectImpl.class)
    public void setRect(final LayoutRect r) {
	this.rect = r;
    }

    /** {@inheritDoc} */
    public Boolean getIsInitialAction() {
	return isInitialAction;
    }

    /** {@inheritDoc} */
    public Integer getStepId() {
	return stepId;
    }

    /** {@inheritDoc} */
    public void setIsInitialAction(final Boolean b) {
	this.isInitialAction = b;

    }

    /** {@inheritDoc} */
    public void setStepId(final Integer i) {
	this.stepId = i;

    }

}
