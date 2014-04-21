/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.List;

/**
 * @author jdoklovic
 * 
 */
public interface JWDLayout {

    public String getWorkflowName();

    public void setWorkflowName(String name);

    public Boolean getIsDraftWorkflow();

    public void setIsDraftWorkflow(Boolean b);

    public Integer getWidth();

    public void setWidth(Integer width);

    public List<NodeLayout> getRoots();

    public void setRoots(List<NodeLayout> roots);

    public LayoutRect getGraphBounds();

    public void setGraphBounds(LayoutRect rect);

    public void setAnnotations(List<AnnotationLayout> notes);

    public List<AnnotationLayout> getAnnotations();
}
