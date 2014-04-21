package com.sysbliss.jira.plugins.workflow.model.layout;

import java.util.List;

public interface NodeLayout extends LayoutObject {

    public Integer getStepId();

    public void setStepId(Integer i);

    public Boolean getIsInitialAction();

    public void setIsInitialAction(Boolean b);

    public LayoutRect getRect();

    public void setRect(LayoutRect r);

    public List<EdgeLayout> getInLinks();

    public void setInLinks(List<EdgeLayout> l);

    public List<EdgeLayout> getOutLinks();

    public void setOutLinks(List<EdgeLayout> l);

}
