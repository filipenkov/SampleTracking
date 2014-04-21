package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.ArrayList;
import java.util.List;

public class NodeLayoutImpl extends AbstractLayoutObject implements NodeLayout {
    protected Integer stepId;
    protected Boolean isInitialAction;

    protected LayoutRect rect;
    protected List<EdgeLayout> inLinks;
    protected List<EdgeLayout> outLinks;

    public NodeLayoutImpl() {
        super();
        this.inLinks = new ArrayList<EdgeLayout>();
        this.outLinks = new ArrayList<EdgeLayout>();
    }

    public LayoutRect getRect() {
        return rect;
    }

    public void setRect(final LayoutRect r) {
        this.rect = r;
    }

    public List<EdgeLayout> getInLinks() {
        return inLinks;
    }

    public void setInLinks(final List<EdgeLayout> l) {
        this.inLinks = l;
    }

    public List<EdgeLayout> getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(final List<EdgeLayout> l) {
        this.outLinks = l;
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getIsInitialAction() {
        return isInitialAction;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getStepId() {
        return stepId;
    }

    /**
     * {@inheritDoc}
     */
    public void setIsInitialAction(final Boolean b) {
        this.isInitialAction = b;

    }

    /**
     * {@inheritDoc}
     */
    public void setStepId(final Integer i) {
        this.stepId = i;

    }

}
