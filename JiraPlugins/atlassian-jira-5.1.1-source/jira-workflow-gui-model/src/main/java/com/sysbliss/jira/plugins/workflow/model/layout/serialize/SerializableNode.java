/**
 * 
 */
package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import java.util.List;

import com.sysbliss.jira.plugins.workflow.model.layout.LayoutObject;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;

/**
 * @author jdoklovic
 * 
 */
public interface SerializableNode extends LayoutObject {

    public Integer getStepId();

    public void setStepId(Integer i);

    public Boolean getIsInitialAction();

    public void setIsInitialAction(Boolean b);

    public LayoutRect getRect();

    public void setRect(LayoutRect r);

    public List<String> getInLinkIds();

    public void setInLinkIds(List<String> ids);

    public List<String> getOutLinkIds();

    public void setOutLinkIds(List<String> ids);
}
