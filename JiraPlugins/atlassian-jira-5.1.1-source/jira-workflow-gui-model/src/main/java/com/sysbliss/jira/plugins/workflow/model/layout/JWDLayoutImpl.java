/**
 *
 */
package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jdoklovic
 */
public class JWDLayoutImpl implements JWDLayout {
    private List<NodeLayout> roots;
    private List<AnnotationLayout> annotations;
    private Integer width;
    private Boolean isDraft;
    private String workflowName;
    private LayoutRect graphBounds;

    public JWDLayoutImpl() {
        roots = new ArrayList<NodeLayout>();
        annotations = new ArrayList<AnnotationLayout>();
    }

    public List<NodeLayout> getRoots() {
        return roots;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String name) {
        this.workflowName = name;
    }

    public Boolean getIsDraftWorkflow() {
        return isDraft;
    }

    public void setIsDraftWorkflow(Boolean b) {
        this.isDraft = b;
    }

    /**
     * {@inheritDoc}
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     */
    public void setRoots(final List<NodeLayout> roots) {
        this.roots = roots;
    }

    public LayoutRect getGraphBounds() {
        return graphBounds;
    }

    public void setGraphBounds(LayoutRect rect) {
        this.graphBounds = rect;
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(final Integer width) {
        this.width = width;
    }

    public List<AnnotationLayout> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<AnnotationLayout> annotations) {
        this.annotations = annotations;
    }
}
